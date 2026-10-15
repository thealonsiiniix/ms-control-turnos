package com.example.ms_control_turnos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "participantes")
@Getter
@Setter
@NoArgsConstructor
public class Participante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id")
    private Partida partida;

    @Column(nullable = false)
    private Long jugadorId;

    /** 1 = juega primero, 2 = juega segundo, etc. */
    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false)
    private Integer posicion;
}
