"use client";

import Navbar from "@/components/Navbar";
import { Hero, type Timezone } from "@/components/home/Hero";
import { EventsWall } from "@/components/home/EventsWall";
import { FollowCTA } from "@/components/home/FollowCTA";
import { StackedPanels } from "@/components/home/StackedPanels";
import { useEffect, useState } from "react";
import api from "@/lib/api";
import { EventSummary } from "@/components/home/types";
import { useAuth } from "@/context/AuthContext";

const heroVideo = "https://www.pexels.com/download/video/34783406/";
const PLACEHOLDER = "https://images.unsplash.com/photo-1470229538611-16ba8c7ffbd7?auto=format&fit=crop&w=1600&q=80";
const HERO_CACHE_KEY = "eventverse:hero-feed";
const HERO_CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

export default function Home() {
  const [timezones, setTimezones] = useState<Timezone[]>([]);
  const [events, setEvents] = useState<EventSummary[]>([]);
  const [loadingEvents, setLoadingEvents] = useState(true);
  const { user } = useAuth();

  useEffect(() => {
    const readCache = (): EventSummary[] | null => {
      if (typeof window === "undefined") return null;
      try {
        const cached = sessionStorage.getItem(HERO_CACHE_KEY);
        if (!cached) return null;
        const parsed = JSON.parse(cached) as { ts: number; events: EventSummary[] };
        if (Date.now() - parsed.ts > HERO_CACHE_TTL_MS) return null;
        return parsed.events;
      } catch {
        return null;
      }
    };

    const writeCache = (data: EventSummary[]) => {
      if (typeof window === "undefined") return;
      try {
        sessionStorage.setItem(HERO_CACHE_KEY, JSON.stringify({ ts: Date.now(), events: data }));
      } catch {
        // ignore cache write errors
      }
    };

    const deriveHeroCards = (source: EventSummary[]) => {
      if (!source || source.length === 0) return;
      const cards: Timezone[] = source.slice(0, 5).map((ev) => ({
        genre: ev.category || "Event",
        eventName: ev.title,
        city: ev.city || "—",
      }));
      setTimezones(cards);
    };

    const fetchFeed = async () => {
      setLoadingEvents(true);

      // Try cached feed first
      const cached = readCache();
      if (cached && cached.length > 0) {
        setEvents(cached);
        deriveHeroCards(cached);
      }

      try {
        const headers = { "X-User-Id": user?.id?.toString() || "guest" };
        const type = user ? "RECOMMENDED" : "TRENDING";
        const [{ data: feed }, { data: allEvents }] = await Promise.all([
          api.get<{ events: { eventId: string }[] }>("/events/feed", {
            params: { type, page: 0, size: 30 },
            headers,
          }),
          api.get<{ content: EventSummary[] }>("/events"),
        ]);

        const ids = feed?.events?.map((e) => e.eventId) || [];
        const map = new Map<number, EventSummary>();
        (allEvents.content || []).forEach((ev) => map.set(ev.id, ev));

        // hydrate feed ids with cached list, fallback to fetch missing
        const hydrated = await Promise.all(
          ids.map(async (id) => {
            const numId = Number(id);
            if (map.has(numId)) return map.get(numId)!;
            try {
              const res = await api.get<EventSummary>(`/events/${id}`);
              return res.data;
            } catch {
              return null;
            }
          })
        );

        const filtered = hydrated.filter((e): e is EventSummary => !!e);
        const result = filtered.length > 0 ? filtered : allEvents.content || [];

        setEvents(result);
        writeCache(result);
        deriveHeroCards(result);
      } catch (err) {
        console.error("Failed to load feed, falling back to /events", err);
        try {
          const { data } = await api.get<{ content: EventSummary[] }>("/events");
          setEvents(data.content || []);
          writeCache(data.content || []);
          deriveHeroCards(data.content || []);
        } catch (e2) {
          setEvents([]);
        }
      } finally {
        setLoadingEvents(false);
      }
    };

    fetchFeed();
  }, [user]);

  useEffect(() => {
    const handleWheel = (e: WheelEvent) => {
      // Skip if another handler already handled it or on zoom gesture
      if (e.defaultPrevented || e.ctrlKey) return;
      e.preventDefault();
      const targetY = window.scrollY + e.deltaY * 0.4;
      window.scrollTo({ top: targetY, behavior: "smooth" });
    };
    window.addEventListener("wheel", handleWheel, { passive: false });
    return () => window.removeEventListener("wheel", handleWheel);
  }, []);

  const featuredEvents = events.slice(0, 6);
  const beigeWall = events.slice(6, 9);

  return (
    <div className="relative min-h-screen text-foreground">
      <Navbar />

      <main className="min-w-screen pb-20 relative z-10">
        <Hero heroVideo={heroVideo} timezones={timezones} />
        <StackedPanels />
        <EventsWall events={beigeWall} fallback={featuredEvents} placeholder={PLACEHOLDER} />
        <FollowCTA />
      </main>
    </div>
  );
}
