package com.example.ms_control_turnos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DadoClient {

    private final RestClient rest;
    private final LlamadaProtegida llamada;

    public DadoClient(@Value("${servicios.dado.url}") String url,
                      @Value("${servicios.timeout-ms:3000}") long timeoutMs,
                      LlamadaProtegida llamada) {
        this.rest = ClientesHttp.crear(url, timeoutMs);
        this.llamada = llamada;
    }

    public DadoRespuesta lanzar(String authorization) {
        DadoRespuesta r = llamada.ejecutar("dado", () -> rest.post()
                .uri("/api/v1/dado/lanzar")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(DadoRespuesta.class));

        if (r == null || r.resultado() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "El servicio dado devolvió una respuesta vacía");
        }
        return r;
    }
}
