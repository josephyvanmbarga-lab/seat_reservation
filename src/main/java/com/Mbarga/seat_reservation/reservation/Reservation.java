package com.mbarga.seat_reservation.reservation;

import com.mbarga.seat_reservation.vehicule.Vehicule;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_passager", nullable = false, length = 100)
    private String nomPassager;

    @Column(name = "siege_numero", nullable = false)
    private int siegeNumero;

    @Column(name = "date_reservation", nullable = false)
    private OffsetDateTime dateReservation = OffsetDateTime.now();

    @Column(name = "date_voyage", nullable = false)
    private OffsetDateTime dateVoyage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    public Reservation() {}

    public Reservation(String nomPassager, int siegeNumero, OffsetDateTime dateVoyage, Vehicule vehicule) {
        this.nomPassager = nomPassager;
        this.siegeNumero = siegeNumero;
        this.dateVoyage = dateVoyage;
        this.vehicule = vehicule;
    }

    public Long getId() { return id; }
    public String getNomPassager() { return nomPassager; }
    public void setNomPassager(String nomPassager) { this.nomPassager = nomPassager; }
    public int getSiegeNumero() { return siegeNumero; }
    public void setSiegeNumero(int siegeNumero) { this.siegeNumero = siegeNumero; }
    public OffsetDateTime getDateReservation() { return dateReservation; }
    public OffsetDateTime getDateVoyage() { return dateVoyage; }
    public void setDateVoyage(OffsetDateTime dateVoyage) { this.dateVoyage = dateVoyage; }
    public Vehicule getVehicule() { return vehicule; }
    public void setVehicule(Vehicule vehicule) { this.vehicule = vehicule; }
}