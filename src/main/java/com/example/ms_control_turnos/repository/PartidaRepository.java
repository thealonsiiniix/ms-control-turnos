package com.example.ms_control_turnos.repository;

import com.example.ms_control_turnos.model.Partida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    List<Partida> findAllByOrderByIdAsc();
}
