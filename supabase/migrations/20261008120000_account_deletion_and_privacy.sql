-- Suppression de compte en libre-service (exigence Google Play) :
-- supprimer auth.users efface en cascade profiles, sightings, friends et safe_zone_alerts.
CREATE OR REPLACE FUNCTION public.delete_my_account()
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  IF auth.uid() IS NULL THEN
    RAISE EXCEPTION 'not authenticated';
  END IF;
  DELETE FROM auth.users WHERE id = auth.uid();
END;
$$;
REVOKE ALL ON FUNCTION public.delete_my_account() FROM PUBLIC, anon;
GRANT EXECUTE ON FUNCTION public.delete_my_account() TO authenticated;

-- Confidentialité : chacun ne lit que ses propres détections.
-- Les chiffres communautaires passent par city_stats_today() (agrégés par ville).
DROP POLICY IF EXISTS "sightings_select_own_or_shared" ON public.sightings;
DROP POLICY IF EXISTS "sightings_select_own" ON public.sightings;
CREATE POLICY "sightings_select_own" ON public.sightings
  FOR SELECT TO authenticated USING (auth.uid() = user_id);
