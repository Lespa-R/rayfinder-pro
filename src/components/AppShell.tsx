import { Link } from "@tanstack/react-router";
import type { ReactNode } from "react";
import { Radar, ShieldAlert, BarChart3, UserRound } from "lucide-react";

const NAV = [
  { to: "/", label: "Radar", icon: Radar },
  { to: "/safe-zone", label: "Safe Zone", icon: ShieldAlert },
  { to: "/stats", label: "Stats", icon: BarChart3 },
  { to: "/auth", label: "Compte", icon: UserRound },
] as const;

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col">
      <header className="flex items-center justify-between px-5 pt-6 pb-3">
        <Link to="/" className="flex items-center gap-2">
          <span className="alert-surface flex size-8 items-center justify-center rounded-md">
            <Radar className="size-4" />
          </span>
          <span className="font-display text-lg font-bold tracking-[0.2em] uppercase">
            Ray<span className="text-primary text-glow">Radar</span>
          </span>
        </Link>
      </header>

      <main className="flex-1 px-5 pb-28">{children}</main>

      <nav className="fixed inset-x-0 bottom-0 z-40 border-t border-border bg-background/95 backdrop-blur">
        <div className="mx-auto flex max-w-md items-stretch">
          {NAV.map(({ to, label, icon: Icon }) => (
            <Link
              key={to}
              to={to}
              className="flex flex-1 flex-col items-center gap-1 py-3 text-[11px] text-muted-foreground transition-colors"
              activeOptions={{ exact: to === "/" }}
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
