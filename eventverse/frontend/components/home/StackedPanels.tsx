import { motion, useScroll, useTransform } from "framer-motion";
import { useRef } from "react";

const heroPanels = [
  {
    id: 1,
    image: "https://images.unsplash.com/photo-1717163059480-5594e1dbe10b?w=600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NTF8fGZvb2QlMjB0cnVja3xlbnwwfHwwfHx8MA%3D%3D",
    overlay: "from-[#92400e]/70 via-[#fde68a]/2 to-[#92400e]/90",
    texts: [
      { value: "Volunteers in motion", top: "20vh", left: "10%" },
      { value: "NGOs, citizens, and causes aligned", top: "40vh", left: "60%" },
      { value: "Food drives & blood camps", top: "16%", left: "78%", small: true },
    ],
  },
  // {
  //   id: 2,
  //   image: "https://plus.unsplash.com/premium_photo-1710010209274-2c2266291da2?w=600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHx0b3BpYy1mZWVkfDEwN3xibzhqUUtUYUUwWXx8ZW58MHx8fHx8",
  //   overlay: "from-[#d2a76b]/70 via-[#4a331d]/65 to-[#120b06]/70",
  //   texts: [
  //     { value: "Curated nights", top: "20vh", left: "10%" },
  //     { value: "Textured lights", top: "40vh", left: "60%" },
  //     { value: "Live curation feed", top: "16%", left: "78%", small: true },
  //   ],
  // },
  {
    id: 3,
    image: "https://images.unsplash.com/photo-1659013796120-0a560e3d99ab?w=600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTM5fHx0b3duJTIwaGFsbCUyMGluZGlhfGVufDB8fDB8fHww",
    overlay: "from-[#7dd3fc]/70 via-[#38bdf8]/2 to-[#0ea5e9]/90",
    texts: [
      { value: "Community town halls in your city", top: "20vh", left: "10%" },
      { value: "Real issues, real people, real action", top: "40vh", left: "60%" },
      { value: "NGO volunteering moments", top: "16%", left: "78%", small: true },
    ],
  },
];

type HeroPanelProps = {
  panel: (typeof heroPanels)[number];
};

function HeroPanel({ panel }: HeroPanelProps) {
  const ref = useRef<HTMLElement | null>(null);
  const { scrollYProgress } = useScroll({
    target: ref,
    offset: ["start end", "end start"],
  });

  // Subtle parallax for the background image only
  const rotateZ = useTransform(scrollYProgress, [0, 1], [-8, 8]);
  const rotateY = useTransform(scrollYProgress, [0, 1], [-14, 14]);
  const bgScale = useTransform(scrollYProgress, [0, 1], [1.12, 1]);
  const bgShiftX = useTransform(scrollYProgress, [0, 1], ["-8%", "8%"]);

  return (
    <motion.section
      ref={ref}
      className="relative overflow-hidden min-h-screen" 
    >
      <motion.div
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage: `url(${panel.image})`,
          scale: bgScale,
          rotateZ,
          rotateY,
          x: bgShiftX,
          willChange: "transform",
        }}
      />
      <div className={`absolute inset-0 bg-gradient-to-r ${panel.overlay}`} />

      <div className="relative z-10 h-full px-6 sm:px-10 py-14">
        {/* Mobile/tablet: stack to avoid overlaps */}
        <div className="space-y-2 md:hidden">
          {panel.texts.map((t, lineIdx) => (
            <motion.p
              key={`m-${panel.id}-${t.value}`}
              initial={{ opacity: 0, y: 16 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, amount: 0.2 }}
              transition={{ delay: lineIdx * 0.08, duration: 0.45, ease: "easeOut" }}
              className={`text-white drop-shadow ${
                t.small ? "text-xs tracking-[0.18em]" : "text-xl font-semibold leading-snug uppercase"
              }`}
            >
              {t.value}
            </motion.p>
          ))}
        </div>

        {/* Desktop: absolute placement */}
        <div className="hidden md:block h-full relative">
          {panel.texts.map((t, lineIdx) => (
            <motion.p
              key={`d-${panel.id}-${t.value}`}
              initial={{ opacity: 0, y: 32 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, amount: 0.3 }}
              transition={{ delay: lineIdx * 0.1, duration: 0.5, ease: "easeOut" }}
              className={`absolute text-white drop-shadow-lg backdrop-blur-[2px] ${
                t.small ? "text-xs sm:text-sm tracking-[0.2em]" : "text-3xl sm:text-4xl md:text-4xl font-semibold uppercase leading-[1.05] max-w-[30ch]"
              }`}
              style={{ top: t.top, left: t.left }}
            >
              {t.value}
            </motion.p>
          ))}
        </div>
      </div>

      <div className="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent" />
    </motion.section>
  );
}

export function StackedPanels() {
  return (
    <div className="space-y-0">
      {heroPanels.map((panel) => (
        <HeroPanel key={panel.id} panel={panel} />
      ))}
    </div>
  );
}


