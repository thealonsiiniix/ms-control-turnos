package com.example.ms_control_turnos.service;

import com.example.ms_control_turnos.client.*;
import com.example.ms_control_turnos.dto.JugadaResponse;
import com.example.ms_control_turnos.model.EstadoPartida;
import com.example.ms_control_turnos.model.Participante;
import com.example.ms_control_turnos.model.Partida;
import com.example.ms_control_turnos.repository.PartidaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;


@Service
public class PartidaService {

    private final PartidaRepository partidaRepository;
    private final JugadoresClient jugadores;
    private final DadoClient dado;
    private final TableroClient tablero;
    private final HistorialClient historial;

    public PartidaService(PartidaRepository partidaRepository,
                          JugadoresClient jugadores,
                          DadoClient dado,
                          TableroClient tablero,
                          HistorialClient historial) {
        this.partidaRepository = partidaRepository;
        this.jugadores = jugadores;
        this.dado = dado;
        this.tablero = tablero;
        this.historial = historial;
    }

    public Partida crear(List<Long> jugadorIds, String authorization) {

        if (new HashSet<>(jugadorIds).size() != jugadorIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hay jugadores repetidos en la partida");
        }

        for (Long id : jugadorIds) {
            if (!jugadores.existe(id, authorization)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El jugador " + id + " no existe");
            }
        }

        Partida partida = new Partida();
        partida.setEstado(EstadoPartida.EN_CURSO);
        partida.setTurnoActual(1);
        partida.setNumeroTurno(1);
        partida.setFechaInicio(LocalDateTime.now());
        jugadorIds.forEach(partida::agregarParticipante);

        return partidaRepository.save(partida);
    }

    public List<Partida> listar() {
        return partidaRepository.findAllByOrderByIdAsc();
    }

    public Optional<Partida> buscarPorId(Long id) {
        return partidaRepository.findById(id);
    }

    public JugadaResponse jugar(Long partidaId, Long jugadorId, String authorization) {

        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La partida no existe"));

        if (partida.getEstado() != EstadoPartida.EN_CURSO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La partida ya finalizó");
        }

        if (!partida.tieneJugador(jugadorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El jugador " + jugadorId + " no participa en esta partida");
        }

        Participante enTurno = partida.participanteEnTurno();
        if (!enTurno.getJugadorId().equals(jugadorId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No es el turno del jugador " + jugadorId + ", le toca al jugador " + enTurno.getJugadorId());
        }

        int posicionAnterior = enTurno.getPosicion();

        int valorDado = dado.lanzar(authorization).resultado();
        MovimientoRespuesta movimiento = tablero.mover(jugadorId, posicionAnterior, valorDado, authorization);
        int posicionNueva = movimiento.posicionNueva();
        historial.registrar(jugadorId, valorDado, posicionAnterior, posicionNueva, authorization);

        enTurno.setPosicion(posicionNueva);

        boolean llegoAMeta = Boolean.TRUE.equals(movimiento.llegoAMeta());
        if (llegoAMeta) {
            partida.finalizar(jugadorId);
        } else {
            partida.pasarAlSiguiente();
        }

        Partida guardada;
        try {
            guardada = partidaRepository.save(partida);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La partida cambió mientras se jugaba, intenta de nuevo");
        }

        Long siguiente = llegoAMeta ? null : guardada.participanteEnTurno().getJugadorId();

        return new JugadaResponse(
                guardada.getId(),
                jugadorId,
                valorDado,
                posicionAnterior,
                posicionNueva,
                movimiento.trampa(),
                llegoAMeta,
                guardada.getEstado(),
                siguiente,
                guardada.getGanadorId());
    }
}
