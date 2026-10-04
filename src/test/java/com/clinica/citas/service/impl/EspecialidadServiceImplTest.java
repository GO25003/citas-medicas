package com.clinica.citas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.clinica.citas.dto.request.EspecialidadRequestDTO;
import com.clinica.citas.dto.response.EspecialidadResponseDTO;
import com.clinica.citas.exception.ResourceNotFoundException;
import com.clinica.citas.mapper.EspecialidadMapper;
import com.clinica.citas.model.Especialidad;
import com.clinica.citas.repository.EspecialidadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EspecialidadServiceImplTest {

    private EspecialidadRepository repository;
    private EspecialidadMapper mapper;
    private EspecialidadServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(EspecialidadRepository.class);
        mapper = mock(EspecialidadMapper.class);
        service = new EspecialidadServiceImpl(repository, mapper);
    }

    @Test
    void crearGuardaYDevuelveRespuesta() {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO();
        Especialidad entity = new Especialidad();
        EspecialidadResponseDTO response = new EspecialidadResponseDTO();
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertSame(response, service.crear(request));
        verify(repository).save(entity);
    }

    @Test
    void actualizarModificaYGuardaEntidadExistente() {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO();
        Especialidad entity = new Especialidad();
        EspecialidadResponseDTO response = new EspecialidadResponseDTO();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertSame(response, service.actualizar(1L, request));
        verify(mapper).updateEntity(request, entity);
        verify(repository).save(entity);
    }

    @Test
    void eliminarBorraEntidadExistente() {
        Especialidad entity = new Especialidad();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.eliminar(1L);

        verify(repository).delete(entity);
    }

    @Test
    void actualizarLanzaNotFoundCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.actualizar(99L, new EspecialidadRequestDTO()));

        assertEquals("No se encontró la especialidad con id 99", exception.getMessage());
    }
}
