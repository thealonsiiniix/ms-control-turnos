package com.example.ms_control_turnos.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DadoRespuesta(Integer resultado) {
}
