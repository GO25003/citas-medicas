package com.clinica.citas.service.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.clinica.citas.dto.request.PacienteRequestDTO;
import com.clinica.citas.dto.response.PacienteResponseDTO;
import com.clinica.citas.exception.ResourceNotFoundException;
import com.clinica.citas.mapper.PacienteMapper;
import com.clinica.citas.model.Paciente;
import com.clinica.citas.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PacienteServiceImplTest {

    private PacienteRepository repository;
    private PacienteMapper mapper;
    private PacienteServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(PacienteRepository.class);
        mapper = mock(PacienteMapper.class);
        service = new PacienteServiceImpl(repository, mapper);
    }

    @Test
    void crearGuardaYDevuelveRespuesta() {
        PacienteRequestDTO request = mock(PacienteRequestDTO.class);
        Paciente entity = mock(Paciente.class);
        PacienteResponseDTO response = new PacienteResponseDTO();
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertSame(response, service.crear(request));
        verify(repository).save(entity);
    }

    @Test
    void listarDevuelveLaListaMapeada() {
        Paciente entity = mock(Paciente.class);
        List<PacienteResponseDTO> respuesta = List.of(new PacienteResponseDTO());
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toResponseList(List.of(entity))).thenReturn(respuesta);

        assertSame(respuesta, service.listar());
    }

    @Test
    void buscarPorIdDevuelveRespuestaCuandoExiste() {
        Paciente entity = mock(Paciente.class);
        PacienteResponseDTO response = new PacienteResponseDTO();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertSame(response, service.buscarPorId(1L));
    }

    @Test
    void buscarPorIdLanzaNotFoundCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.buscarPorId(99L));

        assertTrue(exception.getMessage().contains("99"));
    }

    @Test
    void actualizarModificaYGuardaEntidadExistente() {
        PacienteRequestDTO request = mock(PacienteRequestDTO.class);
        Paciente entity = mock(Paciente.class);
        PacienteResponseDTO response = new PacienteResponseDTO();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.saveAndFlush(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertSame(response, service.actualizar(1L, request));

        verify(mapper).updateEntity(request, entity);
        verify(repository).saveAndFlush(entity);
    }

    @Test
    void actualizarLanzaNotFoundCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.actualizar(99L, mock(PacienteRequestDTO.class)));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void eliminarBorraYHaceFlush() {
        Paciente entity = mock(Paciente.class);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.eliminar(1L);

        verify(repository).delete(entity);
        verify(repository).flush();
    }

    @Test
    void eliminarLanzaNotFoundCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.eliminar(99L));
        verify(repository, never()).delete(any());
    }
}
