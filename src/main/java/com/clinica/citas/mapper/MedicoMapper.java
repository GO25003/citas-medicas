package com.clinica.citas.mapper;

import java.util.List;

import com.clinica.citas.dto.request.MedicoRequestDTO;
import com.clinica.citas.dto.response.MedicoResponseDTO;
import com.clinica.citas.model.Medico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = EspecialidadMapper.class)
public interface MedicoMapper {

    @Mapping(target = "idPersona", ignore = true)
    @Mapping(target = "especialidades", ignore = true)
    @Mapping(target = "disponibilidades", ignore = true)
    Medico toEntity(MedicoRequestDTO request);

    @Mapping(source = "idPersona", target = "idMedico")
    MedicoResponseDTO toResponse(Medico medico);

    List<MedicoResponseDTO> toResponseList(List<Medico> medicos);

    @Mapping(target = "idPersona", ignore = true)
    @Mapping(target = "especialidades", ignore = true)
    @Mapping(target = "disponibilidades", ignore = true)
    void updateEntity(MedicoRequestDTO request, @MappingTarget Medico medico);
}
