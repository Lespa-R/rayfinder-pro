# Publier GlassRadar sur le Google Play Store — guide pas à pas

Ce guide liste tout ce qui reste à faire **de votre côté**. Le code, les visuels, les textes de la fiche et la politique de confidentialité sont déjà prêts dans ce dépôt.

Cochez les cases au fur et à mesure (`[ ]` → `[x]`).

**Durée totale à prévoir : environ 3 semaines**, dont 14 jours de test fermé imposés par Google. Le travail effectif représente 3 à 4 heures.

| Phase | Ce que vous faites | Temps de travail | Délai d'attente |
|---|---|---|---|
| 1 | Tester l'app sur votre téléphone | 20 min | — |
| 2 | Ouvrir le compte développeur Google Play | 30 min | 1 à 7 jours (vérification d'identité) |
| 3 | Mettre la politique de confidentialité en ligne | 10 min | — |
| 4 | Créer la clé de signature et générer le fichier `.aab` | 15 min | — |
| 5 | Créer l'application dans la Play Console | 15 min | — |
| 6 | Remplir la fiche et les déclarations | 1 h | — |
| 7 | Lancer le test fermé avec 12 testeurs | 30 min | **14 jours minimum** |
| 8 | Demander l'accès à la production et publier | 30 min | 1 à 7 jours (examen Google) |

---

## Phase 1 — Tester l'app sur votre téléphone Android

Objectif : vérifier que tout marche sur un vrai téléphone avant d'engager les démarches. Le scan Bluetooth ne fonctionne pas dans l'émulateur.

### 1.1 Activer le mode développeur sur le téléphone

- [ ] Ouvrez **Paramètres › À propos du téléphone**. Sur Samsung : **Paramètres › À propos du téléphone › Informations sur le logiciel**.
- [ ] Touchez **7 fois** la ligne **Numéro de build**. Le téléphone demande votre code, puis affiche « Vous êtes développeur ».
- [ ] Revenez dans **Paramètres › Système › Options pour les développeurs**. Sur Samsung, le menu est en bas de la liste des paramètres.
- [ ] Activez **Débogage USB**.

### 1.2 Installer l'app

- [ ] Branchez le téléphone au Mac en USB.
- [ ] Sur le téléphone, une fenêtre « Autoriser le débogage USB ? » s'affiche. Cochez **Toujours autoriser sur cet ordinateur**, puis touchez **Autoriser**.
- [ ] Dans le Terminal, lancez :

```bash
cd ~/Code/glass-radar && ./gradlew installDebug
```

- [ ] L'icône **GlassRadar** (lunettes blanches sur fond bleu nuit) apparaît dans les applications.

### 1.3 Vérifier le fonctionnement

- [ ] Ouvrez GlassRadar et touchez **Lancer le radar**.
- [ ] Android demande l'autorisation **Appareils à proximité**. Touchez **Autoriser**.
- [ ] Activez l'interrupteur **Afficher tous les appareils Bluetooth**. Une liste d'appareils (écouteurs, montres, télévisions des voisins…) doit apparaître en quelques secondes. Cela prouve que le scan fonctionne.
- [ ] **Si vous avez accès à des lunettes Ray-Ban Meta** (les vôtres, celles d'un proche, ou en magasin d'optique) : allumez-les près du téléphone. L'écran doit passer au rouge et le téléphone vibrer.
  - Si rien ne se passe, laissez l'interrupteur « tout afficher » activé, faites une capture d'écran de la liste et envoyez-la-moi. J'ajusterai la détection.
- [ ] Touchez **Arrêter le radar**, quittez l'app, rouvrez-la : rien ne doit planter.

> **À savoir** : la phase 1 est fortement recommandée, mais elle ne bloque pas les suivantes. Vous pouvez ouvrir le compte développeur (phase 2) en parallèle, puisque la vérification prend plusieurs jours.

---

## Phase 2 — Ouvrir le compte développeur Google Play

### 2.1 Choisir le type de compte

Google propose deux types de compte. Le choix est **définitif**.

| | Compte **personnel** | Compte **organisation** |
|---|---|---|
| Pour qui | Une personne physique | Une société (ex. Datagrow) |
| Nom affiché sur le Store | Votre nom ou un nom de développeur | Le nom de la société |
| Documents demandés | Pièce d'identité | Numéro **D-U-N-S** de la société (gratuit, mais 1 à 4 semaines d'obtention) + pièce d'identité |
| Test fermé obligatoire (12 testeurs / 14 jours) | **Oui** | **Non** |
| Adresse publique | Le pays s'affiche ; l'adresse complète seulement si l'app est payante ou a des achats intégrés | L'adresse de la société s'affiche |

**Recommandation** : si vous voulez publier sous le nom de Datagrow et que la société a déjà un numéro D-U-N-S, prenez le compte **organisation**. Vous éviterez les 14 jours de test fermé. Sinon, le compte **personnel** est le plus rapide à ouvrir.

Pour savoir si la société a déjà un numéro D-U-N-S : https://www.dnb.com/fr-fr/duns-number-lookup (recherche gratuite).

### 2.2 Créer le compte

- [ ] Allez sur https://play.google.com/console/signup
- [ ] Connectez-vous avec le compte Google qui possédera les apps. Conseil : un compte dédié ou votre compte professionnel, pas un compte perso que vous risquez d'abandonner.
- [ ] Choisissez le type de compte (voir 2.1).
- [ ] Renseignez :
  - **Nom du développeur** : le nom affiché publiquement sous l'app (ex. « Datagrow » ou « Robin L. »). Il pourra être modifié plus tard.
  - **Adresse e-mail de contact** publique : par exemple `lespa@datagrow.me`.
  - **Numéro de téléphone** : il sera vérifié par SMS.
  - **Site web** : facultatif.
- [ ] Payez les **frais d'inscription de 25 $** (paiement unique, carte bancaire). Saisissez la carte vous-même : je ne peux pas le faire à votre place.
- [ ] Faites la **vérification d'identité** : photo de votre carte d'identité ou passeport, et parfois un justificatif de domicile.
- [ ] Attendez l'e-mail de confirmation : **1 à 7 jours** en général.

> Pendant l'attente, vous pouvez faire les phases 3 et 4.

---

## Phase 3 — Mettre la politique de confidentialité en ligne

Google exige une URL publique vers la politique de confidentialité, car l'app utilise le Bluetooth. Le fichier est prêt : `docs/privacy.html`.

Choisissez **une** des trois options.

### Option A — Rendre le dépôt GitHub public et activer GitHub Pages (gratuit)

Conséquence : le code source de l'app devient visible de tous. Il ne contient aucun secret : la clé de signature et les mots de passe sont exclus par `.gitignore`.

- [ ] Allez sur https://github.com/Lespa-R/glass-radar/settings
- [ ] Tout en bas, section **Danger Zone** : **Change visibility › Change to public**, puis confirmez.
- [ ] Dans le menu de gauche : **Pages**.
- [ ] **Source** : `Deploy from a branch`. **Branch** : `main`, dossier `/docs`. Cliquez **Save**.
- [ ] Attendez 1 à 2 minutes, puis ouvrez https://lespa-r.github.io/glass-radar/privacy.html pour vérifier que la page s'affiche.
- [ ] Notez cette URL pour la phase 6.

### Option B — L'héberger sur un site que vous possédez déjà

- [ ] Copiez `docs/privacy.html` sur le site (par exemple `https://datagrow.me/glassradar/privacy`), ou recopiez son texte dans une page de votre CMS.
- [ ] Vérifiez que la page est accessible **sans connexion**, depuis une fenêtre de navigation privée.

### Option C — Me demander de le faire

Dites-moi « option A » et je m'occupe du passage en public et de GitHub Pages.

---

## Phase 4 — Créer la clé de signature et générer le fichier à envoyer

### 4.1 Comprendre la clé

Google signe lui-même l'app distribuée (**Play App Signing**, activé par défaut). Vous créez seulement une **clé d'importation**, qui prouve à Google que les mises à jour viennent bien de vous.

- Si vous perdez cette clé, il est possible de la réinitialiser auprès du support Google, mais c'est long.
- **Sauvegardez-la** (voir 4.3).

### 4.2 Générer la clé

- [ ] Dans le Terminal, lancez :

```bash
keytool -genkey -v -keystore ~/glassradar-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

- [ ] Répondez aux questions :
  - **Mot de passe du keystore** : choisissez un mot de passe solide, puis notez-le dans votre gestionnaire de mots de passe.
  - **Prénom et nom** : votre nom.
  - **Unité organisationnelle** / **Organisation** : par exemple « Datagrow », ou laissez vide (Entrée).
  - **Ville**, **Département**, **Code pays** : par exemple `FR`.
  - Confirmez avec `oui`.
- [ ] Le fichier `~/glassradar-upload.jks` est créé dans votre dossier personnel.

### 4.3 Sauvegarder la clé

- [ ] Copiez `~/glassradar-upload.jks` dans un endroit sûr **en dehors du Mac** : gestionnaire de mots de passe (pièce jointe), clé USB, ou drive chiffré.
- [ ] Notez à côté : l'alias `upload` et le mot de passe.
- [ ] **Ne mettez jamais ce fichier sur GitHub.** Le `.gitignore` le bloque déjà.

### 4.4 Indiquer la clé au projet

- [ ] Créez le fichier `~/Code/glass-radar/keystore.properties` avec ce contenu, en remplaçant les mots de passe :

```properties
storeFile=/Users/lespa/glassradar-upload.jks
storePassword=VOTRE_MOT_DE_PASSE
keyAlias=upload
keyPassword=VOTRE_MOT_DE_PASSE
```

`keytool` utilise le même mot de passe pour le keystore et la clé, sauf si vous en avez saisi un différent. Ce fichier est ignoré par git : il reste sur votre Mac.

### 4.5 Générer le fichier `.aab`

- [ ] Lancez :

```bash
cd ~/Code/glass-radar && ./gradlew bundleRelease
```

- [ ] Le fichier à envoyer à Google est créé ici : `~/Code/glass-radar/app/build/outputs/bundle/release/app-release.aab` (environ 2 Mo).
- [ ] Pour l'ouvrir dans le Finder :

```bash
open ~/Code/glass-radar/app/build/outputs/bundle/release/
```

---

## Phase 5 — Créer l'application dans la Play Console

Prérequis : le compte développeur est validé (phase 2).

- [ ] Allez sur https://play.google.com/console
- [ ] Cliquez **Créer une application**.
- [ ] Remplissez :
  - **Nom de l'application** : `GlassRadar – Lunettes à caméra`
  - **Langue par défaut** : `Français – fr-FR`
  - **Application ou jeu** : `Application`
  - **Gratuite ou payante** : `Gratuite`. Attention : une app gratuite ne pourra **jamais** devenir payante.
- [ ] Cochez les deux déclarations : règles du programme pour les développeurs et lois américaines sur l'exportation.
- [ ] Cliquez **Créer l'application**.

Vous arrivez sur le **Tableau de bord** de l'app, avec une liste de tâches « Configurer votre application ». Les phases 6 et 7 suivent cette liste.

---

## Phase 6 — Remplir la fiche et les déclarations

Toutes les réponses sont dans [`play-store/fiche-play-store.md`](play-store/fiche-play-store.md). Ouvrez-le à côté de la Play Console.

### 6.1 Contenu de l'application

Menu de gauche : **Règles et programmes › Contenu de l'application**. Traitez chaque bloc :

- [ ] **Règles de confidentialité** : collez l'URL de la phase 3.
- [ ] **Accès aux applications** : `Toutes les fonctionnalités de mon application sont accessibles sans restriction d'accès`.
- [ ] **Annonces** : `Non, mon application ne contient pas d'annonces`.
- [ ] **Classification du contenu** :
  - Adresse e-mail : `lespa@datagrow.me`
  - Catégorie : `Toutes les autres catégories d'applications` (ou « Utilitaire, productivité, communication ou autre »).
  - Répondez **Non** à toutes les questions : violence, sexualité, langage grossier, substances, jeux d'argent, interaction entre utilisateurs, partage de position, achats numériques.
  - Enregistrez, puis **Envoyer**. La classification obtenue doit être « Tout public » (PEGI 3).
- [ ] **Public cible et contenu** :
  - Tranche d'âge : cochez uniquement **18 ans et plus**. Vous évitez ainsi les règles renforcées pour les enfants.
  - « L'application pourrait-elle attirer les enfants ? » : **Non**.
- [ ] **Applications d'actualités** : **Non**.
- [ ] **Sécurité des données** :
  - « Votre application collecte-t-elle ou partage-t-elle des types de données utilisateur requis ? » : **Non**.
  - Le résumé doit afficher « Aucune donnée collectée » et « Aucune donnée partagée avec des tiers ». Enregistrez, puis **Envoyer**.
- [ ] **Applications gouvernementales** : **Non**.
- [ ] **Fonctionnalités financières** : `Mon application ne fournit aucune fonctionnalité financière`.
- [ ] **Santé** : `Mon application ne propose aucune fonctionnalité de santé`.
- [ ] Si un bloc **Autorisations** ou **Bluetooth** apparaît, collez la justification du fichier fiche.

### 6.2 Fiche principale du Store

Menu de gauche : **Accroître le nombre d'utilisateurs › Présence sur le Store › Fiche principale**.

- [ ] **Nom de l'application** : `GlassRadar – Lunettes à caméra`
- [ ] **Description courte** : copiez celle du fichier fiche.
- [ ] **Description complète** : copiez celle du fichier fiche, en entier, y compris le paragraphe sur les marques.
- [ ] **Icône de l'application** : `play-store/icon-512.png`
- [ ] **Image de présentation** : `play-store/feature-graphic-1024x500.png`
- [ ] **Captures d'écran du téléphone** : glissez `screenshot-1.png`, `screenshot-2.png`, `screenshot-3.png`, `screenshot-4.png`, **dans cet ordre**.
- [ ] Captures pour tablettes et Chromebook : laissez vide (facultatif).
- [ ] Cliquez **Enregistrer**.

Pour retrouver les visuels dans le Finder :

```bash
open ~/Code/glass-radar/play-store/
```

### 6.3 Paramètres du Store

Menu : **Présence sur le Store › Paramètres du Store**.

- [ ] **Catégorie** : `Outils`
- [ ] **Tags** : jusqu'à 5, par exemple sécurité, confidentialité, Bluetooth.
- [ ] **Coordonnées** : e-mail `lespa@datagrow.me`. Téléphone et site web sont facultatifs.
- [ ] **Enregistrer**.

### 6.4 Pays de distribution

Menu : **Tester et publier › Production › Pays/Régions**.

- [ ] Ajoutez au minimum **France**, et éventuellement Belgique, Suisse, Canada et Luxembourg (pays francophones), ou **tous les pays**.
- [ ] La fiche n'existe qu'en français pour l'instant. Si vous ciblez d'autres pays, je peux préparer une version anglaise de la fiche et des visuels.

---

## Phase 7 — Test fermé avec 12 testeurs pendant 14 jours

> **Compte organisation ?** Cette étape n'est pas obligatoire pour vous : passez directement à la phase 8 en publiant en **Production** au lieu du test fermé. Un test interne rapide reste conseillé (voir 7.1).

Règle Google pour les comptes personnels récents : avant de publier, au moins **12 testeurs** doivent avoir **accepté l'invitation** et être restés inscrits **14 jours d'affilée**.

### 7.1 (Facultatif mais conseillé) Test interne immédiat

Ce test permet d'installer la version Play Store sur votre téléphone en quelques minutes, sans examen Google.

- [ ] **Tester et publier › Tests › Tests internes › Créer une release**.
- [ ] Importez `app-release.aab` (phase 4.5).
- [ ] Notes de version : `Première version`.
- [ ] **Suivant › Enregistrer et publier**.
- [ ] Onglet **Testeurs** : créez une liste avec votre adresse Gmail, puis ouvrez le **lien d'inscription** sur votre téléphone.

### 7.2 Recruter 12 testeurs

- [ ] Rassemblez **au moins 15 adresses Gmail**, soit une marge de 3 si certains oublient d'accepter. Pistes : famille, amis, collègues, communauté Le Wagon, contacts LinkedIn.
- [ ] Chaque testeur doit avoir un **téléphone Android** connecté avec cette adresse Gmail.
- [ ] Message type à leur envoyer :

```
Salut ! Je publie ma première app Android, GlassRadar, qui détecte les lunettes connectées à caméra (Ray-Ban Meta) autour de soi.
Google m'impose 14 jours de test avec 12 personnes avant la mise en ligne. Tu peux m'aider ?
1. Envoie-moi l'adresse Gmail utilisée sur ton téléphone Android
2. Je t'enverrai un lien : clique dessus depuis ton téléphone, accepte de devenir testeur, puis installe l'app
3. Garde-la installée 14 jours et ouvre-la de temps en temps
Merci !
```

### 7.3 Créer le test fermé

- [ ] **Tester et publier › Tests › Test fermé**. Utilisez la piste **Alpha** proposée par défaut, ou **Créer un canal**.
- [ ] **Pays/Régions** : France (et les pays de vos testeurs).
- [ ] Onglet **Testeurs** : **Créer une liste d'adresses e-mail**, nommée par exemple « Testeurs GlassRadar ». Collez les adresses séparées par des virgules, puis **Enregistrer**.
- [ ] **Adresse e-mail ou URL de commentaires** : `lespa@datagrow.me`.
- [ ] **Créer une release** : importez `app-release.aab`. Notes de version : `Première version de test`.
- [ ] **Suivant › Enregistrer › Envoyer pour examen** (via **Vue d'ensemble de la publication**).
- [ ] Attendez l'examen de Google : de quelques heures à quelques jours. Vous recevez un e-mail.
- [ ] Une fois l'examen validé, copiez le **lien d'inscription** (onglet Testeurs, « Comment les testeurs rejoignent votre test ») et envoyez-le aux testeurs.

### 7.4 Suivre les 14 jours

- [ ] Vérifiez le compteur sur le **Tableau de bord**. Il indique le nombre de testeurs inscrits et les jours écoulés. Le décompte ne commence qu'à partir de **12 testeurs inscrits**.
- [ ] Relancez ceux qui n'ont pas accepté au bout de 2 à 3 jours.
- [ ] Notez les retours des testeurs (bugs, idées). Google vous les demandera à la phase 8, et je pourrai corriger l'app si besoin (voir « Publier une mise à jour »).

---

## Phase 8 — Demander l'accès à la production et publier

### 8.1 Demander l'accès (comptes personnels uniquement)

Après 14 jours avec au moins 12 testeurs, le Tableau de bord affiche **Demander l'accès en production**.

- [ ] Cliquez dessus et répondez au questionnaire. Exemples de réponses :
  - *Comment avez-vous recruté vos testeurs ?* « Entourage personnel et professionnel, contactés directement. »
  - *Quels retours avez-vous reçus et qu'avez-vous changé ?* Décrivez les vrais retours, même mineurs. Google apprécie les réponses concrètes.
  - *À qui s'adresse l'application ?* « Aux personnes qui veulent savoir si des lunettes connectées équipées d'une caméra sont utilisées près d'elles (lieux publics, réunions, cabinets, etc.). »
  - *Valeur apportée* : « Détection locale, gratuite, sans collecte de données. »
  - *Nombre d'installations attendues la première année* : une estimation honnête.
- [ ] **Envoyer**. Réponse de Google : jusqu'à 7 jours.

### 8.2 Publier en production

- [ ] **Tester et publier › Production › Créer une release**.
- [ ] **Ajouter depuis la bibliothèque** : reprenez le même `.aab` que le test fermé (ou une nouvelle version, voir plus bas).
- [ ] Notes de version : `Première version publique de GlassRadar.`
- [ ] **Suivant › Enregistrer**, puis **Vue d'ensemble de la publication › Envoyer pour examen**.
- [ ] L'examen prend en général 1 à 3 jours, parfois jusqu'à 7. Vous recevez un e-mail quand l'app est en ligne.
- [ ] Vérifiez la page publique : `https://play.google.com/store/apps/details?id=me.datagrow.glassradar`

---

## Publier une mise à jour (plus tard)

À chaque nouvelle version :

1. Demandez-moi la modification. J'**augmente `versionCode`** (1 → 2 → 3…) et `versionName` dans `app/build.gradle.kts`. Google refuse un fichier dont le `versionCode` a déjà été envoyé.
2. Lancez `./gradlew bundleRelease`.
3. Dans la Play Console : **Production › Créer une release**, importez le nouveau `.aab`, ajoutez des notes de version, puis **Envoyer pour examen**.

---

## En cas de problème

| Problème | Que faire |
|---|---|
| `./gradlew installDebug` affiche « no connected devices » | Vérifiez le câble (certains câbles ne font que la charge), réacceptez la fenêtre « Autoriser le débogage USB », débranchez puis rebranchez |
| `bundleRelease` affiche une erreur de mot de passe | Vérifiez `keystore.properties` : chemin exact du `.jks`, mots de passe sans espace en fin de ligne |
| Google refuse l'app pour « usurpation » ou « marque » | Le nom contient déjà le mot « Meta » ailleurs que dans la description ? Envoyez-moi le message de refus et j'adapte les textes |
| Google signale la permission Bluetooth | Collez la justification de `play-store/fiche-play-store.md` dans le formulaire de réponse |
| Le compteur des 14 jours ne démarre pas | Moins de 12 testeurs ont **accepté** le lien. Une simple invitation ne compte pas. Relancez-les |
| « Votre application cible une version d'API trop ancienne » | Demandez-moi de passer à la dernière version d'Android exigée par Google (cible actuelle : API 36) |
| Vous avez perdu la clé `.jks` | Play Console › **Test et publication › Configuration › Intégrité de l'application › Signature d'application** › demander la réinitialisation de la clé d'importation |

---

## Récapitulatif des fichiers utiles

| Fichier | Usage |
|---|---|
| `app/build/outputs/bundle/release/app-release.aab` | Fichier à envoyer à Google (généré à la phase 4.5) |
| `play-store/fiche-play-store.md` | Textes et réponses à copier-coller |
| `play-store/icon-512.png` | Icône Play Store |
| `play-store/feature-graphic-1024x500.png` | Image de présentation |
| `play-store/screenshot-1.png` à `-4.png` | Captures d'écran |
| `docs/privacy.html` | Politique de confidentialité à mettre en ligne |
| `~/glassradar-upload.jks` + `keystore.properties` | Clé de signature (**jamais sur GitHub**, à sauvegarder) |
