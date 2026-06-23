-- Create dino table to track assigned dinos
CREATE TABLE dino (
    id UUID PRIMARY KEY,
    species VARCHAR(100) NOT NULL,
    enclos_id BIGINT NOT NULL,
    CONSTRAINT fk_dino_enclos FOREIGN KEY (enclos_id) REFERENCES enclos(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_dino_enclos_id ON dino(enclos_id);
CREATE INDEX idx_dino_species ON dino(species);
