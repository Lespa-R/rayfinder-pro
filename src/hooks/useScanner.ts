import { useCallback, useEffect, useRef, useState } from "react";
import {
  type Detection,
  looksLikeGlasses,
  makeDemoDetection,
  playAlertTone,
  rssiToDistance,
  vibrate,
} from "@/lib/radar";

type ScanMode = "idle" | "bluetooth" | "demo";

type LEScanEvent = {
  device: { id: string; name?: string | null };
  rssi: number;
  name?: string | null;
};

type BluetoothWithScan = {
  requestLEScan?: (opts: unknown) => Promise<{ stop: () => void }>;
  addEventListener: (type: string, cb: (e: Event) => void) => void;
  removeEventListener: (type: string, cb: (e: Event) => void) => void;
};

function getBluetooth(): BluetoothWithScan | null {
  if (typeof navigator === "undefined") return null;
  const bt = (navigator as unknown as { bluetooth?: BluetoothWithScan }).bluetooth;
  return bt ?? null;
}

export function useScanner(options: { soundOn: boolean }) {
  const { soundOn } = options;
  const [mode, setMode] = useState<ScanMode>("idle");
  const [detections, setDetections] = useState<Detection[]>([]);
  const [lastAlert, setLastAlert] = useState<Detection | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const stopRef = useRef<null | (() => void)>(null);
  const soundRef = useRef(soundOn);
  soundRef.current = soundOn;

  const push = useCallback((incoming: Detection) => {
    setDetections((prev) => {
      const existing = prev.find((d) => d.key === incoming.key);
      if (existing) {
        return prev.map((d) =>
          d.key === incoming.key
            ? { ...d, rssi: incoming.rssi, distance: incoming.distance, lastSeen: Date.now() }
            : d,
        );
      }
      setLastAlert(incoming);
      if (soundRef.current) playAlertTone();
      vibrate();
      return [incoming, ...prev].slice(0, 30);
    });
  }, []);

  const startDemo = useCallback(() => {
    setMode("demo");
    const tick = () => push(makeDemoDetection());
    const id = window.setInterval(tick, 6000);
    window.setTimeout(tick, 900);
    stopRef.current = () => window.clearInterval(id);
  }, [push]);

  const start = useCallback(async () => {
    const bt = getBluetooth();
    if (bt?.requestLEScan) {
      try {
        const scan = await Promise.race([
          bt.requestLEScan({ acceptAllAdvertisements: true, keepRepeatedDevices: true }),
          new Promise<never>((_, reject) =>
            window.setTimeout(() => reject(new Error("timeout")), 4000),
          ),
        ]);
        const handler = (event: Event) => {
          const adv = event as unknown as LEScanEvent;
          const name = adv.name ?? adv.device?.name ?? "";
          if (!looksLikeGlasses(name)) return;
          push({
            key: adv.device?.id ?? name,
            name: name || "Smart glasses",
            model: name.includes("Oakley") ? "Oakley Meta" : "Ray-Ban Meta",
            rssi: adv.rssi,
            distance: rssiToDistance(adv.rssi),
            angle: Math.floor(Math.random() * 360),
            firstSeen: Date.now(),
            lastSeen: Date.now(),
            source: "bluetooth",
          });
        };
        bt.addEventListener("advertisementreceived", handler);
        setMode("bluetooth");
        setNotice(null);
        stopRef.current = () => {
          bt.removeEventListener("advertisementreceived", handler);
          scan.stop();
        };
        return;
      } catch {
        setNotice(
          "Scan Bluetooth refusé ou indisponible sur cet appareil — passage en mode démonstration.",
        );
      }
    } else {
      setNotice(
        "Ce navigateur ne permet pas encore le scan Bluetooth continu — mode démonstration activé.",
      );
    }
    startDemo();
  }, [push, startDemo]);

  const stop = useCallback(() => {
    stopRef.current?.();
    stopRef.current = null;
    setMode("idle");
  }, []);

  useEffect(() => () => stopRef.current?.(), []);

  const clear = useCallback(() => {
    setDetections([]);
    setLastAlert(null);
  }, []);

  return { mode, detections, lastAlert, notice, start, stop, clear, dismissAlert: () => setLastAlert(null) };
}
