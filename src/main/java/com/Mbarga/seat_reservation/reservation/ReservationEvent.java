package com.mbarga.seat_reservation.reservation;

import java.time.OffsetDateTime;

public class ReservationEvent {

    private Long reservationId;
    private String nomPassager;
    private int siegeNumero;
    private Long vehiculeId;
    private String vehiculeImmatriculation;
    private OffsetDateTime dateVoyage;

    public ReservationEvent() {}

    public ReservationEvent(Long reservationId, String nomPassager, int siegeNumero,
                            Long vehiculeId, String vehiculeImmatriculation, OffsetDateTime dateVoyage) {
        this.reservationId = reservationId;
        this.nomPassager = nomPassager;
        this.siegeNumero = siegeNumero;
        this.vehiculeId = vehiculeId;
        this.vehiculeImmatriculation = vehiculeImmatriculation;
        this.dateVoyage = dateVoyage;
    }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public String getNomPassager() { return nomPassager; }
    public void setNomPassager(String nomPassager) { this.nomPassager = nomPassager; }
    public int getSiegeNumero() { return siegeNumero; }
    public void setSiegeNumero(int siegeNumero) { this.siegeNumero = siegeNumero; }
    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long vehiculeId) { this.vehiculeId = vehiculeId; }
    public String getVehiculeImmatriculation() { return vehiculeImmatriculation; }
    public void setVehiculeImmatriculation(String vehiculeImmatriculation) { this.vehiculeImmatriculation = vehiculeImmatriculation; }
    public OffsetDateTime getDateVoyage() { return dateVoyage; }
    public void setDateVoyage(OffsetDateTime dateVoyage) { this.dateVoyage = dateVoyage; }
}