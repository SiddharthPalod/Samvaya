// components/CardStack.tsx
"use client";

import { useEffect, useState } from "react";
import { motion } from "framer-motion";

type Card = {
  id: number;
  title: string;
  meta: string;
  content: string;
};

interface CardStackProps {
  items: Card[];
  offset?: number;
  scaleFactor?: number;
}

export function CardStack({ items, offset = 16, scaleFactor = 0.06 }: CardStackProps) {
  const [cards, setCards] = useState<Card[]>(items);

  useEffect(() => {
    const interval = setInterval(() => {
      setCards((prev) => {
        const clone = [...prev];
        clone.unshift(clone.pop() as Card);
        return clone;
      });
    }, 4200);

    return () => clearInterval(interval);
  }, []);

  return (
    <div className="relative h-[300px] sm:h-[340px] w-full max-w-xl">
      {cards.map((card, index) => (
        <motion.div
          key={card.id}
          className="absolute inset-x-0 mx-auto h-64 rounded-3xl glass border border-white/15 shadow-smooth p-6 flex flex-col justify-between"
          style={{ transformOrigin: "top center" }}
          animate={{
            top: index * -offset,
            scale: 1 - index * scaleFactor,
            zIndex: cards.length - index,
            opacity: 1 - index * 0.1,
          }}
          transition={{ type: "spring", stiffness: 110, damping: 18 }}
        >
          <div className="space-y-3">
            <p className="text-xs uppercase tracking-[0.2em] text-muted">{card.meta}</p>
            <h3 className="text-xl font-semibold text-foreground">{card.title}</h3>
            <p className="text-sm text-muted leading-relaxed">{card.content}</p>
          </div>
          <div className="flex items-center justify-between text-xs text-muted">
            <span className="pill px-3 py-1 rounded-full text-foreground">Curated</span>
            <span>Swipe to view more</span>
          </div>
        </motion.div>
      ))}
    </div>
  );
}

