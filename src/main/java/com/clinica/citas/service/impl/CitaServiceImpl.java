package com.clinica.citas.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.clinica.citas.dto.request.CitaRequestDTO;
import com.clinica.citas.dto.response.CitaResponseDTO;
import com.clinica.citas.exception.BadRequestException;
import com.clinica.citas.exception.ResourceNotFoundException;
import com.clinica.citas.mapper.CitaMapper;
import com.clinica.citas.model.Cita;
import com.clinica.citas.model.Especialidad;
import com.clinica.citas.model.Medico;
import com.clinica.citas.model.Paciente;
import com.clinica.citas.model.enums.EstadoCita;
import com.clinica.citas.repository.CitaRepository;
import com.clinica.citas.repository.EspecialidadRepository;
import com.clinica.citas.repository.MedicoRepository;
import com.clinica.citas.repository.PacienteRepository;
import com.clinica.citas.service.CitaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CitaServiceImpl implements CitaService {

    /** Estados que ocupan el horario del médico. */
    private static final List<EstadoCita> ESTADOS_ACTIVOS =
            List.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA);

    private final CitaRepository citaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final EspecialidadRepository especialidadRepository;
    private final CitaMapper citaMapper;

    public CitaServiceImpl(CitaRepository citaRepository,
                           MedicoRepository medicoRepository,
                           PacienteRepository pacienteRepository,
                           EspecialidadRepository especialidadRepository,
                           CitaMapper citaMapper) {
        this.citaRepository = citaRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.especialidadRepository = especialidadRepository;
        this.citaMapper = citaMapper;
    }

    @Override
    public CitaResponseDTO crear(CitaRequestDTO request) {
        Medico medico = obtenerMedico(request.getIdMedico());
        Paciente paciente = obtenerPaciente(request.getIdPaciente());
        Especialidad especialidad = obtenerEspecialidad(request.getIdEspecialidad());

        validarReglas(medico, especialidad, request.getFechaHoraInicio(),
                request.getDuracionMinutos(), null);

        Cita cita = new Cita();
        asignarDatos(cita, medico, paciente, especialidad, request);
        cita.setEstado(EstadoCita.PENDIENTE);
        return citaMapper.toResponse(citaRepository.save(cita));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listar() {
        return citaMapper.toResponseList(citaRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO buscarPorId(Long id) {
        return citaMapper.toResponse(obtenerEntidad(id));
    }

    @Override
    public CitaResponseDTO actualizar(Long id, CitaRequestDTO request) {
        Cita cita = obtenerEntidad(id);
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new BadRequestException("No se puede modificar una cita cancelada");
        }

        Medico medico = obtenerMedico(request.getIdMedico());
        Paciente paciente = obtenerPaciente(request.getIdPaciente());
        Especialidad especialidad = obtenerEspecialidad(request.getIdEspecialidad());

        validarReglas(medico, especialidad, request.getFechaHoraInicio(),
                request.getDuracionMinutos(), cita.getIdCita());

        asignarDatos(cita, medico, paciente, especialidad, request);
        return citaMapper.toResponse(citaRepository.save(cita));
    }

    @Override
    public CitaResponseDTO confirmar(Long id) {
        Cita cita = obtenerEntidad(id);
        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw new BadRequestException("Solo se puede confirmar una cita pendiente");
        }
        cita.setEstado(EstadoCita.CONFIRMADA);
        return citaMapper.toResponse(citaRepository.save(cita));
    }

    @Override
    public CitaResponseDTO cancelar(Long id) {
        Cita cita = obtenerEntidad(id);
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new BadRequestException("La cita ya está cancelada");
        }
        cita.setEstado(EstadoCita.CANCELADA);
        return citaMapper.toResponse(citaRepository.save(cita));
    }

    /**
     * Reglas de negocio de la cita:
     * 1) la especialidad debe pertenecer al médico,
     * 2) el intervalo debe quedar contenido en una disponibilidad del médico,
     * 3) no puede cruzarse con otra cita PENDIENTE o CONFIRMADA del mismo médico.
     */
    private void validarReglas(Medico medico, Especialidad especialidad,
                               LocalDateTime inicio, Integer duracionMinutos, Long idCitaExcluida) {
        boolean tieneEspecialidad = medico.getEspecialidades().stream()
                .anyMatch(e -> e.getIdEspecialidad().equals(especialidad.getIdEspecialidad()));
        if (!tieneEspecialidad) {
            throw new BadRequestException("El médico no tiene la especialidad solicitada");
        }

        LocalDateTime fin = inicio.plusMinutes(duracionMinutos);
        if (!fin.toLocalDate().equals(inicio.toLocalDate())) {
            throw new BadRequestException("La cita debe iniciar y terminar el mismo día");
        }

        LocalDate fecha = inicio.toLocalDate();
        LocalTime horaInicio = inicio.toLocalTime();
        LocalTime horaFin = fin.toLocalTime();
        boolean dentroDeDisponibilidad = medico.getDisponibilidades().stream()
                .anyMatch(d -> d.isDisponible()
                        && d.getFecha().equals(fecha)
                        && !d.getHoraInicio().isAfter(horaInicio)
                        && !d.getHoraFin().isBefore(horaFin));
        if (!dentroDeDisponibilidad) {
            throw new BadRequestException("El médico no tiene disponibilidad en el horario solicitado");
        }

        List<Cita> citasDelDia = citaRepository.buscarCitasDelMedico(
                medico.getIdPersona(), ESTADOS_ACTIVOS,
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
        boolean hayCruce = citasDelDia.stream()
                .filter(c -> idCitaExcluida == null || !c.getIdCita().equals(idCitaExcluida))
                .anyMatch(c -> inicio.isBefore(c.getFechaHoraInicio().plusMinutes(c.getDuracionMinutos()))
                        && fin.isAfter(c.getFechaHoraInicio()));
        if (hayCruce) {
            throw new BadRequestException("El médico ya tiene una cita que se cruza con ese horario");
        }
    }

    private void asignarDatos(Cita cita, Medico medico, Paciente paciente,
                              Especialidad especialidad, CitaRequestDTO request) {
        cita.setMedico(medico);
        cita.setPaciente(paciente);
        cita.setEspecialidadConsulta(especialidad);
        cita.setFechaHoraInicio(request.getFechaHoraInicio());
        cita.setDuracionMinutos(request.getDuracionMinutos());
        cita.setMotivo(request.getMotivo());
    }

    private Cita obtenerEntidad(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la cita con id " + id));
    }

    private Medico obtenerMedico(Long id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el médico con id " + id));
    }

    private Paciente obtenerPaciente(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el paciente con id " + id));
    }

    private Especialidad obtenerEspecialidad(Long id) {
        return especialidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la especialidad con id " + id));
    }
}
