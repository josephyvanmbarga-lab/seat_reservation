package com.mbarga.seat_reservation.vehicule;

public class VehiculeResponse {

    private Long id;
    private String immatriculation;
    private String modele;
    private int capacite;

    public VehiculeResponse() {}

    public VehiculeResponse(Long id, String immatriculation, String modele, int capacite) {
        this.id = id;
        this.immatriculation = immatriculation;
        this.modele = modele;
        this.capacite = capacite;
    }

    public static VehiculeResponse from(Vehicule v) {
        return new VehiculeResponse(v.getId(), v.getImmatriculation(), v.getModele(), v.getCapacite());
    }

    public Long getId() { return id; }
    public String getImmatriculation() { return immatriculation; }
    public String getModele() { return modele; }
    public int getCapacite() { return capacite; }
}