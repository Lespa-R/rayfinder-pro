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

## RayRadar
- Bluetooth scanning lives in src/hooks/useScanner.ts and falls back to simulated detections when Web Bluetooth LE scanning is unavailable, so the UI always works.
- Community data (sightings, friends, safe zone alerts) is in Lovable Cloud with per-user RLS; city stats come from the city_stats_today function so no personal rows are exposed.
