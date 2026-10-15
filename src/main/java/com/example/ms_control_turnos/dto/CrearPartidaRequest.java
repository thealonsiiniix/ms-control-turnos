package com.example.ms_control_turnos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CrearPartidaRequest(
        @NotNull(message = "jugadorIds es obligatorio")
        @Size(min = 2, max = 4, message = "Una partida necesita entre 2 y 4 jugadores")
        List<@NotNull(message = "Los ids no pueden ser nulos") Long> jugadorIds) {
}
