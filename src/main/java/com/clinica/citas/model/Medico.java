package com.clinica.citas.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
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
public class Medico extends Persona {

    private String numeroColegiado;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "medico_especialidad",
            joinColumns = @JoinColumn(name = "medico_id"),
            inverseJoinColumns = @JoinColumn(name = "especialidad_id"))
    private List<Especialidad> especialidades = new ArrayList<>();

    @OneToMany(mappedBy = "medico", fetch = FetchType.LAZY)
    private List<Disponibilidad> disponibilidades = new ArrayList<>();
}
