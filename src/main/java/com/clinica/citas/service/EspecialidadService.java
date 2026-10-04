package com.clinica.citas.service;

import java.util.List;

import com.clinica.citas.dto.request.EspecialidadRequestDTO;
import com.clinica.citas.dto.response.EspecialidadResponseDTO;

/** Contrato de lógica de negocio para especialidades. */
public interface EspecialidadService {
    EspecialidadResponseDTO crear(EspecialidadRequestDTO request);
    List<EspecialidadResponseDTO> listar();
    EspecialidadResponseDTO buscarPorId(Long id);
    EspecialidadResponseDTO actualizar(Long id, EspecialidadRequestDTO request);
    void eliminar(Long id);
}
