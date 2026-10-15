package com.example.ms_control_turnos.client;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Circuit Breaker sencillo.
 * CERRADO: las llamadas pasan. Tras 'umbralFallos' fallos seguidos pasa a ABIERTO.
 * ABIERTO: rechaza las llamadas sin intentarlas, hasta que pasa 'tiempoAbierto'.
 * SEMI_ABIERTO: deja pasar una prueba; si sale bien se cierra, si falla se vuelve a abrir.
 */
public class CircuitBreaker {

    public enum Estado { CERRADO, ABIERTO, SEMI_ABIERTO }

    private final int umbralFallos;
    private final Duration tiempoAbierto;
    private final Clock reloj;

    private Estado estado = Estado.CERRADO;
    private int fallosConsecutivos = 0;
    private Instant abiertoDesde;

    public CircuitBreaker(int umbralFallos, Duration tiempoAbierto, Clock reloj) {
        this.umbralFallos = umbralFallos;
        this.tiempoAbierto = tiempoAbierto;
        this.reloj = reloj;
    }

    /** Indica si se puede intentar la llamada. */
    public synchronized boolean permitir() {
        if (estado == Estado.ABIERTO) {
            if (Duration.between(abiertoDesde, reloj.instant()).compareTo(tiempoAbierto) >= 0) {
                estado = Estado.SEMI_ABIERTO;
                return true;
            }
            return false;
        }
        return true;
    }

    public synchronized void registrarExito() {
        fallosConsecutivos = 0;
        estado = Estado.CERRADO;
    }

    public synchronized void registrarFallo() {
        if (estado == Estado.SEMI_ABIERTO) {
            abrir();
            return;
        }
        fallosConsecutivos++;
        if (fallosConsecutivos >= umbralFallos) {
            abrir();
        }
    }

    public synchronized Estado getEstado() {
        return estado;
    }

    private void abrir() {
        estado = Estado.ABIERTO;
        abiertoDesde = reloj.instant();
        fallosConsecutivos = 0;
    }
}
