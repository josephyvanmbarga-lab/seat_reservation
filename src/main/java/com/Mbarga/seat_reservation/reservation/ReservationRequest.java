package com.mbarga.seat_reservation.reservation;

import java.time.OffsetDateTime;

public class ReservationRequest {

    private String nomPassager;
    private int siegeNumero;
    private OffsetDateTime dateVoyage;
    private Long vehiculeId;

    public ReservationRequest() {}

    public String getNomPassager() { return nomPassager; }
    public void setNomPassager(String nomPassager) { this.nomPassager = nomPassager; }
    public int getSiegeNumero() { return siegeNumero; }
    public void setSiegeNumero(int siegeNumero) { this.siegeNumero = siegeNumero; }
    public OffsetDateTime getDateVoyage() { return dateVoyage; }
    public void setDateVoyage(OffsetDateTime dateVoyage) { this.dateVoyage = dateVoyage; }
    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long vehiculeId) { this.vehiculeId = vehiculeId; }
}