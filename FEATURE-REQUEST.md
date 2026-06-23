# ✨ FEAT-2077 — Ne jamais affecter un dino dans un parc fermé

**Priorité** : Haute · **Composant** : Affectation des dinos

## Description

Suite à l'incident du parc SIBERIE (fermé pour maintenance après une coupure
électrique des clôtures), il est apparu qu'un dino pouvait encore être affecté
à un enclos d'un parc **fermé**.

## Règle métier

> Lors de l'affectation d'un dino, seuls les parcs dont le statut est `OUVERT`
> sont candidats. Un parc `FERME` (ou tout autre statut) ne doit jamais
> accueillir de nouveau dino, même si un de ses enclos convient parfaitement.

## Critères d'acceptation

1. Un dino n'est jamais affecté à un enclos d'un parc non `OUVERT`.
2. Si les seuls enclos compatibles sont dans des parcs fermés, l'affectation
   échoue (`NoSuitableEnclosException`), comme s'il n'y avait aucun candidat.
3. Le comportement pour les parcs `OUVERT` est inchangé.

## Notes pour l'atelier

L'implémentation est minuscule (~5 lignes de prod). Observez la taille et la
lisibilité de la diff **côté tests** selon que le test du service utilise des
mocks Mockito ou des fakes in-memory.
