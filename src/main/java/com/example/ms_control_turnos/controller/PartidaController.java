package com.example.ms_control_turnos.controller;

import com.example.ms_control_turnos.dto.CrearPartidaRequest;
import com.example.ms_control_turnos.dto.JugadaResponse;
import com.example.ms_control_turnos.dto.JugarRequest;
import com.example.ms_control_turnos.dto.PartidaResponse;
import com.example.ms_control_turnos.service.PartidaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/partidas")
public class PartidaController {

    private final PartidaService partidaService;

    public PartidaController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    /** Crea una partida con los jugadores indicados, en ese orden de juego. */
    @PostMapping
    public ResponseEntity<PartidaResponse> crear(
            @Valid @RequestBody CrearPartidaRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PartidaResponse.desde(partidaService.crear(request.jugadorIds(), authorization)));
    }

    @PostMapping("/{id}/jugar")
    public ResponseEntity<JugadaResponse> jugar(
            @PathVariable Long id,
            @Valid @RequestBody JugarRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        return ResponseEntity.ok(partidaService.jugar(id, request.jugadorId(), authorization));
    }

    @GetMapping
    public ResponseEntity<List<PartidaResponse>> listar() {
        return ResponseEntity.ok(partidaService.listar().stream().map(PartidaResponse::desde).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartidaResponse> buscarPorId(@PathVariable Long id) {
        return partidaService.buscarPorId(id)
                .map(PartidaResponse::desde)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
