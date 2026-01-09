import Link from "next/link";
import { ArrowRight, Sparkles } from "lucide-react";
import { motion } from "framer-motion";
import { useEffect, useState } from "react";
import { useTheme } from "@/context/ThemeContext";

const drops = [
  {
    id: 1,
    title: "Neighborhood food drive at Community Park",
    subtitle: "Volunteers and local NGOs team up to serve over 500 meals",
    tag: "Food drive",
    image:
      "https://images.unsplash.com/photo-1593113630400-ea4288922497?w=600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MjB8fGZvb2QlMjBkb25hdGlvbnxlbnwwfHwwfHx8MA%3D%3D",
      accentLight: "#b8e0e4",
      accentDark: "#0d2df3",
  },
  {
    id: 2,
    title: "City blood donation camp with RedCross partners",
    subtitle: "Donors, coordinators, and hospitals connect for urgent needs",
    tag: "Blood donation",
    image:
      "https://plus.unsplash.com/premium_photo-1723114861354-64b3f6a079e9?w=600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8ODZ8fGJsb29kJTIwZG9uYXRpb258ZW58MHx8MHx8fDA%3D",
    accentLight: "#ff6f7a",
    accentDark: "#d94b57",
  },
  {
    id: 3,
    title: "NGO volunteering & community town hall series",
    subtitle: "Citizens, NGOs, and civic leaders co-create neighborhood solutions",
    tag: "Volunteering",
    image:
      "https://images.unsplash.com/photo-1489515217757-5fd1be406fef?auto=format&fit=crop&w=1600&q=80",
    accentLight: "#e5d6b8",
    accentDark: "#b79c6e",
  },
];

export function FollowCTA() {
  const { theme } = useTheme();
  const isDark = theme === "dark";
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  return (
    <section className="relative overflow-hidden border border-white/10 bg-linear-to-br from-white/5 via-white/0 to-transparent">
      <div className="relative py-6 sm:py-8 space-y-6">
        <div className="flex items-center justify-between gap-4">
          <div className="space-y-2">
            <p className="text-xs px-6 sm:px-8 uppercase tracking-[0.22em] text-muted flex items-center gap-2">
              <Sparkles className="h-4 w-4" />
              Hyperlocal impact near you
            </p>
          </div>
        </div>

        <div className="space-y-0">
          {drops.map((drop, idx) => (
            <motion.div
              key={drop.id}
              initial={false}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true, amount: 0.35 }}
              transition={{ delay: idx * 0.05, duration: 0.45, ease: "easeOut" }}
              className="group relative overflow-hidden border-2 border-zinc-400/10 bg-transparent"
              style={{
                backgroundColor: "transparent",
              }}
            >
              <motion.div
                initial={false}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true, amount: 0.4 }}
                transition={{ delay: idx * 0.05 + 0.05, duration: 0.45, ease: "easeOut" }}
                className="relative overflow-hidden flex flex-col md:flex-row h-full transition-colors duration-300 bg-transparent group-hover:text-black dark:group-hover:text-white"
              >
                <div
                  className="absolute inset-0 overflow-hidden"
                  aria-hidden
                >
                  <div
                    className="absolute inset-0 origin-left scale-x-0 group-hover:scale-x-100 transition-transform duration-700 ease-in-out"
                    style={{
                      backgroundColor: mounted && isDark ? drop.accentDark : drop.accentLight,
                    }}
                  />
                  <div
                    className="absolute inset-0 origin-left scale-x-0 group-hover:scale-x-100 transition-transform duration-700 ease-in-out"
                    style={{
                      backgroundColor: mounted && isDark ? drop.accentDark : drop.accentLight,
                      opacity: 0.08,
                      filter: "blur(28px)",
                    }}
                  />
                </div>
                <div className="absolute -left-24 -top-24 h-56 w-56 rounded-full opacity-0 blur-3xl mix-blend-screen bg-white transition-opacity duration-500" />
                
                <div className="relative flex-1 p-5 pr-0 sm:p-7 sm:pr-2 md:pr-3 flex flex-col justify-between gap-4 z-10">
                  <div className="flex items-center justify-between">
                    <span className="pill px-3 py-1 rounded-full text-[11px] font-semibold uppercase tracking-[0.18em] bg-white text-foreground">
                      0{idx + 1}.
                    </span>
                  </div>

                  <div className="space-y-2">
                    <h5 className="text-xl sm:text-2xl font-extrabold w-1/2 leading-6 uppercase text-foreground transition-transform duration-300 group-hover:translate-y-[-2px]">
                      {drop.title}
                    </h5>
                    <p className="text-sm sm:text-base font-semibold text-foreground/80 leading-snug opacity-0 translate-y-2 group-hover:opacity-100 group-hover:translate-y-0 transition-all duration-300">
                      {drop.subtitle}
                    </p>
                    <div className="inline-flex items-center gap-2 text-xs uppercase tracking-[0.16em] text-foreground/80 opacity-0 translate-y-2 group-hover:opacity-100 group-hover:translate-y-0 transition-all duration-300">
                      View more <ArrowRight className="h-4 w-4" />
                    </div>
                  </div>
                </div>

                <div className="relative h-48 md:h-auto md:w-[28%] pt-4 pb-4 pr-16 md:pt-6 md:pb-6 md:pr-16 md:-ml-8 opacity-0 group-hover:opacity-100 translate-y-8 -rotate-12 group-hover:translate-y-0 group-hover:rotate-0 transition-all duration-700 ease-out z-10">
                  <div className="relative h-full w-full overflow-hidden rounded-lg">
                    <img
                      src={drop.image}
                      alt={drop.title}
                      className="absolute inset-0 h-full w-full object-cover transition-transform duration-500 group-hover:scale-105 group-hover:brightness-110"
                    />
                    <div className="absolute inset-0 bg-linear-to-t from-black/50 via-transparent to-transparent opacity-0 group-hover:opacity-80 transition-opacity duration-500" />
                  </div>
                </div>

              </motion.div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}

