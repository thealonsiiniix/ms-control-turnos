package com.example.ms_control_turnos.dto;

import com.example.ms_control_turnos.model.EstadoPartida;

public record JugadaResponse(
        Long partidaId,
        Long jugadorId,
        Integer dado,
        Integer posicionAnterior,
        Integer posicionNueva,
        Boolean trampa,
        Boolean llegoAMeta,
        EstadoPartida estadoPartida,
        Long siguienteJugadorId,
        Long ganadorId) {
}
