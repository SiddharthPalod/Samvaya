"use client";

import type { CSSProperties } from "react";
import { Inventory, EventResponse } from "../types";

type Props = {
  inventoryForm: Inventory;
  onChange: (updater: (prev: Inventory) => Inventory) => void;
  events: EventResponse[];
  inventorySaving: boolean;
  onSave: () => void;
  onSelectEvent: (id: number) => void;
  inputStyle: CSSProperties;
  cardStyle: CSSProperties;
};

export default function InventoryCard({
  inventoryForm,
  onChange,
  events,
  inventorySaving,
  onSave,
  onSelectEvent,
  inputStyle,
  cardStyle,
}: Props) {
  return (
    <div style={cardStyle} className="glass card-border">
      <h2 style={{ marginTop: 0, marginBottom: 10 }}>Inventory</h2>
      <div style={{ display: "grid", gap: 12, marginTop: 4 }}>
        <label>
          Event selection
          <select
            value={inventoryForm.eventId}
            onChange={async (e) => {
              const id = Number(e.target.value);
              onSelectEvent(id);
            }}
            style={inputStyle}
          >
            <option value={0}>Select event</option>
            {events.map((ev) => (
              <option key={ev.id} value={ev.id}>
                #{ev.id} - {ev.title}
              </option>
            ))}
          </select>
        </label>
        <label>
          Total seats
          <input
            type="number"
            placeholder="Total seats"
            value={inventoryForm.totalSeats}
            onChange={(e) =>
              onChange((p) => ({
                ...p,
                totalSeats: Number(e.target.value),
              }))
            }
            style={inputStyle}
          />
        </label>
        <label>
          Available seats
          <input
            type="number"
            placeholder="Available seats"
            value={inventoryForm.availableSeats}
            onChange={(e) =>
              onChange((p) => ({
                ...p,
                availableSeats: Number(e.target.value),
              }))
            }
            style={inputStyle}
          />
        </label>
        <button
          onClick={onSave}
          disabled={inventorySaving}
          style={{
            padding: "10px 12px",
            background: "linear-gradient(120deg, var(--accent-2), var(--accent-1))",
            color: "#1d130c",
            border: "none",
            borderRadius: 10,
            cursor: "pointer",
            boxShadow: "0 10px 30px rgba(232, 195, 158, 0.35)",
          }}
        >
          {inventorySaving ? "Saving..." : "Save inventory"}
        </button>
      </div>
    </div>
  );
}

