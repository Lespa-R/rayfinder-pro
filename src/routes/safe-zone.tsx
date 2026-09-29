import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { ShieldAlert, UserPlus, X } from "lucide-react";
import { AppShell } from "@/components/AppShell";
import { useAuth } from "@/hooks/useAuth";
import { supabase } from "@/integrations/supabase/client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export const Route = createFileRoute("/safe-zone")({
  head: () => ({
    meta: [
      { title: "Safe Zone RayRadar — alerte tes amis" },
      {
        name: "description",
        content:
          "Ajoute tes amis à ta Safe Zone RayRadar et préviens-les instantanément quand des smart glasses sont détectées autour de toi.",
      },
      { property: "og:title", content: "Safe Zone RayRadar" },
      {
        property: "og:description",
        content: "Une alerte instantanée envoyée à tes amis dès qu'une détection a lieu.",
      },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: SafeZonePage,
});

type Profile = { id: string; display_name: string; city: string | null };

function SafeZonePage() {
  const { user, loading } = useAuth();
  const qc = useQueryClient();
  const [search, setSearch] = useState("");
  const [results, setResults] = useState<Profile[]>([]);

  const friends = useQuery({
    queryKey: ["friends", user?.id],
    enabled: !!user,
    queryFn: async () => {
      const { data, error } = await supabase
        .from("friends")
        .select("id, friend_id")
        .eq("user_id", user!.id);
      if (error) throw error;
      const ids = (data ?? []).map((f) => f.friend_id);
      if (ids.length === 0) return [] as (Profile & { linkId: string })[];
      const { data: profiles, error: pErr } = await supabase
        .from("profiles")
        .select("id, display_name, city")
        .in("id", ids);
      if (pErr) throw pErr;
      return (data ?? []).map((f) => {
        const p = (profiles ?? []).find((x) => x.id === f.friend_id);
        return {
          linkId: f.id,
          id: f.friend_id,
          display_name: p?.display_name ?? "Agent",
          city: p?.city ?? null,
        };
      });
    },
  });

  const alerts = useQuery({
    queryKey: ["safe-zone-alerts", user?.id],
    enabled: !!user,
    queryFn: async () => {
      const { data, error } = await supabase
        .from("safe_zone_alerts")
        .select("id, message, city, created_at, user_id")
        .order("created_at", { ascending: false })
        .limit(20);
      if (error) throw error;
      return data ?? [];
    },
  });

  useEffect(() => {
    if (!user) return;
    const channel = supabase
      .channel("safe-zone")
      .on(
        "postgres_changes",
        { event: "INSERT", schema: "public", table: "safe_zone_alerts" },
        () => {
          void qc.invalidateQueries({ queryKey: ["safe-zone-alerts", user.id] });
          toast.warning("Alerte Safe Zone reçue");
        },
      )
      .subscribe();
    return () => {
      void supabase.removeChannel(channel);
    };
  }, [user, qc]);

  async function runSearch() {
    if (!search.trim() || !user) return;
    const { data, error } = await supabase
      .from("profiles")
      .select("id, display_name, city")
      .ilike("display_name", `%${search.trim()}%`)
      .neq("id", user.id)
      .limit(10);
    if (error) {
      toast.error("Recherche impossible");
      return;
    }
    setResults(data ?? []);
  }

  async function addFriend(id: string) {
    const { error } = await supabase.from("friends").insert({ user_id: user!.id, friend_id: id });
    if (error) toast.error("Ami déjà ajouté ou erreur");
    else {
      toast.success("Ami ajouté à ta Safe Zone");
      void qc.invalidateQueries({ queryKey: ["friends", user!.id] });
    }
  }

  async function removeFriend(linkId: string) {
    await supabase.from("friends").delete().eq("id", linkId);
    void qc.invalidateQueries({ queryKey: ["friends", user!.id] });
  }

  async function sendAlert() {
    const city = window.localStorage.getItem("rayradar.city");
    const { error } = await supabase.from("safe_zone_alerts").insert({
      user_id: user!.id,
      message: "Smart glasses détectées à proximité",
      city: city || null,
    });
    if (error) toast.error("Alerte non envoyée");
    else toast.success("Tes amis ont été prévenus");
    void qc.invalidateQueries({ queryKey: ["safe-zone-alerts", user!.id] });
  }

  if (!loading && !user) {
    return (
      <AppShell>
        <h1 className="font-display text-2xl font-bold">Safe Zone</h1>
        <p className="panel mt-5 p-5 text-sm text-muted-foreground">
          <Link to="/auth" className="text-primary underline">
            Connecte-toi
          </Link>{" "}
          pour créer ta Safe Zone et alerter tes amis.
        </p>
      </AppShell>
    );
  }

  return (
    <AppShell>
      <h1 className="font-display text-2xl font-bold">Safe Zone</h1>
      <p className="mt-1 text-sm text-muted-foreground">
        Préviens tes amis d'un seul geste quand tu détectes des smart glasses.
      </p>

      <Button className="mt-4 w-full" onClick={() => void sendAlert()}>
        <ShieldAlert className="mr-2 size-4" /> Alerter mes amis
      </Button>

      <div className="panel mt-5 space-y-3 p-4">
        <p className="text-xs tracking-widest text-muted-foreground uppercase">Ajouter un ami</p>
        <div className="flex gap-2">
          <Input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Pseudo de ton ami"
          />
          <Button variant="secondary" onClick={() => void runSearch()}>
            Chercher
          </Button>
        </div>
        {results.map((p) => (
          <div key={p.id} className="flex items-center justify-between text-sm">
            <span>{p.display_name}</span>
            <Button size="sm" variant="outline" onClick={() => void addFriend(p.id)}>
              <UserPlus className="size-3.5" />
            </Button>
          </div>
        ))}
      </div>

      <div className="panel mt-4 divide-y divide-border">
        <p className="p-4 text-xs tracking-widest text-muted-foreground uppercase">Mes amis</p>
        {(friends.data ?? []).length === 0 && (
          <p className="p-4 text-sm text-muted-foreground">Aucun ami pour l'instant.</p>
        )}
        {(friends.data ?? []).map((f) => (
          <div key={f.linkId} className="flex items-center justify-between p-4 text-sm">
            <span>{f.display_name}</span>
            <button onClick={() => void removeFriend(f.linkId)} aria-label="Retirer">
              <X className="size-4 text-muted-foreground" />
            </button>
          </div>
        ))}
      </div>

      <div className="panel mt-4 divide-y divide-border">
        <p className="p-4 text-xs tracking-widest text-muted-foreground uppercase">Alertes récentes</p>
        {(alerts.data ?? []).length === 0 && (
          <p className="p-4 text-sm text-muted-foreground">Aucune alerte récente.</p>
        )}
        {(alerts.data ?? []).map((a) => (
          <div key={a.id} className="p-4 text-sm">
            <p>{a.message}</p>
            <p className="font-mono text-xs text-muted-foreground">
              {a.city ?? "Lieu inconnu"} · {new Date(a.created_at).toLocaleString("fr-FR")}
            </p>
          </div>
        ))}
      </div>
    </AppShell>
  );
}
