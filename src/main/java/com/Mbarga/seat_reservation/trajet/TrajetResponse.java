package com.mbarga.seat_reservation.trajet;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class TrajetResponse {

    private Long          id;
    private Long          chauffeurId;
    private String        chauffeurUsername;
    private Double        chauffeurNote;
    private Long          vehiculeId;
    private String        vehiculeImmatriculation;
    private String        vehiculeModele;
    private String        typeDisposition;
    private String        pointDepart;
    private BigDecimal    latDepart;
    private BigDecimal    lngDepart;
    private String        pointArrivee;
    private BigDecimal    latArrivee;
    private BigDecimal    lngArrivee;
    private OffsetDateTime dateHeureDepart;
    private BigDecimal    distanceKm;
    private BigDecimal    prixParSiege;
    private StatutTrajet  statut;
    private int           capacite;
    private int           nbSiegesReserves;
    private int           nbSiegesDisponibles;

    public static TrajetResponse from(Trajet t) {
        return new TrajetResponse(
                t.getId(),
                t.getChauffeur().getId(),
                t.getChauffeur().getUsername(),
                t.getChauffeur().getNoteMoyenne(),
                t.getVehicule().getId(),
                t.getVehicule().getImmatriculation(),
                t.getVehicule().getModele(),
                t.getVehicule().getTypeDisposition().name(),
                t.getPointDepart(),
                t.getLatDepart(),
                t.getLngDepart(),
                t.getPointArrivee(),
                t.getLatArrivee(),
                t.getLngArrivee(),
                t.getDateHeureDepart(),
                t.getDistanceKm(),
                t.getPrixParSiege(),
                t.getStatut(),
                t.getVehicule().getCapacite(),
                t.getNbSiegesReserves(),
                t.getVehicule().getCapacite() - t.getNbSiegesReserves()
        );
    }
}
