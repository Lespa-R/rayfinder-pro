import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { toast } from "sonner";
import { supabase } from "@/integrations/supabase/client";
import { lovable } from "@/integrations/lovable/index";
import { useAuth } from "@/hooks/useAuth";
import { AppShell } from "@/components/AppShell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export const Route = createFileRoute("/auth")({
  head: () => ({
    meta: [
      { title: "Compte RayFinder Pro — connexion et inscription" },
      {
        name: "description",
        content:
          "Connecte-toi à RayFinder Pro pour partager tes détections de smart glasses et alerter tes amis en mode Safe Zone.",
      },
      { property: "og:title", content: "Compte RayFinder Pro" },
      {
        property: "og:description",
        content: "Rejoins la communauté RayFinder Pro et partage tes détections en temps réel.",
      },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: AuthPage,
});

function AuthPage() {
  const { user, loading } = useAuth();
  const navigate = useNavigate();
  const [mode, setMode] = useState<"signin" | "signup">("signin");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [busy, setBusy] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    try {
      if (mode === "signup") {
        const { error } = await supabase.auth.signUp({
          email,
          password,
          options: {
            emailRedirectTo: window.location.origin,
            data: { display_name: name || email.split("@")[0] },
          },
        });
        if (error) throw error;
        toast.success("Compte créé", { description: "Vérifie ta boîte mail pour confirmer." });
      } else {
        const { error } = await supabase.auth.signInWithPassword({ email, password });
        if (error) throw error;
        toast.success("Connecté");
        navigate({ to: "/" });
      }
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Une erreur est survenue");
    } finally {
      setBusy(false);
    }
  }

  async function handleGoogle() {
    const result = await lovable.auth.signInWithOAuth("google", {
      redirect_uri: window.location.origin,
    });
    if (result.error) {
      toast.error("Connexion Google impossible");
      return;
    }
    if (result.redirected) return;
    navigate({ to: "/" });
  }

  if (loading) {
    return (
      <AppShell>
        <p className="text-sm text-muted-foreground">Chargement…</p>
      </AppShell>
    );
  }

  if (user) {
    return (
      <AppShell>
        <div className="panel space-y-4 p-5">
          <div>
            <p className="text-xs tracking-widest text-muted-foreground uppercase">Connecté</p>
            <p className="font-display text-lg">{user.email}</p>
          </div>
          <Button
            variant="outline"
            onClick={async () => {
              await supabase.auth.signOut();
              toast.success("Déconnecté");
            }}
          >
            Se déconnecter
          </Button>
          <Link to="/suppression-compte" className="block text-sm text-muted-foreground underline">
            Supprimer mon compte
          </Link>
        </div>
      </AppShell>
    );
  }

  return (
    <AppShell>
      <div className="panel space-y-5 p-5">
        <div>
          <h1 className="font-display text-2xl font-bold">
            {mode === "signin" ? "Connexion" : "Créer un compte"}
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Nécessaire pour partager tes détections et utiliser la Safe Zone.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3">
          {mode === "signup" && (
            <div className="space-y-1.5">
              <Label htmlFor="name">Pseudo</Label>
              <Input
                id="name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Agent 47"
              />
            </div>
          )}
          <div className="space-y-1.5">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="toi@exemple.com"
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="password">Mot de passe</Label>
            <Input
              id="password"
              type="password"
              required
              minLength={6}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          <Button type="submit" className="w-full" disabled={busy}>
            {mode === "signin" ? "Se connecter" : "S'inscrire"}
          </Button>
        </form>

        <Button variant="outline" className="w-full" onClick={handleGoogle}>
          Continuer avec Google
        </Button>

        <button
          type="button"
          className="w-full text-center text-sm text-muted-foreground underline"
          onClick={() => setMode(mode === "signin" ? "signup" : "signin")}
        >
          {mode === "signin"
            ? "Pas encore de compte ? Inscris-toi"
            : "Déjà un compte ? Connecte-toi"}
        </button>
      </div>
    </AppShell>
  );
}
