package com.clinica.citas.service.impl;

import java.util.List;

import com.clinica.citas.dto.request.PacienteRequestDTO;
import com.clinica.citas.dto.response.PacienteResponseDTO;
import com.clinica.citas.exception.ResourceNotFoundException;
import com.clinica.citas.mapper.PacienteMapper;
import com.clinica.citas.model.Paciente;
import com.clinica.citas.repository.PacienteRepository;
import com.clinica.citas.service.PacienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PacienteMapper pacienteMapper;

    public PacienteServiceImpl(PacienteRepository pacienteRepository, PacienteMapper pacienteMapper) {
        this.pacienteRepository = pacienteRepository;
        this.pacienteMapper = pacienteMapper;
    }

    @Override
    public PacienteResponseDTO crear(PacienteRequestDTO request) {
        Paciente paciente = pacienteMapper.toEntity(request);
        return pacienteMapper.toResponse(pacienteRepository.save(paciente));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> listar() {
        return pacienteMapper.toResponseList(pacienteRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponseDTO buscarPorId(Long id) {
        return pacienteMapper.toResponse(obtenerEntidad(id));
    }

    @Override
    public PacienteResponseDTO actualizar(Long id, PacienteRequestDTO request) {
        Paciente paciente = obtenerEntidad(id);
        pacienteMapper.updateEntity(request, paciente);
        return pacienteMapper.toResponse(pacienteRepository.saveAndFlush(paciente));
    }

    @Override
    public void eliminar(Long id) {
        pacienteRepository.delete(obtenerEntidad(id));
        // flush inmediato: si tiene citas, la BD rechaza el borrado (409)
        pacienteRepository.flush();
    }

    private Paciente obtenerEntidad(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el paciente con id " + id));
    }
}
