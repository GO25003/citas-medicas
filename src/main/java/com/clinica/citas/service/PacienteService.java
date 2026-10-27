package com.clinica.citas.service;

import java.util.List;

import com.clinica.citas.dto.request.PacienteRequestDTO;
import com.clinica.citas.dto.response.PacienteResponseDTO;

/** Contrato de lógica de negocio para pacientes. */
public interface PacienteService {

    PacienteResponseDTO crear(PacienteRequestDTO request);

    List<PacienteResponseDTO> listar();

    PacienteResponseDTO buscarPorId(Long id);

    PacienteResponseDTO actualizar(Long id, PacienteRequestDTO request);

    void eliminar(Long id);
}
