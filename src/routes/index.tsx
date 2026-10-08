import { createFileRoute, Link } from "@tanstack/react-router";
import type { ReactNode } from "react";
import {
  BarChart3,
  Bluetooth,
  Gauge,
  Lock,
  Radar,
  ShieldAlert,
  Smartphone,
  Vibrate,
} from "lucide-react";
import { SiteFooter } from "@/components/AppShell";
import { APP_NAME, PLAY_STORE_LIVE, PLAY_STORE_URL, SITE_URL } from "@/lib/site";

const TITLE = "RayFinder Pro — repérez les lunettes à caméra autour de vous";
const DESCRIPTION =
  "Application Android qui détecte en Bluetooth les lunettes connectées équipées d'une caméra (Ray-Ban Meta, Oakley Meta) et vous alerte. Bientôt sur Google Play.";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: TITLE },
      { name: "description", content: DESCRIPTION },
      { property: "og:title", content: TITLE },
      { property: "og:description", content: DESCRIPTION },
      { property: "og:type", content: "website" },
      { property: "og:url", content: SITE_URL },
      { property: "og:image", content: `${SITE_URL}/og.png` },
      { name: "twitter:card", content: "summary_large_image" },
      { name: "twitter:image", content: `${SITE_URL}/og.png` },
    ],
    links: [{ rel: "canonical", href: SITE_URL }],
  }),
  component: HomePage,
});

function HomePage() {
  return (
    <div className="min-h-screen">
      <header className="mx-auto flex w-full max-w-6xl items-center justify-between px-5 pt-6">
        <Link to="/" className="flex items-center gap-2.5">
          <img src="/logo.svg" alt="" className="size-9 rounded-lg" />
          <span className="font-display text-lg font-bold tracking-tight">
            RayFinder <span className="text-primary">Pro</span>
          </span>
        </Link>
        <nav className="flex items-center gap-5 text-sm text-muted-foreground">
          <Link to="/radar" className="hidden hover:text-foreground sm:inline">
            Radar web
          </Link>
          <Link to="/auth" className="hover:text-foreground">
            Compte
          </Link>
        </nav>
      </header>

      <main>
        <Hero />
        <HowItWorks />
        <Features />
        <Privacy />
        <Faq />
        <FinalCta />
      </main>

      <SiteFooter className="pb-10" />
    </div>
  );
}

/** Badge Google Play : actif une fois l'app publiée, « bientôt » d'ici là. */
function PlayBadge({ large = false }: { large?: boolean }) {
  const size = large ? "px-6 py-4 text-base" : "px-5 py-3 text-sm";
  if (PLAY_STORE_LIVE) {
    return (
      <a
        href={PLAY_STORE_URL}
        target="_blank"
        rel="noreferrer"
        className={`alert-surface inline-flex items-center gap-3 rounded-xl font-semibold ${size}`}
      >
        <Smartphone className="size-5" /> Télécharger sur Google Play
      </a>
    );
  }
  return (
    <span
      className={`inline-flex items-center gap-3 rounded-xl border border-primary/40 bg-primary/10 font-semibold text-primary ${size}`}
    >
      <Smartphone className="size-5" />
      Bientôt sur Google Play
    </span>
  );
}

function Hero() {
  return (
    <section className="mx-auto grid w-full max-w-6xl items-center gap-12 px-5 pt-14 pb-16 md:grid-cols-[1.1fr_0.9fr] md:pt-20">
      <div>
        <p className="text-xs font-semibold tracking-[0.18em] text-primary uppercase">
          Application Android · Bientôt disponible
        </p>
        <h1 className="mt-4 font-display text-4xl leading-[1.05] font-bold tracking-tight text-balance sm:text-5xl lg:text-6xl">
          Repérez les lunettes à caméra autour de vous.
        </h1>
        <p className="mt-5 max-w-xl text-lg text-pretty text-muted-foreground">
          Les lunettes connectées comme les Ray-Ban Meta peuvent filmer sans qu'on le remarque.{" "}
          {APP_NAME} écoute leurs signaux Bluetooth et vous prévient dès qu'une paire est à
          proximité.
        </p>
        <div className="mt-8 flex flex-wrap items-center gap-4">
          <PlayBadge large />
          <Link
            to="/radar"
            className="text-sm font-medium text-muted-foreground underline underline-offset-4 hover:text-foreground"
          >
            Essayer le radar web
          </Link>
        </div>
        <p className="mt-6 text-sm text-muted-foreground">
          Gratuit · Sans publicité · Le radar fonctionne sans compte
        </p>
      </div>
      <div className="relative mx-auto w-full max-w-sm">
        <div
          className="absolute -inset-10 -z-10 rounded-full opacity-60 blur-3xl"
          style={{ background: "radial-gradient(circle, rgb(240 68 56 / 0.35), transparent 65%)" }}
        />
        <PhoneShot src="/screens/app-1.webp" alt="Écran d'alerte : lunettes Meta très proches" />
      </div>
    </section>
  );
}

function PhoneShot({ src, alt }: { src: string; alt: string }) {
  return (
    <div className="overflow-hidden rounded-t-[2.2rem] [mask-image:linear-gradient(to_bottom,black_75%,transparent)]">
      <img src={src} alt={alt} width={615} height={1065} className="block w-full" loading="lazy" />
    </div>
  );
}

function SectionTitle({
  kicker,
  title,
  children,
}: {
  kicker: string;
  title: string;
  children?: ReactNode;
}) {
  return (
    <div className="max-w-2xl">
      <p className="text-xs font-semibold tracking-[0.18em] text-primary uppercase">{kicker}</p>
      <h2 className="mt-3 font-display text-3xl font-bold tracking-tight text-balance sm:text-4xl">
        {title}
      </h2>
      {children && <p className="mt-4 text-pretty text-muted-foreground">{children}</p>}
    </div>
  );
}

const STEPS = [
  {
    title: "Lancez le radar",
    text: "Un seul bouton. L'app scanne en continu les signaux Bluetooth autour de vous.",
  },
  {
    title: "Elle reconnaît les lunettes",
    text: "Les lunettes Meta émettent des identifiants Bluetooth officiels : l'app les repère, même quand leur nom est masqué.",
  },
  {
    title: "Vous êtes alerté",
    text: "Le téléphone vibre, l'écran passe au rouge et la distance est estimée : très proche, proche ou à portée.",
  },
];

function HowItWorks() {
  return (
    <section className="border-t border-border/60">
      <div className="mx-auto grid w-full max-w-6xl gap-12 px-5 py-20 md:grid-cols-[0.9fr_1.1fr] md:items-center">
        <div className="order-2 mx-auto w-full max-w-sm md:order-1">
          <PhoneShot
            src="/screens/app-2.webp"
            alt="Liste de trois appareils Meta avec leur distance estimée"
          />
        </div>
        <div className="order-1 md:order-2">
          <SectionTitle
            kicker="Comment ça marche"
            title="Trois secondes pour savoir si quelqu'un peut filmer."
          />
          <ol className="mt-10 space-y-8">
            {STEPS.map((step, i) => (
              <li key={step.title} className="flex gap-5">
                <span className="flex size-10 shrink-0 items-center justify-center rounded-full bg-primary/15 font-display font-bold text-primary">
                  {i + 1}
                </span>
                <div>
                  <h3 className="font-display text-lg font-bold">{step.title}</h3>
                  <p className="mt-1 text-muted-foreground">{step.text}</p>
                </div>
              </li>
            ))}
          </ol>
        </div>
      </div>
    </section>
  );
}

const FEATURES = [
  {
    icon: Vibrate,
    title: "Alerte immédiate",
    text: "Vibration et écran rouge dès qu'une paire apparaît.",
  },
  { icon: Gauge, title: "Distance estimée", text: "Moins de 2 m, quelques mètres ou à portée." },
  {
    icon: Bluetooth,
    title: "Mode diagnostic",
    text: "Affichez tous les appareils Bluetooth pour vérifier le scan.",
  },
  {
    icon: ShieldAlert,
    title: "Safe Zone",
    text: "Prévenez vos amis d'un geste quand des lunettes sont repérées.",
  },
  {
    icon: BarChart3,
    title: "Stats de votre ville",
    text: "Les détections partagées aujourd'hui, ville par ville.",
  },
  {
    icon: Radar,
    title: "Un compte, deux écrans",
    text: "Le même compte sur l'app et sur ce site.",
  },
];

function Features() {
  return (
    <section className="border-t border-border/60">
      <div className="mx-auto w-full max-w-6xl px-5 py-20">
        <SectionTitle kicker="Fonctionnalités" title="Seul ou en communauté.">
          Le radar suffit pour être prévenu. Avec un compte gratuit, partagez vos détections et
          alertez vos proches.
        </SectionTitle>
        <div className="mt-12 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {FEATURES.map(({ icon: Icon, title, text }) => (
            <div key={title} className="panel p-6">
              <Icon className="size-6 text-primary" />
              <h3 className="mt-4 font-display text-lg font-bold">{title}</h3>
              <p className="mt-1 text-sm text-muted-foreground">{text}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function Privacy() {
  const points = [
    "Le radar fonctionne sans compte : les signaux sont analysés sur votre téléphone.",
    "Rien n'est envoyé tant que vous ne partagez pas une détection.",
    "Aucune position GPS, aucune publicité, aucun traceur.",
    "Suppression du compte et de toutes vos données en un geste.",
  ];
  return (
    <section className="border-t border-border/60">
      <div className="mx-auto grid w-full max-w-6xl gap-10 px-5 py-20 md:grid-cols-2 md:items-center">
        <SectionTitle
          kicker="Vie privée"
          title="Une app pour protéger votre vie privée, pas pour la collecter."
        />
        <ul className="space-y-4">
          {points.map((p) => (
            <li key={p} className="flex gap-3">
              <Lock className="mt-0.5 size-5 shrink-0 text-primary" />
              <span>{p}</span>
            </li>
          ))}
          <li className="pl-8 text-sm">
            <Link to="/confidentialite" className="text-primary underline underline-offset-4">
              Lire la politique de confidentialité
            </Link>
          </li>
        </ul>
      </div>
    </section>
  );
}

const FAQ = [
  {
    q: "Quand l'app sera-t-elle disponible ?",
    a: "Elle est en cours de publication sur Google Play. Ce site sera mis à jour avec le lien de téléchargement dès sa sortie.",
  },
  {
    q: "Quelles lunettes sont détectées ?",
    a: "Les lunettes connectées Meta : Ray-Ban Meta et Oakley Meta. D'autres appareils Meta, comme les casques Quest, peuvent aussi apparaître.",
  },
  {
    q: "La détection est-elle fiable à 100 % ?",
    a: "Non. Elle repose sur les signaux Bluetooth que les lunettes émettent. Des lunettes éteintes, en veille profonde ou rangées dans leur étui peuvent ne rien émettre.",
  },
  {
    q: "Et sur iPhone ?",
    a: "L'app est disponible uniquement sur Android pour l'instant. Vous pouvez essayer le radar web, mais les navigateurs limitent fortement le scan Bluetooth.",
  },
  {
    q: "Faut-il créer un compte ?",
    a: "Non. Le compte, gratuit, sert seulement aux fonctions communautaires : partage, Safe Zone et statistiques.",
  },
];

function Faq() {
  return (
    <section className="border-t border-border/60">
      <div className="mx-auto w-full max-w-3xl px-5 py-20">
        <SectionTitle kicker="Questions fréquentes" title="Bon à savoir" />
        <div className="mt-10 divide-y divide-border border-y border-border">
          {FAQ.map(({ q, a }) => (
            <details key={q} className="group py-5">
              <summary className="flex cursor-pointer list-none items-center justify-between gap-4 font-display text-lg font-bold">
                {q}
                <span className="text-primary transition-transform group-open:rotate-45">+</span>
              </summary>
              <p className="mt-3 text-muted-foreground">{a}</p>
            </details>
          ))}
        </div>
        <p className="mt-6 text-xs text-muted-foreground">
          {APP_NAME} est un service indépendant, sans lien avec Meta Platforms, Inc. ni
          EssilorLuxottica. Ray-Ban, Oakley et Meta sont des marques de leurs propriétaires
          respectifs.
        </p>
      </div>
    </section>
  );
}

function FinalCta() {
  return (
    <section className="mx-auto w-full max-w-6xl px-5 pb-10">
      <div className="panel flex flex-col items-start gap-6 p-8 md:flex-row md:items-center md:justify-between md:p-12">
        <div>
          <h2 className="font-display text-3xl font-bold tracking-tight">
            Bientôt dans votre poche.
          </h2>
          <p className="mt-2 text-muted-foreground">
            En attendant, créez votre compte : il fonctionnera dans l'app dès sa sortie.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-4">
          <PlayBadge />
          <Link to="/auth" className="text-sm font-medium underline underline-offset-4">
            Créer mon compte
          </Link>
        </div>
      </div>
    </section>
  );
}
