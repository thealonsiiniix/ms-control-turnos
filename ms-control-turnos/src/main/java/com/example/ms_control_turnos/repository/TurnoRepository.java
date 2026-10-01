package com.example.ms_control_turnos.repository;

import com.example.ms_control_turnos.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    Optional<Turno> findByJugadorIdAndActivoTrue(Long jugadorId);

    Optional<Turno> findFirstByActivoTrue();
}