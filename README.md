# RayFinder Pro

Détecteur de lunettes connectées à caméra (Ray-Ban Meta, Oakley Meta), édité par SASU DataGrow.

| Partie | Dossier | Techno |
|---|---|---|
| Site web https://rayfinder.datagrow.me (présentation de l'app, radar web `/radar`, Safe Zone, stats, pages légales) | racine | Lovable · TanStack Start · Tailwind |
| App Android | [`android/`](android/) | Kotlin · Jetpack Compose |
| Backend commun (comptes, détections partagées, amis, alertes) | [`supabase/`](supabase/) | Lovable Cloud (Supabase) |
| Visuels et fiche Play Store | [`play-store/`](play-store/) | HTML → PNG (`render.sh`) |

Projet Lovable : https://lovable.dev/projects/b43a81f9-b0e5-4e15-8abc-95cd062cb987. Les commits poussés sur `main` y sont synchronisés.

Guide de publication sur Google Play, étape par étape : [`PUBLICATION.md`](PUBLICATION.md).

## Site web

```bash
npm install
npm run dev
```

Pages légales : `src/routes/mentions-legales.tsx`, `confidentialite.tsx`, `suppression-compte.tsx`. Infos éditeur et lien Play Store : `src/lib/site.ts`.

## App Android

```bash
cd android
./gradlew installDebug      # sur un téléphone branché (débogage USB)
./gradlew bundleRelease     # fichier .aab pour Google Play (signature : android/keystore.properties)
```

- **Détection** (`MetaSignatures.kt`) : identifiants Bluetooth SIG de Meta (`0x01AB`, `0x058E`), de Luxottica (`0x0D53`), UUID de service `0xFD5F`, `0xFEB7` et `0xFEB8`, et nom de l'appareil. Le radar fonctionne sans compte ni réseau.
- **Backend** (`Backend.kt`) : appels REST vers Lovable Cloud. L'URL et la clé publique sont lues dans le `.env` de Lovable au moment du build, sans être recopiées dans le code.
- **Onglets** : Radar, Safe Zone, Stats, Compte (inscription et connexion par e-mail, ville, suppression de compte).

Le scan BLE ne fonctionne pas dans l'émulateur. Pour vérifier l'interface, le **mode démo** (versions de debug uniquement) simule des appareils :

```bash
adb shell am start -n me.datagrow.rayfinderpro/.MainActivity --ez demo true
```

Les visuels Play Store (`play-store/raw/`) sont de vraies captures faites dans ce mode.
