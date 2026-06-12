# 🐛 BUG-1042 — Enclos sur-rempli lors de l'affectation d'un dino

**Priorité** : Critique · **Environnement** : Production · **Composant** : Affectation des dinos

## Description

L'équipe d'exploitation du parc HAWAII remonte un incident : un **T-Rex** a été
affecté à un enclos **forêt de 220 m²** qui hébergeait déjà un **Velociraptor**.

Or les besoins des espèces sont :

| Espèce       | Surface requise |
|--------------|-----------------|
| TREX         | 200 m²          |
| Velociraptor | 50 m²           |

L'enclos de 220 m² ne pouvait donc accueillir le T-Rex : `220 - 50 = 170 m²`
disponibles, alors que 200 m² sont requis. L'affectation aurait dû être refusée
(`NoSuitableEnclosException`) et le T-Rex orienté vers un autre enclos.

Les soigneurs signalent le même phénomène sur l'eau et la nourriture : les
quantités consommées par les **dinos déjà présents** dans l'enclos semblent
**ignorées** lors du calcul des ressources disponibles.

## Étapes de reproduction (constatées en prod)

1. Affecter un Velociraptor → il est placé dans l'enclos forêt de 220 m² du parc HAWAII. ✅
2. Affecter un T-Rex (climat CHAUD/TEMPERE, habitat PLAINE/FORET, 200 m² requis).
3. **Constaté** : le T-Rex est affecté au même enclos. ❌
4. **Attendu** : l'enclos est refusé (surface restante insuffisante), le T-Rex va ailleurs ou l'affectation échoue.

## Notes

- Le bug ne se produit **que** lorsque l'enclos contient déjà au moins un dino.
- L'affectation initiale du Velociraptor (enclos vide) fonctionne normalement.
- Aucune erreur dans les logs : l'affectation se déroule "avec succès".

## Pour l'atelier

Premier réflexe : écrire un cas de test dans `DinoAssignmentServiceImplTest`
reproduisant ce scénario… Arrivez-vous à le faire échouer ?
