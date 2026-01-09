// app/events/page.tsx
"use client";
import { useEffect, useMemo, useState } from "react";
import api from "@/lib/api";
import Navbar from "@/components/Navbar";
import Link from "next/link";
import { motion } from "framer-motion";
import { MapPin, Users, Calendar, Search, Sparkles } from "lucide-react";
import { useTheme } from "@/context/ThemeContext";
import debounce from "lodash.debounce";

interface Event {
  id: number;
  title: string;
  description: string;
  city: string;
  venue?: string;
  time: string;
  capacity: number;
  imageUrl?: string;
}

interface PageResponse {
  content: Event[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

const PLACEHOLDER =
  "https://images.unsplash.com/photo-1522158637959-30385a09e0da?q=80&w=1400&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D";

export default function EventsPage() {
  const [events, setEvents] = useState<Event[]>([]);
  const [originalEvents, setOriginalEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [query, setQuery] = useState("");
  const [searching, setSearching] = useState(false);
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const { theme } = useTheme();
  const isLight = theme === "light";
  const dateFormatter = useMemo(
    () =>
      new Intl.DateTimeFormat("en-US", {
        month: "short",
        day: "numeric",
        timeZone: "UTC",
      }),
    []
  );

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setError(null);
        const { data } = await api.get<PageResponse>("/events");
        const list = data.content || [];
        setEvents(list);
        setOriginalEvents(list);
      } catch (error: any) {
        console.error("Failed to fetch events", error);
        if (error.code === "ECONNREFUSED" || error.message?.includes("Network Error")) {
          setError("Cannot connect to server. Please make sure the backend is running on port 8085.");
        } else if (error.response) {
          setError(`Failed to load events: ${error.response.status} ${error.response.statusText}`);
        } else {
          setError("Failed to load events. Please try again later.");
        }
      } finally {
        setLoading(false);
      }
    };
    fetchEvents();
  }, []);

  const runSearch = useMemo(
    () =>
      debounce(async (text: string) => {
        if (!text.trim()) {
          setSearching(false);
          setEvents(originalEvents);
          return;
        }
        setSearching(true);
        try {
          const { data } = await api.get<PageResponse>("/events/search", {
            params: { query: text, page: 0, size: 30 },
          });
          setEvents(data.content || []);
          setError(null);
        } catch (err) {
          console.error("Search failed", err);
          setError("Search failed. Please try again.");
        } finally {
          setSearching(false);
        }
      }, 250),
    [originalEvents]
  );

  const runSuggest = useMemo(
    () =>
      debounce(async (text: string) => {
        if (!text.trim()) {
          setSuggestions([]);
          return;
        }
        try {
          const { data } = await api.get<PageResponse>("/events/search", {
            params: { query: text, page: 0, size: 5 },
          });
          const list = data.content || [];
          const titles = Array.from(new Set(list.map((e) => e.title).filter(Boolean)));
          setSuggestions(titles.slice(0, 5));
        } catch {
          setSuggestions([]);
        }
      }, 200),
    []
  );

  useEffect(() => {
    runSearch(query);
    runSuggest(query);
    return () => runSearch.cancel();
  }, [query, runSearch, runSuggest]);

  const filtered = useMemo(() => events, [events]);

  return (
    <div className="min-h-screen text-foreground">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-28 pb-16 relative z-10">
        <section className="glass rounded-3xl border border-white/15 shadow-smooth p-8 mb-10 relative overflow-hidden">
          <div className="grid-overlay rounded-3xl" />
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
            <div>
              <p className="pill inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs text-foreground">
                <Sparkles className="h-4 w-4" />
                Curated in real-time
              </p>
              <h1 className="text-3xl md:text-4xl font-semibold text-foreground mt-4">Discover events with taste.</h1>
              <p className="text-muted mt-2 max-w-2xl">
                Minimal noise, high signal. Filter by vibe or city and jump into experiences that already feel premium.
              </p>
            </div>
            <div className="relative w-full md:w-96">
              <Search
                className="absolute left-3 top-3.5 h-5 w-5 text-muted cursor-pointer"
                onClick={() => {
                  (runSearch as any).flush?.();
                  runSearch(query);
                }}
              />
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    (runSearch as any).flush?.();
                    runSearch(query);
                  }
                }}
                placeholder="Search by title, city, or vibe"
                className="w-full bg-white/10 border border-white/20 rounded-full pl-11 pr-4 py-3 text-sm text-foreground placeholder:text-muted focus:outline-none focus:ring-2 focus:ring-[#c27a48]/50"
              />
              {searching && <span className="absolute right-4 top-3.5 text-xs text-muted">Searching…</span>}
              {suggestions.length > 0 && (
                <div
                  className={`absolute mt-2 w-full rounded-2xl border shadow-lg ${
                    isLight
                      ? "bg-[#f0e4d5] text-[#1f140d] border-black/10"
                      : "bg-[rgba(28,16,10,0.96)] text-foreground border-white/10 backdrop-blur"
                  }`}
                >
                  {suggestions.map((sug) => (
                    <button
                      key={sug}
                      className={`w-full text-left px-4 py-2 text-sm rounded-2xl transition-colors ${
                        isLight ? "hover:bg-[#e3d4bd]" : "hover:bg-white/5"
                      }`}
                      onClick={() => {
                        setQuery(sug);
                        (runSearch as any).flush?.();
                        runSearch(sug);
                        setSuggestions([]);
                      }}
                    >
                      {sug}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>
        </section>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <div key={i} className="h-72 rounded-3xl glass animate-pulse" />
            ))}
          </div>
        ) : error ? (
          <div className="glass rounded-2xl p-6 border border-red-400/30 text-center text-red-100">
            <p className="font-medium">{error}</p>
            <button
              onClick={() => window.location.reload()}
              className="mt-4 px-4 py-2 rounded-full bg-red-500 text-white hover:bg-red-600 transition-colors"
            >
              Retry
            </button>
          </div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-12 glass rounded-2xl border border-white/15">
            <p className="text-foreground text-lg">No events match that vibe yet.</p>
            <p className="text-muted text-sm mt-2">Try clearing filters or check back soon.</p>
          </div>
        ) : (
          <>
            <section className="grid lg:grid-cols-5 gap-6 mb-10">
              {filtered.length > 0 && (
                <Link href={`/events/${filtered[0].id}`} className="lg:col-span-3">
                  <motion.div
                    initial={{ opacity: 0, y: 18 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true, amount: 0.3 }}
                    className="relative h-full min-h-[340px] rounded-3xl overflow-hidden shadow-smooth"
                  >
                    <img
                      src={filtered[0].imageUrl || PLACEHOLDER}
                      alt={filtered[0].title}
                      className="absolute inset-0 h-full w-full object-cover"
                    />
                    <div className="absolute inset-0 bg-gradient-to-r from-black/70 via-black/35 to-transparent" />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/40 via-transparent to-transparent" />
                    <div className="absolute inset-0 p-6 sm:p-8 flex flex-col justify-end gap-3">
                      <div className="flex items-center gap-3">
                        <span className="pill px-3 py-1 rounded-full text-foreground bg-white/10 border border-white/20">
                          Spotlight
                        </span>
                        <span className="pill px-3 py-1 rounded-full text-foreground bg-white/10 border border-white/20">
                          {filtered[0].city}
                        </span>
                      </div>
                      <h2 className="text-2xl sm:text-3xl font-semibold text-white drop-shadow-lg">
                        {filtered[0].title}
                      </h2>
                      <p className="text-white/85 max-w-2xl line-clamp-3">
                        {filtered[0].description || "A crafted experience worth your night."}
                      </p>
                      <div className="flex flex-wrap gap-4 text-sm text-white/85">
                        <span className="inline-flex items-center gap-2">
                          <Calendar className="h-4 w-4" />
                          {dateFormatter.format(new Date(filtered[0].time))}
                        </span>
                        <span className="inline-flex items-center gap-2">
                          <MapPin className="h-4 w-4" />
                          {filtered[0].venue || filtered[0].city}
                        </span>
                        <span className="inline-flex items-center gap-2">
                          <Users className="h-4 w-4" />
                          {filtered[0].capacity} seats
                        </span>
                      </div>
                    </div>
                  </motion.div>
                </Link>
              )}

              <div className="lg:col-span-2 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-1 gap-4">
                {[filtered[1], filtered[2]].filter(Boolean).map((event, idx) => (
                  <Link key={event!.id} href={`/events/${event!.id}`} className="block h-full">
                    <motion.div
                      initial={{ opacity: 0, y: 14 }}
                      whileInView={{ opacity: 1, y: 0 }}
                      viewport={{ once: true, amount: 0.3 }}
                      transition={{ delay: idx * 0.05 }}
                      className="glass rounded-2xl p-5 border border-white/15 shadow-smooth h-full flex gap-4"
                    >
                      <div className="relative w-24 h-24 rounded-xl overflow-hidden flex-shrink-0">
                        <img
                          src={event!.imageUrl || PLACEHOLDER}
                          alt={event!.title}
                          className="w-full h-full object-cover"
                        />
                        <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent" />
                      </div>
                      <div className="flex flex-col gap-2">
                        <p className="text-xs pill px-2 py-1 rounded-full self-start text-foreground">{event!.city}</p>
                        <h3 className="text-lg font-semibold text-foreground leading-snug">{event!.title}</h3>
                        <p className="text-sm text-muted line-clamp-2">
                          {event!.description || "Experience the details crafted for you."}
                        </p>
                        <div className="flex items-center gap-2 text-xs text-muted mt-auto">
                          <Calendar className="w-4 h-4" />
                          {dateFormatter.format(new Date(event!.time))}
                        </div>
                      </div>
                    </motion.div>
                  </Link>
                ))}
              </div>
            </section>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {filtered.slice(3).map((event, index) => (
                <motion.div
                  key={event.id}
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true, amount: 0.2 }}
                  transition={{ delay: index * 0.05 }}
                  className="group relative"
                >
                  <Link href={`/events/${event.id}`} className="block h-full">
                    <div className="h-full rounded-3xl overflow-hidden glass border border-white/15 hover:-translate-y-2 transition-all duration-300 shadow-smooth flex flex-col">
                      <div className="relative h-48 overflow-hidden">
                        <img
                          src={event.imageUrl || PLACEHOLDER}
                          alt={event.title}
                          className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                        />
                        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
                        <span className="absolute top-3 left-3 pill text-xs px-3 py-1 rounded-full text-foreground">
                          {event.city}
                        </span>
                      </div>
                      <div className="p-5 flex flex-col gap-3 flex-1">
                        <h3 className="text-lg font-semibold text-foreground leading-snug">{event.title}</h3>
                        <p className="text-sm text-muted line-clamp-2">{event.description || "An unforgettable experience awaits."}</p>
                        <div className="mt-auto space-y-2 text-sm text-muted">
                          <div className="flex items-center gap-2">
                            <Calendar className="w-4 h-4 text-foreground/80" />
                            <span>{dateFormatter.format(new Date(event.time))}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <MapPin className="w-4 h-4 text-foreground/80" />
                            <span>{event.venue || event.city}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <Users className="w-4 h-4 text-foreground/80" />
                            <span>{event.capacity} seats</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </Link>
                </motion.div>
              ))}
            </div>
          </>
        )}
      </main>
    </div>
  );
}

