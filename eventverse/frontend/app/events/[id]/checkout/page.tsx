"use client";

import { useEffect, useMemo, useState, useCallback } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { motion } from "framer-motion";
import { Calendar, Clock, MapPin, Minus, Plus, Shield, Sparkles, TicketCheck } from "lucide-react";
import Navbar from "@/components/Navbar";
import api from "@/lib/api";
import { useAuth } from "@/context/AuthContext";

type EventDetail = {
  id: number;
  title: string;
  description: string;
  city: string;
  venue?: string;
  time: string;
  capacity: number;
  imageUrl?: string;
};

type TicketStatus = "LOCKED" | "CONFIRMED" | "CANCELLED" | "EXPIRED";

type TicketResponse = {
  id: string;
  eventId: number;
  userId: number;
  status: TicketStatus;
  price: number | string;
  quantity: number;
  lockedAt: string;
  lockExpiresAt: string;
};

const PLACEHOLDER =
  "https://images.unsplash.com/photo-1522158637959-30385a09e0da?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.1.0";

export default function CheckoutPage() {
  const { id } = useParams();
  const router = useRouter();
  const { user, loading: authLoading } = useAuth();

  const [event, setEvent] = useState<EventDetail | null>(null);
  const [quantity, setQuantity] = useState<number>(1);
  const [ticket, setTicket] = useState<TicketResponse | null>(null);
  const [lockError, setLockError] = useState<string | null>(null);
  const [confirmError, setConfirmError] = useState<string | null>(null);
  const [cancelError, setCancelError] = useState<string | null>(null);
  const [locking, setLocking] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const [canceling, setCanceling] = useState(false);
  const [expiresInMs, setExpiresInMs] = useState<number | null>(null);

  const eventId = useMemo(() => Number(id), [id]);

  useEffect(() => {
    const fetchEvent = async () => {
      try {
        const { data } = await api.get<EventDetail>(`/events/${eventId}`);
        setEvent(data);
      } catch (error: any) {
        console.error("Failed to fetch event", error);
        setEvent(null);
      }
    };
    if (!Number.isNaN(eventId)) {
      fetchEvent();
    }
  }, [eventId]);

  // Countdown for lock expiry
  useEffect(() => {
    if (!ticket?.lockExpiresAt) {
      setExpiresInMs(null);
      return;
    }
    const expiry = new Date(ticket.lockExpiresAt).getTime();
    const update = () => {
      const diff = expiry - Date.now();
      setExpiresInMs(diff > 0 ? diff : 0);
    };
    update();
    const interval = setInterval(update, 1000);
    return () => clearInterval(interval);
  }, [ticket?.lockExpiresAt]);

  // Redirect unauthenticated users to login
  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login");
    }
  }, [authLoading, user, router]);

  const handleQuantity = (delta: number) => {
    setQuantity((prev) => Math.min(Math.max(prev + delta, 1), 10));
  };

  const lockTickets = useCallback(async () => {
    if (!user) {
      router.push("/login");
      return;
    }
    setLockError(null);
    setConfirmError(null);
    setLocking(true);
    try {
      const { data } = await api.post<TicketResponse>("/tickets/lock", {
        eventId,
        userId: user.id,
        quantity,
      });
      setTicket(data);
    } catch (err: any) {
      console.error("Lock tickets failed", err);
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        "Unable to lock tickets. Please try again.";
      setLockError(message);
    } finally {
      setLocking(false);
    }
  }, [user, router, eventId, quantity]);

  const confirmTicket = useCallback(async () => {
    if (!user || !ticket) {
      return;
    }
    setConfirmError(null);
    setConfirming(true);
    try {
      const idempotencyKey = crypto.randomUUID();
      const { data } = await api.post<TicketResponse>("/tickets/confirm", {
        ticketId: ticket.id,
        userId: user.id,
        idempotencyKey,
      });
      setTicket(data);
    } catch (err: any) {
      console.error("Confirm ticket failed", err);
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        "Unable to confirm ticket. Please try again.";
      setConfirmError(message);
    } finally {
      setConfirming(false);
    }
  }, [ticket, user]);

  const cancelTicket = useCallback(async () => {
    if (!user || !ticket) return;
    setCancelError(null);
    setCanceling(true);
    try {
      const { data } = await api.post<TicketResponse>("/tickets/cancel", {
        ticketId: ticket.id,
        userId: user.id,
      });
      setTicket(data);
    } catch (err: any) {
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        "Unable to cancel ticket. Please try again.";
      setCancelError(message);
    } finally {
      setCanceling(false);
    }
  }, [ticket, user]);

  const expiryLabel = useMemo(() => {
    if (expiresInMs == null) return null;
    const totalSeconds = Math.floor(expiresInMs / 1000);
    const minutes = Math.floor(totalSeconds / 60)
      .toString()
      .padStart(2, "0");
    const seconds = (totalSeconds % 60).toString().padStart(2, "0");
    return `${minutes}:${seconds}`;
  }, [expiresInMs]);

  const priceDisplay = useMemo(() => {
    const price = ticket?.price;
    if (price == null) return null;
    const numeric = typeof price === "string" ? Number(price) : price;
    if (Number.isNaN(numeric)) return price.toString();
    return `₹${numeric.toFixed(2)}`;
  }, [ticket?.price]);

  const statusAccent = useMemo(() => {
    switch (ticket?.status) {
      case "CONFIRMED":
        return "text-emerald-300";
      case "LOCKED":
        return "text-amber-300";
      case "CANCELLED":
        return "text-red-300";
      case "EXPIRED":
        return "text-orange-300";
      default:
        return "text-muted";
    }
  }, [ticket?.status]);

  const isExpired = expiresInMs === 0 && ticket?.status === "LOCKED";

  return (
    <div className="min-h-screen text-foreground">
      <Navbar />

      <main className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 pt-24 pb-16">
        <div className="flex items-center gap-2 mb-6">
          <Link href={`/events/${id}`} className="text-muted hover:text-foreground text-sm">
            ← Back to event
          </Link>
        </div>

        <div className="grid lg:grid-cols-5 gap-8">
          <div className="lg:col-span-3 glass rounded-3xl p-6 border border-white/15 shadow-smooth">
            <div className="flex items-start justify-between gap-4 mb-6">
              <div className="space-y-1">
                <p className="pill inline-flex px-3 py-1 rounded-full text-xs text-foreground">
                  <Sparkles className="h-4 w-4 mr-2" /> Reserve your seats
                </p>
                <h1 className="text-2xl font-semibold text-foreground">Checkout</h1>
                <p className="text-muted text-sm">
                  Lock seats for 10 minutes, then confirm to finalize your booking.
                </p>
              </div>
              {ticket && (
                <div className={`text-sm font-medium ${statusAccent}`}>
                  Status: {ticket.status}
                </div>
              )}
            </div>

            <div className="space-y-4">
              <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
                <div className="flex items-center gap-3">
                  <TicketCheck className="h-5 w-5 text-foreground" />
                  <div className="flex-1">
                    <p className="text-sm text-muted">Tickets</p>
                    <div className="flex items-center gap-3 mt-2">
                      <div className="inline-flex items-center gap-3 bg-white/10 border border-white/15 rounded-full px-3 py-2">
                        <button
                          type="button"
                          onClick={() => handleQuantity(-1)}
                          className="p-1 rounded-full hover:bg-white/10 disabled:opacity-50"
                          disabled={quantity <= 1 || locking}
                          aria-label="Decrease tickets"
                        >
                          <Minus className="h-4 w-4" />
                        </button>
                        <span className="text-lg font-semibold w-8 text-center">{quantity}</span>
                        <button
                          type="button"
                          onClick={() => handleQuantity(1)}
                          className="p-1 rounded-full hover:bg-white/10 disabled:opacity-50"
                          disabled={quantity >= 10 || locking}
                          aria-label="Increase tickets"
                        >
                          <Plus className="h-4 w-4" />
                        </button>
                      </div>
                      <div className="text-sm text-muted">Max 10 per booking</div>
                    </div>
                  </div>
                </div>
              </div>

              <button
                onClick={lockTickets}
                disabled={locking || isExpired}
                className="w-full rounded-xl py-3 bg-linear-to-r from-[#c27a48] via-[#e8c39e] to-[#8a5a44] text-white font-semibold hover:-translate-y-px transition-transform shadow-lg shadow-orange-500/30 disabled:opacity-60 disabled:cursor-not-allowed"
              >
                {locking ? "Locking..." : isExpired ? "Lock expired — relock" : "Lock tickets"}
              </button>

              {lockError && (
                <div className="text-sm text-red-300 bg-red-500/10 border border-red-500/30 rounded-xl p-3">
                  {lockError}
                </div>
              )}

              {ticket && (
                <div className="rounded-2xl border border-white/10 bg-white/5 p-4 space-y-3">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-muted">Reservation</p>
                      <p className="text-lg font-semibold text-foreground">Ticket #{ticket.id.slice(0, 8)}</p>
                    </div>
                    {expiryLabel && ticket.status === "LOCKED" && (
                      <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-500/10 text-amber-200 text-sm">
                        <Clock className="h-4 w-4" />
                        Expires in {expiryLabel}
                      </div>
                    )}
                    {ticket.status === "CONFIRMED" && (
                      <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-200 text-sm">
                        <Shield className="h-4 w-4" />
                        Confirmed
                      </div>
                    )}
                  </div>
                  <div className="grid sm:grid-cols-2 gap-3 text-sm text-muted">
                    <div className="flex items-center gap-2">
                      <TicketCheck className="h-4 w-4 text-foreground/80" />
                      <span>{ticket.quantity} seats</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-foreground/80" />
                      <span>
                        Locked at {new Date(ticket.lockedAt).toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit" })}
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Clock className="h-4 w-4 text-foreground/80" />
                      <span>
                        Expires at {new Date(ticket.lockExpiresAt).toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit" })}
                      </span>
                    </div>
                    {priceDisplay && (
                      <div className="flex items-center gap-2">
                        <Sparkles className="h-4 w-4 text-foreground/80" />
                        <span>Total: {priceDisplay}</span>
                      </div>
                    )}
                  </div>

                  <div className="flex flex-col sm:flex-row gap-3">
                    <button
                      onClick={confirmTicket}
                      disabled={confirming || ticket.status !== "LOCKED" || isExpired}
                      className="flex-1 rounded-xl py-3 bg-linear-to-r from-[#c27a48] via-[#e8c39e] to-[#8a5a44] text-white font-semibold hover:-translate-y-px transition-transform shadow-lg shadow-orange-500/30 disabled:opacity-60 disabled:cursor-not-allowed disabled:hover:translate-y-0"
                    >
                      {confirming ? "Confirming..." : "Confirm booking"}
                    </button>
                    {ticket.status !== "CANCELLED" && (
                      <button
                        onClick={cancelTicket}
                        disabled={canceling}
                        className="rounded-xl px-4 py-3 border border-red-400/50 text-sm text-red-200 hover:bg-red-500/10 transition-colors"
                      >
                        {canceling ? "Cancelling..." : "Cancel ticket"}
                      </button>
                    )}
                    <button
                      onClick={() => router.push("/my-tickets")}
                      className="rounded-xl px-4 py-3 border border-white/20 text-sm text-muted hover:text-foreground hover:border-white/40 transition-colors"
                    >
                      View my tickets
                    </button>
                  </div>

                  {confirmError && (
                    <div className="text-sm text-red-300 bg-red-500/10 border border-red-500/30 rounded-xl p-3">
                      {confirmError}
                    </div>
                  )}
                  {cancelError && (
                    <div className="text-sm text-red-300 bg-red-500/10 border border-red-500/30 rounded-xl p-3">
                      {cancelError}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          <div className="lg:col-span-2 space-y-4">
            <div className="glass rounded-3xl p-5 border border-white/15 shadow-smooth">
              {event ? (
                <>
                  <div className="relative h-48 rounded-2xl overflow-hidden mb-4">
                    <img
                      src={event.imageUrl || PLACEHOLDER}
                      alt={event.title}
                      className="w-full h-full object-cover"
                    />
                    <div className="absolute inset-0 bg-linear-to-t from-black/70 via-black/30 to-transparent" />
                    <div className="absolute bottom-3 left-3">
                      <span className="pill text-xs px-3 py-1 rounded-full bg-white/10 text-foreground border border-white/20">
                        {event.city}
                      </span>
                    </div>
                  </div>
                  <h2 className="text-xl font-semibold text-foreground mb-2">{event.title}</h2>
                  <p className="text-sm text-muted mb-4 line-clamp-3">
                    {event.description || "Experience crafted for you."}
                  </p>
                  <div className="space-y-2 text-sm text-muted">
                    <div className="flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-foreground/80" />
                      <span>{new Date(event.time).toLocaleString()}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <MapPin className="h-4 w-4 text-foreground/80" />
                      <span>{event.venue ? `${event.venue}, ${event.city}` : event.city}</span>
                    </div>
                  </div>
                </>
              ) : (
                <div className="text-muted text-sm">Loading event details...</div>
              )}
            </div>

            <div className="glass rounded-3xl p-5 border border-white/15 shadow-smooth space-y-3">
              <h3 className="font-semibold text-foreground">How it works</h3>
              <ul className="text-sm text-muted space-y-2 list-disc list-inside">
                <li>Select the number of seats you need.</li>
                <li>Lock holds the seats for 10 minutes.</li>
                <li>Confirm to finalize the booking.</li>
              </ul>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}


