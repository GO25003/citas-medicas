package com.clinica.citas.dto.response;

import java.time.LocalDateTime;

import com.clinica.citas.model.enums.EstadoCita;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CitaResponseDTO {
    private Long idCita;
    private Long idMedico;
    private String nombreMedico;
    private Long idPaciente;
    private String nombrePaciente;
    private Long idEspecialidad;
    private String nombreEspecialidad;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Integer duracionMinutos;
    private String motivo;
    private EstadoCita estado;
}
