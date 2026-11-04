INSERT INTO ligne (id_ligne, nom, image, couleur) VALUES
(1, 'Ligne 1', 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iMTIiIGZpbGw9IiNGRkNEMDgiLz48dGV4dCB4PSIxMiIgeT0iMTciIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC13ZWlnaHQ9ImJvbGQiIGZvbnQtc2l6ZT0iMTQiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGZpbGw9IiMxRDFEMUIiPjE8L3RleHQ+PC9zdmc+', '#FFCD08'),
(2, 'Ligne A', 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHJlY3Qgd2lkdGg9IjI0IiBoZWlnaHQ9IjI0IiByeD0iNCIgZmlsbD0iI0VDMUUyQiIvPjx0ZXh0IHg9IjEyIiB5PSIxNyIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXdlaWdodD0iYm9sZCIgZm9udC1zaXplPSIxNCIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0iI0ZGRkZGRiI+QTwvdGV4dD48L3N2Zz4=', '#EC1E2B');

INSERT INTO station (id_station, nom) VALUES
(1, 'Charles de Gaulle Étoile'),
(2, 'Châtelet'),
(3, 'Gare de Lyon'),
(4, 'Bastille'),
(5, 'Boissy-Saint-Léger'),
(6, 'Sartrouville');

INSERT INTO ligne_station (id_ligne, id_station) VALUES
(1, 1), (1, 2), (1, 3), (1, 4),
(2, 1), (2, 2), (2, 3), (2, 5), (2, 6);

INSERT INTO utilisateur (id_utilisateur, identifiant, nom, prenom, mot_de_passe, profil) VALUES
(1, 'aubin.sabiani', 'Sabiani', 'Aubin', '$2a$10$U/8KsbDuW4W4r8GdhC.6V.PhQFmhjVr.TUTAoIcvb6Np0GZQJaZr2', 'RESPONSABLE_EXPLOITATION'),
(2, 'lucy.gros', 'Gros', 'Lucy', '$2a$10$Pi6MU3qiW7fYaLXtXiZCmejApnKLpiW5flUloan7buu1QShUTGt76', 'AGENT_SIGNALEMENT');

INSERT INTO prestataire (id_prestataire, nom, contact) VALUES
(1, 'Escalator & Co', '+33 6 80 40 80 90'),
(2, 'Signalis SAS', 'contact@signalis.fr'),
(3, 'Ascenseurs Durand', '+33 1 45 67 89 10'),
(4, 'Portiques Services', 'support@portiques-services.fr');

INSERT INTO equipement (id_equipement, reference, nom, description, type, id_station, etat) VALUES
(1, '89541', 'Feu de signalisation quai 1', 'Feu de signalisation en sortie de quai direction La Défense.', 'Feu de signalisation', 1, 'EN_PANNE'),
(2, '89542', 'Escalier mécanique sortie 2', 'Escalier mécanique montant vers la sortie avenue de Wagram.', 'Escalier mécanique', 1, 'EN_PANNE'),
(3, '89543', 'Ascenseur quai A', 'Ascenseur reliant la salle d''échanges au quai du RER A.', 'Ascenseur', 2, 'EN_PANNE'),
(4, '89544', 'Portique entrée Rivoli', 'Ligne de portiques de validation de l''entrée Rivoli.', 'Portique', 2, 'EN_PANNE'),
(5, '89545', 'Distributeur hall 1', 'Distributeur automatique de titres de transport du hall 1.', 'Distributeur de titres', 3, 'EN_PANNE'),
(6, '89546', 'Écran d''information quai 2', 'Écran d''affichage des prochains passages.', 'Écran d''information', 3, 'HORS_SERVICE'),
(7, '89547', 'Escalier mécanique correspondance', 'Escalier mécanique de la correspondance ligne 5.', 'Escalier mécanique', 4, 'EN_PANNE'),
(8, '89548', 'Portique sortie principale', 'Portiques de la sortie principale.', 'Portique', 5, 'EN_PANNE'),
(9, '89549', 'Ascenseur parvis', 'Ascenseur d''accès au parvis de la gare.', 'Ascenseur', 6, 'EN_SERVICE'),
(10, '89550', 'Distributeur hall nord', 'Distributeur automatique de titres du hall nord.', 'Distributeur de titres', 6, 'EN_SERVICE'),
(11, '89551', 'Feu de signalisation voie 2', 'Feu de signalisation en entrée de gare.', 'Feu de signalisation', 5, 'EN_SERVICE');

INSERT INTO incident (id_incident, id_equipement, titre, description, date_ouverture, statut, id_prestataire, date_resolution) VALUES
(1, 1, 'Panne de signalisation', 'Le feu reste bloqué au rouge, les trains circulent en marche à vue.', LOCALTIMESTAMP - INTERVAL '2' HOUR, 'NON_AFFECTE', NULL, NULL),
(2, 2, 'Escalator cassé', 'Marche arrachée, escalier mécanique arrêté et balisé.', LOCALTIMESTAMP - INTERVAL '47' HOUR, 'NON_AFFECTE', NULL, NULL),
(3, 3, 'Ascenseur bloqué', 'Ascenseur bloqué entre deux niveaux, aucun voyageur à l''intérieur.', LOCALTIMESTAMP - INTERVAL '48' HOUR, 'NON_AFFECTE', NULL, NULL),
(4, 4, 'Portes bloquées', 'Deux portiques refusent tous les titres valides.', LOCALTIMESTAMP - INTERVAL '71' HOUR, 'NON_AFFECTE', NULL, NULL),
(5, 5, 'Distributeur hors ligne', 'Le distributeur n''accepte plus les paiements par carte.', LOCALTIMESTAMP - INTERVAL '72' HOUR, 'NON_AFFECTE', NULL, NULL),
(6, 7, 'Escalier à l''arrêt', 'Arrêt d''urgence déclenché, redémarrage impossible.', LOCALTIMESTAMP - INTERVAL '100' HOUR, 'NON_AFFECTE', NULL, NULL),
(7, 6, 'Écran éteint', 'L''écran n''affiche plus les prochains passages.', LOCALTIMESTAMP - INTERVAL '30' HOUR, 'AFFECTE', 2, NULL),
(8, 8, 'Portique endommagé', 'Vantail du portique tordu après un choc.', LOCALTIMESTAMP - INTERVAL '80' HOUR, 'AFFECTE', 4, NULL),
(9, 9, 'Ascenseur en panne', 'Porte palière qui ne se ferme plus.', LOCALTIMESTAMP - INTERVAL '240' HOUR, 'RESOLU', 3, LOCALTIMESTAMP - INTERVAL '200' HOUR),
(10, 10, 'Distributeur bloqué', 'Billet coincé dans le distributeur.', LOCALTIMESTAMP - INTERVAL '150' HOUR, 'RESOLU', 4, LOCALTIMESTAMP - INTERVAL '120' HOUR),
(11, 11, 'Feu clignotant', 'Le feu clignote de façon intermittente.', LOCALTIMESTAMP - INTERVAL '96' HOUR, 'RESOLU', 2, LOCALTIMESTAMP - INTERVAL '60' HOUR),
(12, 2, 'Rail endommagé', 'Rail de guidage de la main courante déformé.', LOCALTIMESTAMP - INTERVAL '336' HOUR, 'RESOLU', 1, LOCALTIMESTAMP - INTERVAL '300' HOUR);

INSERT INTO evaluation (id_evaluation, id_incident, note, commentaire, date_evaluation) VALUES
(1, 9, 4, 'Intervention rapide et propre.', LOCALTIMESTAMP - INTERVAL '199' HOUR),
(2, 10, 2, 'Délai d''intervention trop long.', LOCALTIMESTAMP - INTERVAL '119' HOUR),
(3, 12, 3, NULL, LOCALTIMESTAMP - INTERVAL '299' HOUR);
