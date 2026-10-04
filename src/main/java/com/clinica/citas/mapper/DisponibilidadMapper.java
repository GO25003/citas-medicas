package com.clinica.citas.mapper;

import java.util.List;

import com.clinica.citas.dto.request.DisponibilidadRequestDTO;
import com.clinica.citas.dto.response.DisponibilidadResponseDTO;
import com.clinica.citas.model.Disponibilidad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DisponibilidadMapper {
    @Mapping(target = "idDisponibilidad", ignore = true)
    @Mapping(target = "medico", ignore = true)
    Disponibilidad toEntity(DisponibilidadRequestDTO request);

    @Mapping(source = "medico.idPersona", target = "medicoId")
    DisponibilidadResponseDTO toResponse(Disponibilidad disponibilidad);

    List<DisponibilidadResponseDTO> toResponseList(List<Disponibilidad> disponibilidades);

    @Mapping(target = "idDisponibilidad", ignore = true)
    @Mapping(target = "medico", ignore = true)
    void updateEntity(DisponibilidadRequestDTO request, @MappingTarget Disponibilidad disponibilidad);
}
