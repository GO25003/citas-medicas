package com.clinica.citas.service;

import java.util.List;

import com.clinica.citas.dto.request.MedicoRequestDTO;
import com.clinica.citas.dto.response.MedicoResponseDTO;

/** Contrato de lógica de negocio para médicos. */
public interface MedicoService {

    MedicoResponseDTO crear(MedicoRequestDTO request);

    List<MedicoResponseDTO> listar();

    MedicoResponseDTO buscarPorId(Long id);

    MedicoResponseDTO actualizar(Long id, MedicoRequestDTO request);

    void eliminar(Long id);
}
