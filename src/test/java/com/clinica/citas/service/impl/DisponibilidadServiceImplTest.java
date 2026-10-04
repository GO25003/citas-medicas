package com.clinica.citas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.clinica.citas.dto.request.DisponibilidadRequestDTO;
import com.clinica.citas.dto.response.DisponibilidadResponseDTO;
import com.clinica.citas.exception.ResourceNotFoundException;
import com.clinica.citas.mapper.DisponibilidadMapper;
import com.clinica.citas.model.Disponibilidad;
import com.clinica.citas.model.Medico;
import com.clinica.citas.repository.DisponibilidadRepository;
import com.clinica.citas.repository.MedicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DisponibilidadServiceImplTest {

    private DisponibilidadRepository repository;
    private MedicoRepository medicoRepository;
    private DisponibilidadMapper mapper;
    private DisponibilidadServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(DisponibilidadRepository.class);
        medicoRepository = mock(MedicoRepository.class);
        mapper = mock(DisponibilidadMapper.class);
        service = new DisponibilidadServiceImpl(repository, medicoRepository, mapper);
    }

    @Test
    void crearAsociaMedicoGuardaYDevuelveRespuesta() {
        DisponibilidadRequestDTO request = new DisponibilidadRequestDTO();
        request.setMedicoId(7L);
        Disponibilidad entity = new Disponibilidad();
        Medico medico = new Medico();
        DisponibilidadResponseDTO response = new DisponibilidadResponseDTO();
        when(mapper.toEntity(request)).thenReturn(entity);
        when(medicoRepository.findById(7L)).thenReturn(Optional.of(medico));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertSame(response, service.crear(request));
        assertSame(medico, entity.getMedico());
        verify(repository).save(entity);
    }

    @Test
    void actualizarModificaYGuardaEntidadExistente() {
        DisponibilidadRequestDTO request = new DisponibilidadRequestDTO();
        request.setMedicoId(7L);
        Disponibilidad entity = new Disponibilidad();
        Medico medico = new Medico();
        DisponibilidadResponseDTO response = new DisponibilidadResponseDTO();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(medicoRepository.findById(7L)).thenReturn(Optional.of(medico));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertSame(response, service.actualizar(1L, request));
        verify(mapper).updateEntity(request, entity);
        assertSame(medico, entity.getMedico());
        verify(repository).save(entity);
    }

    @Test
    void eliminarBorraEntidadExistente() {
        Disponibilidad entity = new Disponibilidad();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.eliminar(1L);

        verify(repository).delete(entity);
    }

    @Test
    void actualizarLanzaNotFoundCuandoDisponibilidadNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.actualizar(99L, new DisponibilidadRequestDTO()));

        assertEquals("No se encontró la disponibilidad con id 99", exception.getMessage());
    }

    @Test
    void crearLanzaNotFoundCuandoMedicoNoExiste() {
        DisponibilidadRequestDTO request = new DisponibilidadRequestDTO();
        request.setMedicoId(99L);
        when(mapper.toEntity(request)).thenReturn(new Disponibilidad());
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.crear(request));

        assertEquals("No se encontró el médico con id 99", exception.getMessage());
    }
}
