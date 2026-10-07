# GlassRadar

Application Android (Kotlin + Jetpack Compose) qui détecte en Bluetooth Low Energy la présence de lunettes connectées Meta (Ray-Ban Meta, Oakley Meta…) et vibre quand une paire apparaît.

## Fonctionnement

L'application scanne les annonces BLE et repère les identifiants publiés par le Bluetooth SIG (voir `MetaSignatures.kt`) :

- Company ID `0x01AB` (Meta Platforms), `0x058E` (Meta Platforms Technologies), `0x0D53` (Luxottica)
- UUID de service 16 bits Meta `0xFD5F`, `0xFEB7`, `0xFEB8`
- nom d'appareil contenant « Ray-Ban », « Oakley » ou « Meta »

La distance est estimée à partir du RSSI. Le switch « Afficher tous les appareils » sert à vérifier que le scan fonctionne sans lunettes à portée.

Limites : les lunettes n'émettent pas en permanence (surtout en veille ou rangées dans leur étui), et un casque Quest peut aussi être signalé.

## Développement

```bash
./gradlew assembleDebug        # APK : app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # sur un téléphone branché (débogage USB activé)
```

Le scan BLE ne fonctionne pas dans l'émulateur : il faut tester sur un vrai téléphone.

## Publication sur le Play Store

1. **Clé de signature** (à garder précieusement, hors du dépôt) :
   ```bash
   keytool -genkey -v -keystore ~/glassradar-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
   ```
2. Créer `keystore.properties` à la racine (ignoré par git) :
   ```properties
   storeFile=/Users/lespa/glassradar-upload.jks
   storePassword=...
   keyAlias=upload
   keyPassword=...
   ```
3. Générer le bundle : `./gradlew bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`
4. Dans la [Play Console](https://play.google.com/console) : créer l'app, importer l'`.aab` en test interne d'abord.
5. Fiche et formulaires :
   - **Politique de confidentialité** : publier `PRIVACY.md` sur une URL publique et la renseigner.
   - **Sécurité des données** : « Aucune donnée collectée ni partagée ».
   - **Nom** : éviter « Meta » ou « Ray-Ban » dans le titre (marques déposées) ; on peut les citer dans la description pour dire ce que l'app détecte.
   - Captures d'écran téléphone (au moins 2) + icône 512×512 + bannière 1024×500.
6. Les comptes développeur personnels récents doivent faire tourner un **test fermé avec 12 testeurs pendant 14 jours** avant la mise en production.
