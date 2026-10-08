<!-- LOVABLE:BEGIN -->
> [!IMPORTANT]
> This project is connected to [Lovable](https://lovable.dev). Avoid rewriting
> published git history — force pushing, or rebasing/amending/squashing commits
> that are already pushed — as it rewrites history on Lovable's side and the
> user will likely lose their project history.
>
> Commits you push to the connected branch sync back to Lovable and show up in
> the editor, so keep the branch in a working state.
<!-- LOVABLE:END -->

## RayFinder Pro
- Monorepo : site web Lovable (TanStack Start) à la racine, app Android native (Kotlin/Compose) dans `android/`. Ne pas toucher à `android/` ni à `play-store/` depuis Lovable.
- Bluetooth scanning (web) lives in src/hooks/useScanner.ts and falls back to simulated detections when Web Bluetooth LE scanning is unavailable, so the UI always works.
- Community data (profiles, sightings, friends, safe_zone_alerts) is in Lovable Cloud with per-user RLS; city stats come from the city_stats_today function so no personal rows are exposed. Sightings are readable only by their owner.
- The Android app calls the same tables and RPCs (city_stats_today, delete_my_account) over REST: do not rename or drop them, or their columns, without updating `android/app/src/main/java/me/datagrow/rayfinderpro/Backend.kt`.
- `/` is the marketing landing for the Android app (Play Store badge driven by PLAY_STORE_LIVE); the web radar lives at `/radar`. Public site: https://rayfinder.datagrow.me.
- Publisher/legal info lives in src/lib/site.ts; legal pages: /mentions-legales, /confidentialite, /suppression-compte (required by Google Play).
