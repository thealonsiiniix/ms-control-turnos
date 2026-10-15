package com.example.ms_control_turnos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Registra cada jugada en ms-historial-jugadas. */
@Component
public class HistorialClient {

    public record JugadaPeticion(Long jugadorId, Integer valorDado, Integer posicionAnterior, Integer posicionNueva) {
    }

    private final RestClient rest;
    private final LlamadaProtegida llamada;

    public HistorialClient(@Value("${servicios.historial.url}") String url,
                           @Value("${servicios.timeout-ms:3000}") long timeoutMs,
                           LlamadaProtegida llamada) {
        this.rest = ClientesHttp.crear(url, timeoutMs);
        this.llamada = llamada;
    }

    public void registrar(Long jugadorId, int valorDado, int posicionAnterior, int posicionNueva, String authorization) {
        llamada.ejecutar("historial", () -> rest.post()
                .uri("/api/v1/historial")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(new JugadaPeticion(jugadorId, valorDado, posicionAnterior, posicionNueva))
                .retrieve()
                .toBodilessEntity());
    }
}
