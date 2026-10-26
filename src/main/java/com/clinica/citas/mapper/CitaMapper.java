package com.clinica.citas.mapper;

import java.util.List;

import com.clinica.citas.dto.response.CitaResponseDTO;
import com.clinica.citas.model.Cita;
import com.clinica.citas.model.Persona;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * La entidad Cita se arma en CitaServiceImpl porque requiere buscar
 * médico, paciente y especialidad; aquí solo se convierte a respuesta.
 */
@Mapper(componentModel = "spring")
public interface CitaMapper {

    @Mapping(source = "medico.idPersona", target = "idMedico")
    @Mapping(source = "medico", target = "nombreMedico", qualifiedByName = "nombreCompleto")
    @Mapping(source = "paciente.idPersona", target = "idPaciente")
    @Mapping(source = "paciente", target = "nombrePaciente", qualifiedByName = "nombreCompleto")
    @Mapping(source = "especialidadConsulta.idEspecialidad", target = "idEspecialidad")
    @Mapping(source = "especialidadConsulta.nombre", target = "nombreEspecialidad")
    @Mapping(target = "fechaHoraFin",
            expression = "java(cita.getFechaHoraInicio().plusMinutes(cita.getDuracionMinutos()))")
    CitaResponseDTO toResponse(Cita cita);

    List<CitaResponseDTO> toResponseList(List<Cita> citas);

    @Named("nombreCompleto")
    default String nombreCompleto(Persona persona) {
        return persona.getNombre() + " " + persona.getApellido();
    }
}
