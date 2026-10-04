package com.clinica.citas.service;

import java.util.List;

import com.clinica.citas.dto.request.DisponibilidadRequestDTO;
import com.clinica.citas.dto.response.DisponibilidadResponseDTO;

public interface DisponibilidadService {
    DisponibilidadResponseDTO crear(DisponibilidadRequestDTO request);
    List<DisponibilidadResponseDTO> listar();
    DisponibilidadResponseDTO buscarPorId(Long id);
    DisponibilidadResponseDTO actualizar(Long id, DisponibilidadRequestDTO request);
    void eliminar(Long id);
}
