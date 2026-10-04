package com.clinica.citas.mapper;

import java.util.List;

import com.clinica.citas.dto.request.EspecialidadRequestDTO;
import com.clinica.citas.dto.response.EspecialidadResponseDTO;
import com.clinica.citas.model.Especialidad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EspecialidadMapper {
    @Mapping(target = "idEspecialidad", ignore = true)
    Especialidad toEntity(EspecialidadRequestDTO request);

    EspecialidadResponseDTO toResponse(Especialidad especialidad);

    List<EspecialidadResponseDTO> toResponseList(List<Especialidad> especialidades);

    @Mapping(target = "idEspecialidad", ignore = true)
    void updateEntity(EspecialidadRequestDTO request, @MappingTarget Especialidad especialidad);
}
