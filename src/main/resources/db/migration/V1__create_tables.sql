
CREATE TABLE vehicule (
                          id BIGSERIAL PRIMARY KEY,
                          immatriculation VARCHAR(50) NOT NULL UNIQUE,
                          modele VARCHAR(100),
                          capacite INT NOT NULL CHECK (capacite > 0)
);

CREATE TABLE reservation (
                             id BIGSERIAL PRIMARY KEY,
                             nom_passager VARCHAR(100) NOT NULL,
                             siege_numero INT NOT NULL CHECK (siege_numero > 0),
                             date_reservation TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             date_voyage TIMESTAMPTZ NOT NULL,
                             vehicule_id BIGINT NOT NULL,

                             CONSTRAINT fk_vehicule
                                 FOREIGN KEY (vehicule_id)
                                     REFERENCES vehicule(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT unique_siege
                                 UNIQUE (vehicule_id, date_voyage, siege_numero)
);

CREATE TABLE notification (
                              id BIGSERIAL PRIMARY KEY,
                              message TEXT NOT NULL,
                              date_envoi TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              lu BOOLEAN DEFAULT FALSE,
                              reservation_id BIGINT NOT NULL,

                              CONSTRAINT fk_reservation
                                  FOREIGN KEY (reservation_id)
                                      REFERENCES reservation(id)
                                      ON DELETE CASCADE
);
