package com.mbarga.seat_reservation.reservation;

import java.time.OffsetDateTime;

public class ReservationResponse {

    private Long id;
    private String nomPassager;
    private int siegeNumero;
    private OffsetDateTime dateReservation;
    private OffsetDateTime dateVoyage;
    private Long vehiculeId;
    private String vehiculeImmatriculation;

    public ReservationResponse() {}

    public ReservationResponse(Long id, String nomPassager, int siegeNumero,
                               OffsetDateTime dateReservation, OffsetDateTime dateVoyage,
                               Long vehiculeId, String vehiculeImmatriculation) {
        this.id = id;
        this.nomPassager = nomPassager;
        this.siegeNumero = siegeNumero;
        this.dateReservation = dateReservation;
        this.dateVoyage = dateVoyage;
        this.vehiculeId = vehiculeId;
        this.vehiculeImmatriculation = vehiculeImmatriculation;
    }

    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getNomPassager(),
                r.getSiegeNumero(),
                r.getDateReservation(),
                r.getDateVoyage(),
                r.getVehicule().getId(),
                r.getVehicule().getImmatriculation()
        );
    }

    public Long getId() { return id; }
    public String getNomPassager() { return nomPassager; }
    public int getSiegeNumero() { return siegeNumero; }
    public OffsetDateTime getDateReservation() { return dateReservation; }
    public OffsetDateTime getDateVoyage() { return dateVoyage; }
    public Long getVehiculeId() { return vehiculeId; }
    public String getVehiculeImmatriculation() { return vehiculeImmatriculation; }
}