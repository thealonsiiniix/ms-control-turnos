package com.example.ms_control_turnos.dto;

import jakarta.validation.constraints.NotNull;

public record JugarRequest(@NotNull(message = "jugadorId es obligatorio") Long jugadorId) {
}
