
CREATE TABLE public.profiles (
  id UUID PRIMARY KEY REFERENCES auth.users ON DELETE CASCADE,
  display_name TEXT NOT NULL DEFAULT 'Agent',
  city TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
GRANT SELECT, INSERT, UPDATE, DELETE ON public.profiles TO authenticated;
GRANT ALL ON public.profiles TO service_role;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
CREATE POLICY "profiles_select_auth" ON public.profiles FOR SELECT TO authenticated USING (true);
CREATE POLICY "profiles_insert_own" ON public.profiles FOR INSERT TO authenticated WITH CHECK (auth.uid() = id);
CREATE POLICY "profiles_update_own" ON public.profiles FOR UPDATE TO authenticated USING (auth.uid() = id) WITH CHECK (auth.uid() = id);

CREATE TABLE public.sightings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES auth.users ON DELETE CASCADE,
  device_name TEXT NOT NULL,
  device_key TEXT NOT NULL,
  model TEXT,
  rssi INTEGER,
  distance_m NUMERIC,
  city TEXT,
  lat NUMERIC,
  lng NUMERIC,
  shared BOOLEAN NOT NULL DEFAULT false,
  note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX sightings_created_at_idx ON public.sightings (created_at DESC);
CREATE INDEX sightings_city_idx ON public.sightings (city);
GRANT SELECT, INSERT, UPDATE, DELETE ON public.sightings TO authenticated;
GRANT ALL ON public.sightings TO service_role;
ALTER TABLE public.sightings ENABLE ROW LEVEL SECURITY;
CREATE POLICY "sightings_select_own_or_shared" ON public.sightings FOR SELECT TO authenticated USING (auth.uid() = user_id OR shared = true);
CREATE POLICY "sightings_insert_own" ON public.sightings FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
CREATE POLICY "sightings_update_own" ON public.sightings FOR UPDATE TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "sightings_delete_own" ON public.sightings FOR DELETE TO authenticated USING (auth.uid() = user_id);

CREATE TABLE public.friends (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES auth.users ON DELETE CASCADE,
  friend_id UUID NOT NULL REFERENCES auth.users ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, friend_id)
);
GRANT SELECT, INSERT, UPDATE, DELETE ON public.friends TO authenticated;
GRANT ALL ON public.friends TO service_role;
ALTER TABLE public.friends ENABLE ROW LEVEL SECURITY;
CREATE POLICY "friends_select_involved" ON public.friends FOR SELECT TO authenticated USING (auth.uid() = user_id OR auth.uid() = friend_id);
CREATE POLICY "friends_insert_own" ON public.friends FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
CREATE POLICY "friends_delete_own" ON public.friends FOR DELETE TO authenticated USING (auth.uid() = user_id);

CREATE TABLE public.safe_zone_alerts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES auth.users ON DELETE CASCADE,
  message TEXT NOT NULL DEFAULT 'Smart glasses détectées à proximité',
  city TEXT,
  lat NUMERIC,
  lng NUMERIC,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX safe_zone_alerts_created_at_idx ON public.safe_zone_alerts (created_at DESC);
GRANT SELECT, INSERT, UPDATE, DELETE ON public.safe_zone_alerts TO authenticated;
GRANT ALL ON public.safe_zone_alerts TO service_role;
ALTER TABLE public.safe_zone_alerts ENABLE ROW LEVEL SECURITY;
CREATE POLICY "safe_zone_select_friends" ON public.safe_zone_alerts FOR SELECT TO authenticated USING (
  auth.uid() = user_id OR EXISTS (
    SELECT 1 FROM public.friends f WHERE f.user_id = safe_zone_alerts.user_id AND f.friend_id = auth.uid()
  )
);
CREATE POLICY "safe_zone_insert_own" ON public.safe_zone_alerts FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  INSERT INTO public.profiles (id, display_name)
  VALUES (NEW.id, COALESCE(NEW.raw_user_meta_data ->> 'display_name', split_part(NEW.email, '@', 1), 'Agent'))
  ON CONFLICT (id) DO NOTHING;
  RETURN NEW;
END;
$$;
CREATE TRIGGER on_auth_user_created
AFTER INSERT ON auth.users
FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

CREATE OR REPLACE FUNCTION public.city_stats_today()
RETURNS TABLE (city TEXT, detections BIGINT)
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT COALESCE(s.city, 'Inconnue') AS city, count(*) AS detections
  FROM public.sightings s
  WHERE s.shared = true AND s.created_at >= date_trunc('day', now())
  GROUP BY 1
  ORDER BY 2 DESC
  LIMIT 20;
$$;
GRANT EXECUTE ON FUNCTION public.city_stats_today() TO anon, authenticated;

ALTER PUBLICATION supabase_realtime ADD TABLE public.safe_zone_alerts;
ALTER PUBLICATION supabase_realtime ADD TABLE public.sightings;
