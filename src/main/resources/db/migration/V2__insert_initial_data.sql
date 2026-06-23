-- =============================================
-- PARCS - Localisations géographiques réalistes
-- =============================================

-- Hawaii: climat tropical chaud, volcans et forêts luxuriantes
INSERT INTO parc (nom, climat, statut) VALUES ('HAWAII', 'CHAUD', 'OUVERT');

-- Belle-Île-en-Mer: île bretonne au climat océanique tempéré
INSERT INTO parc (nom, climat, statut) VALUES ('BELLEILE', 'TEMPERE', 'OUVERT');

-- Noirmoutier: île vendéenne, projet futur
INSERT INTO parc (nom, climat, statut) VALUES ('NOIRMOUTIER', 'TEMPERE', 'PROJET');

-- =============================================
-- ENCLOS HAWAII (id=1) - Parc tropical
-- =============================================

-- Enclos 1: Grande plaine pour carnivores (T-Rex, Carnotaurus)
INSERT INTO enclos (typologie, surface, parc_id) VALUES ('PLAINE', 15000.00, 1);

-- Enclos 2: Forêt tropicale pour petits carnivores (Velociraptor, Dilophosaurus)
INSERT INTO enclos (typologie, surface, parc_id) VALUES ('FORET', 5000.00, 1);

-- Enclos 3: Lagon pour espèces aquatiques/semi-aquatiques (Spinosaurus)
INSERT INTO enclos (typologie, surface, parc_id) VALUES ('EAU', 5000.00, 1);

-- Enclos 4: Savane pour grands herbivores (Brachiosaurus, Diplodocus)
INSERT INTO enclos (typologie, surface, parc_id) VALUES ('PLAINE', 20000.00, 1);

-- =============================================
-- ENCLOS BELLE-ILE (id=2) - Parc tempéré breton
-- =============================================

-- Enclos 5: Landes et prairies pour herbivores (Triceratops, Stegosaurus)
INSERT INTO enclos (typologie, surface, parc_id) VALUES ('PLAINE', 10000.00, 2);

-- Enclos 6: Forêt de chênes pour herbivores forestiers
INSERT INTO enclos (typologie, surface, parc_id) VALUES ('FORET', 6000.00, 2);

-- Enclos 7: Falaises et côtes rocheuses (pas d'enclos MONTAGNE pour Pterodactyl)
INSERT INTO enclos (typologie, surface, parc_id) VALUES ('EAU', 4000.00, 2);

-- =============================================
-- RESSOURCES HAWAII
-- =============================================

-- Enclos 1 (PLAINE carnivores): viande et eau douce
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('NOURRITURE', 1000.00, 1, 'ANIMAL', NULL);
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('EAU', 3000.00, 1, NULL, 'DOUCE');

-- Enclos 2 (FORET carnivores): viande et eau douce pour Velociraptor
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('NOURRITURE', 500.00, 2, 'ANIMAL', NULL);
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('EAU', 1000.00, 2, NULL, 'DOUCE');

-- Enclos 3 (EAU): poissons et eau salée du lagon
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('NOURRITURE', 500.00, 3, 'POISSON', NULL);
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('EAU', 50000.00, 3, NULL, 'SALEE');

-- Enclos 4 (PLAINE herbivores): végétation luxuriante pour Brachiosaurus
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('NOURRITURE', 2000.00, 4, 'VEGETAL', NULL);
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('EAU', 5000.00, 4, NULL, 'DOUCE');

-- =============================================
-- RESSOURCES BELLE-ILE
-- =============================================

-- Enclos 5 (PLAINE herbivores): prairies bretonnes pour Triceratops
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('NOURRITURE', 1500.00, 5, 'VEGETAL', NULL);
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('EAU', 2000.00, 5, NULL, 'DOUCE');

-- Enclos 6 (FORET): forêt de chênes
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('NOURRITURE', 800.00, 6, 'VEGETAL', NULL);
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('EAU', 1500.00, 6, NULL, 'DOUCE');

-- Enclos 7 (EAU): côtes bretonnes
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('NOURRITURE', 300.00, 7, 'POISSON', NULL);
INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau)
VALUES ('EAU', 30000.00, 7, NULL, 'SALEE');
