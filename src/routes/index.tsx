import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Bell, BellOff, Share2, Trash2 } from "lucide-react";
import { AppShell } from "@/components/AppShell";
import { RadarScope } from "@/components/RadarScope";
import { useScanner } from "@/hooks/useScanner";
import { useAuth } from "@/hooks/useAuth";
import { signalLabel, type Detection } from "@/lib/radar";
import { supabase } from "@/integrations/supabase/client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "RayRadar — détecte les smart glasses autour de toi" },
      {
        name: "description",
        content:
          "RayRadar repère les Ray-Ban Meta et autres smart glasses à proximité via Bluetooth, t'alerte discrètement et affiche un radar en temps réel.",
      },
      { property: "og:title", content: "RayRadar — radar de smart glasses" },
      {
        property: "og:description",
        content: "Détection Bluetooth en temps réel, alerte discrète et partage communautaire.",
      },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: RadarPage,
});

function RadarPage() {
  const { user } = useAuth();
  const [soundOn, setSoundOn] = useState(true);
  const [city, setCity] = useState("");
  const { mode, detections, lastAlert, notice, start, stop, clear, dismissAlert } = useScanner({
    soundOn,
  });
  const scanning = mode !== "idle";

  useEffect(() => {
    const saved = window.localStorage.getItem("rayradar.city");
    if (saved) setCity(saved);
  }, []);

  useEffect(() => {
    if (notice) toast.info(notice);
  }, [notice]);

  async function share(d: Detection) {
    if (!user) {
      toast.error("Connecte-toi pour partager une détection");
      return;
    }
    window.localStorage.setItem("rayradar.city", city);
    let coords: { lat: number; lng: number } | null = null;
    try {
      coords = await new Promise((resolve) => {
        if (!navigator.geolocation) return resolve(null);
        navigator.geolocation.getCurrentPosition(
          (p) => resolve({ lat: p.coords.latitude, lng: p.coords.longitude }),
          () => resolve(null),
          { timeout: 4000 },
        );
      });
    } catch {
      coords = null;
    }

    const { error } = await supabase.from("sightings").insert({
      user_id: user.id,
      device_name: d.name,
      device_key: d.key,
      model: d.model,
      rssi: d.rssi,
      distance_m: d.distance,
      city: city || null,
      lat: coords?.lat ?? null,
      lng: coords?.lng ?? null,
      shared: true,
    });
    if (error) toast.error("Partage impossible");
    else toast.success("Détection partagée avec la communauté");
  }

  return (
    <AppShell>
      {lastAlert && (
        <div className="alert-surface mb-4 flex items-center justify-between gap-3 rounded-xl px-4 py-3">
          <div>
            <p className="animate-alert-pulse text-[11px] font-bold tracking-[0.2em] uppercase">
              Détection
            </p>
            <p className="font-display text-sm font-bold">{lastAlert.name}</p>
            <p className="text-xs opacity-80">
              {signalLabel(lastAlert.rssi)} · ~{lastAlert.distance} m
            </p>
          </div>
          <button onClick={dismissAlert} className="text-xs underline">
            OK
          </button>
        </div>
      )}

      <RadarScope detections={detections} active={scanning} />

      <div className="mt-5 flex items-center gap-2">
        <Button className="flex-1" onClick={() => (scanning ? stop() : void start())}>
          {scanning ? "Arrêter le scan" : "Lancer le scan"}
        </Button>
        <Button variant="outline" size="icon" onClick={() => setSoundOn((v) => !v)} aria-label="Son">
          {soundOn ? <Bell className="size-4" /> : <BellOff className="size-4" />}
        </Button>
        <Button variant="outline" size="icon" onClick={clear} aria-label="Effacer">
          <Trash2 className="size-4" />
        </Button>
      </div>

      <p className="mt-2 text-center text-[11px] tracking-widest text-muted-foreground uppercase">
        {mode === "bluetooth" ? "Scan Bluetooth actif" : mode === "demo" ? "Mode démonstration" : "Radar en veille"}
      </p>

      <div className="mt-5 space-y-1.5">
        <Input
          value={city}
          onChange={(e) => setCity(e.target.value)}
          onBlur={() => window.localStorage.setItem("rayradar.city", city)}
          placeholder="Ta ville (pour les stats)"
        />
      </div>

      <div className="panel mt-4 divide-y divide-border">
        {detections.length === 0 && (
          <p className="p-4 text-sm text-muted-foreground">
            Aucune détection pour l'instant. Lance le scan et garde le téléphone avec toi.
          </p>
        )}
        {detections.map((d) => (
          <div key={d.key} className="flex items-center justify-between gap-3 p-4">
            <div className="min-w-0">
              <p className="truncate font-display text-sm font-bold">{d.name}</p>
              <p className="font-mono text-xs text-muted-foreground">
                {signalLabel(d.rssi)} · {d.rssi} dBm · ~{d.distance} m
              </p>
            </div>
            <Button size="sm" variant="secondary" onClick={() => void share(d)}>
              <Share2 className="mr-1 size-3.5" /> Partager
            </Button>
          </div>
        ))}
      </div>

      {!user && (
        <p className="mt-4 text-center text-sm text-muted-foreground">
          <Link to="/auth" className="text-primary underline">
            Connecte-toi
          </Link>{" "}
          pour partager tes détections et alerter tes amis.
        </p>
      )}
    </AppShell>
  );
}
