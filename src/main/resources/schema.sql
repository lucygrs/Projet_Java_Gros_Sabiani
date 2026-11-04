DROP TABLE IF EXISTS evaluation;
DROP TABLE IF EXISTS incident;
DROP TABLE IF EXISTS equipement;
DROP TABLE IF EXISTS prestataire;
DROP TABLE IF EXISTS station_utilisateur;
DROP TABLE IF EXISTS utilisateur;
DROP TABLE IF EXISTS ligne_station;
DROP TABLE IF EXISTS station;
DROP TABLE IF EXISTS ligne;

CREATE TABLE ligne (
    id_ligne INT AUTO_INCREMENT PRIMARY KEY,
    nom      VARCHAR(100) NOT NULL,
    image    LONGTEXT,
    couleur  VARCHAR(7)
);

CREATE TABLE station (
    id_station INT AUTO_INCREMENT PRIMARY KEY,
    nom        VARCHAR(100) NOT NULL
);

CREATE TABLE ligne_station (
    id_ligne_station INT AUTO_INCREMENT PRIMARY KEY,
    id_ligne         INT NOT NULL,
    id_station       INT NOT NULL,
    CONSTRAINT fk_ligne_station_ligne FOREIGN KEY (id_ligne) REFERENCES ligne (id_ligne),
    CONSTRAINT fk_ligne_station_station FOREIGN KEY (id_station) REFERENCES station (id_station),
    CONSTRAINT uk_ligne_station UNIQUE (id_ligne, id_station)
);

CREATE TABLE utilisateur (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    identifiant    VARCHAR(100) NOT NULL UNIQUE,
    nom            VARCHAR(100) NOT NULL,
    prenom         VARCHAR(100) NOT NULL,
    mot_de_passe   VARCHAR(100) NOT NULL,
    profil         VARCHAR(30)  NOT NULL
);

CREATE TABLE station_utilisateur (
    id_station_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    id_station             INT NOT NULL,
    id_utilisateur         INT NOT NULL,
    CONSTRAINT fk_station_utilisateur_station FOREIGN KEY (id_station) REFERENCES station (id_station),
    CONSTRAINT fk_station_utilisateur_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur (id_utilisateur)
);

CREATE TABLE prestataire (
    id_prestataire INT AUTO_INCREMENT PRIMARY KEY,
    nom            VARCHAR(100) NOT NULL,
    contact        VARCHAR(100) NOT NULL
);

CREATE TABLE equipement (
    id_equipement INT AUTO_INCREMENT PRIMARY KEY,
    reference     VARCHAR(50)  NOT NULL UNIQUE,
    nom           VARCHAR(100) NOT NULL,
    description   VARCHAR(500),
    type          VARCHAR(100) NOT NULL,
    id_station    INT          NOT NULL,
    etat          VARCHAR(20)  NOT NULL,
    CONSTRAINT fk_equipement_station FOREIGN KEY (id_station) REFERENCES station (id_station)
);

CREATE TABLE incident (
    id_incident     INT AUTO_INCREMENT PRIMARY KEY,
    id_equipement   INT          NOT NULL,
    titre           VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    date_ouverture  DATETIME     NOT NULL,
    statut          VARCHAR(20)  NOT NULL,
    id_prestataire  INT,
    date_resolution DATETIME,
    CONSTRAINT fk_incident_equipement FOREIGN KEY (id_equipement) REFERENCES equipement (id_equipement),
    CONSTRAINT fk_incident_prestataire FOREIGN KEY (id_prestataire) REFERENCES prestataire (id_prestataire)
);

CREATE TABLE evaluation (
    id_evaluation   INT AUTO_INCREMENT PRIMARY KEY,
    id_incident     INT      NOT NULL UNIQUE,
    note            INT      NOT NULL,
    commentaire     VARCHAR(500),
    date_evaluation DATETIME NOT NULL,
    CONSTRAINT fk_evaluation_incident FOREIGN KEY (id_incident) REFERENCES incident (id_incident),
    CONSTRAINT ck_evaluation_note CHECK (note BETWEEN 1 AND 5)
);
