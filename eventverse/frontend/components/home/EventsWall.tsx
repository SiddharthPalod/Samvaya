"use client";

import Link from "next/link";
import { HandHeart, ArrowRight } from "lucide-react";
import { motion } from "framer-motion";
import { useMemo } from "react";
import { useTheme } from "@/context/ThemeContext";
import { EventSummary } from "./types";

type EventsWallProps = {
  events: EventSummary[];
  fallback: EventSummary[];
  placeholder: string;
};

export function EventsWall({ events, fallback, placeholder }: EventsWallProps) {
  const { theme } = useTheme();
  const isLight = theme === "light";
  const wall = events.length ? events : fallback.slice(0, 3);
  const dateFormatter = useMemo(
    () =>
      new Intl.DateTimeFormat("en-US", {
        month: "short",
        day: "numeric",
        timeZone: "UTC",
      }),
    []
  );

  return (
    <section className={`relative overflow-hidden ${
      isLight 
        ? " bg-gradient-to-br from-[#f0e4d5]/95 via-[#e8dcc9]/90 to-[#f0e4d5]/95 backdrop-blur-xl" 
        : " bg-gradient-to-br from-white/5 via-white/[0.02] to-transparent backdrop-blur-md"
    }`}>
      {/* Grain texture overlay */}
      <div className="absolute inset-0 opacity-[0.03] pointer-events-none grain-overlay" />
      
      <div className="p-6 sm:p-8 space-y-6 relative z-10">
        <div className="flex items-center justify-between">
          <div className={`flex items-center gap-3 text-sm font-semibold uppercase tracking-[0.2em] ${
            isLight ? "text-[#1f140d]" : "text-foreground"
          }`}>
            <HandHeart className={`h-5 w-5 ${isLight ? "text-[#d27b45]" : "text-[var(--accent-1)]"}`} />
            Hyperlocal impact events
          </div>
          <Link
            href="/events"
            className={`group flex items-center gap-2 text-sm font-semibold uppercase tracking-[0.14em] transition-all ${
              isLight 
                ? "text-[#7b6757] hover:text-[#1f140d]" 
                : "text-muted hover:text-foreground"
            }`}
          >
            Explore all
            <ArrowRight className="h-3.5 w-3.5 transition-transform group-hover:translate-x-1" />
          </Link>
        </div>

        <div className="overflow-x-auto pb-2 -mx-2 px-2">
          <div className="flex gap-5 sm:gap-6 min-w-full">
            {wall.map((event, idx) => (
              <Link 
                key={event.id || idx} 
                href={`/events/${event.id}`} 
                className="shrink-0 w-[300px] sm:w-[340px] md:w-[380px] group"
              >
                <motion.div
                  initial={{ opacity: 0, y: 30, scale: 0.95 }}
                  whileInView={{ opacity: 1, y: 0, scale: 1 }}
                  viewport={{ once: true, amount: 0.3 }}
                  transition={{ duration: 0.5, delay: idx * 0.08, ease: [0.16, 1, 0.3, 1] }}
                  whileHover={{ y: -8, scale: 1.02 }}
                  className={`relative h-full rounded-3xl overflow-hidden transition-all duration-500 ${
                    isLight
                      ? "bg-gradient-to-br from-[#f7f1e9] via-[#f0e4d5] to-[#e8dcc9] border border-black/10 shadow-lg shadow-black/10"
                      : "bg-gradient-to-br from-white/[0.08] via-white/[0.04] to-transparent border border-white/15 shadow-smooth"
                  }`}
                >
                  {/* Animated gradient border effect */}
                  <div className={`absolute inset-0 rounded-3xl opacity-0 group-hover:opacity-100 transition-opacity duration-500 ${
                    isLight
                      ? "bg-gradient-to-br from-[#d27b45]/20 via-[#f0c48a]/15 to-[#8a5a3c]/20"
                      : "bg-gradient-to-br from-[#d27b45]/30 via-[#f0c48a]/20 to-[#8a5a3c]/25"
                  }`} style={{ padding: '1px' }}>
                    <div className={`h-full w-full rounded-3xl ${
                      isLight ? "bg-gradient-to-br from-[#f7f1e9] via-[#f0e4d5] to-[#e8dcc9]" : "bg-[rgba(28,16,10,0.85)]"
                    }`} />
                  </div>

                  {/* Image container with overlay */}
                  <div className="relative h-48 sm:h-52 md:h-56 overflow-hidden">
                    <motion.img
                      src={event.imageUrl || placeholder}
                      alt={event.title}
                      className="absolute inset-0 h-full w-full object-cover"
                      whileHover={{ scale: 1.1 }}
                      transition={{ duration: 0.6, ease: "easeOut" }}
                    />
                    {/* Dynamic gradient overlay */}
                    <div className={`absolute inset-0 bg-gradient-to-t ${
                      isLight
                        ? "from-black/60 via-black/20 to-transparent"
                        : "from-black/80 via-black/40 to-black/10"
                    }`} />
                    
                    {/* Category badge */}
                    <div className="absolute top-4 left-4 z-10">
                      <motion.div
                        initial={{ opacity: 0, x: -10 }}
                        whileInView={{ opacity: 1, x: 0 }}
                        viewport={{ once: true }}
                        transition={{ delay: idx * 0.08 + 0.2 }}
                        className={`px-3 py-1.5 rounded-full backdrop-blur-md text-xs font-bold uppercase tracking-[0.15em] ${
                          isLight
                            ? "bg-[#f0e4d5]/95 text-[#1f140d] border border-black/10"
                            : "bg-white/10 text-foreground border border-white/20"
                        }`}
                      >
                        {event.category || "Record"}
                      </motion.div>
                    </div>

                    {/* Subtle texture overlay */}
                    <div className="absolute inset-0 opacity-[0.15] grain-overlay mix-blend-overlay" />
                  </div>

                  {/* Content section */}
                  <div className={`relative p-5 sm:p-6 space-y-3 ${
                    isLight ? "bg-gradient-to-br from-[#f7f1e9] via-[#f0e4d5] to-[#e8dcc9]" : "bg-[rgba(28,16,10,0.85)]"
                  }`}>
                    <motion.h3 
                      className={`text-lg sm:text-xl font-bold leading-tight line-clamp-2 transition-colors ${
                        isLight ? "text-[#1f140d]" : "text-foreground"
                      }`}
                      initial={{ opacity: 0 }}
                      whileInView={{ opacity: 1 }}
                      viewport={{ once: true }}
                      transition={{ delay: idx * 0.08 + 0.3 }}
                    >
                      {event.title}
                    </motion.h3>
                    
                    <div className={`flex items-center justify-between text-xs font-semibold uppercase tracking-[0.18em] pt-2 border-t ${
                      isLight 
                        ? "border-black/10 text-[#7b6757]" 
                        : "border-white/10 text-muted"
                    }`}>
                      <motion.span
                        initial={{ opacity: 0 }}
                        whileInView={{ opacity: 1 }}
                        viewport={{ once: true }}
                        transition={{ delay: idx * 0.08 + 0.35 }}
                      >
                        {event.city || "Latin America"}
                      </motion.span>
                      <motion.span 
                        suppressHydrationWarning
                        initial={{ opacity: 0 }}
                        whileInView={{ opacity: 1 }}
                        viewport={{ once: true }}
                        transition={{ delay: idx * 0.08 + 0.4 }}
                        className={`px-3 py-1 rounded-full ${
                          isLight
                            ? "bg-[#f0c48a]/30 text-[#8a5a3c]"
                            : "bg-[#d27b45]/20 text-[#f0c48a]"
                        }`}
                      >
                        {event.time ? dateFormatter.format(new Date(event.time)).toUpperCase() : "TBA"}
                      </motion.span>
                    </div>

                    {/* Hover indicator */}
                    <motion.div
                      className={`absolute bottom-4 right-4 w-8 h-8 rounded-full flex items-center justify-center transition-all ${
                        isLight
                          ? "bg-[#d27b45]/10 group-hover:bg-[#d27b45]/20"
                          : "bg-white/5 group-hover:bg-white/10"
                      }`}
                      whileHover={{ scale: 1.1, rotate: 45 }}
                    >
                      <ArrowRight className={`h-4 w-4 ${
                        isLight ? "text-[#d27b45]" : "text-[#f0c48a]"
                      }`} />
                    </motion.div>
                  </div>

                  {/* Shine effect on hover */}
                  <motion.div
                    className="absolute inset-0 opacity-0 group-hover:opacity-100 pointer-events-none"
                    initial={false}
                    animate={{
                      background: [
                        "linear-gradient(120deg, transparent 0%, transparent 40%, rgba(255,255,255,0.1) 50%, transparent 60%, transparent 100%)",
                        "linear-gradient(120deg, transparent 0%, transparent 40%, rgba(255,255,255,0.1) 50%, transparent 60%, transparent 100%)",
                      ],
                    }}
                    transition={{
                      duration: 1.5,
                      repeat: Infinity,
                      repeatDelay: 2,
                    }}
                    style={{
                      backgroundPosition: "200% 0",
                      backgroundSize: "200% 100%",
                    }}
                  />
                </motion.div>
              </Link>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

