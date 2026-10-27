package com.clinica.citas.mapper;

import java.util.List;

import com.clinica.citas.dto.request.PacienteRequestDTO;
import com.clinica.citas.dto.response.PacienteResponseDTO;
import com.clinica.citas.model.Paciente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PacienteMapper {

    @Mapping(target = "idPersona", ignore = true)
    @Mapping(target = "historialCitas", ignore = true)
    Paciente toEntity(PacienteRequestDTO request);

    @Mapping(source = "idPersona", target = "idPaciente")
    PacienteResponseDTO toResponse(Paciente paciente);

    List<PacienteResponseDTO> toResponseList(List<Paciente> pacientes);

    @Mapping(target = "idPersona", ignore = true)
    @Mapping(target = "historialCitas", ignore = true)
    void updateEntity(PacienteRequestDTO request, @MappingTarget Paciente paciente);
}
