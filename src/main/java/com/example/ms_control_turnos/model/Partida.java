package com.example.ms_control_turnos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partidas")
@Getter
@Setter
@NoArgsConstructor
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPartida estado;

    private Integer turnoActual;

    private Integer numeroTurno;

    private Long ganadorId;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    private List<Participante> participantes = new ArrayList<>();

    public void agregarParticipante(Long jugadorId) {
        Participante p = new Participante();
        p.setPartida(this);
        p.setJugadorId(jugadorId);
        p.setOrden(participantes.size() + 1);
        p.setPosicion(0);
        participantes.add(p);
    }

    public Participante participanteEnTurno() {
        return participantes.stream()
                .filter(p -> p.getOrden().equals(turnoActual))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("La partida no tiene un participante en turno"));
    }

    public boolean tieneJugador(Long jugadorId) {
        return participantes.stream().anyMatch(p -> p.getJugadorId().equals(jugadorId));
    }

    public void pasarAlSiguiente() {
        turnoActual = turnoActual % participantes.size() + 1;
        numeroTurno++;
    }

    public void finalizar(Long ganadorId) {
        this.estado = EstadoPartida.FINALIZADA;
        this.ganadorId = ganadorId;
        this.fechaFin = LocalDateTime.now();
    }
}
