-- Create parc table
CREATE TABLE parc (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(50) NOT NULL UNIQUE,
    climat VARCHAR(20) NOT NULL,
    statut VARCHAR(20) NOT NULL
);

-- Create enclos table
CREATE TABLE enclos (
    id BIGSERIAL PRIMARY KEY,
    typologie VARCHAR(20) NOT NULL,
    surface NUMERIC(10,2) NOT NULL,
    parc_id BIGINT NOT NULL,
    CONSTRAINT fk_enclos_parc FOREIGN KEY (parc_id) REFERENCES parc(id) ON DELETE CASCADE
);

-- Create ressource table (SINGLE_TABLE inheritance)
CREATE TABLE ressource (
    id BIGSERIAL PRIMARY KEY,
    type_ressource VARCHAR(20) NOT NULL,
    quantite NUMERIC(10,2) NOT NULL,
    enclos_id BIGINT NOT NULL,
    type_nourriture VARCHAR(20),
    type_eau VARCHAR(20),
    CONSTRAINT fk_ressource_enclos FOREIGN KEY (enclos_id) REFERENCES enclos(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_enclos_parc_id ON enclos(parc_id);
CREATE INDEX idx_ressource_enclos_id ON ressource(enclos_id);
CREATE INDEX idx_parc_nom ON parc(nom);
