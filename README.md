# Stock Manager — Backend

API de gestion de stock et de ventes pour une boutique : produits, ventes, paiements, mouvements de stock, commandes fournisseurs, lots et dates de péremption.

**Stack** : Java 17, Spring Boot 4.1, Spring Security, Spring Data JPA, Liquibase, SQLite (local) / PostgreSQL (production), H2 pour les tests.
Frontend : [stock-manager-frontend](https://github.com/Samassekou196/stock-manager-frontend).

## Fonctionnalités
- Ventes avec mode de paiement (espèces, carte, mobile money), montant reçu, monnaie rendue et caissier enregistré.
- Historique des mouvements de stock (ventes, achats, ajustements) ; ajustements réservés à l'administrateur, avec motif obligatoire.
- Commandes fournisseurs avec réception partielle ou complète.
- Lots et dates de péremption, vente selon le principe FEFO (premier expiré, premier sorti) pour les produits à péremption.
- Prix d'achat, coût par lot, code-barres et SKU uniques.
- Authentification par session, protection CSRF, rôles `ADMIN` et `CASHIER`.

## Lancer en local
Prérequis : Java 17. Maven est fourni (`mvnw`).

```bash
export SPRING_PROFILES_ACTIVE=local
export SPRING_DATASOURCE_URL="jdbc:sqlite:stock_manager.db"   # créée au premier lancement
export APP_BOOTSTRAP_ADMIN_USERNAME=admin
export APP_BOOTSTRAP_ADMIN_PASSWORD="un-mot-de-passe-de-12-caracteres-min"
./mvnw spring-boot:run
```
L'API écoute sur http://localhost:8080. Le compte administrateur n'est créé que s'il n'existe pas encore.
Sous PowerShell : `$env:NOM = "valeur"` à la place de `export`, et `.\mvnw.cmd`.

## Variables d'environnement
| Variable | Rôle |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` (SQLite) ou `prod` |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Base de données (obligatoires en `prod`) |
| `APP_BOOTSTRAP_ADMIN_USERNAME` / `_PASSWORD` | Création du premier administrateur (mot de passe de 12 à 72 caractères) |
| `APP_CORS_ALLOWED_ORIGINS` | Origines autorisées, séparées par des virgules (défaut `http://localhost:3000`) |
| `SESSION_COOKIE_SECURE` / `SESSION_COOKIE_SAME_SITE` | Réglage du cookie de session |

## Tests
```bash
./mvnw test
```
Les tests s'exécutent sur H2 en mémoire.

## Limites connues
Le schéma contient des tables prévues pour des fonctions pas encore développées (retours de vente, sessions de caisse, dépenses, inventaires, journal d'audit, points de fidélité).

## Licence
MIT — voir [LICENSE](LICENSE).
