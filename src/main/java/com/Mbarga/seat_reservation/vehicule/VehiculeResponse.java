package com.mbarga.seat_reservation.vehicule;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehiculeResponse {

    private Long            id;
    private String          immatriculation;
    private String          modele;
    private int             capacite;
    private TypeDisposition typeDisposition;
    private Long            chauffeurId;
    private String          chauffeurUsername;

    public static VehiculeResponse from(Vehicule v) {
        return new VehiculeResponse(
                v.getId(),
                v.getImmatriculation(),
                v.getModele(),
                v.getCapacite(),
                v.getTypeDisposition(),
                v.getChauffeur().getId(),
                v.getChauffeur().getUsername()
        );
    }
}
