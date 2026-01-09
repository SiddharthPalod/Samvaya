"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import Navbar from "@/components/Navbar";
import api from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { Calendar, TicketCheck, Clock, MapPin } from "lucide-react";

type Ticket = {
  id: string;
  eventId: number;
  userId: number;
  status: "LOCKED" | "CONFIRMED" | "CANCELLED" | "EXPIRED";
  price: number | string;
  quantity: number;
  lockedAt: string;
  lockExpiresAt: string;
};

export default function MyTicketsPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [cancelingId, setCancelingId] = useState<string | null>(null);
  const [cancelError, setCancelError] = useState<string | null>(null);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      router.push("/login");
      return;
    }
    const fetchTickets = async () => {
      try {
        setError(null);
        const { data } = await api.get<Ticket[]>("/tickets/me", {
          headers: { "X-User-Id": user.id },
        });
        setTickets(data);
      } catch (err: any) {
        console.error("Failed to load tickets", err);
        const message =
          err.response?.data?.message ||
          err.response?.data?.error ||
          "Failed to load tickets. Please try again.";
        setError(message);
      } finally {
        setLoading(false);
      }
    };
    fetchTickets();
  }, [authLoading, user, router]);

  const cancelTicket = async (ticketId: string) => {
    if (!user) return;
    setCancelError(null);
    setCancelingId(ticketId);
    try {
      const { data } = await api.post<Ticket>("/tickets/cancel", {
        ticketId,
        userId: user.id,
      });
      setTickets((prev) => prev.map((t) => (t.id === ticketId ? data : t)));
    } catch (err: any) {
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Failed to cancel ticket. Please try again.";
      setCancelError(message);
    } finally {
      setCancelingId(null);
    }
  };

  const formatPrice = (p: number | string) => {
    const n = typeof p === "string" ? Number(p) : p;
    if (Number.isNaN(n)) return String(p);
    return `₹${n.toFixed(2)}`;
  };

  return (
    <div className="min-h-screen text-foreground">
      <Navbar />
      <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 pt-24 pb-16">
        <div className="flex items-center justify-between mb-6">
          <div>
            <p className="pill inline-flex px-3 py-1 rounded-full text-xs text-foreground">Your bookings</p>
            <h1 className="text-2xl font-semibold mt-2">My tickets</h1>
          </div>
          <Link
            href="/events"
            className="inline-flex items-center gap-2 px-4 py-2 rounded-full text-sm font-semibold text-white bg-gradient-to-r from-[#c27a48] via-[#e8c39e] to-[#8a5a44] shadow-lg shadow-orange-500/30 hover:-translate-y-[1px] transition-transform w-fit"
          >
            Browse events
          </Link>
        </div>

        {loading ? (
          <div className="glass rounded-3xl p-6 border border-white/15 shadow-smooth">
            <div className="animate-pulse h-4 w-32 bg-white/10 rounded mb-3" />
            <div className="space-y-3">
              {[1, 2, 3].map((i) => (
                <div key={i} className="h-16 bg-white/5 rounded-2xl" />
              ))}
            </div>
          </div>
        ) : error ? (
          <div className="glass rounded-3xl p-6 border border-red-400/30 text-red-100 shadow-smooth">
            <p className="font-medium">{error}</p>
            <button
              onClick={() => window.location.reload()}
              className="mt-3 px-4 py-2 rounded-full bg-red-500 text-white hover:bg-red-600 transition-colors"
            >
              Retry
            </button>
          </div>
        ) : tickets.length === 0 ? (
          <div className="glass rounded-3xl p-6 border border-white/15 text-muted shadow-smooth">
            <p className="text-foreground font-medium">No tickets yet.</p>
            <p className="text-sm mt-1">Book an event to see it here.</p>
          </div>
        ) : (
          <div className="grid gap-4">
            {tickets.map((t) => (
              <div key={t.id} className="glass rounded-3xl p-5 border border-white/15 shadow-smooth flex flex-col gap-3">
                <div className="flex items-center justify-between gap-3">
                  <div className="flex items-center gap-2 text-sm text-muted">
                    <TicketCheck className="h-4 w-4 text-foreground/80" />
                    <span className="text-foreground font-semibold">Ticket #{t.id.slice(0, 8)}</span>
                  </div>
                  <span className="text-xs px-3 py-1 rounded-full border border-white/20 bg-white/10 text-foreground">
                    {t.status}
                  </span>
                </div>
                <div className="grid sm:grid-cols-2 gap-2 text-sm text-muted">
                  <div className="flex items-center gap-2">
                    <Calendar className="h-4 w-4 text-foreground/80" />
                    <span>Event ID: {t.eventId}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <TicketCheck className="h-4 w-4 text-foreground/80" />
                    <span>Quantity: {t.quantity}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Clock className="h-4 w-4 text-foreground/80" />
                    <span>Locked: {new Date(t.lockedAt).toLocaleString()}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Clock className="h-4 w-4 text-foreground/80" />
                    <span>Expires: {new Date(t.lockExpiresAt).toLocaleString()}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <MapPin className="h-4 w-4 text-foreground/80" />
                    <span>Total: {formatPrice(t.price)}</span>
                  </div>
                </div>
                {t.status !== "CANCELLED" && t.status !== "EXPIRED" && (
                  <div className="flex justify-end">
                    <button
                      onClick={() => cancelTicket(t.id)}
                      disabled={cancelingId === t.id}
                      className="px-4 py-2 rounded-full border border-red-400/50 text-red-200 hover:bg-red-500/10 transition-colors text-sm"
                    >
                      {cancelingId === t.id ? "Cancelling..." : "Cancel ticket"}
                    </button>
                  </div>
                )}
              </div>
            ))}
            {cancelError && (
              <div className="text-sm text-red-300 bg-red-500/10 border border-red-500/30 rounded-xl p-3">
                {cancelError}
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}


