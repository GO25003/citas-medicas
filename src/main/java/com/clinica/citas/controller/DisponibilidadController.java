package com.clinica.citas.controller;

import java.util.List;

import com.clinica.citas.dto.request.DisponibilidadRequestDTO;
import com.clinica.citas.dto.response.DisponibilidadResponseDTO;
import com.clinica.citas.service.DisponibilidadService;
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

@RestController
@RequestMapping("/api/disponibilidades")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    @GetMapping
    public List<DisponibilidadResponseDTO> listar() {
        return disponibilidadService.listar();
    }

    @GetMapping("/{id}")
    public DisponibilidadResponseDTO buscarPorId(@PathVariable Long id) {
        return disponibilidadService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<DisponibilidadResponseDTO> crear(
            @Valid @RequestBody DisponibilidadRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(disponibilidadService.crear(request));
    }

    @PutMapping("/{id}")
    public DisponibilidadResponseDTO actualizar(@PathVariable Long id,
                                                 @Valid @RequestBody DisponibilidadRequestDTO request) {
        return disponibilidadService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        disponibilidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
