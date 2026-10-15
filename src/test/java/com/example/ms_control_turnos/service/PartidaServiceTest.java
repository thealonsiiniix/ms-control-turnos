package com.example.ms_control_turnos.service;

import com.example.ms_control_turnos.client.*;
import com.example.ms_control_turnos.dto.JugadaResponse;
import com.example.ms_control_turnos.model.EstadoPartida;
import com.example.ms_control_turnos.model.Partida;
import com.example.ms_control_turnos.repository.PartidaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PartidaServiceTest {

    private static final String AUTH = "Bearer token-de-prueba";

    private final PartidaRepository repo = mock(PartidaRepository.class);
    private final JugadoresClient jugadores = mock(JugadoresClient.class);
    private final DadoClient dado = mock(DadoClient.class);
    private final TableroClient tablero = mock(TableroClient.class);
    private final HistorialClient historial = mock(HistorialClient.class);
    private final PartidaService service = new PartidaService(repo, jugadores, dado, tablero, historial);

    private Partida partidaEnCurso(Long... jugadorIds) {
        Partida p = new Partida();
        p.setId(10L);
        p.setEstado(EstadoPartida.EN_CURSO);
        p.setTurnoActual(1);
        p.setNumeroTurno(1);
        for (Long id : jugadorIds) {
            p.agregarParticipante(id);
        }
        when(repo.findById(10L)).thenReturn(Optional.of(p));
        when(repo.save(any(Partida.class))).thenAnswer(inv -> inv.getArgument(0));
        return p;
    }

    // ---------- crear ----------

    @Test
    void crearPartidaAsignaOrdenYPosicionInicial() {
        when(jugadores.existe(anyLong(), anyString())).thenReturn(true);
        when(repo.save(any(Partida.class))).thenAnswer(inv -> inv.getArgument(0));

        Partida p = service.crear(List.of(5L, 7L), AUTH);

        assertEquals(EstadoPartida.EN_CURSO, p.getEstado());
        assertEquals(1, p.getTurnoActual());
        assertEquals(2, p.getParticipantes().size());
        assertEquals(5L, p.getParticipantes().get(0).getJugadorId());
        assertEquals(1, p.getParticipantes().get(0).getOrden());
        assertEquals(2, p.getParticipantes().get(1).getOrden());
        assertEquals(0, p.getParticipantes().get(1).getPosicion());
    }

    @Test
    void crearConJugadorInexistenteDa400() {
        when(jugadores.existe(5L, AUTH)).thenReturn(true);
        when(jugadores.existe(99L, AUTH)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.crear(List.of(5L, 99L), AUTH));

        assertEquals(400, ex.getStatusCode().value());
        verify(repo, never()).save(any(Partida.class));
    }

    @Test
    void crearConJugadoresRepetidosDa400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.crear(List.of(5L, 5L), AUTH));

        assertEquals(400, ex.getStatusCode().value());
    }

    // ---------- jugar ----------

    @Test
    void jugarAvanzaLaFichaRegistraEnHistorialYPasaElTurno() {
        Partida p = partidaEnCurso(1L, 2L);
        when(dado.lanzar(AUTH)).thenReturn(new DadoRespuesta(4));
        when(tablero.mover(1L, 0, 4, AUTH)).thenReturn(new MovimientoRespuesta(4, false, false));

        JugadaResponse r = service.jugar(10L, 1L, AUTH);

        assertEquals(4, r.dado());
        assertEquals(0, r.posicionAnterior());
        assertEquals(4, r.posicionNueva());
        assertFalse(r.llegoAMeta());
        assertEquals(EstadoPartida.EN_CURSO, r.estadoPartida());
        assertEquals(2L, r.siguienteJugadorId());
        assertEquals(2, p.getTurnoActual());
        assertEquals(2, p.getNumeroTurno());
        assertEquals(4, p.getParticipantes().get(0).getPosicion());
        verify(historial).registrar(1L, 4, 0, 4, AUTH);
    }

    @Test
    void despuesDelUltimoJugadorVuelveAJugarElPrimero() {
        Partida p = partidaEnCurso(1L, 2L);
        p.setTurnoActual(2);
        when(dado.lanzar(AUTH)).thenReturn(new DadoRespuesta(3));
        when(tablero.mover(2L, 0, 3, AUTH)).thenReturn(new MovimientoRespuesta(3, false, false));

        JugadaResponse r = service.jugar(10L, 2L, AUTH);

        assertEquals(1L, r.siguienteJugadorId());
        assertEquals(1, p.getTurnoActual());
    }

    @Test
    void llegarALaMetaFinalizaLaPartidaYDeclaraAlGanador() {
        Partida p = partidaEnCurso(1L, 2L);
        p.getParticipantes().get(0).setPosicion(27);
        when(dado.lanzar(AUTH)).thenReturn(new DadoRespuesta(3));
        when(tablero.mover(1L, 27, 3, AUTH)).thenReturn(new MovimientoRespuesta(30, false, true));

        JugadaResponse r = service.jugar(10L, 1L, AUTH);

        assertTrue(r.llegoAMeta());
        assertEquals(EstadoPartida.FINALIZADA, r.estadoPartida());
        assertEquals(1L, r.ganadorId());
        assertNull(r.siguienteJugadorId());
        assertEquals(EstadoPartida.FINALIZADA, p.getEstado());
        assertNotNull(p.getFechaFin());
    }

    @Test
    void jugarFueraDeTurnoDa409YNoLlamaANadie() {
        partidaEnCurso(1L, 2L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.jugar(10L, 2L, AUTH));

        assertEquals(409, ex.getStatusCode().value());
        verify(dado, never()).lanzar(anyString());
        verify(tablero, never()).mover(anyLong(), anyInt(), anyInt(), anyString());
        verify(historial, never()).registrar(anyLong(), anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    void jugadorQueNoParticipaDa400() {
        partidaEnCurso(1L, 2L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.jugar(10L, 99L, AUTH));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void partidaFinalizadaNoPermiteJugar() {
        Partida p = partidaEnCurso(1L, 2L);
        p.finalizar(1L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.jugar(10L, 1L, AUTH));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void partidaInexistenteDa404() {
        when(repo.findById(77L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.jugar(77L, 1L, AUTH));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void siFallaElHistorialLaPartidaNoAvanza() {
        Partida p = partidaEnCurso(1L, 2L);
        when(dado.lanzar(AUTH)).thenReturn(new DadoRespuesta(4));
        when(tablero.mover(1L, 0, 4, AUTH)).thenReturn(new MovimientoRespuesta(4, false, false));
        org.mockito.Mockito.doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "historial caído"))
                .when(historial).registrar(anyLong(), anyInt(), anyInt(), anyInt(), anyString());

        assertThrows(ResponseStatusException.class, () -> service.jugar(10L, 1L, AUTH));

        assertEquals(1, p.getTurnoActual());
        assertEquals(0, p.getParticipantes().get(0).getPosicion());
        verify(repo, never()).save(any(Partida.class));
    }
}
