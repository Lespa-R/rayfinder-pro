# Publier RayFinder Pro sur le Google Play Store — guide pas à pas

Ce guide liste tout ce qui reste à faire **de votre côté**. Le code, les visuels, les textes de la fiche, les mentions légales et la politique de confidentialité sont déjà prêts dans ce dépôt.

Le dépôt contient deux choses : le **site web** (projet Lovable, à la racine) et l'**app Android** (dossier `android/`). Les deux partagent le même backend Lovable Cloud : mêmes comptes, mêmes données.

Cochez les cases au fur et à mesure (`[ ]` → `[x]`).

**Durée totale à prévoir : environ 3 semaines** avec un compte personnel (dont 14 jours de test fermé imposés par Google), quelques jours avec un compte organisation. Le travail effectif représente 3 heures environ.

| Phase | Ce que vous faites | Temps de travail | Délai d'attente |
|---|---|---|---|
| 1 | Tester l'app sur votre téléphone | 20 min | — |
| 2 | Vérifier votre compte Google Play (déjà ouvert) | 5 min | — |
| 3 | Publier le site Lovable sur `rayfinder.datagrow.me` | 15 min | jusqu'à quelques heures (DNS) |
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
cd ~/Code/Lespa-R/rayfinder-pro/android && ./gradlew installDebug
```

- [ ] L'icône **RayFinder Pro** (lunettes blanches sur fond bleu nuit) apparaît dans les applications.

### 1.3 Vérifier le fonctionnement

- [ ] Ouvrez RayFinder Pro et touchez **Lancer le radar**.
- [ ] Android demande l'autorisation **Appareils à proximité**. Touchez **Autoriser**.
- [ ] Activez l'interrupteur **Afficher tous les appareils Bluetooth**. Une liste d'appareils (écouteurs, montres, télévisions des voisins…) doit apparaître en quelques secondes. Cela prouve que le scan fonctionne.
- [ ] **Si vous avez accès à des lunettes Ray-Ban Meta** (les vôtres, celles d'un proche, ou en magasin d'optique) : allumez-les près du téléphone. L'écran doit passer au rouge et le téléphone vibrer.
  - Si rien ne se passe, laissez l'interrupteur « tout afficher » activé, faites une capture d'écran de la liste et envoyez-la-moi. J'ajusterai la détection.
- [ ] Touchez **Arrêter le radar**, quittez l'app, rouvrez-la : rien ne doit planter.
- [ ] Onglet **Compte** › **Pas encore de compte ? Inscrivez-vous** : créez votre compte, cliquez le lien de confirmation reçu par e-mail, puis connectez-vous.
- [ ] Onglet **Compte** : renseignez votre ville et touchez **Enregistrer la ville**.
- [ ] Onglet **Safe Zone** › **Alerter mes amis** : l'alerte apparaît dans « Alertes récentes ».
- [ ] Onglet **Stats** : la page s'affiche (0 détection au début, c'est normal).
- [ ] Gardez ce compte : il servira de **compte de test pour Google** (phase 6.1).

> **À savoir** : la phase 1 est fortement recommandée, mais elle ne bloque pas les suivantes.

---

## Phase 2 — Vérifier votre compte Google Play ✅ déjà ouvert

Votre compte développeur existe déjà : il reste seulement deux vérifications, qui changent la suite du guide.

- [ ] Ouvrez https://play.google.com/console › **Paramètres** (roue dentée, en bas à gauche) › **Compte de développeur** › **Informations sur le compte**.
- [ ] Repérez le **type de compte** :
  - **Personnel** : le test fermé de la phase 7 (12 testeurs pendant 14 jours) est **obligatoire** si le compte a été créé après le 13 novembre 2023.
  - **Organisation** : vous pouvez sauter la phase 7 et publier directement en production.
- [ ] Vérifiez que la **vérification d'identité** est terminée (pas de bandeau orange en haut de la console).
- [ ] Vérifiez le **nom du développeur** affiché publiquement. Pour rester cohérent avec les mentions légales, « DataGrow » est idéal. Il se modifie dans **Paramètres › Compte de développeur › Informations sur le compte**.
- [ ] Vérifiez qu'il n'existe pas déjà une app « RayFinder Pro » dans **Toutes les applications**, créée lors de votre essai précédent. Si c'est le cas, vous pouvez la réutiliser à la phase 5, à condition qu'aucun fichier n'y ait encore été importé : l'identifiant d'une app est figé dès le premier import, et celui de la nouvelle version est `me.datagrow.rayfinderpro`.

---

## Phase 3 — Publier le site Lovable

Google exige des URL publiques pour la **politique de confidentialité** et pour la **suppression de compte**. Ces pages font partie du site Lovable (même projet que le backend) :

| Page | Adresse une fois publiée |
|---|---|
| Accueil (radar web, Safe Zone, stats) | https://rayfinder.datagrow.me/ |
| Mentions légales DataGrow | https://rayfinder.datagrow.me/mentions-legales |
| Politique de confidentialité | https://rayfinder.datagrow.me/confidentialite |
| Suppression de compte | https://rayfinder.datagrow.me/suppression-compte |

L'app Android contient déjà des liens vers ces adresses : en bas du radar et dans l'onglet Compte.

### 3.1 Publier le site

- [ ] Ouvrez le projet : https://lovable.dev/projects/b43a81f9-b0e5-4e15-8abc-95cd062cb987
- [ ] En haut à droite : **Publish**, puis **Publish** (ou **Update** si le site est déjà publié). Lovable lui donne d'abord une adresse en `.lovable.app`.

### 3.2 Brancher le domaine `rayfinder.datagrow.me`

- [ ] Dans Lovable : **Project settings › Domains › Connect domain**. Saisissez `rayfinder.datagrow.me`.
- [ ] Lovable affiche des **enregistrements DNS** à créer, en général un enregistrement **A** et un **TXT** de vérification. Gardez cette page ouverte.
- [ ] Chez le gestionnaire du domaine `datagrow.me` (OVH, Gandi, Cloudflare, Google Domains…), ouvrez la **zone DNS** et ajoutez exactement ces enregistrements, avec `rayfinder` comme sous-domaine.
  - Sur Cloudflare : désactivez le proxy (nuage **gris**, « DNS only ») pour ces enregistrements, sinon la vérification échoue.
- [ ] Revenez dans Lovable et cliquez **Verify**. La propagation DNS prend de quelques minutes à quelques heures. Le certificat HTTPS est créé automatiquement ensuite.
- [ ] Ouvrez les 4 adresses du tableau pour vérifier qu'elles s'affichent, en HTTPS.

### 3.3 Régler l'adresse des e-mails de confirmation

- [ ] Dans Lovable : **Cloud › Users › Authentication settings** (ou **Auth › URL Configuration**), mettez `https://rayfinder.datagrow.me` comme **Site URL**. C'est l'adresse vers laquelle pointe le lien de confirmation des e-mails d'inscription, envoyés depuis le site comme depuis l'app.
- [ ] Ajoutez aussi `https://rayfinder.datagrow.me/**` dans les **Redirect URLs**, si ce champ existe.

> Si vous changez finalement d'adresse, prévenez-moi avant la phase 4 : elle est écrite dans l'app (`SITE_URL` dans `android/app/build.gradle.kts`) et dans le site (`src/lib/site.ts`).

---

## Phase 4 — Créer la clé de signature et générer le fichier à envoyer

### 4.1 Comprendre la clé

Google signe lui-même l'app distribuée (**Play App Signing**, activé par défaut). Vous créez seulement une **clé d'importation**, qui prouve à Google que les mises à jour viennent bien de vous.

- Si vous perdez cette clé, il est possible de la réinitialiser auprès du support Google, mais c'est long.
- **Sauvegardez-la** (voir 4.3).

### 4.2 Générer la clé

- [ ] Dans le Terminal, lancez :

```bash
keytool -genkey -v -keystore ~/rayfinder-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

- [ ] Répondez aux questions :
  - **Mot de passe du keystore** : choisissez un mot de passe solide, puis notez-le dans votre gestionnaire de mots de passe.
  - **Prénom et nom** : votre nom.
  - **Unité organisationnelle** / **Organisation** : par exemple « Datagrow », ou laissez vide (Entrée).
  - **Ville**, **Département**, **Code pays** : par exemple `FR`.
  - Confirmez avec `oui`.
- [ ] Le fichier `~/rayfinder-upload.jks` est créé dans votre dossier personnel.

### 4.3 Sauvegarder la clé

- [ ] Copiez `~/rayfinder-upload.jks` dans un endroit sûr **en dehors du Mac** : gestionnaire de mots de passe (pièce jointe), clé USB, ou drive chiffré.
- [ ] Notez à côté : l'alias `upload` et le mot de passe.
- [ ] **Ne mettez jamais ce fichier sur GitHub.** Le `.gitignore` le bloque déjà.

### 4.4 Indiquer la clé au projet

- [ ] Créez le fichier `~/Code/Lespa-R/rayfinder-pro/android/keystore.properties` avec ce contenu, en remplaçant les mots de passe :

```properties
storeFile=/Users/lespa/rayfinder-upload.jks
storePassword=VOTRE_MOT_DE_PASSE
keyAlias=upload
keyPassword=VOTRE_MOT_DE_PASSE
```

`keytool` utilise le même mot de passe pour le keystore et la clé, sauf si vous en avez saisi un différent. Ce fichier est ignoré par git : il reste sur votre Mac.

### 4.5 Générer le fichier `.aab`

- [ ] Lancez :

```bash
cd ~/Code/Lespa-R/rayfinder-pro/android && ./gradlew bundleRelease
```

- [ ] Le fichier à envoyer à Google est créé ici : `~/Code/Lespa-R/rayfinder-pro/android/app/build/outputs/bundle/release/app-release.aab` (environ 2 Mo).
- [ ] Pour l'ouvrir dans le Finder :

```bash
open ~/Code/Lespa-R/rayfinder-pro/android/app/build/outputs/bundle/release/
```

---

## Phase 5 — Créer l'application dans la Play Console

Prérequis : le compte développeur est validé (phase 2).

- [ ] Allez sur https://play.google.com/console
- [ ] Cliquez **Créer une application**.
- [ ] Remplissez :
  - **Nom de l'application** : `RayFinder Pro: lunettes caméra`
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

Menu de gauche : **Règles et programmes › Contenu de l'application**. Les réponses détaillées sont dans [`play-store/fiche-play-store.md`](play-store/fiche-play-store.md). Traitez chaque bloc :

- [ ] **Règles de confidentialité** : `https://rayfinder.datagrow.me/confidentialite`
- [ ] **Accès aux applications** : `Tout ou partie des fonctionnalités de mon application sont soumises à des restrictions`. Cliquez **Ajouter des instructions** :
  - Nom : `Compte de test`
  - Nom d'utilisateur / Mot de passe : le compte créé à la phase 1.3. Créez-en un dédié si vous préférez ne pas donner le vôtre.
  - Instructions : `Onglet Compte › Se connecter. Le radar (onglet Radar) fonctionne sans compte ; le compte débloque Safe Zone et Stats.`
- [ ] **Annonces** : `Non, mon application ne contient pas d'annonces`.
- [ ] **Classification du contenu** :
  - Adresse e-mail : `lespa+rayfinder@datagrow.me`
  - Catégorie : `Toutes les autres catégories d'applications`.
  - Violence, sexualité, langage grossier, substances, jeux d'argent, achats numériques : **Non**.
  - « Les utilisateurs peuvent-ils interagir ou échanger des informations ? » : **Oui** (pseudos visibles, alertes entre amis).
  - « L'application partage-t-elle la position actuelle de l'utilisateur avec d'autres utilisateurs ? » : **Non** (seule une ville saisie à la main).
  - Enregistrez, puis **Envoyer**.
- [ ] **Public cible et contenu** :
  - Tranche d'âge : cochez uniquement **18 ans et plus**. Vous évitez ainsi les règles renforcées pour les enfants.
  - « L'application pourrait-elle attirer les enfants ? » : **Non**.
- [ ] **Applications d'actualités** : **Non**.
- [ ] **Sécurité des données** : suivez le tableau de la section « Sécurité des données » du fichier fiche. En résumé :
  - Collecte de données : **Oui** ; chiffrement en transit : **Oui** ; suppression possible : **Oui**.
  - **URL de suppression du compte** : `https://rayfinder.datagrow.me/suppression-compte`
  - Types collectés (tous **facultatifs**, **non partagés**) : adresse e-mail, nom, ID utilisateur, position approximative, autre contenu généré par l'utilisateur, autres actions.
  - Enregistrez, puis **Envoyer**.
- [ ] **Applications gouvernementales** : **Non**.
- [ ] **Fonctionnalités financières** : `Mon application ne fournit aucune fonctionnalité financière`.
- [ ] **Santé** : `Mon application ne propose aucune fonctionnalité de santé`.
- [ ] Si un bloc **Autorisations** ou **Bluetooth** apparaît, collez la justification du fichier fiche.

### 6.2 Fiche principale du Store

Menu de gauche : **Accroître le nombre d'utilisateurs › Présence sur le Store › Fiche principale**.

- [ ] **Nom de l'application** : `RayFinder Pro: lunettes caméra`
- [ ] **Description courte** : copiez celle du fichier fiche.
- [ ] **Description complète** : copiez celle du fichier fiche, en entier, y compris le paragraphe sur les marques.
- [ ] **Icône de l'application** : `play-store/icon-512.png`
- [ ] **Image de présentation** : `play-store/feature-graphic-1024x500.png`
- [ ] **Captures d'écran du téléphone** : glissez `screenshot-1.png`, `screenshot-2.png`, `screenshot-3.png`, `screenshot-4.png`, **dans cet ordre**.
- [ ] Captures pour tablettes et Chromebook : laissez vide (facultatif).
- [ ] Cliquez **Enregistrer**.

Pour retrouver les visuels dans le Finder :

```bash
open ~/Code/Lespa-R/rayfinder-pro/play-store/
```

### 6.3 Paramètres du Store

Menu : **Présence sur le Store › Paramètres du Store**.

- [ ] **Catégorie** : `Outils`
- [ ] **Tags** : jusqu'à 5, par exemple sécurité, confidentialité, Bluetooth.
- [ ] **Coordonnées** : e-mail `lespa+rayfinder@datagrow.me` ; site web : `https://rayfinder.datagrow.me` ; téléphone facultatif.
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
Salut ! Je publie ma première app Android, RayFinder Pro, qui détecte les lunettes connectées à caméra (Ray-Ban Meta) autour de soi.
Google m'impose 14 jours de test avec 12 personnes avant la mise en ligne. Tu peux m'aider ?
1. Envoie-moi l'adresse Gmail utilisée sur ton téléphone Android
2. Je t'enverrai un lien : clique dessus depuis ton téléphone, accepte de devenir testeur, puis installe l'app
3. Garde-la installée 14 jours et ouvre-la de temps en temps
Merci !
```

### 7.3 Créer le test fermé

- [ ] **Tester et publier › Tests › Test fermé**. Utilisez la piste **Alpha** proposée par défaut, ou **Créer un canal**.
- [ ] **Pays/Régions** : France (et les pays de vos testeurs).
- [ ] Onglet **Testeurs** : **Créer une liste d'adresses e-mail**, nommée par exemple « Testeurs RayFinder Pro ». Collez les adresses séparées par des virgules, puis **Enregistrer**.
- [ ] **Adresse e-mail ou URL de commentaires** : `lespa+rayfinder@datagrow.me`.
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
- [ ] Notes de version : `Première version publique de RayFinder Pro.`
- [ ] **Suivant › Enregistrer**, puis **Vue d'ensemble de la publication › Envoyer pour examen**.
- [ ] L'examen prend en général 1 à 3 jours, parfois jusqu'à 7. Vous recevez un e-mail quand l'app est en ligne.
- [ ] Vérifiez la page publique : `https://play.google.com/store/apps/details?id=me.datagrow.rayfinderpro`
- [ ] Demandez-moi d'activer le bouton « Google Play » du site (`PLAY_STORE_LIVE = true` dans `src/lib/site.ts`), ou demandez-le à Lovable.

---

## Publier une mise à jour (plus tard)

À chaque nouvelle version :

1. Demandez-moi la modification. J'**augmente `versionCode`** (1 → 2 → 3…) et `versionName` dans `android/app/build.gradle.kts`. Google refuse un fichier dont le `versionCode` a déjà été envoyé.
2. Lancez `./gradlew bundleRelease` depuis le dossier `android/`.
3. Dans la Play Console : **Production › Créer une release**, importez le nouveau `.aab`, ajoutez des notes de version, puis **Envoyer pour examen**.

---

## En cas de problème

| Problème | Que faire |
|---|---|
| `./gradlew installDebug` affiche « no connected devices » | Vérifiez le câble (certains câbles ne font que la charge), réacceptez la fenêtre « Autoriser le débogage USB », débranchez puis rebranchez |
| `bundleRelease` affiche une erreur de mot de passe | Vérifiez `android/keystore.properties` : chemin exact du `.jks`, mots de passe sans espace en fin de ligne |
| Google refuse l'app pour « usurpation » ou « marque » | Le nom contient déjà le mot « Meta » ailleurs que dans la description ? Envoyez-moi le message de refus et j'adapte les textes |
| Google signale la permission Bluetooth | Collez la justification de `play-store/fiche-play-store.md` dans le formulaire de réponse |
| Le compteur des 14 jours ne démarre pas | Moins de 12 testeurs ont **accepté** le lien. Une simple invitation ne compte pas. Relancez-les |
| « Votre application cible une version d'API trop ancienne » | Demandez-moi de passer à la dernière version d'Android exigée par Google (cible actuelle : API 36) |
| L'onglet Compte dit « Service indisponible » | Le fichier `.env` (géré par Lovable, à la racine) est absent ou incomplet au moment du build : faites `git pull`, puis régénérez l'app |
| Le lien de confirmation d'inscription mène à une page d'erreur | Vérifiez la **Site URL** dans les réglages d'authentification Lovable Cloud (phase 3) |
| « Your APK or Android App Bundle needs to have the package name … » | La fiche Play Console est liée à un autre nom de package, et ce lien est définitif. Créez une nouvelle app dans la Play Console : son nom de package sera fixé par le premier envoi (`me.datagrow.rayfinderpro`). Supprimez l'ancienne fiche si elle n'a jamais été publiée |
| Vous avez perdu la clé `.jks` | Play Console › **Test et publication › Configuration › Intégrité de l'application › Signature d'application** › demander la réinitialisation de la clé d'importation |

---

## Cohabitation Lovable et app Android

- **Lovable reste connecté au dépôt** : ne le déconnectez pas. Les modifications faites dans Lovable arrivent sur GitHub, et inversement.
- Lovable ignore le dossier `android/`. Évitez seulement de lui demander de supprimer ou déplacer des dossiers à la racine.
- **Ne modifiez pas les tables du backend** (`profiles`, `sightings`, `friends`, `safe_zone_alerts`) ni les fonctions `city_stats_today` et `delete_my_account` sans me prévenir : l'app Android les utilise directement. Ajouter une colonne ou une table ne pose pas de problème. En renommer ou en supprimer une casse l'app déjà installée chez les utilisateurs.
- Si vous ajoutez une connexion Google à l'app Android plus tard, il faudra une configuration spécifique : dans l'app, seule la connexion par e-mail est proposée pour l'instant.
- [ ] Sur GitHub : le dépôt temporaire `Lespa-R/glass-radar` fait doublon. Supprimez-le dans https://github.com/Lespa-R/glass-radar/settings › Danger Zone › **Delete this repository**.

---

## Récapitulatif des fichiers utiles

| Fichier | Usage |
|---|---|
| `android/app/build/outputs/bundle/release/app-release.aab` | Fichier à envoyer à Google (généré à la phase 4.5) |
| `play-store/fiche-play-store.md` | Textes et réponses à copier-coller |
| `play-store/icon-512.png` | Icône Play Store |
| `play-store/feature-graphic-1024x500.png` | Image de présentation |
| `play-store/screenshot-1.png` à `-4.png` | Captures d'écran |
| `src/routes/` | Site Lovable, dont `mentions-legales.tsx`, `confidentialite.tsx`, `suppression-compte.tsx` |
| `src/lib/site.ts` | Infos éditeur DataGrow, lien Play Store (`PLAY_STORE_LIVE` à passer à `true` après publication) |
| `~/rayfinder-upload.jks` + `android/keystore.properties` | Clé de signature (**jamais sur GitHub**, à sauvegarder) |
