"use client";

import type { CSSProperties } from "react";
import { EventResponse } from "../types";

type Props = {
  events: EventResponse[];
  selectedEventId: number | null;
  onSelect: (id: number) => void;
  loading: boolean;
  onRefresh: () => void;
  cardStyle: CSSProperties;
};

export default function EventsListCard({
  events,
  selectedEventId,
  onSelect,
  loading,
  onRefresh,
  cardStyle,
}: Props) {
  return (
    <section style={{ ...cardStyle, marginBottom: 20 }} className="glass card-border">
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          marginBottom: 12,
          gap: 12,
          flexWrap: "wrap",
        }}
      >
        <h2 style={{ margin: 0 }}>Events</h2>
        <button
          onClick={onRefresh}
          disabled={loading}
          style={{
            padding: "8px 10px",
            background: "transparent",
            color: "var(--foreground)",
            border: "1px solid var(--card-border)",
            borderRadius: 10,
            cursor: "pointer",
            minWidth: 110,
          }}
        >
          {loading ? "Refreshing..." : "Refresh"}
        </button>
      </div>
      <div style={{ display: "grid", gap: 10 }}>
        {events.map((ev) => (
          <div
            key={ev.id}
            style={{
              padding: 12,
              borderRadius: 12,
              border: "1px solid var(--card-border)",
              background: selectedEventId === ev.id ? "rgba(255,255,255,0.06)" : "rgba(0,0,0,0.1)",
              cursor: "pointer",
              transition: "transform 120ms ease, box-shadow 120ms ease",
              boxShadow:
                selectedEventId === ev.id
                  ? "0 10px 24px rgba(0,0,0,0.15)"
                  : "0 4px 14px rgba(0,0,0,0.12)",
            }}
            onClick={() => onSelect(ev.id)}
          >
            <div style={{ display: "flex", justifyContent: "space-between" }}>
              <div>
                <strong>#{ev.id}</strong> {ev.title} — {ev.city}
              </div>
              <div style={{ color: "var(--accent-2)" }}>{`₹${(ev.price ?? 0).toFixed(2)}`}</div>
            </div>
            <div style={{ color: "var(--muted)", marginTop: 4 }}>
              {ev.category} | capacity {ev.capacity} | organizer {ev.organizerId}
            </div>
          </div>
        ))}
        {!events.length && <div>No events yet.</div>}
      </div>
    </section>
  );
}

