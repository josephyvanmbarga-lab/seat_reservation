-- V3 : Restructuration complète du schéma

-- 1. Suppression des tables dépendantes (ordre inverse des FK)
DROP TABLE IF EXISTS notification CASCADE;
DROP TABLE IF EXISTS reservation  CASCADE;
DROP TABLE IF EXISTS vehicule     CASCADE;

-- 2. Extension de la table users
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email      VARCHAR(100) UNIQUE,
    ADD COLUMN IF NOT EXISTS telephone  VARCHAR(20),
    ADD COLUMN IF NOT EXISTS note_moyenne DECIMAL(3,2) DEFAULT 0.0,
    ADD COLUMN IF NOT EXISTS nb_avis    INT          DEFAULT 0;

-- 3. Table vehicule (liée au chauffeur)
CREATE TABLE vehicule (
    id              BIGSERIAL    PRIMARY KEY,
    immatriculation VARCHAR(50)  NOT NULL UNIQUE,
    modele          VARCHAR(100),
    capacite        INT          NOT NULL CHECK (capacite > 0),
    type_disposition VARCHAR(30) NOT NULL DEFAULT 'MINIBUS',
    chauffeur_id    BIGINT       NOT NULL,
    CONSTRAINT fk_vehicule_chauffeur FOREIGN KEY (chauffeur_id) REFERENCES users(id)
);

-- 4. Table trajet
CREATE TABLE trajet (
    id                 BIGSERIAL       PRIMARY KEY,
    chauffeur_id       BIGINT          NOT NULL,
    vehicule_id        BIGINT          NOT NULL,
    point_depart       VARCHAR(255)    NOT NULL,
    lat_depart         DECIMAL(10,7)   NOT NULL,
    lng_depart         DECIMAL(10,7)   NOT NULL,
    point_arrivee      VARCHAR(255)    NOT NULL,
    lat_arrivee        DECIMAL(10,7)   NOT NULL,
    lng_arrivee        DECIMAL(10,7)   NOT NULL,
    date_heure_depart  TIMESTAMPTZ     NOT NULL,
    distance_km        DECIMAL(10,3),
    prix_par_siege     DECIMAL(10,2),
    statut             VARCHAR(20)     NOT NULL DEFAULT 'PLANIFIE',
    nb_sieges_reserves INT             NOT NULL DEFAULT 0,
    CONSTRAINT fk_trajet_chauffeur FOREIGN KEY (chauffeur_id) REFERENCES users(id),
    CONSTRAINT fk_trajet_vehicule  FOREIGN KEY (vehicule_id)  REFERENCES vehicule(id)
);

-- 5. Table reservation (restructurée)
CREATE TABLE reservation (
    id                      BIGSERIAL    PRIMARY KEY,
    passager_id             BIGINT       NOT NULL,
    trajet_id               BIGINT       NOT NULL,
    siege_numero            INT          NOT NULL CHECK (siege_numero > 0),
    statut                  VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMEE',
    mode_paiement           VARCHAR(20)  NOT NULL,
    operateur_mobile_money  VARCHAR(20),
    prix_paye               DECIMAL(10,2),
    date_reservation        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_passager FOREIGN KEY (passager_id) REFERENCES users(id),
    CONSTRAINT fk_reservation_trajet   FOREIGN KEY (trajet_id)   REFERENCES trajet(id) ON DELETE CASCADE,
    CONSTRAINT unique_siege_trajet     UNIQUE (trajet_id, siege_numero)
);

-- 6. Table notification (avec user_id)
CREATE TABLE notification (
    id             BIGSERIAL   PRIMARY KEY,
    user_id        BIGINT      NOT NULL,
    reservation_id BIGINT      NOT NULL,
    message        TEXT        NOT NULL,
    date_envoi     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lu             BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notification_user        FOREIGN KEY (user_id)        REFERENCES users(id)       ON DELETE CASCADE,
    CONSTRAINT fk_notification_reservation FOREIGN KEY (reservation_id) REFERENCES reservation(id) ON DELETE CASCADE
);

-- 7. Table avis
CREATE TABLE avis (
    id           BIGSERIAL   PRIMARY KEY,
    passager_id  BIGINT      NOT NULL,
    chauffeur_id BIGINT      NOT NULL,
    trajet_id    BIGINT      NOT NULL,
    note         INT         NOT NULL CHECK (note BETWEEN 1 AND 5),
    commentaire  TEXT,
    date_avis    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avis_passager  FOREIGN KEY (passager_id)  REFERENCES users(id),
    CONSTRAINT fk_avis_chauffeur FOREIGN KEY (chauffeur_id) REFERENCES users(id),
    CONSTRAINT fk_avis_trajet    FOREIGN KEY (trajet_id)    REFERENCES trajet(id),
    CONSTRAINT unique_avis       UNIQUE (passager_id, trajet_id)
);

-- 8. Table position_live (1 ligne par user par trajet, mise à jour en continu)
CREATE TABLE position_live (
    id         BIGSERIAL     PRIMARY KEY,
    user_id    BIGINT        NOT NULL,
    trajet_id  BIGINT        NOT NULL,
    latitude   DECIMAL(10,7) NOT NULL,
    longitude  DECIMAL(10,7) NOT NULL,
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_position_user   FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_position_trajet FOREIGN KEY (trajet_id) REFERENCES trajet(id)  ON DELETE CASCADE,
    CONSTRAINT unique_position    UNIQUE (user_id, trajet_id)
);

-- 9. Table lien_partage
CREATE TABLE lien_partage (
    id             BIGSERIAL   PRIMARY KEY,
    token          VARCHAR(36) NOT NULL UNIQUE,
    reservation_id BIGINT      NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_at      TIMESTAMPTZ NOT NULL,
    actif          BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_lien_reservation FOREIGN KEY (reservation_id) REFERENCES reservation(id) ON DELETE CASCADE
);
