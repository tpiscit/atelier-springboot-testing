-- Cleanup Flyway V2 data to ensure test isolation
-- Execute in correct order due to foreign key constraints
-- This script runs ONCE per test class (BEFORE_TEST_CLASS) to remove Flyway-inserted data
-- allowing tests to start with a clean slate and create their own test data

-- Delete dinos associated with Flyway parcs
DELETE FROM dino 
WHERE enclos_id IN (
    SELECT id FROM enclos 
    WHERE parc_id IN (
        SELECT id FROM parc 
        WHERE nom IN ('HAWAII', 'BELLEILE', 'NOIRMOUTIER')
    )
);

-- Delete ressources associated with Flyway enclos
DELETE FROM ressource 
WHERE enclos_id IN (
    SELECT id FROM enclos 
    WHERE parc_id IN (
        SELECT id FROM parc 
        WHERE nom IN ('HAWAII', 'BELLEILE', 'NOIRMOUTIER')
    )
);

-- Delete enclos associated with Flyway parcs
DELETE FROM enclos 
WHERE parc_id IN (
    SELECT id FROM parc 
    WHERE nom IN ('HAWAII', 'BELLEILE', 'NOIRMOUTIER')
);

-- Delete Flyway parcs
DELETE FROM parc 
WHERE nom IN ('HAWAII', 'BELLEILE', 'NOIRMOUTIER');
