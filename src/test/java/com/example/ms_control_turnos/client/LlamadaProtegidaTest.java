package com.example.ms_control_turnos.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LlamadaProtegidaTest {

    private final LlamadaProtegida llamada = new LlamadaProtegida(2, 60_000);

    @Test
    void devuelveElResultadoSiTodoSale() {
        assertEquals("ok", llamada.ejecutar("dado", () -> "ok"));
    }

    @Test
    void servicioCaidoDa503() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> llamada.ejecutar("dado", () -> {
                    throw new ResourceAccessException("sin conexión");
                }));

        assertEquals(503, ex.getStatusCode().value());
    }

    @Test
    void trasLosFallosDelUmbralElCircuitoSeAbreYNoSeVuelveALlamar() {
        AtomicInteger intentos = new AtomicInteger();

        for (int i = 0; i < 2; i++) {
            assertThrows(ResponseStatusException.class, () -> llamada.ejecutar("tablero", () -> {
                intentos.incrementAndGet();
                throw new ResourceAccessException("sin conexión");
            }));
        }

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> llamada.ejecutar("tablero", () -> {
                    intentos.incrementAndGet();
                    return "no debería ejecutarse";
                }));

        assertEquals(503, ex.getStatusCode().value());
        assertEquals(2, intentos.get());
    }

    @Test
    void error5xxDelServicioCuentaComoFalloYDa503() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> llamada.ejecutar("historial", () -> {
                    throw HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "error",
                            HttpHeaders.EMPTY, new byte[0], null);
                }));

        assertEquals(503, ex.getStatusCode().value());
    }

    @Test
    void error4xxDa502YNoAbreElCircuito() {
        for (int i = 0; i < 5; i++) {
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> llamada.ejecutar("jugadores", () -> {
                        throw HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "no autorizado",
                                HttpHeaders.EMPTY, new byte[0], null);
                    }));
            assertEquals(502, ex.getStatusCode().value());
        }

        assertEquals("ok", llamada.ejecutar("jugadores", () -> "ok"));
    }
}
