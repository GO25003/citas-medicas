package com.clinica.citas.service.impl;

import java.util.List;

import com.clinica.citas.dto.request.DisponibilidadRequestDTO;
import com.clinica.citas.dto.response.DisponibilidadResponseDTO;
import com.clinica.citas.exception.ResourceNotFoundException;
import com.clinica.citas.mapper.DisponibilidadMapper;
import com.clinica.citas.model.Disponibilidad;
import com.clinica.citas.model.Medico;
import com.clinica.citas.repository.DisponibilidadRepository;
import com.clinica.citas.repository.MedicoRepository;
import com.clinica.citas.service.DisponibilidadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DisponibilidadServiceImpl implements DisponibilidadService {

    private final DisponibilidadRepository disponibilidadRepository;
    private final MedicoRepository medicoRepository;
    private final DisponibilidadMapper disponibilidadMapper;

    public DisponibilidadServiceImpl(DisponibilidadRepository disponibilidadRepository,
                                     MedicoRepository medicoRepository,
                                     DisponibilidadMapper disponibilidadMapper) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.medicoRepository = medicoRepository;
        this.disponibilidadMapper = disponibilidadMapper;
    }

    @Override
    public DisponibilidadResponseDTO crear(DisponibilidadRequestDTO request) {
        Disponibilidad disponibilidad = disponibilidadMapper.toEntity(request);
        disponibilidad.setMedico(obtenerMedico(request.getMedicoId()));
        return disponibilidadMapper.toResponse(disponibilidadRepository.save(disponibilidad));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadResponseDTO> listar() {
        return disponibilidadMapper.toResponseList(disponibilidadRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadResponseDTO buscarPorId(Long id) {
        return disponibilidadMapper.toResponse(obtenerEntidad(id));
    }

    @Override
    public DisponibilidadResponseDTO actualizar(Long id, DisponibilidadRequestDTO request) {
        Disponibilidad disponibilidad = obtenerEntidad(id);
        disponibilidadMapper.updateEntity(request, disponibilidad);
        disponibilidad.setMedico(obtenerMedico(request.getMedicoId()));
        return disponibilidadMapper.toResponse(disponibilidadRepository.save(disponibilidad));
    }

    @Override
    public void eliminar(Long id) {
        disponibilidadRepository.delete(obtenerEntidad(id));
    }

    private Disponibilidad obtenerEntidad(Long id) {
        return disponibilidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la disponibilidad con id " + id));
    }

    private Medico obtenerMedico(Long medicoId) {
        return medicoRepository.findById(medicoId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el médico con id " + medicoId));
    }
}
