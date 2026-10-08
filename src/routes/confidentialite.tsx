import { createFileRoute, Link } from "@tanstack/react-router";
import { LegalPage } from "@/components/AppShell";
import { APP_NAME, CONTACT_EMAIL, LEGAL_UPDATED, PUBLISHER } from "@/lib/site";

export const Route = createFileRoute("/confidentialite")({
  head: () => ({
    meta: [
      { title: "Politique de confidentialité — RayFinder Pro" },
      {
        name: "description",
        content:
          "Données traitées par RayFinder Pro (site et app Android) : radar local sans compte, données de compte uniquement si vous en créez un.",
      },
    ],
  }),
  component: Confidentialite,
});

function Confidentialite() {
  const mail = <a href={`mailto:${CONTACT_EMAIL}`}>{CONTACT_EMAIL}</a>;
  return (
    <LegalPage title="Politique de confidentialité" updated={LEGAL_UPDATED}>
      <p>
        {APP_NAME} (le site et l'application Android) détecte les lunettes connectées équipées d'une
        caméra à proximité. Cette politique explique quelles données sont traitées, pourquoi, et
        comment exercer vos droits.
      </p>

      <h2>Responsable du traitement</h2>
      <p>
        {PUBLISHER.name}, {PUBLISHER.address} — SIRET {PUBLISHER.siret}. Contact : {mail}.
      </p>

      <h2>Le radar fonctionne sans compte et sans rien envoyer</h2>
      <p>
        Pour détecter les lunettes, {APP_NAME} lit les signaux Bluetooth publics que les appareils
        proches diffusent (identifiant du fabricant, nom de l'appareil, puissance du signal). Ces
        informations sont analysées sur votre téléphone et ne quittent pas l'appareil, sauf si vous
        choisissez de partager une détection (voir ci-dessous). Dans l'app Android, les appareils
        non revus depuis 30 secondes sont oubliés.
      </p>

      <h2>Données traitées si vous créez un compte</h2>
      <p>Le compte est facultatif. Il sert uniquement aux fonctions communautaires :</p>
      <ul>
        <li>
          <strong>Compte</strong> : adresse e-mail, mot de passe (stocké chiffré), pseudo et, si
          vous la renseignez, votre ville. Si vous vous connectez avec Google, nous recevons votre
          adresse e-mail et votre nom.
        </li>
        <li>
          <strong>Détections partagées</strong> (uniquement quand vous appuyez sur « Partager ») :
          nom et identifiant Bluetooth de l'appareil détecté, puissance du signal, distance estimée,
          ville et date. Aucune position GPS n'est collectée.
        </li>
        <li>
          <strong>Safe Zone</strong> : la liste des amis que vous ajoutez et les alertes que vous
          envoyez (message, ville, date).
        </li>
      </ul>
      <p>
        <strong>Finalités et base légale</strong> : fournir les fonctions que vous utilisez
        (exécution du service) — partager une détection, alerter vos amis, calculer les statistiques
        du jour par ville. Les statistiques publiées sont agrégées par ville : aucune autre personne
        ne voit vos détections individuelles. Votre pseudo et votre ville sont visibles des autres
        utilisateurs connectés, pour qu'ils puissent vous ajouter en ami.
      </p>
      <p>
        <strong>Ce que nous ne faisons pas</strong> : pas de publicité, pas de revente ni de partage
        de données à des fins commerciales, pas d'outil de mesure d'audience ni de traceur
        publicitaire.
      </p>

      <h2>Hébergement et sous-traitants</h2>
      <p>
        Les données des comptes sont hébergées par Lovable Labs Sweden AB (Lovable Cloud) sur
        l'infrastructure de Supabase, Inc. Ces prestataires peuvent traiter des données hors de
        l'Union européenne, notamment aux États-Unis, avec les garanties prévues par le RGPD
        (clauses contractuelles types). La liste de leurs sous-traitants est publiée sur{" "}
        <a href="https://trust.lovable.dev">trust.lovable.dev</a>.
      </p>

      <h2>Durée de conservation</h2>
      <p>
        Les données de compte, détections partagées, amis et alertes sont conservés tant que le
        compte existe. Ils sont supprimés définitivement quand vous supprimez votre compte.
      </p>

      <h2>Stockage local</h2>
      <p>
        Le site enregistre dans votre navigateur votre session de connexion et la ville saisie.
        L'app Android enregistre votre session sur le téléphone. Ces données sont strictement
        nécessaires au fonctionnement et ne servent pas au suivi.
      </p>

      <h2>Permissions de l'app Android</h2>
      <ul>
        <li>
          <strong>Appareils à proximité</strong> (Android 12 et plus) : scanner le Bluetooth. Elle
          est déclarée avec l'option qui interdit d'en déduire votre position.
        </li>
        <li>
          <strong>Localisation</strong> (Android 11 et moins uniquement) : exigée par ces versions
          d'Android pour tout scan Bluetooth. L'app ne lit jamais votre position.
        </li>
        <li>
          <strong>Internet</strong> : uniquement pour les fonctions de compte (connexion, partage,
          Safe Zone, statistiques).
        </li>
        <li>
          <strong>Vibreur</strong> : vous alerter lors d'une détection.
        </li>
      </ul>

      <h2>Vos droits</h2>
      <p>
        Vous disposez d'un droit d'accès, de rectification, d'effacement, de limitation,
        d'opposition et de portabilité. Vous pouvez supprimer votre compte et toutes vos données
        vous-même, depuis l'app ou <Link to="/suppression-compte">depuis le site</Link>. Pour toute
        autre demande, écrivez à {mail}. Vous pouvez aussi saisir la CNIL (
        <a href="https://www.cnil.fr">cnil.fr</a>).
      </p>

      <h2>Modifications</h2>
      <p>
        En cas de changement, la nouvelle version sera publiée sur cette page avec sa date de mise à
        jour.
      </p>
    </LegalPage>
  );
}
