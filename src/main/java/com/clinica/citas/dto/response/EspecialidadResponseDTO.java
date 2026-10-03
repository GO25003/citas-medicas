package com.clinica.citas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EspecialidadResponseDTO {
    private Long idEspecialidad;
    private String nombre;
    private String descripcion;
}
