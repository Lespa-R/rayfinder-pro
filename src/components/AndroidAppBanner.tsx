import { Smartphone } from "lucide-react";
import { PLAY_STORE_LIVE, PLAY_STORE_URL } from "@/lib/site";

/** Le navigateur ne sait presque jamais scanner le Bluetooth : on oriente vers l'app Android. */
export function AndroidAppBanner() {
  return (
    <div className="panel flex items-center gap-4 p-4">
      <span className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-secondary text-primary">
        <Smartphone className="size-5" />
      </span>
      <div className="min-w-0 flex-1">
        <p className="font-display text-sm font-bold">La vraie détection est dans l'app Android</p>
        <p className="text-xs text-muted-foreground">
          Les navigateurs limitent le scan Bluetooth. L'app scanne en continu et utilise le même
          compte.
        </p>
      </div>
      {PLAY_STORE_LIVE ? (
        <a
          href={PLAY_STORE_URL}
          target="_blank"
          rel="noreferrer"
          className="alert-surface shrink-0 rounded-md px-3 py-2 text-xs font-semibold"
        >
          Google Play
        </a>
      ) : (
        <span className="shrink-0 rounded-md border border-border px-3 py-2 text-xs text-muted-foreground">
          Bientôt
        </span>
      )}
    </div>
  );
}
