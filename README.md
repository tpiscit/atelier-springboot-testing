# Atelier Spring Boot Testing

Application Spring Boot de gestion d'un parc de dinosaures, utilisee comme support pour un atelier sur le testing.

## Prerequis

- **Java 25** (JDK) — [Adoptium](https://adoptium.net/) ou via SDKMAN
- **Docker** et **Docker Compose** — daemon Docker lance
- **Git**
- **Un IDE Java** — IntelliJ IDEA (recommande) ou VS Code avec les extensions Java

### Verification

```bash
java -version          # doit afficher 25+
docker --version
docker compose version
git --version
```

## Avant de commencer : preparer l'environnement (warm-up)

Des le clone (branche `main`), pendant l'introduction de l'atelier, lancez :

```bash
./mvnw test
```

Cette commande met a profit le temps de l'intro pour :

- telecharger toutes les dependances Maven (et les plugins) ;
- pre-telecharger (pull) l'image Docker `postgres:16` via un test de warm-up
  ([`DockerImageWarmupTest`](src/test/java/fr/liksi/parcmanager/DockerImageWarmupTest.java)).

L'image `postgres:16` ainsi recuperee est **reutilisee par Testcontainers** aux
etapes qui passent sur PostgreSQL : plus d'attente de telechargement a ce
moment-la.

> Le test de warm-up n'existe que sur `main`. Il disparait automatiquement des
> que vous basculez sur une etape (`git switch atelier/...`).

## Stack technique

| Composant     | Version |
|---------------|---------|
| Spring Boot   | 4.0.2   |
| PostgreSQL    | 16      |
| MongoDB       | 7.0     |
| Testcontainers| 1.21.4  |
| MapStruct     | 1.5.5   |
| Flyway        | migrations SQL |
| Mongock        | migrations MongoDB |

## Demarrage rapide

```bash
./mvnw spring-boot:run
```

L'application demarre automatiquement les conteneurs PostgreSQL et MongoDB via Docker Compose (`docker-compose.local.yml`).

## Lancer les tests

```bash
./mvnw test
```

## Depannage

### Erreur Testcontainers liee a Ryuk (firewall / antivirus d'entreprise)

Si les tests echouent au demarrage des containers avec une erreur mentionnant Ryuk, desactiver le watchdog :

```bash
# Linux / macOS / Git Bash
export TESTCONTAINERS_RYUK_DISABLED=true
./mvnw test
```

```powershell
# Windows PowerShell
$env:TESTCONTAINERS_RYUK_DISABLED = "true"
.\mvnw.cmd test
```

```cmd
REM Windows CMD
set TESTCONTAINERS_RYUK_DISABLED=true
mvnw.cmd test
```

