package com.example.ms_control_turnos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


@Component
public class LlamadaProtegida {

    private final Map<String, CircuitBreaker> circuitos = new ConcurrentHashMap<>();
    private final int umbralFallos;
    private final Duration tiempoAbierto;
    private final Clock reloj = Clock.systemUTC();

    public LlamadaProtegida(@Value("${circuito.umbral-fallos:3}") int umbralFallos,
                            @Value("${circuito.apertura-ms:10000}") long aperturaMs) {
        this.umbralFallos = umbralFallos;
        this.tiempoAbierto = Duration.ofMillis(aperturaMs);
    }

    public <T> T ejecutar(String servicio, Supplier<T> llamada) {
        CircuitBreaker circuito = circuitos.computeIfAbsent(
                servicio, s -> new CircuitBreaker(umbralFallos, tiempoAbierto, reloj));

        if (!circuito.permitir()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio " + servicio + " no está disponible (circuito abierto), intenta más tarde");
        }

        try {
            T resultado = llamada.get();
            circuito.registrarExito();
            return resultado;

        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is5xxServerError()) {
                circuito.registrarFallo();
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "El servicio " + servicio + " respondió con error " + e.getStatusCode().value());
            }
            circuito.registrarExito();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "El servicio " + servicio + " rechazó la petición (" + e.getStatusCode().value() + ")");

        } catch (ResourceAccessException e) {
            circuito.registrarFallo();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo conectar con el servicio " + servicio);

        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Respuesta inesperada del servicio " + servicio);
        }
    }
}
