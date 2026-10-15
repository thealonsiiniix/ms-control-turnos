package com.example.ms_control_turnos.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManejadorErroresTest {

    private final ManejadorErrores manejador = new ManejadorErrores();

    @Test
    void elMensajeDeLaExcepcionLlegaEnElCuerpo() {
        ResponseEntity<Map<String, Object>> r = manejador.estado(new ResponseStatusException(
                HttpStatus.CONFLICT, "No es el turno del jugador 1, le toca al jugador 2"));

        assertEquals(409, r.getStatusCode().value());
        assertEquals("No es el turno del jugador 1, le toca al jugador 2", r.getBody().get("mensaje"));
        assertEquals(409, r.getBody().get("status"));
    }

    @Test
    void sinMensajeUsaLaDescripcionDelEstado() {
        ResponseEntity<Map<String, Object>> r = manejador.estado(new ResponseStatusException(HttpStatus.NOT_FOUND));

        assertEquals("Not Found", r.getBody().get("mensaje"));
    }
}
