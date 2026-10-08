# Fiche Play Store — RayFinder Pro

À copier-coller dans la Play Console (Présence sur le Store › Fiche principale).

## Détails de l'application

**Nom de l'application** (30 car. max)
```
RayFinder Pro: lunettes caméra
```

**Description courte** (80 car. max)
```
Détectez en Bluetooth les lunettes connectées à caméra autour de vous.
```

**Description complète** (4000 car. max)
```
Les lunettes connectées équipées d'une caméra se fondent dans le décor : impossible de savoir, à l'œil nu, si la personne en face de vous peut filmer ou photographier.

RayFinder Pro écoute les signaux Bluetooth émis autour de vous et vous alerte dès qu'il reconnaît des lunettes connectées Meta (Ray-Ban Meta, Oakley Meta).

FONCTIONNALITÉS
• Alerte immédiate : le téléphone vibre et l'écran passe au rouge quand une paire est repérée
• Distance estimée : très proche (moins de 2 m), proche ou à portée
• Un seul bouton pour lancer ou arrêter le radar
• Mode diagnostic pour afficher tous les appareils Bluetooth et vérifier que la détection fonctionne

COMMUNAUTÉ (avec un compte gratuit, facultatif)
• Partagez une détection d'un geste
• Safe Zone : prévenez vos amis quand des lunettes à caméra sont repérées
• Statistiques du jour, ville par ville
• Le même compte fonctionne sur l'app et sur le site

RESPECT DE LA VIE PRIVÉE
• Le radar fonctionne sans compte : les signaux Bluetooth sont analysés sur votre téléphone
• Rien n'est envoyé tant que vous ne partagez pas une détection
• Aucune position GPS collectée, aucune publicité, aucun traceur
• Suppression du compte et de toutes vos données en un geste, depuis l'app

BON À SAVOIR
La détection repose sur les signaux Bluetooth publics émis par les lunettes. Elle n'est pas infaillible : des lunettes éteintes, en veille profonde ou rangées dans leur étui peuvent ne rien émettre. D'autres appareils Meta (casques de réalité virtuelle, par exemple) peuvent aussi être signalés.

RayFinder Pro est une application indépendante, sans lien avec Meta Platforms, Inc. ni EssilorLuxottica. Ray-Ban, Oakley et Meta sont des marques de leurs propriétaires respectifs.
```

## Éléments graphiques (dossier `play-store/`)

| Champ Play Console | Fichier |
|---|---|
| Icône de l'application (512 × 512) | `icon-512.png` |
| Image de présentation (1024 × 500) | `feature-graphic-1024x500.png` |
| Captures d'écran du téléphone (2 à 8) | `screenshot-1.png` à `screenshot-4.png` (1080 × 1920) |

Pour modifier un visuel : éditer `play-store/src/*.html` puis lancer `play-store/render.sh`.

## Catégorisation et coordonnées

- **Type** : Application · **Catégorie** : Outils
- **Adresse e-mail** : lespa+rayfinder@datagrow.me
- **Tags** : sécurité, vie privée, Bluetooth
- **Site Web** : `https://rayfinder-pro.lovable.app`

## Contenu de l'application (Règles › Contenu de l'application)

Le site publié par Lovable héberge les pages légales. Remplacez `https://rayfinder-pro.lovable.app` par l'adresse réelle si elle diffère.

- **Politique de confidentialité** : `https://rayfinder-pro.lovable.app/confidentialite`
- **Accès aux applications** : `Tout ou partie des fonctionnalités de mon application sont soumises à des restrictions`. Fournissez **un compte de test** (e-mail + mot de passe d'un compte que vous créez vous-même sur le site, avec la confirmation e-mail faite), avec l'instruction : « Onglet Compte › Se connecter. Le radar (onglet Radar) fonctionne sans compte. »
- **Annonces** : non, l'application ne contient pas d'annonces
- **Classification du contenu** (questionnaire IARC) : catégorie « Toutes les autres catégories d'applications ». Violence, sexualité, langage, substances, jeux d'argent : **Non**. « Les utilisateurs peuvent-ils interagir ou échanger des informations ? » : **Oui** (pseudos visibles, alertes Safe Zone prédéfinies entre amis). « L'application partage-t-elle la position de l'utilisateur ? » : **Non** (seule une ville saisie à la main). Achats numériques : **Non**.
- **Public cible** : 18 ans et plus (évite les obligations de la politique Familles)
- **Application d'actualités** : non · **Applications gouvernementales** : non · **Fonctionnalités financières** : aucune · **Santé** : non
- **Autorisation Bluetooth** : si Google la demande, justification : « Scan Bluetooth Low Energy pour détecter les lunettes connectées à proximité, fonction principale de l'app. Aucune localisation déduite (flag neverForLocation). »

## Sécurité des données (formulaire détaillé)

**Vue d'ensemble**
- L'application collecte-t-elle ou partage-t-elle des types de données utilisateur requis ? **Oui**
- Toutes les données collectées sont-elles chiffrées en transit ? **Oui** (HTTPS)
- Proposez-vous un moyen de demander la suppression des données ? **Oui**
- **URL de suppression du compte** : `https://rayfinder-pro.lovable.app/suppression-compte`
- La création de compte est possible dans l'app : la suppression est disponible dans l'app (Compte › Supprimer mon compte) **et** via l'URL ci-dessus, comme Google l'exige.

**Types de données à cocher** — pour chacun : *Collectée* oui, *Partagée* non, *Traitement éphémère* non, *Facultative* oui (le compte est facultatif) :

| Catégorie Play Console | Type | Finalités à cocher |
|---|---|---|
| Informations personnelles | **Adresse e-mail** | Fonctionnalités de l'application, Gestion du compte |
| Informations personnelles | **Nom** (pseudo) | Fonctionnalités de l'application, Gestion du compte |
| Informations personnelles | **ID utilisateur** | Fonctionnalités de l'application, Gestion du compte |
| Localisation | **Position approximative** (ville saisie) | Fonctionnalités de l'application |
| Activité dans les applications | **Autre contenu généré par l'utilisateur** (détections partagées, alertes Safe Zone) | Fonctionnalités de l'application |
| Activité dans les applications | **Autres actions** (liste d'amis) | Fonctionnalités de l'application |

Ne cochez rien d'autre : pas de position précise, pas de contacts, pas d'identifiants publicitaires, pas de données de plantage, pas d'analyse d'audience.
