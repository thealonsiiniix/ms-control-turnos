package com.example.ms_control_turnos.service;

import com.example.ms_control_turnos.model.Turno;
import com.example.ms_control_turnos.repository.TurnoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;

    public TurnoService(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    public Turno crearTurno(Long jugadorId) {

        Turno turno = new Turno();

        turno.setJugadorId(jugadorId);
        turno.setNumeroTurno(1);
        turno.setActivo(true);
        turno.setPartidaFinalizada(false);
        turno.setFecha(LocalDateTime.now());

        return turnoRepository.save(turno);
    }

    public Turno obtenerTurnoActual() {

        return turnoRepository
                .findFirstByActivoTrue()
                .orElse(null);
    }

    public Turno obtenerTurnoJugador(Long jugadorId) {

        return turnoRepository
                .findByJugadorIdAndActivoTrue(jugadorId)
                .orElse(null);
    }

    public List<Turno> listarTurnos() {

        return turnoRepository.findAll();
    }

    public Turno avanzarTurno(Long id) {

        Turno turno = turnoRepository
                .findById(id)
                .orElse(null);

        if (turno == null) {
            return null;
        }

        turno.setActivo(false);

        return turnoRepository.save(turno);
    }

    public Turno finalizarPartida(Long id) {

        Turno turno = turnoRepository
                .findById(id)
                .orElse(null);

        if (turno == null) {
            return null;
        }

        turno.setActivo(false);
        turno.setPartidaFinalizada(true);

        return turnoRepository.save(turno);
    }

    public void eliminar(Long id) {

        turnoRepository.deleteById(id);
    }
}