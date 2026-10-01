package com.example.ms_control_turnos.controller;

import com.example.ms_control_turnos.model.Turno;
import com.example.ms_control_turnos.service.TurnoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @PostMapping
    public ResponseEntity<Turno> crearTurno(
            @RequestParam Long jugadorId) {

        Turno turno = turnoService.crearTurno(jugadorId);

        return ResponseEntity.ok(turno);
    }

    @GetMapping
    public ResponseEntity<List<Turno>> listarTurnos() {

        return ResponseEntity.ok(
                turnoService.listarTurnos()
        );
    }

    @GetMapping("/actual")
    public ResponseEntity<Turno> obtenerTurnoActual() {

        Turno turno = turnoService.obtenerTurnoActual();

        if (turno == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(turno);
    }

    @GetMapping("/jugador/{jugadorId}")
    public ResponseEntity<Turno> obtenerTurnoJugador(
            @PathVariable Long jugadorId) {

        Turno turno = turnoService.obtenerTurnoJugador(jugadorId);

        if (turno == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(turno);
    }

    @PutMapping("/{id}/avanzar")
    public ResponseEntity<Turno> avanzarTurno(
            @PathVariable Long id) {

        Turno turno = turnoService.avanzarTurno(id);

        if (turno == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(turno);
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<Turno> finalizarPartida(
            @PathVariable Long id) {

        Turno turno = turnoService.finalizarPartida(id);

        if (turno == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(turno);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        turnoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}