import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { toast } from "sonner";
import { LegalPage } from "@/components/AppShell";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/hooks/useAuth";
import { supabase } from "@/integrations/supabase/client";
import { CONTACT_EMAIL, LEGAL_UPDATED } from "@/lib/site";

export const Route = createFileRoute("/suppression-compte")({
  head: () => ({
    meta: [
      { title: "Supprimer mon compte — RayFinder Pro" },
      {
        name: "description",
        content: "Comment supprimer votre compte RayFinder Pro et toutes les données associées.",
      },
    ],
  }),
  component: SuppressionCompte,
});

function SuppressionCompte() {
  const { user, loading } = useAuth();
  const navigate = useNavigate();
  const [confirming, setConfirming] = useState(false);
  const [busy, setBusy] = useState(false);

  async function deleteAccount() {
    setBusy(true);
    const { error } = await supabase.rpc("delete_my_account");
    setBusy(false);
    if (error) {
      toast.error("Suppression impossible", { description: `Écrivez-nous à ${CONTACT_EMAIL}.` });
      return;
    }
    await supabase.auth.signOut();
    toast.success("Votre compte et vos données ont été supprimés");
    navigate({ to: "/" });
  }

  return (
    <LegalPage title="Supprimer mon compte" updated={LEGAL_UPDATED}>
      <p>
        Vous pouvez supprimer à tout moment votre compte RayFinder Pro. La suppression est immédiate
        et définitive. Elle efface :
      </p>
      <ul>
        <li>votre compte (adresse e-mail, mot de passe, pseudo, ville) ;</li>
        <li>toutes les détections que vous avez partagées ;</li>
        <li>votre liste d'amis Safe Zone, et votre présence dans celle des autres ;</li>
        <li>toutes les alertes Safe Zone que vous avez envoyées.</li>
      </ul>
      <p>Aucune donnée n'est conservée après la suppression.</p>

      <h2>Depuis l'app Android</h2>
      <p>Onglet « Compte » › « Supprimer mon compte », puis confirmez.</p>

      <h2>Depuis ce site</h2>
      {loading ? (
        <p>Chargement…</p>
      ) : user ? (
        <div className="panel space-y-3 p-4 not-prose">
          <p className="text-sm">
            Connecté en tant que <strong>{user.email}</strong>.
          </p>
          {confirming ? (
            <div className="flex flex-wrap gap-2">
              <Button variant="destructive" disabled={busy} onClick={() => void deleteAccount()}>
                Oui, supprimer définitivement
              </Button>
              <Button variant="outline" onClick={() => setConfirming(false)}>
                Annuler
              </Button>
            </div>
          ) : (
            <Button variant="destructive" onClick={() => setConfirming(true)}>
              Supprimer mon compte
            </Button>
          )}
        </div>
      ) : (
        <p>
          <Link to="/auth">Connectez-vous</Link>, puis revenez sur cette page pour supprimer votre
          compte.
        </p>
      )}

      <h2>Sans accès à votre compte</h2>
      <p>
        Écrivez à <a href={`mailto:${CONTACT_EMAIL}`}>{CONTACT_EMAIL}</a> depuis l'adresse du
        compte, avec l'objet « Suppression de compte ». La demande est traitée sous 30 jours.
      </p>
    </LegalPage>
  );
}
