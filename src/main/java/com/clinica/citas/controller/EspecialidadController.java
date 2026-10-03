package com.clinica.citas.controller;

import java.util.List;

import com.clinica.citas.dto.request.EspecialidadRequestDTO;
import com.clinica.citas.dto.response.EspecialidadResponseDTO;
import com.clinica.citas.service.EspecialidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Punto de entrada REST para la gestión de especialidades. */
@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadController {
    private final EspecialidadService especialidadService;

    public EspecialidadController(EspecialidadService especialidadService) {
        this.especialidadService = especialidadService;
    }

    @GetMapping
    public List<EspecialidadResponseDTO> listar() {
        return especialidadService.listar();
    }

    @GetMapping("/{id}")
    public EspecialidadResponseDTO buscarPorId(@PathVariable Long id) {
        return especialidadService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<EspecialidadResponseDTO> crear(@Valid @RequestBody EspecialidadRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(especialidadService.crear(request));
    }

    @PutMapping("/{id}")
    public EspecialidadResponseDTO actualizar(@PathVariable Long id,
                                               @Valid @RequestBody EspecialidadRequestDTO request) {
        return especialidadService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        especialidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
