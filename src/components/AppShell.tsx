import { Link } from "@tanstack/react-router";
import type { ReactNode } from "react";
import { Radar, ShieldAlert, BarChart3, UserRound } from "lucide-react";
import { PUBLISHER } from "@/lib/site";

const NAV = [
  { to: "/radar", label: "Radar", icon: Radar },
  { to: "/safe-zone", label: "Safe Zone", icon: ShieldAlert },
  { to: "/stats", label: "Stats", icon: BarChart3 },
  { to: "/auth", label: "Compte", icon: UserRound },
] as const;

export function SiteFooter({ className = "" }: { className?: string }) {
  return (
    <footer
      className={`mx-auto w-full max-w-2xl px-5 pt-10 text-xs text-muted-foreground ${className}`}
    >
      <div className="flex flex-wrap gap-x-4 gap-y-2 border-t border-border pt-4">
        <Link to="/mentions-legales" className="hover:text-foreground">
          Mentions légales
        </Link>
        <Link to="/confidentialite" className="hover:text-foreground">
          Confidentialité
        </Link>
        <Link to="/suppression-compte" className="hover:text-foreground">
          Supprimer mon compte
        </Link>
        <span>© 2026 {PUBLISHER.name}</span>
      </div>
    </footer>
  );
}

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col">
      <header className="mx-auto flex w-full max-w-2xl items-center justify-between px-5 pt-6 pb-3">
        <Link to="/" className="flex items-center gap-2.5">
          <img src="/logo.svg" alt="" className="size-9 rounded-lg" />
          <span className="font-display text-lg font-bold tracking-tight">
            RayFinder <span className="text-primary">Pro</span>
          </span>
        </Link>
      </header>

      <main className="mx-auto w-full max-w-2xl flex-1 px-5">{children}</main>

      <SiteFooter className="pb-28" />

      <nav className="fixed inset-x-0 bottom-0 z-40 border-t border-border bg-background/95 backdrop-blur">
        <div className="mx-auto flex max-w-md items-stretch">
          {NAV.map(({ to, label, icon: Icon }) => (
            <Link
              key={to}
              to={to}
              className="flex flex-1 flex-col items-center gap-1 py-3 text-[11px] text-muted-foreground transition-colors"
              activeProps={{ className: "text-primary" }}
            >
              <Icon className="size-5" />
              {label}
            </Link>
          ))}
        </div>
      </nav>
    </div>
  );
}

/** Page de texte (mentions légales, confidentialité…) avec titre et date de mise à jour. */
export function LegalPage({
  title,
  updated,
  children,
}: {
  title: string;
  updated: string;
  children: ReactNode;
}) {
  return (
    <AppShell>
      <article className="prose-legal">
        <h1 className="font-display text-3xl font-bold tracking-tight">{title}</h1>
        <p className="text-sm text-muted-foreground">Dernière mise à jour : {updated}</p>
        {children}
      </article>
    </AppShell>
  );
}
