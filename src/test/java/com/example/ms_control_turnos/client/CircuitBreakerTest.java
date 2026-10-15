package com.example.ms_control_turnos.client;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircuitBreakerTest {

    /** Reloj que se puede adelantar a mano, para probar el paso del tiempo. */
    static class RelojManual extends Clock {
        private Instant ahora = Instant.parse("2026-10-13T12:00:00Z");

        void avanzar(Duration d) {
            ahora = ahora.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    private final RelojManual reloj = new RelojManual();
    private final CircuitBreaker cb = new CircuitBreaker(3, Duration.ofSeconds(10), reloj);

    @Test
    void empiezaCerradoYDejaPasar() {
        assertEquals(CircuitBreaker.Estado.CERRADO, cb.getEstado());
        assertTrue(cb.permitir());
    }

    @Test
    void seAbreTrasLosFallosSeguidosDelUmbral() {
        cb.registrarFallo();
        cb.registrarFallo();
        assertEquals(CircuitBreaker.Estado.CERRADO, cb.getEstado());

        cb.registrarFallo();

        assertEquals(CircuitBreaker.Estado.ABIERTO, cb.getEstado());
        assertFalse(cb.permitir());
    }

    @Test
    void unExitoReiniciaElContadorDeFallos() {
        cb.registrarFallo();
        cb.registrarFallo();
        cb.registrarExito();
        cb.registrarFallo();
        cb.registrarFallo();

        assertEquals(CircuitBreaker.Estado.CERRADO, cb.getEstado());
    }

    @Test
    void despuesDelTiempoPasaASemiAbiertoYUnExitoLoCierra() {
        cb.registrarFallo();
        cb.registrarFallo();
        cb.registrarFallo();

        reloj.avanzar(Duration.ofSeconds(11));

        assertTrue(cb.permitir());
        assertEquals(CircuitBreaker.Estado.SEMI_ABIERTO, cb.getEstado());

        cb.registrarExito();
        assertEquals(CircuitBreaker.Estado.CERRADO, cb.getEstado());
    }

    @Test
    void unFalloEnSemiAbiertoVuelveAAbrirElCircuito() {
        cb.registrarFallo();
        cb.registrarFallo();
        cb.registrarFallo();
        reloj.avanzar(Duration.ofSeconds(11));
        cb.permitir();

        cb.registrarFallo();

        assertEquals(CircuitBreaker.Estado.ABIERTO, cb.getEstado());
        assertFalse(cb.permitir());
    }
}
