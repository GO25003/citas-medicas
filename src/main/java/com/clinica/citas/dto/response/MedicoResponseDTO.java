package com.clinica.citas.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicoResponseDTO {
    private Long idMedico;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String numeroColegiado;
    private List<EspecialidadResponseDTO> especialidades;
}
