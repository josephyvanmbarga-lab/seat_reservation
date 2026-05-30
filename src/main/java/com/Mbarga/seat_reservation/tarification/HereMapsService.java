package com.mbarga.seat_reservation.tarification;

import com.mbarga.seat_reservation.trajet.EstimationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class HereMapsService {

    private static final String HERE_ROUTING_URL = "https://router.hereapi.com/v8/routes";

    @Value("${app.here.api-key}")
    private String apiKey;

    @Value("${app.tarif.par-km}")
    private BigDecimal tarifParKm;

    private final RestClient restClient = RestClient.create();

    public EstimationResponse estimer(BigDecimal latDepart, BigDecimal lngDepart,
                                      BigDecimal latArrivee, BigDecimal lngArrivee) {
        String origin      = latDepart  + "," + lngDepart;
        String destination = latArrivee + "," + lngArrivee;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri(HERE_ROUTING_URL + "?transportMode=car&origin={o}&destination={d}&return=summary&apikey={k}",
                            origin, destination, apiKey)
                    .retrieve()
                    .body(Map.class);

            long   distanceM = extractDistance(response);
            long   dureeSec  = extractDuration(response);
            BigDecimal distKm = BigDecimal.valueOf(distanceM)
                    .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            BigDecimal prix = distKm.multiply(tarifParKm).setScale(2, RoundingMode.HALF_UP);

            return new EstimationResponse(distKm, dureeSec, prix);

        } catch (Exception e) {
            // Fallback Haversine si HERE est indisponible
            double distKmD = haversine(latDepart.doubleValue(), lngDepart.doubleValue(),
                    latArrivee.doubleValue(), lngArrivee.doubleValue());
            BigDecimal distKm = BigDecimal.valueOf(distKmD).setScale(3, RoundingMode.HALF_UP);
            BigDecimal prix   = distKm.multiply(tarifParKm).setScale(2, RoundingMode.HALF_UP);
            long dureeSec = (long)(distKmD / 40 * 3600); // estimation ~40 km/h en ville
            return new EstimationResponse(distKm, dureeSec, prix);
        }
    }

    @SuppressWarnings("unchecked")
    private long extractDistance(Map<String, Object> response) {
        List<Map<String, Object>> routes   = (List<Map<String, Object>>) response.get("routes");
        List<Map<String, Object>> sections = (List<Map<String, Object>>) routes.get(0).get("sections");
        Map<String, Object>       summary  = (Map<String, Object>) sections.get(0).get("summary");
        return ((Number) summary.get("length")).longValue();
    }

    @SuppressWarnings("unchecked")
    private long extractDuration(Map<String, Object> response) {
        List<Map<String, Object>> routes   = (List<Map<String, Object>>) response.get("routes");
        List<Map<String, Object>> sections = (List<Map<String, Object>>) routes.get(0).get("sections");
        Map<String, Object>       summary  = (Map<String, Object>) sections.get(0).get("summary");
        return ((Number) summary.get("duration")).longValue();
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
