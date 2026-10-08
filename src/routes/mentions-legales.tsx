import { createFileRoute, Link } from "@tanstack/react-router";
import { LegalPage } from "@/components/AppShell";
import { APP_NAME, CONTACT_EMAIL, LEGAL_UPDATED, PUBLISHER } from "@/lib/site";

export const Route = createFileRoute("/mentions-legales")({
  head: () => ({
    meta: [
      { title: "Mentions légales — RayFinder Pro" },
      {
        name: "description",
        content:
          "Mentions légales du site et de l'application RayFinder Pro, édités par SASU DataGrow.",
      },
    ],
  }),
  component: MentionsLegales,
});

function MentionsLegales() {
  return (
    <LegalPage title="Mentions légales" updated={LEGAL_UPDATED}>
      <h2>Éditeur</h2>
      <p>Le site et l'application Android {APP_NAME} sont édités par :</p>
      <p>
        <strong>{PUBLISHER.name}</strong>
        <br />
        SIRET : {PUBLISHER.siret}
        <br />
        {PUBLISHER.address}
        <br />
        Responsable de la publication : {PUBLISHER.director}
        <br />
        Contact : <a href={`mailto:${CONTACT_EMAIL}`}>{CONTACT_EMAIL}</a>
        <br />
        Site : <a href={PUBLISHER.website}>datagrow.me</a>
      </p>

      <h2>Hébergement</h2>
      <p>
        Le site et les données des comptes sont hébergés par <strong>Lovable Labs Sweden AB</strong>{" "}
        (service Lovable Cloud), Regeringsgatan 25, 111 53 Stockholm, Suède —{" "}
        <a href="https://lovable.dev">lovable.dev</a>. La base de données et l'authentification
        reposent sur l'infrastructure de <strong>Supabase, Inc.</strong>
      </p>
      <p>
        L'application Android est distribuée via <strong>Google Play</strong>, service fourni par
        Google Ireland Limited, Gordon House, Barrow Street, Dublin 4, Irlande.
      </p>

      <h2>Propriété intellectuelle</h2>
      <p>
        L'ensemble des contenus de {APP_NAME} (textes, images, logos, code source) est la propriété
        exclusive de {PUBLISHER.name} ou de leurs auteurs respectifs. Toute reproduction,
        représentation ou diffusion, en tout ou partie, sans autorisation préalable écrite, est
        interdite.
      </p>
      <p>
        {APP_NAME} est un service indépendant, sans lien avec Meta Platforms, Inc. ni
        EssilorLuxottica. Ray-Ban, Oakley et Meta sont des marques de leurs propriétaires
        respectifs, citées uniquement pour décrire les appareils détectés.
      </p>

      <h2>Limitation de responsabilité</h2>
      <p>
        La détection repose sur les signaux Bluetooth publics émis par les appareils environnants.
        Elle n'est pas infaillible : des lunettes éteintes, en veille ou rangées peuvent ne rien
        émettre, et d'autres appareils peuvent être signalés à tort. Le service est fourni « en
        l'état », sans garantie d'aucune sorte. {PUBLISHER.name} ne saurait être tenue responsable
        d'une absence de détection, d'une détection erronée, ni des décisions prises sur la base des
        informations affichées ou partagées par la communauté.
      </p>

      <h2>Données personnelles</h2>
      <p>
        Pour savoir quelles données sont traitées et comment exercer vos droits, consultez la{" "}
        <Link to="/confidentialite">politique de confidentialité</Link>.
      </p>

      <h2>Droit applicable</h2>
      <p>
        Les présentes mentions sont régies par le droit français. En cas de litige, et à défaut de
        résolution amiable, les tribunaux français seront seuls compétents.
      </p>
    </LegalPage>
  );
}
