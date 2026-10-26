package com.clinica.citas.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CitaRequestDTO {
    @NotNull
    private Long idMedico;

    @NotNull
    private Long idPaciente;

    @NotNull
    private Long idEspecialidad;

    @NotNull
    @Future
    private LocalDateTime fechaHoraInicio;

    @NotNull
    @Min(1)
    private Integer duracionMinutos;

    @Size(max = 500)
    private String motivo;
}
