import type { Detection } from "@/lib/radar";

export function RadarScope({ detections, active }: { detections: Detection[]; active: boolean }) {
  return (
    <div className="relative mx-auto aspect-square w-full max-w-sm">
      <div className="absolute inset-0 rounded-full border border-border/70 bg-card/40" />
      {[0.72, 0.46, 0.22].map((s) => (
        <div
          key={s}
          className="absolute rounded-full border border-border/60"
          style={{
            inset: `${((1 - s) / 2) * 100}%`,
          }}
        />
      ))}
      <div className="absolute left-1/2 top-0 h-full w-px -translate-x-1/2 bg-border/50" />
      <div className="absolute top-1/2 left-0 h-px w-full -translate-y-1/2 bg-border/50" />

      {active && (
        <>
          <div className="absolute inset-0 overflow-hidden rounded-full">
            <div className="animate-sweep h-full w-full origin-center">
              <div
                className="h-1/2 w-1/2 origin-bottom-right"
                style={{
                  background:
                    "conic-gradient(from 180deg, color-mix(in oklab, var(--color-alert) 45%, transparent), transparent 70%)",
                }}
              />
            </div>
          </div>
          <div className="animate-ping-out absolute inset-0 rounded-full border border-primary/50" />
        </>
      )}

      {detections.slice(0, 8).map((d, i) => {
        const radius = Math.min(46, 6 + (d.distance / 40) * 42);
        const rad = (d.angle * Math.PI) / 180;
        const x = 50 + radius * Math.cos(rad);
        const y = 50 + radius * Math.sin(rad);
        return (
          <div
            key={d.key}
            className="absolute -translate-x-1/2 -translate-y-1/2"
            style={{ left: `${x}%`, top: `${y}%` }}
          >
            <span className="block size-3 rounded-full bg-primary shadow-[0_0_14px_var(--color-alert)]" />
            {i < 3 && (
              <span className="mt-1 block max-w-24 truncate text-[10px] text-muted-foreground">
                {d.distance} m
              </span>
            )}
          </div>
        );
      })}

      <div className="absolute inset-0 flex items-center justify-center">
        <span className="font-mono text-xs tracking-widest text-muted-foreground">
          {active ? `${detections.length} SIGNAL${detections.length > 1 ? "S" : ""}` : "EN VEILLE"}
        </span>
      </div>
    </div>
  );
}
