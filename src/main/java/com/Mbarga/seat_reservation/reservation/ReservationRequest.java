package com.mbarga.seat_reservation.reservation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class ReservationRequest {

    @NotBlank(message = "Le nom du passager est obligatoire")
    private String nomPassager;

    @Min(value = 1, message = "Le numéro de siège doit être supérieur à 0")
    private int siegeNumero;

    @NotNull(message = "La date de voyage est obligatoire")
    @Future(message = "La date de voyage doit être dans le futur")
    private OffsetDateTime dateVoyage;

    @NotNull(message = "L'identifiant du véhicule est obligatoire")
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