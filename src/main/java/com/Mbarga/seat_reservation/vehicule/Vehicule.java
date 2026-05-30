package com.mbarga.seat_reservation.vehicule;

import com.mbarga.seat_reservation.auth.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicule")
@Getter @Setter @NoArgsConstructor
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String immatriculation;

    @Column(length = 100)
    private String modele;

    @Column(nullable = false)
    private int capacite;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_disposition", nullable = false, length = 30)
    private TypeDisposition typeDisposition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chauffeur_id", nullable = false)
    private User chauffeur;

    public Vehicule(String immatriculation, String modele, int capacite,
                    TypeDisposition typeDisposition, User chauffeur) {
        this.immatriculation  = immatriculation;
        this.modele           = modele;
        this.capacite         = capacite;
        this.typeDisposition  = typeDisposition;
        this.chauffeur        = chauffeur;
    }
}
