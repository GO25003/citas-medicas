package com.clinica.citas.service.impl;

import java.util.List;

import com.clinica.citas.dto.request.MedicoRequestDTO;
import com.clinica.citas.dto.response.MedicoResponseDTO;
import com.clinica.citas.exception.ResourceNotFoundException;
import com.clinica.citas.mapper.MedicoMapper;
import com.clinica.citas.model.Especialidad;
import com.clinica.citas.model.Medico;
import com.clinica.citas.repository.EspecialidadRepository;
import com.clinica.citas.repository.MedicoRepository;
import com.clinica.citas.service.MedicoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepository;
    private final EspecialidadRepository especialidadRepository;
    private final MedicoMapper medicoMapper;

    public MedicoServiceImpl(MedicoRepository medicoRepository,
                             EspecialidadRepository especialidadRepository,
                             MedicoMapper medicoMapper) {
        this.medicoRepository = medicoRepository;
        this.especialidadRepository = especialidadRepository;
        this.medicoMapper = medicoMapper;
    }

    @Override
    public MedicoResponseDTO crear(MedicoRequestDTO request) {
        Medico medico = medicoMapper.toEntity(request);
        medico.setEspecialidades(obtenerEspecialidades(request.getIdsEspecialidades()));
        return medicoMapper.toResponse(medicoRepository.save(medico));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicoResponseDTO> listar() {
        return medicoMapper.toResponseList(medicoRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public MedicoResponseDTO buscarPorId(Long id) {
        return medicoMapper.toResponse(obtenerEntidad(id));
    }

    @Override
    public MedicoResponseDTO actualizar(Long id, MedicoRequestDTO request) {
        Medico medico = obtenerEntidad(id);
        medicoMapper.updateEntity(request, medico);
        medico.getEspecialidades().clear();
        medico.getEspecialidades().addAll(obtenerEspecialidades(request.getIdsEspecialidades()));
        return medicoMapper.toResponse(medicoRepository.saveAndFlush(medico));
    }

    @Override
    public void eliminar(Long id) {
        medicoRepository.delete(obtenerEntidad(id));
        // flush inmediato: si tiene disponibilidades o citas, la BD rechaza el borrado (409)
        medicoRepository.flush();
    }

    private Medico obtenerEntidad(Long id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el médico con id " + id));
    }

    private List<Especialidad> obtenerEspecialidades(List<Long> idsEspecialidades) {
        List<Long> ids = idsEspecialidades.stream().distinct().toList();
        List<Especialidad> especialidades = especialidadRepository.findAllById(ids);
        if (especialidades.size() != ids.size()) {
            throw new ResourceNotFoundException("Una o más especialidades indicadas no existen");
        }
        return especialidades;
    }
}
