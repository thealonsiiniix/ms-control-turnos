package com.example.ms_control_turnos.dto;

import com.example.ms_control_turnos.model.Participante;

public record ParticipanteResponse(Long jugadorId, Integer orden, Integer posicion) {

    public static ParticipanteResponse desde(Participante p) {
        return new ParticipanteResponse(p.getJugadorId(), p.getOrden(), p.getPosicion());
    }
}
