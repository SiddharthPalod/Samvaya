// app/events/[id]/page.tsx
"use client";
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import api from "@/lib/api";
import Navbar from "@/components/Navbar";
import { Calendar, MapPin, Users, ArrowLeft, Sparkles } from "lucide-react";
import Link from "next/link";
import { motion } from "framer-motion";
import EventChat from "@/components/chat/EventChat";

interface EventDetail {
  id: number;
  title: string;
  description: string;
  city: string;
  venue?: string;
  time: string;
  capacity?: number;
  totalSeats?: number;
  availableSeats?: number;
  organizerId: number;
  category?: string;
  imageUrl?: string;
}

export default function EventDetailPage() {
  const { id } = useParams();
  const [event, setEvent] = useState<EventDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchEvent = async () => {
      try {
        const { data } = await api.get<EventDetail>(`/events/${id}`);
        setEvent(data);
      } catch (error: any) {
        console.error("Failed to fetch event", error);
      } finally {
        setLoading(false);
      }
    };
    fetchEvent();
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-slate-200">Loading...</div>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-red-200">Event not found</div>
      </div>
    );
  }

  const totalSeats = event.totalSeats ?? event.capacity ?? 0;
  const availableSeats =
    event.availableSeats ?? event.capacity ?? totalSeats ?? 0;

  return (
    <div className="min-h-screen text-foreground">
      <Navbar />

      <div className="relative h-[55vh]">
        <img
          src={
            event.imageUrl ||
            "https://images.unsplash.com/photo-1522158637959-30385a09e0da?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
          }
          className="w-full h-full object-cover"
          alt={event.title}
        />
        <div className="absolute inset-0 bg-linear-to-t from-black via-black/70 to-transparent" />
        <div className="absolute inset-0 bg-linear-to-r from-[#c27a48]/50 via-transparent to-[#8a5a44]/40" />

        <div className="absolute bottom-0 left-0 w-full pb-16">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <Link href="/events" className="inline-flex items-center text-slate-300 hover:text-white mb-6 transition-colors">
              <ArrowLeft className="w-4 h-4 mr-2" /> Back to Events
            </Link>
            <motion.h1
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5 }}
              className="text-4xl sm:text-5xl font-semibold mb-4 max-w-3xl text-white"
            >
              {event.title}
            </motion.h1>
            <div className="flex flex-wrap gap-3 text-sm text-white">
              <span className="px-3 py-1 rounded-full bg-white/20 backdrop-blur-sm border border-white/30">{event.city}</span>
              {event.category && <span className="px-3 py-1 rounded-full bg-white/20 backdrop-blur-sm border border-white/30">{event.category}</span>}
            </div>
          </div>
        </div>
      </div>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-12 relative z-10 pb-16">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 glass rounded-3xl p-8 border border-white/15 shadow-smooth">
            <p className="pill inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs text-foreground mb-4">
              <Sparkles className="h-4 w-4" />
              Experience overview
            </p>
            <h2 className="text-2xl font-semibold mb-3 text-foreground">About this event</h2>
            <p className="text-muted leading-relaxed">
              {event.description || "No description provided for this event."}
            </p>
          </div>

          <div className="space-y-4">
            <div className="glass rounded-3xl p-6 border border-white/15 shadow-smooth space-y-4">
              <h3 className="font-semibold text-foreground">Event details</h3>
              <div className="space-y-3 text-sm text-muted">
                <div className="flex gap-3">
                  <div className="p-2 rounded-lg bg-white/10">
                    <Calendar className="w-5 h-5 text-foreground/80" />
                  </div>
                  <div>
                    <p className="text-xs text-muted">Date & time</p>
                    <p className="font-medium text-foreground">{new Date(event.time).toLocaleString()}</p>
                  </div>
                </div>
                <div className="flex gap-3">
                  <div className="p-2 rounded-lg bg-white/10">
                    <MapPin className="w-5 h-5 text-foreground/80" />
                  </div>
                  <div>
                    <p className="text-xs text-muted">Location</p>
                    <p className="font-medium text-foreground">{event.venue ? `${event.venue}, ${event.city}` : event.city}</p>
                  </div>
                </div>
                <div className="flex gap-3">
                  <div className="p-2 rounded-lg bg-white/10">
                    <Users className="w-5 h-5 text-foreground/80" />
                  </div>
                  <div>
                    <p className="text-xs text-muted">Seats</p>
                    <p className="font-medium text-foreground">
                      {availableSeats} / {totalSeats} seats available
                    </p>
                  </div>
                </div>
              </div>

              <Link
                href={`/events/${id}/checkout`}
                className="w-full inline-flex justify-center mt-4 rounded-full py-3 bg-linear-to-r from-[#c27a48] via-[#e8c39e] to-[#8a5a44] text-white font-semibold hover:-translate-y-px transition-transform shadow-lg shadow-orange-500/30"
              >
                Book ticket
              </Link>
            </div>

            <div className="glass rounded-3xl p-5 border border-white/15 shadow-smooth space-y-3">
              <h4 className="text-sm text-foreground">Why this feels special</h4>
              <div className="flex items-center gap-2 text-sm text-muted">
                <span className="h-2 w-2 rounded-full bg-indigo-300" />
                Curated lineup & visual identity
              </div>
              <div className="flex items-center gap-2 text-sm text-muted">
                <span className="h-2 w-2 rounded-full bg-sky-300" />
                Flow-friendly booking in two taps
              </div>
              <div className="flex items-center gap-2 text-sm text-muted">
                <span className="h-2 w-2 rounded-full bg-purple-300" />
                Live capacity signals to avoid surprises
              </div>
            </div>
          </div>
        </div>

        {/* Chat Section */}
        <div className="mt-8">
          <EventChat eventId={id as string} />
        </div>
      </main>
    </div>
  );
}

