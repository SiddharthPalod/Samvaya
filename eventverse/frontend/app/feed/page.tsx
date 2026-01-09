"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import Navbar from "@/components/Navbar";
import api from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { EventSummary } from "@/components/home/types";
import { Calendar, MapPin, Users } from "lucide-react";

type FeedItem = {
  eventId: string;
  score: number;
};

export default function FeedPage() {
  const { user } = useAuth();
  const [events, setEvents] = useState<EventSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadFeed = async () => {
      setLoading(true);
      setError(null);
      try {
        const headers = { "X-User-Id": user?.id?.toString() || "guest" };
        const type = user ? "RECOMMENDED" : "TRENDING";
        const { data } = await api.get<{ events: FeedItem[] }>("/events/feed", {
          params: { type, page: 0, size: 30 },
          headers,
        });
        const ids = data?.events?.map((e) => e.eventId) || [];
        const details = await Promise.all(
          ids.map(async (id) => {
            try {
              const res = await api.get<EventSummary>(`/events/${id}`);
              return res.data;
            } catch {
              return null;
            }
          })
        );
        const filtered = details.filter((e): e is EventSummary => !!e);
        setEvents(filtered);
        if (filtered.length === 0) {
          setError("No recommendations yet. Check back soon.");
        }
      } catch (err: any) {
        console.error("Failed to load feed", err);
        setError("Failed to load feed. Please try again.");
      } finally {
        setLoading(false);
      }
    };
    loadFeed();
  }, [user]);

  return (
    <div className="min-h-screen text-foreground">
      <Navbar />
      <main className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 pt-24 pb-16">
        <div className="mb-8 flex items-center justify-between">
          <div>
            <p className="text-sm text-muted uppercase tracking-[0.2em]">
              {user ? "Personalized for you" : "Trending now"}
            </p>
            <h1 className="text-3xl font-semibold mt-2">Feed</h1>
          </div>
          {loading && <span className="text-sm text-muted">Loading…</span>}
        </div>

        {error && (
          <div className="glass rounded-2xl p-6 border border-red-400/30 text-red-100 mb-6">
            {error}
          </div>
        )}

        {!loading && events.length === 0 && !error && (
          <div className="glass rounded-2xl p-6 border border-white/15 text-muted">
            No events to show yet.
          </div>
        )}

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {events.map((event) => (
            <Link key={event.id} href={`/events/${event.id}`} className="block h-full">
              <div className="h-full rounded-3xl overflow-hidden glass border border-white/15 shadow-smooth flex flex-col">
                <div className="relative h-44 overflow-hidden">
                  <img
                    src={
                      event.imageUrl ||
                      "https://images.unsplash.com/photo-1522158637959-30385a09e0da?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.1.0"
                    }
                    alt={event.title}
                    className="w-full h-full object-cover"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent" />
                  <span className="absolute top-3 left-3 pill text-xs px-3 py-1 rounded-full text-foreground bg-white/10 border border-white/20">
                    {event.city}
                  </span>
                </div>
                <div className="p-5 flex flex-col gap-3 flex-1">
                  <h3 className="text-lg font-semibold text-foreground leading-snug line-clamp-2">
                    {event.title}
                  </h3>
                  <p className="text-sm text-muted line-clamp-2">{event.description || "See details"}</p>
                  <div className="mt-auto space-y-2 text-sm text-muted">
                    <div className="flex items-center gap-2">
                      <Calendar className="w-4 h-4 text-foreground/80" />
                      <span suppressHydrationWarning>{new Date(event.time).toLocaleString()}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <MapPin className="w-4 h-4 text-foreground/80" />
                      <span>{event.venue || event.city}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Users className="w-4 h-4 text-foreground/80" />
                      <span>{event.category || "Event"}</span>
                    </div>
                  </div>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </main>
    </div>
  );
}
