package com.clinica.citas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.clinica.citas.dto.request.MedicoRequestDTO;
import com.clinica.citas.dto.response.MedicoResponseDTO;
import com.clinica.citas.exception.ResourceNotFoundException;
import com.clinica.citas.mapper.MedicoMapper;
import com.clinica.citas.model.Especialidad;
import com.clinica.citas.model.Medico;
import com.clinica.citas.repository.EspecialidadRepository;
import com.clinica.citas.repository.MedicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MedicoServiceImplTest {

    private MedicoRepository medicoRepository;
    private EspecialidadRepository especialidadRepository;
    private MedicoMapper medicoMapper;
    private MedicoServiceImpl service;

    private Especialidad especialidad1;
    private Especialidad especialidad2;

    @BeforeEach
    void setUp() {
        medicoRepository = mock(MedicoRepository.class);
        especialidadRepository = mock(EspecialidadRepository.class);
        medicoMapper = mock(MedicoMapper.class);
        service = new MedicoServiceImpl(medicoRepository, especialidadRepository, medicoMapper);

        especialidad1 = mock(Especialidad.class);
        especialidad2 = mock(Especialidad.class);
    }

    private MedicoRequestDTO requestConEspecialidades(Long... ids) {
        MedicoRequestDTO request = mock(MedicoRequestDTO.class);
        when(request.getIdsEspecialidades()).thenReturn(List.of(ids));
        return request;
    }

    // ---------- crear ----------

    @Test
    void crearAsignaEspecialidadesGuardaYDevuelveRespuesta() {
        MedicoRequestDTO request = requestConEspecialidades(1L, 2L);
        Medico medico = mock(Medico.class);
        MedicoResponseDTO response = new MedicoResponseDTO();
        List<Especialidad> especialidades = List.of(especialidad1, especialidad2);
        when(medicoMapper.toEntity(request)).thenReturn(medico);
        when(especialidadRepository.findAllById(any())).thenReturn(especialidades);
        when(medicoRepository.save(medico)).thenReturn(medico);
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        assertSame(response, service.crear(request));

        verify(medico).setEspecialidades(especialidades);
        verify(medicoRepository).save(medico);
    }

    @Test
    void crearLanzaNotFoundCuandoAlgunaEspecialidadNoExiste() {
        MedicoRequestDTO request = requestConEspecialidades(1L, 2L);
        when(medicoMapper.toEntity(request)).thenReturn(mock(Medico.class));
        when(especialidadRepository.findAllById(any())).thenReturn(List.of(especialidad1));

        assertThrows(ResourceNotFoundException.class, () -> service.crear(request));
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void crearIgnoraIdsDeEspecialidadDuplicados() {
        MedicoRequestDTO request = requestConEspecialidades(1L, 1L);
        Medico medico = mock(Medico.class);
        when(medicoMapper.toEntity(request)).thenReturn(medico);
        when(especialidadRepository.findAllById(any())).thenReturn(List.of(especialidad1));
        when(medicoRepository.save(medico)).thenReturn(medico);
        when(medicoMapper.toResponse(medico)).thenReturn(new MedicoResponseDTO());

        service.crear(request);

        verify(medicoRepository).save(medico);
    }

    // ---------- listar / buscar ----------

    @Test
    void listarDevuelveLaListaMapeada() {
        Medico medico = mock(Medico.class);
        List<MedicoResponseDTO> respuesta = List.of(new MedicoResponseDTO());
        when(medicoRepository.findAll()).thenReturn(List.of(medico));
        when(medicoMapper.toResponseList(List.of(medico))).thenReturn(respuesta);

        assertSame(respuesta, service.listar());
    }

    @Test
    void buscarPorIdDevuelveRespuestaCuandoExiste() {
        Medico medico = mock(Medico.class);
        MedicoResponseDTO response = new MedicoResponseDTO();
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        assertSame(response, service.buscarPorId(1L));
    }

    @Test
    void buscarPorIdLanzaNotFoundCuandoNoExiste() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.buscarPorId(99L));

        assertTrue(exception.getMessage().contains("99"));
    }

    // ---------- actualizar ----------

    @Test
    void actualizarReemplazaEspecialidadesYGuarda() {
        MedicoRequestDTO request = requestConEspecialidades(2L);
        Medico medico = mock(Medico.class);
        List<Especialidad> actuales = new ArrayList<>(List.of(especialidad1));
        MedicoResponseDTO response = new MedicoResponseDTO();
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(medico.getEspecialidades()).thenReturn(actuales);
        when(especialidadRepository.findAllById(any())).thenReturn(List.of(especialidad2));
        when(medicoRepository.saveAndFlush(medico)).thenReturn(medico);
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        assertSame(response, service.actualizar(1L, request));

        verify(medicoMapper).updateEntity(request, medico);
        assertEquals(List.of(especialidad2), actuales);
    }

    @Test
    void actualizarLanzaNotFoundCuandoElMedicoNoExiste() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.actualizar(99L, requestConEspecialidades(1L)));
        verify(medicoRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizarLanzaNotFoundCuandoUnaEspecialidadNoExiste() {
        MedicoRequestDTO request = requestConEspecialidades(1L, 2L);
        Medico medico = mock(Medico.class);
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(medico.getEspecialidades()).thenReturn(new ArrayList<>());
        when(especialidadRepository.findAllById(any())).thenReturn(List.of(especialidad1));

        assertThrows(ResourceNotFoundException.class, () -> service.actualizar(1L, request));
        verify(medicoRepository, never()).saveAndFlush(any());
    }

    // ---------- eliminar ----------

    @Test
    void eliminarBorraYHaceFlush() {
        Medico medico = mock(Medico.class);
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));

        service.eliminar(1L);

        verify(medicoRepository).delete(medico);
        verify(medicoRepository).flush();
    }

    @Test
    void eliminarLanzaNotFoundCuandoNoExiste() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.eliminar(99L));
        verify(medicoRepository, never()).delete(any());
    }
}
