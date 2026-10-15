package com.example.ms_control_turnos.dto;

import com.example.ms_control_turnos.model.EstadoPartida;
import com.example.ms_control_turnos.model.Partida;

import java.util.List;

public record PartidaResponse(
        Long id,
        EstadoPartida estado,
        Long jugadorEnTurnoId,
        Integer numeroTurno,
        Long ganadorId,
        List<ParticipanteResponse> participantes) {

    public static PartidaResponse desde(Partida p) {
        Long enTurno = p.getEstado() == EstadoPartida.EN_CURSO
                ? p.participanteEnTurno().getJugadorId()
                : null;

        return new PartidaResponse(
                p.getId(),
                p.getEstado(),
                enTurno,
                p.getNumeroTurno(),
                p.getGanadorId(),
                p.getParticipantes().stream().map(ParticipanteResponse::desde).toList());
    }
}
