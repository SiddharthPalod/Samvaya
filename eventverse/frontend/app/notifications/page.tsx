"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Navbar from "@/components/Navbar";
import api from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { useTheme } from "@/context/ThemeContext";
import { Bell, CheckCircle2, XCircle, TicketCheck, Calendar, DollarSign, RefreshCw } from "lucide-react";

type Notification = {
  id: number;
  type: string;
  eventId: string;
  status: string;
  createdAt: string;
  payload: {
    ticketId?: string;
    eventIdRef?: string;
    userId?: string;
    amount?: number;
    type?: string;
    occurredAt?: string;
  };
};

export default function NotificationsPage() {
  const { user, loading: authLoading } = useAuth();
  const { theme } = useTheme();
  const router = useRouter();
  const isLight = theme === "light";
  
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      router.push("/login");
      return;
    }
    fetchNotifications();
  }, [authLoading, user, router]);

  const fetchNotifications = async () => {
    try {
      setError(null);
      if (!user?.id) return;
      // Convert userId to string to ensure proper API call
      const { data } = await api.get<Notification[]>(`/webhooks/notifications/user/${String(user.id)}`, {
        params: { page: 0, size: 50 }
      });
      setNotifications(data || []);
    } catch (err: any) {
      console.error("Failed to load notifications", err);
      setError(err.response?.data?.message || "Failed to load notifications");
      setNotifications([]); // Set empty array on error
    } finally {
      setLoading(false);
    }
  };

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case "TICKET_CONFIRMED":
        return <CheckCircle2 className="h-5 w-5 text-green-400" />;
      case "TICKET_CANCELLED":
        return <XCircle className="h-5 w-5 text-red-400" />;
      default:
        return <Bell className="h-5 w-5 text-muted" />;
    }
  };

  const getNotificationTitle = (notification: Notification) => {
    switch (notification.type) {
      case "TICKET_CONFIRMED":
        return "Ticket Confirmed";
      case "TICKET_CANCELLED":
        return "Ticket Cancelled";
      default:
        return "Notification";
    }
  };

  const getNotificationMessage = (notification: Notification) => {
    const { payload } = notification;
    switch (notification.type) {
      case "TICKET_CONFIRMED":
        return `Your ticket for event ${payload.eventIdRef || "N/A"} has been confirmed.`;
      case "TICKET_CANCELLED":
        return `Your ticket for event ${payload.eventIdRef || "N/A"} has been cancelled.`;
      default:
        return "You have a new notification.";
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const formatPrice = (amount?: number) => {
    if (!amount) return "N/A";
    return `₹${amount.toFixed(2)}`;
  };

  return (
    <div className="min-h-screen text-foreground">
      <Navbar />
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 pt-24 pb-16">
        <div className="flex items-center justify-between mb-6">
          <div>
            <p className="pill inline-flex px-3 py-1 rounded-full text-xs text-foreground">Your Activity</p>
            <h1 className="text-2xl font-semibold mt-2">Notifications</h1>
            <p className="text-sm text-muted mt-1">View all notifications sent to you</p>
          </div>
          <button
            onClick={fetchNotifications}
            disabled={loading}
            className={`inline-flex items-center gap-2 px-4 py-2 rounded-full text-sm font-semibold transition-transform ${
              loading
                ? "opacity-50 cursor-not-allowed"
                : isLight
                ? "text-[#120a06] bg-gradient-to-br from-white to-[#f0e4d5] border border-black/10 hover:-translate-y-px"
                : "text-[#120a06] bg-white hover:bg-white/90 hover:-translate-y-px"
            }`}
          >
            <RefreshCw className={`h-4 w-4 ${loading ? "animate-spin" : ""}`} />
            Refresh
          </button>
        </div>

        {loading ? (
          <div className="glass rounded-3xl p-6 border border-white/15 shadow-smooth">
            <div className="animate-pulse space-y-4">
              {[1, 2, 3, 4].map((i) => (
                <div key={i} className="h-24 bg-white/5 rounded-2xl" />
              ))}
            </div>
          </div>
        ) : error ? (
          <div className={`glass rounded-3xl p-6 border ${
            isLight ? "border-red-400/30 bg-red-50/50" : "border-red-400/30 bg-red-500/10"
          } shadow-smooth`}>
            <p className="font-medium text-red-200">{error}</p>
            <button
              onClick={fetchNotifications}
              className="mt-3 px-4 py-2 rounded-full bg-red-500 text-white hover:bg-red-600 transition-colors"
            >
              Retry
            </button>
          </div>
        ) : notifications.length === 0 ? (
          <div className="glass rounded-3xl p-12 border border-white/15 shadow-smooth text-center">
            <Bell className="h-12 w-12 mx-auto mb-4 text-muted" />
            <p className="text-foreground font-medium mb-2">No notifications yet</p>
            <p className="text-sm text-muted">You'll receive notifications when tickets are confirmed or cancelled</p>
          </div>
        ) : (
          <div className="space-y-4">
            {notifications.map((notification) => (
              <div
                key={notification.id}
                className={`glass rounded-3xl p-6 border ${
                  notification.type === "TICKET_CONFIRMED"
                    ? isLight
                      ? "border-green-400/30 bg-green-50/30"
                      : "border-green-400/30 bg-green-500/10"
                    : notification.type === "TICKET_CANCELLED"
                    ? isLight
                      ? "border-red-400/30 bg-red-50/30"
                      : "border-red-400/30 bg-red-500/10"
                    : "border-white/15"
                } shadow-smooth`}
              >
                <div className="flex items-start gap-4">
                  <div className={`p-3 rounded-xl ${
                    isLight
                      ? "bg-white/80"
                      : "bg-white/10"
                  }`}>
                    {getNotificationIcon(notification.type)}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-start justify-between mb-2">
                      <div>
                        <h3 className="text-lg font-semibold text-foreground">
                          {getNotificationTitle(notification)}
                        </h3>
                        <p className="text-sm text-muted mt-1">
                          {getNotificationMessage(notification)}
                        </p>
                      </div>
                      <span className={`text-xs px-2 py-1 rounded-full ${
                        notification.status === "SUCCESS"
                          ? isLight
                            ? "bg-green-100 text-green-800"
                            : "bg-green-500/20 text-green-300"
                          : isLight
                          ? "bg-yellow-100 text-yellow-800"
                          : "bg-yellow-500/20 text-yellow-300"
                      }`}>
                        {notification.status}
                      </span>
                    </div>
                    
                    <div className="mt-4 grid sm:grid-cols-2 gap-3 text-sm">
                      {notification.payload.ticketId && (
                        <div className={`flex items-center gap-2 p-2 rounded-lg ${
                          isLight ? "bg-white/50" : "bg-white/5"
                        }`}>
                          <TicketCheck className="h-4 w-4 text-foreground/80" />
                          <span className="text-muted">Ticket:</span>
                          <span className="text-foreground font-mono text-xs">
                            {notification.payload.ticketId.slice(0, 8)}...
                          </span>
                        </div>
                      )}
                      {notification.payload.eventIdRef && (
                        <div className={`flex items-center gap-2 p-2 rounded-lg ${
                          isLight ? "bg-white/50" : "bg-white/5"
                        }`}>
                          <Calendar className="h-4 w-4 text-foreground/80" />
                          <span className="text-muted">Event ID:</span>
                          <span className="text-foreground">{notification.payload.eventIdRef}</span>
                        </div>
                      )}
                      {notification.payload.amount != null && notification.payload.amount !== 0 && (
                        <div className={`flex items-center gap-2 p-2 rounded-lg ${
                          isLight ? "bg-white/50" : "bg-white/5"
                        }`}>
                          <DollarSign className="h-4 w-4 text-foreground/80" />
                          <span className="text-muted">Amount:</span>
                          <span className="text-foreground font-semibold">
                            {formatPrice(notification.payload.amount)}
                          </span>
                        </div>
                      )}
                      <div className={`flex items-center gap-2 p-2 rounded-lg ${
                        isLight ? "bg-white/50" : "bg-white/5"
                      }`}>
                        <Calendar className="h-4 w-4 text-foreground/80" />
                        <span className="text-muted">Sent:</span>
                        <span className="text-foreground text-xs">
                          {formatDate(notification.createdAt)}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
