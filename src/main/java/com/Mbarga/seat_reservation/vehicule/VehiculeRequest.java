package com.mbarga.seat_reservation.vehicule;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class VehiculeRequest {

    @NotBlank(message = "L'immatriculation est obligatoire")
    private String immatriculation;

    private String modele;

    @Min(value = 1, message = "La capacité doit être supérieure à 0")
    private int capacite;

    public VehiculeRequest() {}

    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
}