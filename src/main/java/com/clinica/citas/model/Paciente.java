package com.clinica.citas.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Paciente extends Persona {

    private LocalDate fechaNacimiento;

    @OneToMany(mappedBy = "paciente", fetch = FetchType.LAZY)
    private List<Cita> historialCitas = new ArrayList<>();
}
