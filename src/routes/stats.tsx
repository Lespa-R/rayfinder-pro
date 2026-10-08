import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { AppShell } from "@/components/AppShell";
import { useAuth } from "@/hooks/useAuth";

export const Route = createFileRoute("/stats")({
  head: () => ({
    meta: [
      { title: "Stats communautaires RayFinder Pro — détections du jour" },
      {
        name: "description",
        content:
          "Combien de smart glasses ont été détectées aujourd'hui dans ta ville ? Les chiffres partagés par la communauté RayFinder Pro.",
      },
      { property: "og:title", content: "Stats communautaires RayFinder Pro" },
      {
        property: "og:description",
        content: "Les détections de smart glasses partagées aujourd'hui, ville par ville.",
      },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: StatsPage,
});

type CityStat = { city: string; detections: number };

function StatsPage() {
  const { user, loading } = useAuth();

  const { data, isLoading } = useQuery({
    queryKey: ["city-stats", user?.id],
    enabled: !!user,
    queryFn: async (): Promise<CityStat[]> => {
      const { data, error } = await supabase.rpc("city_stats_today");
      if (error) throw error;
      return (data ?? []) as CityStat[];
    },
  });

  const total = (data ?? []).reduce((sum, row) => sum + Number(row.detections), 0);
  const max = Math.max(1, ...(data ?? []).map((r) => Number(r.detections)));

  return (
    <AppShell>
      <h1 className="font-display text-2xl font-bold">Stats communautaires</h1>
      <p className="mt-1 text-sm text-muted-foreground">Détections partagées depuis minuit.</p>

      {!loading && !user && (
        <p className="panel mt-5 p-5 text-sm text-muted-foreground">
          Connecte-toi pour voir les chiffres de la communauté.
        </p>
      )}

      {user && (
        <>
          <div className="panel mt-5 p-5">
            <p className="text-xs tracking-widest text-muted-foreground uppercase">Total aujourd'hui</p>
            <p className="font-display text-glow text-5xl font-bold text-primary">{total}</p>
          </div>

          <div className="panel mt-4 divide-y divide-border p-2">
            {isLoading && <p className="p-3 text-sm text-muted-foreground">Chargement…</p>}
            {!isLoading && (data ?? []).length === 0 && (
              <p className="p-3 text-sm text-muted-foreground">
                Aucune détection partagée aujourd'hui. Sois le premier.
              </p>
            )}
            {(data ?? []).map((row) => (
              <div key={row.city} className="space-y-2 p-3">
                <div className="flex items-center justify-between text-sm">
                  <span>{row.city}</span>
                  <span className="font-mono text-primary">{row.detections}</span>
                </div>
                <div className="h-1.5 w-full rounded-full bg-muted">
                  <div
                    className="alert-surface h-full rounded-full"
                    style={{ width: `${(Number(row.detections) / max) * 100}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </AppShell>
  );
}
