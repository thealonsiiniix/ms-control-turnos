package com.example.ms_control_turnos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/** Llama a ms-tablero para calcular la casilla final (RF-03). */
@Component
public class TableroClient {

    public record MoverPeticion(Long jugadorId, Integer posicionActual, Integer dado) {
    }

    private final RestClient rest;
    private final LlamadaProtegida llamada;

    public TableroClient(@Value("${servicios.tablero.url}") String url,
                         @Value("${servicios.timeout-ms:3000}") long timeoutMs,
                         LlamadaProtegida llamada) {
        this.rest = ClientesHttp.crear(url, timeoutMs);
        this.llamada = llamada;
    }

    public MovimientoRespuesta mover(Long jugadorId, int posicionActual, int dado, String authorization) {
        MovimientoRespuesta r = llamada.ejecutar("tablero", () -> rest.post()
                .uri("/api/v1/tablero/mover")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(new MoverPeticion(jugadorId, posicionActual, dado))
                .retrieve()
                .body(MovimientoRespuesta.class));

        if (r == null || r.posicionNueva() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "El servicio tablero devolvió una respuesta vacía");
        }
        return r;
    }
}
