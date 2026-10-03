package com.clinica.citas.service.impl;

import java.util.List;

import com.clinica.citas.dto.request.EspecialidadRequestDTO;
import com.clinica.citas.dto.response.EspecialidadResponseDTO;
import com.clinica.citas.exception.ResourceNotFoundException;
import com.clinica.citas.mapper.EspecialidadMapper;
import com.clinica.citas.model.Especialidad;
import com.clinica.citas.repository.EspecialidadRepository;
import com.clinica.citas.service.EspecialidadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository especialidadRepository;
    private final EspecialidadMapper especialidadMapper;

    public EspecialidadServiceImpl(EspecialidadRepository especialidadRepository,
                                   EspecialidadMapper especialidadMapper) {
        this.especialidadRepository = especialidadRepository;
        this.especialidadMapper = especialidadMapper;
    }

    @Override
    public EspecialidadResponseDTO crear(EspecialidadRequestDTO request) {
        Especialidad especialidad = especialidadMapper.toEntity(request);
        return especialidadMapper.toResponse(especialidadRepository.save(especialidad));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadResponseDTO> listar() {
        return especialidadMapper.toResponseList(especialidadRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public EspecialidadResponseDTO buscarPorId(Long id) {
        return especialidadMapper.toResponse(obtenerEntidad(id));
    }

    @Override
    public EspecialidadResponseDTO actualizar(Long id, EspecialidadRequestDTO request) {
        Especialidad especialidad = obtenerEntidad(id);
        especialidadMapper.updateEntity(request, especialidad);
        return especialidadMapper.toResponse(especialidadRepository.save(especialidad));
    }

    @Override
    public void eliminar(Long id) {
        especialidadRepository.delete(obtenerEntidad(id));
    }

    private Especialidad obtenerEntidad(Long id) {
        return especialidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la especialidad con id " + id));
    }
}
