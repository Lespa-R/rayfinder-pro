# Fiche Play Store — GlassRadar

À copier-coller dans la Play Console (Présence sur le Store › Fiche principale).

## Détails de l'application

**Nom de l'application** (30 car. max)
```
GlassRadar – Lunettes à caméra
```

**Description courte** (80 car. max)
```
Détectez en Bluetooth les lunettes connectées à caméra autour de vous.
```

**Description complète** (4000 car. max)
```
Les lunettes connectées équipées d'une caméra se fondent dans le décor : impossible de savoir, à l'œil nu, si la personne en face de vous peut filmer ou photographier.

GlassRadar écoute les signaux Bluetooth émis autour de vous et vous alerte dès qu'il reconnaît des lunettes connectées Meta (Ray-Ban Meta, Oakley Meta).

FONCTIONNALITÉS
• Alerte immédiate : le téléphone vibre et l'écran passe au rouge quand une paire est repérée
• Distance estimée : très proche (moins de 2 m), proche ou à portée
• Un seul bouton pour lancer ou arrêter le radar
• Mode diagnostic pour afficher tous les appareils Bluetooth et vérifier que la détection fonctionne

RESPECT DE LA VIE PRIVÉE
• 100 % local : aucun compte, aucune publicité, aucune connexion Internet
• Aucune donnée collectée ni partagée
• Les signaux captés sont oubliés après 30 secondes
• Votre position n'est jamais lue

BON À SAVOIR
La détection repose sur les signaux Bluetooth publics émis par les lunettes. Elle n'est pas infaillible : des lunettes éteintes, en veille profonde ou rangées dans leur étui peuvent ne rien émettre. D'autres appareils Meta (casques de réalité virtuelle, par exemple) peuvent aussi être signalés.

GlassRadar est une application indépendante, sans lien avec Meta Platforms, Inc. ni EssilorLuxottica. Ray-Ban, Oakley et Meta sont des marques de leurs propriétaires respectifs.
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
- **Adresse e-mail** : lespa@datagrow.me
- **Tags** : sécurité, vie privée, Bluetooth

## Contenu de l'application (Règles › Contenu de l'application)

- **Politique de confidentialité** : URL publique de `docs/privacy.html` (voir README)
- **Accès aux applications** : toutes les fonctionnalités sont accessibles sans restriction
- **Annonces** : non, l'application ne contient pas d'annonces
- **Classification du contenu** (questionnaire IARC) : catégorie « Utilitaire / Outil », répondre non à toutes les questions → Tout public
- **Public cible** : 18 ans et plus (évite les obligations de la politique Familles)
- **Sécurité des données** : « L'application ne collecte ni ne partage aucun des types de données requis » ; chiffrement en transit : sans objet ; suppression de données : sans objet
- **Application d'actualités** : non · **Applications gouvernementales** : non · **Fonctionnalités financières** : aucune · **Santé** : non
- **Autorisation Bluetooth** : si Google la demande, justification : « Scan Bluetooth Low Energy pour détecter les lunettes connectées à proximité, fonction principale de l'app. Aucune localisation déduite (flag neverForLocation). »
