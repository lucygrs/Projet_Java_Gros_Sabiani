# Journal — IDFLM (ALTN72, 2026-27)

**Groupe :** Aubin SABIANI, Lucy GROS

## README
- Prérequis : Java 21, Maven (wrapper `mvnw` fourni), MariaDB 10.6 ou plus (testé avec MariaDB 13)
- Configuration : variables d'environnement `DB_URL`, `DB_USER`, `DB_PASSWORD` ; sans elles, l'application se connecte à `jdbc:mariadb://localhost:3306/idflm` avec l'utilisateur `root` sans mot de passe
- Initialiser la base : automatique au démarrage. La base `idflm` est créée si elle n'existe pas, puis `src/main/resources/schema.sql` recrée les tables et `src/main/resources/data.sql` insère les données de démonstration (dates relatives à l'instant du démarrage). Pour conserver les données entre deux démarrages : `DB_INIT_MODE=never`
- Lancer : `mvnw spring-boot:run`, puis ouvrir http://localhost:8080
- Tests : `mvnw test` (base H2 en mémoire, MariaDB n'a pas besoin de tourner)
- API REST (F12) : documentation Swagger sur http://localhost:8080/swagger-ui/index.html, authentification HTTP Basic avec un compte de test, par exemple `curl -u lucy.gros:Agent2026! http://localhost:8080/api/incidents`
- Application déployée : idflm.aubinserv.fr
- Comptes de test :
  - Responsable d'exploitation : `aubin.sabiani` / `Responsable2026!`
  - Agent de signalement : `lucy.gros` / `Agent2026!`
- Données de démonstration : 2 lignes, 6 stations, 11 équipements de 6 types, 4 prestataires, 12 incidents (6 non affectés ouverts depuis 2 h, 47 h, 48 h, 71 h, 72 h et 100 h ; 2 affectés ; 4 résolus dont 3 évalués)
- Limites connues : l'interface charge Tailwind CSS, la police Poppins et Bootstrap Icons depuis des CDN : une connexion Internet est nécessaire pour un affichage correct

## ARCHITECTURE
- Dépôt : https://github.com/lucygrs/Projet_Java_Gros_Sabiani
- Schéma : Navigateur ou client de l'API → Spring Security → couche web (`controleur` pour les pages, `api` pour le REST, DTO) → couche service (`service`) → couche persistance (`repository`, Spring Data JPA) → MariaDB
- Packages (`fr.efrei.idflm`) :
  - `config` : beans techniques (horloge `Clock`, encodeur de mots de passe, sécurité, documentation OpenAPI)
  - `api` : contrôleur REST des incidents et traduction des exceptions en JSON (`GestionnaireErreursApi`)
  - `modele` : entités JPA, énumérations (`Profil`, `StatutIncident`, `EtatEquipement`, `NiveauAttention`) et règles de cycle de vie d'un incident
  - `repository` : interfaces Spring Data JPA
  - `service` : règles de gestion et cas d'usage, dont `PolitiqueUrgence` (seuils 48 h / 72 h)
  - `dto` : formulaires validés par Bean Validation et indicateurs du tableau de bord
  - `exception` : exceptions métier personnalisées
  - `controleur` : contrôleurs Spring MVC (pages Thymeleaf et fragments des pop-ups), pagination et traduction des exceptions en pages d'erreur (`GestionnaireErreursPages`)
- Vues : `src/main/resources/templates` (pages, `fragments/gabarit.html` pour le menu, la pagination et la pop-up commune, `popups/` pour les fragments chargés en fetch), `static/js/popups.js`, `static/images`
- Routes web :
  - `/connexion`, `/deconnexion` : F1
  - `/tableau-de-bord` : F11 (filtres station et statut, recherche, pagination)
  - `/incidents` (en cours), `/incidents/passes` (F9), `/incidents/{id}` (F5), `/incidents/nouveau` (F4), `/incidents/{id}/modification` (F6), `/incidents/{id}/affectation` (F7), `/incidents/{id}/resolution` (F8 et F10), `/incidents/{id}/evaluation`
  - `/equipements` (F2, filtrable par `?station=`), `/equipements/{id}/fiche` (F3)
  - `/lignes`, `/lignes/{id}/stations`, `/utilisateurs`, `/prestataires`, `/parametres`
- Routes de l'API REST (F12, JSON) :
  - `GET /api/incidents` : incidents actifs avec leur niveau d'attention
  - `GET /api/incidents/resolus` : historique, du plus récent au plus ancien (responsable)
  - `GET /api/incidents/{id}` : un incident, 404 s'il n'existe pas
  - `POST /api/incidents` : déclaration, 201 et en-tête `Location`
  - `PUT /api/incidents/{id}/prestataire` : affectation ou réaffectation (responsable), 409 si l'incident est résolu
  - `PUT /api/incidents/{id}/resolution` : résolution et évaluation, 409 si l'incident est déjà résolu ou sans prestataire, 400 si la note est hors de 1 à 5
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
  - un utilisateur non connecté est renvoyé vers la page de connexion (RG11) ; un mauvais mot de passe réaffiche la connexion avec un message (F1) ; de bons identifiants ouvrent la session
  - un agent reçoit 403 sur le tableau de bord, l'historique, l'affectation, la modification, les formulaires de création et la gestion humaine, même en forçant la requête (RG11) ; le responsable y accède
  - un agent accède à la déclaration, au détail, à la résolution, aux équipements et aux lignes
  - un incident inexistant affiche une page 404 avec un message en français, sans trace technique
  - API : la demande d'un incident inexistant renvoie 404 avec un message JSON en français ; une requête sans authentification renvoie 401 ; les incidents actifs n'incluent aucun incident résolu ; l'historique commence par la résolution la plus récente ; un agent reçoit 403 sur l'historique et l'affectation ; l'affectation passe l'incident en « pris en charge » ; la résolution passe l'incident en « résolu » ; résoudre un incident déjà résolu renvoie 409 ; une note de 6 renvoie 400 ; la déclaration renvoie 201 avec l'adresse du nouvel incident
- Tests de la couche web : `@SpringBootTest` + MockMvc sur la base H2, avec une session authentifiée construite dans le test (sans dépendance supplémentaire) ; les tests de l'API s'authentifient en HTTP Basic et sont `@Transactional` pour annuler leurs modifications
- Vérification manuelle dans un navigateur : connexion des deux profils, tableau de bord, pop-up de résolution (étoiles, envoi en fetch, passage dans l'historique), listes avec recherche, paramètres et déconnexion ; API et Swagger interrogés sur MariaDB
- Non couvert : le rendu visuel des écrans (vérifié à la main), les messages d'erreur de validation affichés dans les pop-ups

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

- Interface : Tailwind CSS 3.4.16 chargé par son CDN (Play CDN) dans le fragment commun `gabarit.html`, avec les couleurs IDFLM dans `tailwind.config` et quelques composants (`bouton-principal`, `bouton-secondaire`, `champ-saisie`, `tableau-donnees`…) déclarés une seule fois avec `@apply` pour éviter de répéter les mêmes classes ; aucun fichier CSS. Limite : le Play CDN compile les styles dans le navigateur, il est prévu pour le prototypage et non pour la production (une compilation Tailwind par Maven serait l'étape suivante)
- Listes déroulantes avec recherche : `popups.js` transforme les listes marquées `data-recherche` (équipement à la déclaration, station d'un équipement) en champ de recherche insensible aux accents ; la liste d'origine reste celle qui est envoyée
- Sécurité : Spring Security avec une page de connexion personnalisée, comptes lus dans la table `utilisateur` (`ConnexionService`), mots de passe BCrypt, rôle Spring = profil. Les droits sont déclarés par URL et par verbe HTTP dans `SecuriteConfiguration` : un agent qui force une URL réservée reçoit une page « Accès refusé » (403), masquer les boutons n'est qu'un confort
- Exceptions :
  - classe abstraite `ExceptionMetier` (non vérifiée, donc annule la transaction en cours) et une exception personnalisée par cas, avec un message en français qui dit quoi, sur quel objet et pourquoi
  - `RessourceIntrouvableException` regroupe les ressources inexistantes (incident, équipement, station, ligne, prestataire, utilisateur) : traduites en 404
  - `TransitionInterditeException` (incident déjà résolu), `ResolutionImpossibleException`, `ReferenceDejaUtiliseeException`, `IdentifiantDejaUtiliseException`, `SuppressionImpossibleException` : conflits avec l'état courant, traduits en 409
  - `MotDePasseObligatoireException` et les erreurs de Bean Validation : données invalides, traduites en 400
  - les services ne connaissent pas HTTP : la traduction est centralisée dans un `@ControllerAdvice` (pages) et un `@RestControllerAdvice` (API) plutôt que dans des `ResponseStatusException` dispersées, car le projet compte de nombreuses exceptions métier (fiche Gestion des exceptions §10)
  - aucun service ne retourne `null` : les recherches par identifiant utilisent `orElseThrow`
  - côté API, `GestionnaireErreursApi` renvoie un corps JSON `ErreurApi` (statut, message, détails de validation) ; une erreur inattendue est journalisée avec sa trace et seul un message générique est renvoyé (500)
- ResponseEntity : utilisé seulement là où il apporte quelque chose, dans `GestionnaireErreursApi` pour choisir le code HTTP de chaque erreur et dans `POST /api/incidents` pour renvoyer 201 avec l'en-tête `Location` ; les autres méthodes de l'API renvoient directement l'objet (200)
- API : deux chaînes Spring Security. `/api/**` est authentifié en HTTP Basic, sans session (donc sans risque CSRF), avec les mêmes droits que les pages (historique et affectation réservés au responsable) ; le reste de l'application garde la connexion par formulaire. Les entités ne sont jamais exposées : l'API renvoie un DTO `IncidentReponse`. Documentation : springdoc-openapi 3.1.1 (Swagger UI), limitée aux routes `/api/**`
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
- Besoin : retrouver vite un incident, un équipement ou un compte quand les listes s'allongent
- Périmètre : champ « Rechercher » et pagination de 10 lignes sur toutes les listes ; filtres station et statut sur le tableau de bord
- Choix : requêtes JPQL paginées (`Page`, `Pageable`) dans les repositories, record `Pagination` qui construit les liens précédent et suivant en conservant la recherche et les filtres
- Ce qui fonctionne : recherche, filtres et pagination sur le tableau de bord, les incidents, les équipements, les lignes, les stations, les comptes et les prestataires ; recherche dans les listes déroulantes des équipements et des stations
- Difficultés : conserver la recherche et les filtres dans les liens de pagination, résolu en construisant les liens à partir de la requête courante
- Reste à faire : tri par colonne, filtres combinables sur les autres listes
### Fonctionnalité transverse 2 : pop-ups asynchrones (fetch)
- Besoin : déclarer, modifier, affecter, résoudre ou consulter sans quitter la page
- Choix : `static/js/popups.js` charge un fragment Thymeleaf avec `fetch` et l'affiche dans une pop-up ; les formulaires sont envoyés en `fetch` ; une erreur de validation renvoie la pop-up avec le code 400 et les messages, une erreur métier renvoie un message avec le code 404 ou 409, un succès recharge la liste
- Sécurité : le jeton CSRF accompagne chaque envoi, les verbes PUT et DELETE passent par le champ `_method`
- Ce qui fonctionne : toutes les pop-ups (détail F5, fiche équipement F3, formulaires, résolution avec étoiles, évaluation, paramètres)
- Difficultés : les redirections après un PUT ou un DELETE envoyé en fetch, résolues en envoyant toujours un POST avec le champ `_method` (une redirection 302 après un POST est suivie en GET)
- Reste à faire : messages de validation affichés sous chaque champ plutôt qu'en tête de pop-up

## BILAN
- Contribution de chacun : répartition prévue par le groupe : socle et couche web (Aubin SABIANI), règles métier et API REST (Lucy GROS) ; à compléter par chaque membre
- Plus grande difficulté et comment elle a été surmontée : à compléter par le groupe
- Fonctionnalités non réalisées et pourquoi : déploiement sur un serveur (§7.9), écarté par le groupe pour le moment
- Trois points à retenir du cours et du projet : à compléter par le groupe

# DEPLOYEMENT
https://idflm.aubinserv.fr

## Reponsable d'exploitation
LOGIN : demo.exploitation
MDP : demo.exploitation

## Agent de signalement
LOGIN : demo.signalement
MDP : demo.signalement