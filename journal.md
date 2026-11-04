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
  - `modele` : entités JPA, énumérations (`Profil`, `StatutIncident`, `EtatEquipement`, `NiveauAttention`) et règles de cycle de vie d'un incident
  - `repository` : interfaces Spring Data JPA
  - `service` : règles de gestion et cas d'usage, dont `PolitiqueUrgence` (seuils 48 h / 72 h)
  - `dto` : formulaires validés par Bean Validation et indicateurs du tableau de bord
  - `exception` : exceptions métier personnalisées
  - `controleur` : partie suivante
- Modèle de données : `ligne`, `station`, `ligne_station`, `equipement`, `incident`, `prestataire`, `evaluation`, `utilisateur` (voir `schema.sql`)

## TESTS
- Stratégie :
  - tests unitaires des règles métier sans base ni Spring : le modèle `Incident`, `PolitiqueUrgence` et les services avec des repositories simulés (Mockito) et une horloge figée (`Clock.fixed`)
  - tests des repositories avec `@DataJpaTest` sur une base H2 en mémoire (mode MariaDB) initialisée par les mêmes scripts que l'application
  - test de validation des formulaires (Bean Validation)
- Commande : `mvnw test`
- Couvert :
  - un incident non affecté ouvert depuis 47 h est vert, à 48 h pile il devient orange, à 71 h 59 il est encore orange, à 72 h il est rouge (RG3)
  - un incident affecté est « pris en charge », quelle que soit sa durée (RG4)
  - un incident déclaré démarre non affecté et sans prestataire (RG1, RG2), il est réaffectable tant qu'il n'est pas résolu (F7)
  - un incident résolu ne peut plus être affecté, modifié, résolu de nouveau ni supprimé (RG5, règle de modification du statut)
  - un incident sans prestataire ne peut pas être résolu ; la résolution ne peut pas précéder l'ouverture (RG5)
  - la déclaration date l'ouverture avec l'horloge et met l'équipement en panne ; la résolution enregistre l'évaluation et remet l'équipement en service sauf si un autre incident est actif
  - un incident inexistant lève `IncidentIntrouvableException` avec un message en français
  - la note est un entier de 1 à 5 (RG8) ; une référence en double, un identifiant en double et un compte sans mot de passe sont refusés
  - les suppressions d'éléments encore utilisés sont refusées
  - le contexte Spring démarre avec les données de démonstration
  - l'historique des incidents résolus est trié du plus récent au plus ancien (RG7)
  - les incidents actifs excluent les incidents résolus (RG6) et se filtrent par station et par statut
  - les incidents d'un équipement sont triés par date d'ouverture (RG7)
  - la référence d'un équipement est unique (RG9)
  - une seule évaluation par incident (RG8)
  - les mots de passe sont stockés hachés
  - la requête native compte les stations d'une ligne
- Non couvert : couche web, sécurité et API (parties 3 et 4)

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

- Exceptions :
  - classe abstraite `ExceptionMetier` (non vérifiée, donc annule la transaction en cours) et une exception personnalisée par cas, avec un message en français qui dit quoi, sur quel objet et pourquoi
  - `RessourceIntrouvableException` regroupe les ressources inexistantes (incident, équipement, station, ligne, prestataire, utilisateur) : traduites en 404
  - `TransitionInterditeException` (incident déjà résolu), `ResolutionImpossibleException`, `ReferenceDejaUtiliseeException`, `IdentifiantDejaUtiliseException`, `SuppressionImpossibleException` : conflits avec l'état courant, traduits en 409
  - `MotDePasseObligatoireException` et les erreurs de Bean Validation : données invalides, traduites en 400
  - les services ne connaissent pas HTTP : la traduction est centralisée dans un `@ControllerAdvice` (pages) et un `@RestControllerAdvice` (API) plutôt que dans des `ResponseStatusException` dispersées, car le projet compte de nombreuses exceptions métier (fiche Gestion des exceptions §10)
  - aucun service ne retourne `null` : les recherches par identifiant utilisent `orElseThrow`
- SOLID :
  - S appliqué : `PolitiqueUrgence` est la seule classe qui connaît les seuils de 48 h et 72 h ; les règles de cycle de vie vivent dans `Incident` (`affecter`, `resoudre`, `modifierInformations`) au lieu d'être dispersées dans les services ; les contrôleurs ne contiendront aucune règle. Changer un seuil ou une transition ne modifie qu'une classe
  - D appliqué : chaque service reçoit par son constructeur ses repositories (interfaces) et l'horloge `Clock`. Conséquence concrète : les tests unitaires remplacent la base par des simulations Mockito et l'heure par `Clock.fixed`, sans démarrer Spring
  - O écarté : il n'existe qu'une seule règle d'urgence, identique pour tous les équipements ; une interface `PolitiqueUrgence` avec une seule implémentation serait du sur-découpage
  - L écarté : le domaine n'utilise pas l'héritage ; la seule hiérarchie est celle des exceptions, où chaque sous-classe reste utilisable partout où `ExceptionMetier` est attendue
  - I écarté : chaque service est petit et cohérent (un sujet par service) ; les différences entre profils sont gérées par la sécurité côté serveur, pas par des interfaces séparées
- Requête SQL native : `LigneRepository.compterStationsDeLaLigne` compte les lignes de `ligne_station` pour une ligne (colonne « Nombre de stations » de l'écran Lignes).
  - Intérêt : requête courte, lisible, exécutée directement par la base sans charger les entités.
  - Limites : elle dépend des noms de tables et de colonnes (un renommage dans les entités ne la met pas à jour et l'erreur n'apparaît qu'à l'exécution), elle échappe à la vérification de JPQL au démarrage et elle lie le code à un dialecte SQL. L'équivalent JPQL `SELECT COUNT(ls) FROM LigneStation ls WHERE ls.ligne.id = :idLigne` aurait été aussi simple et plus sûr : c'est pourquoi c'est la seule requête native du projet.
- @Transactional : uniquement sur les méthodes de service qui écrivent, pour que chaque opération soit atomique (RG10) et que les modifications d'entités chargées soient enregistrées à la fin de la transaction
  - `IncidentService.declarerIncident` : l'incident et l'état « en panne » de l'équipement
  - `IncidentService.resoudreIncident` : date de résolution, évaluation et remise en service de l'équipement, tout ou rien
  - `IncidentService.supprimerIncident` : suppression et remise en service de l'équipement
  - `IncidentService.modifierIncident`, `affecterPrestataire` : lecture puis modification de l'incident dans la même transaction
  - `StationService.ajouterStationALaLigne` : création éventuelle de la station et de sa desserte
  - `StationService.retirerStationDeLaLigne` : retrait de la desserte et suppression éventuelle de la station
  - création, modification et suppression des équipements, lignes, prestataires et utilisateurs : vérification (unicité, utilisation) et écriture dans la même transaction
  - les lectures n'en ont pas besoin : une seule requête suffit
- Règles de gestion propres au groupe :
  - F6 : seuls le titre et la description d'un incident sont modifiables, par le responsable d'exploitation, tant que l'incident n'est pas résolu ; l'équipement n'est pas modifiable pour garder son état cohérent
  - statut : modifiable (affectation, réaffectation, résolution) tant qu'aucune résolution n'a été enregistrée, figé ensuite (`TransitionInterditeException`, 409)
  - résolution : impossible sans prestataire (bouton grisé et refus côté serveur), la note et le commentaire sont saisis au même moment (F8 et F10 en une seule opération)
  - état d'un équipement : il passe « en panne » à la déclaration d'un incident et revient « en service » à la résolution ou à la suppression du dernier incident actif
  - suppressions refusées : station qui a encore des équipements, ligne qui dessert des stations, équipement qui a des incidents, prestataire affecté, son propre compte, incident résolu
  - une station ajoutée à une ligne est réutilisée si elle existe déjà (même nom), au lieu d'être dupliquée
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
