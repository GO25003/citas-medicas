package com.clinica.citas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.clinica.citas.dto.request.CitaRequestDTO;
import com.clinica.citas.dto.response.CitaResponseDTO;
import com.clinica.citas.exception.BadRequestException;
import com.clinica.citas.exception.ResourceNotFoundException;
import com.clinica.citas.mapper.CitaMapper;
import com.clinica.citas.model.Cita;
import com.clinica.citas.model.Disponibilidad;
import com.clinica.citas.model.Especialidad;
import com.clinica.citas.model.Medico;
import com.clinica.citas.model.Paciente;
import com.clinica.citas.model.enums.EstadoCita;
import com.clinica.citas.repository.CitaRepository;
import com.clinica.citas.repository.EspecialidadRepository;
import com.clinica.citas.repository.MedicoRepository;
import com.clinica.citas.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CitaServiceImplTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 12, 1, 9, 0);

    private CitaRepository citaRepository;
    private MedicoRepository medicoRepository;
    private PacienteRepository pacienteRepository;
    private EspecialidadRepository especialidadRepository;
    private CitaMapper citaMapper;
    private CitaServiceImpl service;

    private Medico medico;
    private Paciente paciente;
    private Especialidad especialidad;
    private Disponibilidad disponibilidad;
    private CitaRequestDTO request;

    @BeforeEach
    void setUp() {
        citaRepository = mock(CitaRepository.class);
        medicoRepository = mock(MedicoRepository.class);
        pacienteRepository = mock(PacienteRepository.class);
        especialidadRepository = mock(EspecialidadRepository.class);
        citaMapper = mock(CitaMapper.class);
        service = new CitaServiceImpl(citaRepository, medicoRepository, pacienteRepository,
                especialidadRepository, citaMapper);

        especialidad = mock(Especialidad.class);
        when(especialidad.getIdEspecialidad()).thenReturn(1L);

        disponibilidad = mock(Disponibilidad.class);
        when(disponibilidad.isDisponible()).thenReturn(true);
        when(disponibilidad.getFecha()).thenReturn(INICIO.toLocalDate());
        when(disponibilidad.getHoraInicio()).thenReturn(LocalTime.of(8, 0));
        when(disponibilidad.getHoraFin()).thenReturn(LocalTime.of(12, 0));

        medico = mock(Medico.class);
        when(medico.getIdPersona()).thenReturn(1L);
        when(medico.getEspecialidades()).thenReturn(List.of(especialidad));
        when(medico.getDisponibilidades()).thenReturn(List.of(disponibilidad));

        paciente = mock(Paciente.class);

        request = mock(CitaRequestDTO.class);
        when(request.getIdMedico()).thenReturn(1L);
        when(request.getIdPaciente()).thenReturn(2L);
        when(request.getIdEspecialidad()).thenReturn(1L);
        when(request.getFechaHoraInicio()).thenReturn(INICIO);
        when(request.getDuracionMinutos()).thenReturn(30);
        when(request.getMotivo()).thenReturn("Control general");

        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(pacienteRepository.findById(2L)).thenReturn(Optional.of(paciente));
        when(especialidadRepository.findById(1L)).thenReturn(Optional.of(especialidad));
        when(citaRepository.buscarCitasDelMedico(any(), any(), any(), any())).thenReturn(List.of());
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Cita citaExistente(Long id, EstadoCita estado, LocalDateTime inicio, int duracion) {
        Cita cita = mock(Cita.class);
        when(cita.getIdCita()).thenReturn(id);
        when(cita.getEstado()).thenReturn(estado);
        when(cita.getFechaHoraInicio()).thenReturn(inicio);
        when(cita.getDuracionMinutos()).thenReturn(duracion);
        return cita;
    }

    // ---------- crear ----------

    @Test
    void crearGuardaCitaPendienteYDevuelveRespuesta() {
        CitaResponseDTO response = new CitaResponseDTO();
        when(citaMapper.toResponse(any(Cita.class))).thenReturn(response);

        assertSame(response, service.crear(request));

        ArgumentCaptor<Cita> captor = ArgumentCaptor.forClass(Cita.class);
        verify(citaRepository).save(captor.capture());
        assertEquals(EstadoCita.PENDIENTE, captor.getValue().getEstado());
        assertEquals(INICIO, captor.getValue().getFechaHoraInicio());
    }

    @Test
    void crearLanzaNotFoundCuandoMedicoNoExiste() {
        when(medicoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.crear(request));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void crearLanzaNotFoundCuandoPacienteNoExiste() {
        when(pacienteRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.crear(request));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void crearLanzaNotFoundCuandoEspecialidadNoExiste() {
        when(especialidadRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.crear(request));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void crearRechazaEspecialidadQueElMedicoNoTiene() {
        Especialidad otra = mock(Especialidad.class);
        when(otra.getIdEspecialidad()).thenReturn(2L);
        when(medico.getEspecialidades()).thenReturn(List.of(otra));

        assertThrows(BadRequestException.class, () -> service.crear(request));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void crearRechazaCitaQueTerminaOtroDia() {
        when(request.getFechaHoraInicio()).thenReturn(LocalDateTime.of(2026, 12, 1, 23, 30));
        when(request.getDuracionMinutos()).thenReturn(60);

        assertThrows(BadRequestException.class, () -> service.crear(request));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void crearRechazaCuandoElMedicoNoTieneDisponibilidades() {
        when(medico.getDisponibilidades()).thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> service.crear(request));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void crearRechazaCuandoLaDisponibilidadEstaMarcadaComoNoDisponible() {
        when(disponibilidad.isDisponible()).thenReturn(false);

        assertThrows(BadRequestException.class, () -> service.crear(request));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void crearRechazaCitaQueTerminaDespuesDeLaDisponibilidad() {
        when(disponibilidad.getHoraFin()).thenReturn(LocalTime.of(10, 0));
        when(request.getFechaHoraInicio()).thenReturn(LocalDateTime.of(2026, 12, 1, 9, 45));

        assertThrows(BadRequestException.class, () -> service.crear(request));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void crearRechazaCitaQueSeCruzaConOtraCitaActiva() {
        Cita existente = citaExistente(5L, EstadoCita.CONFIRMADA, INICIO.plusMinutes(15), 30);
        when(citaRepository.buscarCitasDelMedico(any(), any(), any(), any())).thenReturn(List.of(existente));

        assertThrows(BadRequestException.class, () -> service.crear(request));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void crearPermiteCitaContiguaSinCruce() {
        Cita existente = citaExistente(5L, EstadoCita.CONFIRMADA, INICIO.plusMinutes(30), 30);
        when(citaRepository.buscarCitasDelMedico(any(), any(), any(), any())).thenReturn(List.of(existente));
        when(citaMapper.toResponse(any(Cita.class))).thenReturn(new CitaResponseDTO());

        service.crear(request);

        verify(citaRepository).save(any(Cita.class));
    }

    // ---------- listar / buscar ----------

    @Test
    void listarDevuelveLaListaMapeada() {
        Cita cita = mock(Cita.class);
        List<CitaResponseDTO> respuesta = List.of(new CitaResponseDTO());
        when(citaRepository.findAll()).thenReturn(List.of(cita));
        when(citaMapper.toResponseList(List.of(cita))).thenReturn(respuesta);

        assertSame(respuesta, service.listar());
    }

    @Test
    void buscarPorIdDevuelveRespuestaCuandoExiste() {
        Cita cita = mock(Cita.class);
        CitaResponseDTO response = new CitaResponseDTO();
        when(citaRepository.findById(7L)).thenReturn(Optional.of(cita));
        when(citaMapper.toResponse(cita)).thenReturn(response);

        assertSame(response, service.buscarPorId(7L));
    }

    @Test
    void buscarPorIdLanzaNotFoundCuandoNoExiste() {
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.buscarPorId(99L));

        assertTrue(exception.getMessage().contains("99"));
    }

    // ---------- actualizar ----------

    @Test
    void actualizarNoSeCruzaConSiMismaYGuardaCambios() {
        Cita cita = citaExistente(7L, EstadoCita.PENDIENTE, INICIO, 30);
        when(citaRepository.findById(7L)).thenReturn(Optional.of(cita));
        when(citaRepository.buscarCitasDelMedico(any(), any(), any(), any())).thenReturn(List.of(cita));
        CitaResponseDTO response = new CitaResponseDTO();
        when(citaMapper.toResponse(cita)).thenReturn(response);

        assertSame(response, service.actualizar(7L, request));

        verify(cita).setFechaHoraInicio(INICIO);
        verify(cita).setMedico(medico);
        verify(citaRepository).save(cita);
    }

    @Test
    void actualizarLanzaNotFoundCuandoLaCitaNoExiste() {
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.actualizar(99L, request));
    }

    @Test
    void actualizarRechazaCitaCancelada() {
        Cita cita = citaExistente(7L, EstadoCita.CANCELADA, INICIO, 30);
        when(citaRepository.findById(7L)).thenReturn(Optional.of(cita));

        assertThrows(BadRequestException.class, () -> service.actualizar(7L, request));
        verify(citaRepository, never()).save(any());
    }

    // ---------- confirmar ----------

    @Test
    void confirmarCambiaEstadoDePendienteAConfirmada() {
        Cita cita = citaExistente(7L, EstadoCita.PENDIENTE, INICIO, 30);
        when(citaRepository.findById(7L)).thenReturn(Optional.of(cita));
        when(citaMapper.toResponse(cita)).thenReturn(new CitaResponseDTO());

        service.confirmar(7L);

        verify(cita).setEstado(EstadoCita.CONFIRMADA);
        verify(citaRepository).save(cita);
    }

    @Test
    void confirmarRechazaCitaQueNoEstaPendiente() {
        Cita cita = citaExistente(7L, EstadoCita.CONFIRMADA, INICIO, 30);
        when(citaRepository.findById(7L)).thenReturn(Optional.of(cita));

        assertThrows(BadRequestException.class, () -> service.confirmar(7L));
        verify(citaRepository, never()).save(any());
    }

    // ---------- cancelar ----------

    @Test
    void cancelarCambiaEstadoACancelada() {
        Cita cita = citaExistente(7L, EstadoCita.PENDIENTE, INICIO, 30);
        when(citaRepository.findById(7L)).thenReturn(Optional.of(cita));
        when(citaMapper.toResponse(cita)).thenReturn(new CitaResponseDTO());

        service.cancelar(7L);

        verify(cita).setEstado(EstadoCita.CANCELADA);
        verify(citaRepository).save(cita);
    }

    @Test
    void cancelarRechazaCitaYaCancelada() {
        Cita cita = citaExistente(7L, EstadoCita.CANCELADA, INICIO, 30);
        when(citaRepository.findById(7L)).thenReturn(Optional.of(cita));

        assertThrows(BadRequestException.class, () -> service.cancelar(7L));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void cancelarLanzaNotFoundCuandoNoExiste() {
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.cancelar(99L));
    }
}
