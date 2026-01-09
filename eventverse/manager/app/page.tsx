"use client";

import type { CSSProperties } from "react";
import { useEffect, useMemo, useState } from "react";
import api from "@/lib/api";
import Sidebar from "./components/Sidebar";
import EventFormCard from "./components/EventFormCard";
import InventoryCard from "./components/InventoryCard";
import EventsListCard from "./components/EventsListCard";
import UserManagementCard from "./components/UserManagementCard";
import WebhookManagementCard from "./components/WebhookManagementCard";
import AnalyticsDashboardCard from "./components/AnalyticsDashboardCard";
import {
  EventPayload,
  EventResponse,
  Inventory,
  UserListItem,
  AnalyticsSummary,
  RealtimeEventSnapshot,
} from "./types";

const emptyEvent: EventPayload = {
  title: "",
  description: "",
  city: "",
  time: "",
  capacity: 0,
  organizerId: 0,
  venue: "",
  category: "",
  imageUrl: "",
  publicEvent: true,
  price: 0,
};

const normalizeEvent = (event?: Partial<EventResponse>): EventPayload => ({
  title: event?.title ?? "",
  description: event?.description ?? "",
  city: event?.city ?? "",
  time: event?.time ? new Date(event.time).toISOString().slice(0, 16) : "",
  capacity: event?.capacity ?? 0,
  organizerId: event?.organizerId ?? 0,
  venue: event?.venue ?? "",
  category: event?.category ?? "",
  imageUrl: event?.imageUrl ?? "",
  publicEvent: event?.publicEvent ?? true,
  price: event?.price ?? 0,
});

export default function Page() {
  const [activeTab, setActiveTab] = useState<"events" | "users" | "webhooks" | "analytics">(
    "events"
  );
  const [events, setEvents] = useState<EventResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [inventorySaving, setInventorySaving] = useState(false);
  const [eventForm, setEventForm] = useState<EventPayload>(emptyEvent);
  const [selectedEventId, setSelectedEventId] = useState<number | null>(null);
  const [inventoryForm, setInventoryForm] = useState<Inventory>({
    eventId: 0,
    totalSeats: 0,
    availableSeats: 0,
  });
  const [message, setMessage] = useState("");
  const [userMessage, setUserMessage] = useState("");
  const [userSaving, setUserSaving] = useState(false);
  const [userForm, setUserForm] = useState({ email: "", password: "" });
  const [users, setUsers] = useState<UserListItem[]>([]);
  const [usersLoading, setUsersLoading] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [analyticsSummary, setAnalyticsSummary] = useState<AnalyticsSummary | null>(null);
  const [realtimeSnapshot, setRealtimeSnapshot] = useState<RealtimeEventSnapshot | null>(null);
  const [analyticsLoading, setAnalyticsLoading] = useState(false);

  const selectedEvent = useMemo(
    () => events.find((e) => e.id === selectedEventId) || null,
    [events, selectedEventId]
  );

  useEffect(() => {
    setEventForm(normalizeEvent(selectedEvent || undefined));
  }, [selectedEvent]);

  const fetchEvents = async () => {
    setLoading(true);
    try {
      const res = await api.get("/admin/events");
      setEvents(res.data?.content ?? res.data ?? []);
    } catch (err) {
      setMessage("Failed to load events");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  const upsertEvent = async () => {
    setSaving(true);
    setMessage("");
    const payload = {
      ...eventForm,
      time: eventForm.time ? new Date(eventForm.time).toISOString() : new Date().toISOString(),
      price: Number(eventForm.price || 0),
      capacity: Number(eventForm.capacity || 0),
      organizerId: Number(eventForm.organizerId || 0),
      imageUrl: eventForm.imageUrl || undefined,
    };
    try {
      if (selectedEventId) {
        await api.put(`/admin/events/${selectedEventId}`, payload);
        setMessage("Event updated");
      } else {
        await api.post("/admin/events", payload);
        setMessage("Event created");
      }
      await fetchEvents();
    } catch (err: any) {
      setMessage(err?.response?.data?.message || "Failed to save event");
    } finally {
      setSaving(false);
    }
  };

  const deleteEvent = async () => {
    if (!selectedEventId) {
      setMessage("Select an event to delete");
      return;
    }
    const confirmed = window.confirm("Delete this event? This cannot be undone.");
    if (!confirmed) return;
    setDeleting(true);
    setMessage("");
    try {
      await api.delete(`/admin/events/${selectedEventId}`);
      setMessage("Event deleted");
      setSelectedEventId(null);
      setEventForm(emptyEvent);
      setInventoryForm({ eventId: 0, totalSeats: 0, availableSeats: 0 });
      await fetchEvents();
    } catch (err: any) {
      setMessage(err?.response?.data?.message || "Failed to delete event");
    } finally {
      setDeleting(false);
    }
  };

  const loadInventory = async (eventId: number) => {
    try {
      const res = await api.get<Inventory>(`/admin/inventory/${eventId}`);
      setInventoryForm(res.data);
    } catch {
      setInventoryForm({ eventId, totalSeats: 0, availableSeats: 0 });
    }
  };

  const handleSelectEvent = async (eventId: number) => {
    setSelectedEventId(eventId || null);
    setInventoryForm((p) => ({ ...p, eventId }));
    if (eventId) {
      await loadInventory(eventId);
    } else {
      setEventForm(normalizeEvent());
      setInventoryForm({ eventId: 0, totalSeats: 0, availableSeats: 0 });
    }
  };

  const saveInventory = async () => {
    if (!inventoryForm.eventId) {
      setMessage("Select an event to manage inventory");
      return;
    }
    setInventorySaving(true);
    try {
      await api.put(`/admin/inventory/${inventoryForm.eventId}`, {
        totalSeats: Number(inventoryForm.totalSeats),
        availableSeats: Number(inventoryForm.availableSeats),
      });
      setMessage("Inventory saved");
    } catch (err: any) {
      setMessage(err?.response?.data?.message || "Failed to save inventory");
    } finally {
      setInventorySaving(false);
    }
  };

  const createUser = async () => {
    setUserMessage("");
    setUserSaving(true);
    try {
      const { data } = await api.post("/auth/register", {
        email: userForm.email,
        password: userForm.password,
      });
      setUserMessage("User created");
      setUsers((prev) => [{ id: data?.userId ?? null, email: userForm.email }, ...prev]);
      setUserForm({ email: "", password: "" });
      setSelectedUserId(data?.userId ?? null);
    } catch (err: any) {
      setUserMessage(err?.response?.data?.message || "Failed to create user");
    } finally {
      setUserSaving(false);
    }
  };

  const updateUser = async () => {
    if (!selectedUserId) {
      setUserMessage("Select a user to update");
      return;
    }
    setUserMessage("");
    setUserSaving(true);
    try {
      await api.put(
        `/auth/admin/users/${selectedUserId}`,
        {
          email: userForm.email,
          newPassword: userForm.password || undefined,
        },
        { headers: { "X-Admin-Superuser": "true" } }
      );
      setUserMessage("User updated");
      await fetchUsers();
    } catch (err: any) {
      setUserMessage(
        err?.response?.data?.message ||
          err?.response?.data?.error ||
          "Failed to update user"
      );
    } finally {
      setUserSaving(false);
    }
  };

  const deleteUser = async () => {
    if (!selectedUserId) {
      setUserMessage("Select a user to delete");
      return;
    }
    const confirmed = window.confirm("Delete this user? This cannot be undone.");
    if (!confirmed) return;
    setUserMessage("");
    setUserSaving(true);
    try {
      await api.delete(`/auth/admin/users/${selectedUserId}`, {
        headers: { "X-Admin-Superuser": "true" },
      });
      setUserMessage("User deleted");
      setSelectedUserId(null);
      setUserForm({ email: "", password: "" });
      await fetchUsers();
    } catch (err: any) {
      setUserMessage(
        err?.response?.data?.message ||
          err?.response?.data?.error ||
          "Failed to delete user"
      );
    } finally {
      setUserSaving(false);
    }
  };

  const fetchUsers = async () => {
    setUsersLoading(true);
    setUserMessage("");
    try {
      const { data } = await api.get<UserListItem[]>("/auth/admin/users/full", {
        headers: { "X-Admin-Superuser": "true" },
      });
      setUsers(data);
      // maintain selection if still present
      if (selectedUserId) {
        const found = data.find((u) => u.id === selectedUserId);
        if (found) {
          setUserForm({ email: found.email, password: "" });
        } else {
          setSelectedUserId(null);
          setUserForm({ email: "", password: "" });
        }
      }
    } catch (err: any) {
      setUserMessage(
        err?.response?.data?.message ||
          err?.response?.data?.error ||
          "Failed to load users"
      );
    } finally {
      setUsersLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === "users") {
      fetchUsers();
    }
    if (activeTab === "analytics") {
      refreshAnalytics();
    }
  }, [activeTab]);

  useEffect(() => {
    if (activeTab === "analytics" && selectedEventId) {
      fetchRealtime(selectedEventId);
    } else if (activeTab === "analytics") {
      setRealtimeSnapshot(null);
    }
  }, [activeTab, selectedEventId]);

  const fetchRealtime = async (eventId: number) => {
    try {
      const { data } = await api.get<RealtimeEventSnapshot>(`/analytics/realtime/event/${eventId}`);
      setRealtimeSnapshot(data);
    } catch {
      setRealtimeSnapshot(null);
    }
  };

  const refreshAnalytics = async () => {
    setAnalyticsLoading(true);
    try {
      const { data } = await api.get<AnalyticsSummary>("/analytics/dashboard/summary?days=7");
      setAnalyticsSummary(data);
      if (selectedEventId) {
        await fetchRealtime(selectedEventId);
      }
    } catch (err) {
      setMessage("Failed to load analytics");
    } finally {
      setAnalyticsLoading(false);
    }
  };

  const cardStyle: CSSProperties = {
    background: "var(--card)",
    padding: 20,
    borderRadius: 16,
    border: "1px solid var(--card-border)",
    boxShadow: "0 10px 30px rgba(0,0,0,0.12)",
  };

  const inputStyle: CSSProperties = {
    padding: "10px 12px",
    borderRadius: 10,
    border: "1px solid var(--card-border)",
    background: "rgba(255,255,255,0.04)",
    color: "var(--foreground)",
  };

  return (
    <main style={{ padding: "32px 24px 48px", maxWidth: 1200, margin: "0 auto" }}>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ marginBottom: 6, color: "var(--foreground)" }}>Samvaya Manager (Super Admin)</h1>
        <p style={{ margin: 0, color: "var(--muted)" }}>Manage events, inventory, and users.</p>
      </div>

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "230px 1fr",
          gap: 20,
          alignItems: "start",
        }}
      >
        <Sidebar activeTab={activeTab} onChange={setActiveTab} cardStyle={cardStyle} />

        <div>
          {activeTab === "events" && (
            <>
              {message && (
                <div
                  style={{
                    marginBottom: 16,
                    padding: 12,
                    borderRadius: 12,
                    background: "var(--card)",
                    border: "1px solid var(--card-border)",
                  }}
                  className="glass card-border"
                >
                  {message}
                </div>
              )}

              <section
                style={{
                  display: "grid",
                  gridTemplateColumns: "1.1fr 0.9fr",
                  gap: 20,
                  marginBottom: 28,
                  alignItems: "start",
                }}
              >
                <EventFormCard
                  eventForm={eventForm}
                  onChange={setEventForm}
                  onSave={upsertEvent}
                  onDelete={deleteEvent}
                  onClear={() => {
                    setSelectedEventId(null);
                    setEventForm(emptyEvent);
                    setInventoryForm({ eventId: 0, totalSeats: 0, availableSeats: 0 });
                  }}
                  saving={saving}
                  deleting={deleting}
                  selectedEventId={selectedEventId}
                  cardStyle={cardStyle}
                  inputStyle={inputStyle}
                />

                <InventoryCard
                  inventoryForm={inventoryForm}
                  onChange={setInventoryForm}
                  events={events}
                  inventorySaving={inventorySaving}
                  onSave={saveInventory}
                  onSelectEvent={handleSelectEvent}
                  inputStyle={inputStyle}
                  cardStyle={cardStyle}
                />
              </section>

              <EventsListCard
                events={events}
                selectedEventId={selectedEventId}
                onSelect={handleSelectEvent}
                loading={loading}
                onRefresh={fetchEvents}
                cardStyle={cardStyle}
              />
            </>
          )}

          {activeTab === "analytics" && (
            <AnalyticsDashboardCard
              summary={analyticsSummary}
              realtime={realtimeSnapshot}
              onRefresh={refreshAnalytics}
              loading={analyticsLoading}
              cardStyle={cardStyle}
            />
          )}
        </div>
      </div>

      {activeTab === "users" && (
        <div style={{ marginTop: 20 }}>
          <UserManagementCard
            userForm={userForm}
            onChange={setUserForm}
            onCreate={createUser}
            onUpdate={updateUser}
            onDelete={deleteUser}
            userSaving={userSaving}
            userMessage={userMessage}
            users={users}
            usersLoading={usersLoading}
            onRefresh={fetchUsers}
            onSelectUser={(u) => {
              setSelectedUserId(u.id ?? null);
              setUserForm({ email: u.email, password: "" });
            }}
            inputStyle={inputStyle}
            cardStyle={cardStyle}
          />
        </div>
      )}

      {activeTab === "webhooks" && (
        <div style={{ marginTop: 20 }}>
          <WebhookManagementCard inputStyle={inputStyle} cardStyle={cardStyle} />
        </div>
      )}
    </main>
  );
}

