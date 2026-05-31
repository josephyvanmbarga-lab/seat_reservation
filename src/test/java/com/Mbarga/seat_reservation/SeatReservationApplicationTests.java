package com.Mbarga.seat_reservation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test d'intégration complet — nécessite PostgreSQL et Kafka actifs.
 * Désactivé en CI ; à lancer manuellement avec les services démarrés.
 */
@SpringBootTest
@Disabled("Requiert PostgreSQL + Kafka — lancer manuellement avec les services actifs")
class SeatReservationApplicationTests {

    @Test
    void contextLoads() {
    }
}
