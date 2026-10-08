export type Detection = {
  key: string;
  name: string;
  model: string;
  rssi: number;
  distance: number;
  angle: number;
  firstSeen: number;
  lastSeen: number;
  source: "bluetooth" | "demo";
};

const GLASSES_PATTERNS = [
  "ray-ban",
  "rayban",
  "meta",
  "wayfarer",
  "headliner",
  "skyler",
  "oakley",
  "halo",
  "smart glasses",
];

export function looksLikeGlasses(name: string | undefined | null) {
  if (!name) return false;
  const lower = name.toLowerCase();
  return GLASSES_PATTERNS.some((p) => lower.includes(p));
}

export function rssiToDistance(rssi: number) {
  // Simple log-distance path loss model (txPower -59 dBm at 1m).
  const value = Math.pow(10, (-59 - rssi) / 20);
  return Math.max(0.5, Math.min(60, Number(value.toFixed(1))));
}

export function signalLabel(rssi: number) {
  if (rssi > -55) return "Très proche";
  if (rssi > -70) return "Proche";
  if (rssi > -85) return "À portée";
  return "Lointain";
}

const DEMO_DEVICES = [
  { name: "Ray-Ban Meta Wayfarer", model: "Wayfarer Gen 2" },
  { name: "Ray-Ban Meta Headliner", model: "Headliner" },
  { name: "Meta Glasses 4F2A", model: "Inconnu" },
  { name: "Oakley Meta HSTN", model: "HSTN" },
  { name: "RayBan Skyler B7", model: "Skyler" },
];

export function makeDemoDetection(): Detection {
  const pick = DEMO_DEVICES[Math.floor(Math.random() * DEMO_DEVICES.length)]!;
  const rssi = -40 - Math.floor(Math.random() * 55);
  const now = Date.now();
  return {
    key: `${pick.name}-${Math.floor(Math.random() * 9999)}`,
    name: pick.name,
    model: pick.model,
    rssi,
    distance: rssiToDistance(rssi),
    angle: Math.floor(Math.random() * 360),
    firstSeen: now,
    lastSeen: now,
    source: "demo",
  };
}

let audioCtx: AudioContext | null = null;

export function playAlertTone() {
  if (typeof window === "undefined") return;
  try {
    const Ctor =
      window.AudioContext ??
      (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
    if (!Ctor) return;
    audioCtx = audioCtx ?? new Ctor();
    void audioCtx.resume();
    const now = audioCtx.currentTime;
    [880, 1180].forEach((freq, i) => {
      const osc = audioCtx!.createOscillator();
      const gain = audioCtx!.createGain();
      osc.type = "triangle";
      osc.frequency.value = freq;
      gain.gain.setValueAtTime(0.0001, now + i * 0.16);
      gain.gain.exponentialRampToValueAtTime(0.12, now + i * 0.16 + 0.02);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + i * 0.16 + 0.14);
      osc.connect(gain).connect(audioCtx!.destination);
      osc.start(now + i * 0.16);
      osc.stop(now + i * 0.16 + 0.18);
    });
  } catch {
    /* audio unavailable */
  }
}

export function vibrate() {
  if (typeof navigator !== "undefined" && "vibrate" in navigator) {
    navigator.vibrate?.([60, 40, 90]);
  }
}
