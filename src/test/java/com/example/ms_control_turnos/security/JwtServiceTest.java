package com.example.ms_control_turnos.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "una-clave-de-prueba-de-al-menos-32-caracteres!!";
    private static final String OTRA = "otra-clave-distinta-de-al-menos-32-caracteres!!!";

    private final JwtService service = new JwtService(SECRET);

    private String token(String secret, long expiraEnMs) {
        return Jwts.builder()
                .subject("1")
                .expiration(new Date(System.currentTimeMillis() + expiraEnMs))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    void tokenValidoDevuelveElSubject() {
        assertEquals("1", service.validar(token(SECRET, 60_000)).getSubject());
    }

    @Test
    void tokenExpiradoSeRechaza() {
        assertThrows(JwtException.class, () -> service.validar(token(SECRET, -60_000)));
    }

    @Test
    void tokenFirmadoConOtraClaveSeRechaza() {
        assertThrows(JwtException.class, () -> service.validar(token(OTRA, 60_000)));
    }

    @Test
    void tokenSinExpiracionSeRechaza() {
        String sinExp = Jwts.builder()
                .subject("1")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
        assertThrows(JwtException.class, () -> service.validar(sinExp));
    }

    @Test
    void secretoCortoNoSePermite() {
        assertThrows(IllegalStateException.class, () -> new JwtService("corto"));
    }
}
