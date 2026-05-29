package com.mbarga.seat_reservation.vehicule;

public class VehiculeRequest {

    private String immatriculation;
    private String modele;
    private int capacite;

    public VehiculeRequest() {}

    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
}