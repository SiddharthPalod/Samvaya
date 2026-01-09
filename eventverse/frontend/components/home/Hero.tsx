import { motion } from "framer-motion";
import { Sparkles } from "lucide-react";

export type Timezone = {
  genre?: string;
  eventName?: string;
  city?: string;
  // legacy props kept for backwards compatibility
  time?: string;
  zone?: string;
};

type HeroProps = {
  heroVideo: string;
  timezones: Timezone[];
};

export function Hero({ heroVideo, timezones }: HeroProps) {
  return (
    <section className="hero-video shadow-smooth min-h-screen">
      <video src={heroVideo} autoPlay loop muted playsInline />
      <div className="hero-overlay" />
      <div className="grain-overlay" />

      <div className="absolute inset-0 z-10 flex flex-col justify-end gap-5 p-6 sm:p-10 text-white">
        <motion.div
          initial={{ opacity: 0, scale: 0.96 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.8, ease: "easeOut" }}
          className="text-center self-center space-y-3"
        >
          <p className="logo-wordmark text-5xl sm:text-7xl md:text-8xl text-white drop-shadow-[0_10px_25px_rgba(0,0,0,0.55)]">
            SAMVAYA
          </p>
          <p className="max-w-2xl mx-auto text-xs sm:text-sm md:text-base font-medium tracking-[0.18em] uppercase text-white/80">
            Connecting volunteers, NGOs, and citizens
          </p>
        </motion.div>

        <div className="flex flex-col gap-2">
          <div className="grid sm:grid-cols-5 bg-black/10 gap-2 text-left text-[11px] uppercase tracking-widest">
            {timezones.map((t, idx) => {
              const genre = t.genre ?? t.city ?? "Event";
              const eventName = t.eventName ?? t.time ?? "Upcoming";
              const city = t.city ?? t.zone ?? "—";
              return (
                <div key={`${genre}-${eventName}-${idx}`} className="px-2.5 py-2 text-white">
                  <p className="text-white/70 text-[10px]">{genre}</p>
                  <p className="text-sm font-semibold text-white drop-shadow-sm">{eventName}</p>
                  <p className="text-white/60 text-[10px]">{city}</p>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
}

