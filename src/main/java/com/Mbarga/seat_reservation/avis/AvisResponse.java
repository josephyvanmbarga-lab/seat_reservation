package com.mbarga.seat_reservation.avis;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class AvisResponse {

    private Long           id;
    private Long           passagerId;
    private String         passagerUsername;
    private Long           chauffeurId;
    private Long           trajetId;
    private int            note;
    private String         commentaire;
    private OffsetDateTime dateAvis;

    public static AvisResponse from(Avis a) {
        return new AvisResponse(
                a.getId(),
                a.getPassager().getId(),
                a.getPassager().getUsername(),
                a.getChauffeur().getId(),
                a.getTrajet().getId(),
                a.getNote(),
                a.getCommentaire(),
                a.getDateAvis()
        );
    }
}
