package com.clinica.citas.service;

import java.util.List;

import com.clinica.citas.dto.request.CitaRequestDTO;
import com.clinica.citas.dto.response.CitaResponseDTO;

/** Contrato de lógica de negocio para citas médicas. */
public interface CitaService {

    CitaResponseDTO crear(CitaRequestDTO request);

    List<CitaResponseDTO> listar();

    CitaResponseDTO buscarPorId(Long id);

    CitaResponseDTO actualizar(Long id, CitaRequestDTO request);

    CitaResponseDTO confirmar(Long id);

    /** Cambia el estado a CANCELADA; la cita no se elimina físicamente. */
    CitaResponseDTO cancelar(Long id);
}
