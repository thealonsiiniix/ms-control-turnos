package com.example.ms_control_turnos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/** Consulta a ms-jugadores para comprobar que un jugador existe (RF-01). */
@Component
public class JugadoresClient {

    private final RestClient rest;
    private final LlamadaProtegida llamada;

    public JugadoresClient(@Value("${servicios.jugadores.url}") String url,
                           @Value("${servicios.timeout-ms:3000}") long timeoutMs,
                           LlamadaProtegida llamada) {
        this.rest = ClientesHttp.crear(url, timeoutMs);
        this.llamada = llamada;
    }

    public boolean existe(Long jugadorId, String authorization) {
        return llamada.ejecutar("jugadores", () -> {
            try {
                rest.get()
                        .uri("/api/v1/jugadores/{id}", jugadorId)
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .retrieve()
                        .toBodilessEntity();
                return true;
            } catch (HttpClientErrorException.NotFound e) {
                return false;
            }
        });
    }
}
