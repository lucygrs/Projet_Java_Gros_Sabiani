# Journal — IDFLM (ALTN72, 2026-27)

**Groupe :** Aubin SABIANI, Lucy GROS

## README
- Prérequis : Java 21, Maven (wrapper `mvnw` fourni), MariaDB 10.6 ou plus (testé avec MariaDB 13)
- Configuration : variables d'environnement `DB_URL`, `DB_USER`, `DB_PASSWORD` ; sans elles, l'application se connecte à `jdbc:mariadb://localhost:3306/idflm` avec l'utilisateur `root` sans mot de passe
- Initialiser la base : automatique au démarrage. La base `idflm` est créée si elle n'existe pas, puis `src/main/resources/schema.sql` recrée les tables et `src/main/resources/data.sql` insère les données de démonstration (dates relatives à l'instant du démarrage). Pour conserver les données entre deux démarrages : `DB_INIT_MODE=never`
- Lancer : `mvnw spring-boot:run`
- Application déployée : pas de déploiement pour le moment
- Comptes de test :
  - Responsable d'exploitation : `aubin.sabiani` / `Responsable2026!`
  - Agent de signalement : `lucy.gros` / `Agent2026!`
- Données de démonstration : 2 lignes, 6 stations, 11 équipements de 6 types, 4 prestataires, 12 incidents (6 non affectés ouverts depuis 2 h, 47 h, 48 h, 71 h, 72 h et 100 h ; 2 affectés ; 4 résolus dont 3 évalués)
- Limites connues : …

## ARCHITECTURE
- Dépôt : https://github.com/lucygrs/Projet_Java_Gros_Sabiani
- Schéma : Navigateur → Spring Security → couche web (`controleur`, DTO) → couche service (`service`) → couche persistance (`repository`, Spring Data JPA) → MariaDB
- Packages (`fr.efrei.idflm`) :
  - `config` : beans techniques (horloge `Clock`, encodeur de mots de passe)
  - `modele` : entités JPA et énumérations (`Profil`, `StatutIncident`, `EtatEquipement`)
  - `repository` : interfaces Spring Data JPA
  - `service`, `controleur`, `dto`, `exception` : parties suivantes
- Modèle de données : `ligne`, `station`, `ligne_station`, `equipement`, `incident`, `prestataire`, `evaluation`, `utilisateur`, `station_utilisateur` (voir `schema.sql`)

## TESTS
- Stratégie : tests des repositories avec `@DataJpaTest` sur une base H2 en mémoire (mode MariaDB) initialisée par les mêmes scripts que l'application
- Commande : `mvnw test`
- Couvert :
  - le contexte Spring démarre avec les données de démonstration
  - l'historique des incidents résolus est trié du plus récent au plus ancien (RG7)
  - les incidents actifs excluent les incidents résolus (RG6) et se filtrent par station et par statut
  - les incidents d'un équipement sont triés par date d'ouverture (RG7)
  - la référence d'un équipement est unique (RG9)
  - une seule évaluation par incident (RG8)
  - les mots de passe sont stockés hachés
  - la requête native compte les stations d'une ligne
- Non couvert : règles métier et couche web (parties suivantes)

## CHOIX TECHNIQUES
- Droits par profil (source : tableau du plan relationnel) :

| N° | Fonctionnalité | Profil 1 : Responsable d'exploitation | Profil 2 : Agent de signalement |
|---|---|---|---|
| F2–F3 | Consulter équipements et fiches | Accès complet | Lecture seule |
| F4 | Déclarer un incident | Accès complet | Accès complet |
| F5–F6 | Consulter / modifier un incident | Accès complet | Lecture seule (F5) |
| F7 | Affecter ou réaffecter un prestataire | Accès complet | Interdit |
| F8 | Déclarer un incident résolu | Accès complet | Accès complet |
| F9 | Consulter l'historique | Accès complet | Interdit |
| F10 | Évaluer un prestataire | Accès complet | Accès complet |
| F11 | Tableau de bord | Accès complet | Interdit |

- Exceptions : …
- SOLID : …
- Requête SQL native : `LigneRepository.compterStationsDeLaLigne` compte les lignes de `ligne_station` pour une ligne (colonne « Nombre de stations » de l'écran Lignes).
  - Intérêt : requête courte, lisible, exécutée directement par la base sans charger les entités.
  - Limites : elle dépend des noms de tables et de colonnes (un renommage dans les entités ne la met pas à jour et l'erreur n'apparaît qu'à l'exécution), elle échappe à la vérification de JPQL au démarrage et elle lie le code à un dialecte SQL. L'équivalent JPQL `SELECT COUNT(ls) FROM LigneStation ls WHERE ls.ligne.id = :idLigne` aurait été aussi simple et plus sûr : c'est pourquoi c'est la seule requête native du projet.
- @Transactional : …
- Horloge : un bean `Clock` est injecté à la place de `LocalDateTime.now()` pour pouvoir figer l'heure dans les tests des seuils d'urgence (§7.8)
- Mots de passe : hachés avec BCrypt (`PasswordEncoder` de Spring Security), y compris dans `data.sql`
- Secrets : aucun identifiant de base n'est écrit en dur, ils sont lus dans les variables d'environnement

## LOT ALPHA
### Fonctionnalité transverse 1 : recherche et pagination
- Besoin · périmètre envisagé · choix et essais · ce qui fonctionne · difficultés · reste à faire
- Partie 1 : requêtes de recherche paginées (`Page`/`Pageable`) dans les repositories des incidents, équipements, lignes, stations, prestataires et utilisateurs
### Fonctionnalité transverse 2 : pop-ups asynchrones (fetch)
- …

## BILAN
- Contribution de chacun : …
- Plus grande difficulté et comment elle a été surmontée : …
- Fonctionnalités non réalisées et pourquoi : …
- Trois points à retenir du cours et du projet : …
