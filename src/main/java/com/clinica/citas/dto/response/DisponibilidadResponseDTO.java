package com.clinica.citas.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadResponseDTO {
    private Long idDisponibilidad;
    private Long medicoId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean disponible;
}
