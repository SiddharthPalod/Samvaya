"use client";

import { useRef } from "react";
import type { CSSProperties } from "react";
import { EventPayload } from "../types";

type Props = {
  eventForm: EventPayload;
  onChange: (updater: (prev: EventPayload) => EventPayload) => void;
  onSave: () => void;
  onDelete: () => void;
  onClear: () => void;
  saving: boolean;
  deleting: boolean;
  selectedEventId: number | null;
  cardStyle: CSSProperties;
  inputStyle: CSSProperties;
};

export default function EventFormCard({
  eventForm,
  onChange,
  onSave,
  onDelete,
  onClear,
  saving,
  deleting,
  selectedEventId,
  cardStyle,
  inputStyle,
}: Props) {
  const uploadingRef = useRef(false);
  return (
    <div style={cardStyle} className="glass card-border">
      <h2 style={{ marginTop: 0, marginBottom: 10 }}>{selectedEventId ? "Edit event" : "Create event"}</h2>
      <div style={{ display: "grid", gap: 12, marginTop: 4 }}>
        <label>
          Title
          <input
            id="event-title"
            placeholder="Title"
            value={eventForm.title}
            onChange={(e) => onChange((p) => ({ ...p, title: e.target.value }))}
            style={inputStyle}
          />
        </label>
        <label style={{ display: "grid", gap: 6, alignItems: "start" }}>
          Description
          <textarea
            id="event-description"
            placeholder="Description"
            value={eventForm.description}
            onChange={(e) => onChange((p) => ({ ...p, description: e.target.value }))}
            style={{ ...inputStyle, minHeight: 80 }}
          />
        </label>
        <label>
          City
          <input
            id="event-city"
            placeholder="City"
            value={eventForm.city}
            onChange={(e) => onChange((p) => ({ ...p, city: e.target.value }))}
            style={inputStyle}
          />
        </label>
        <label>
          Time
          <input
            id="event-time"
            type="datetime-local"
            value={eventForm.time}
            onChange={(e) => onChange((p) => ({ ...p, time: e.target.value }))}
            style={inputStyle}
          />
        </label>
        <label>
          Capacity
          <input
            id="event-capacity"
            type="number"
            placeholder="Capacity"
            value={eventForm.capacity}
            onChange={(e) => onChange((p) => ({ ...p, capacity: Number(e.target.value) }))}
            style={inputStyle}
          />
        </label>
        <label>
          Organizer ID
          <input
            id="event-organizer"
            type="number"
            placeholder="Organizer ID"
            value={eventForm.organizerId}
            onChange={(e) => onChange((p) => ({ ...p, organizerId: Number(e.target.value) }))}
            style={inputStyle}
          />
        </label>
        <label>
          Venue
          <input
            id="event-venue"
            placeholder="Venue"
            value={eventForm.venue}
            onChange={(e) => onChange((p) => ({ ...p, venue: e.target.value }))}
            style={inputStyle}
          />
        </label>
        <label>
          Category
          <input
            id="event-category"
            placeholder="Category"
            value={eventForm.category}
            onChange={(e) => onChange((p) => ({ ...p, category: e.target.value }))}
            style={inputStyle}
          />
        </label>
        <label style={{ display: "grid", gap: 6 }}>
          <span>Image</span>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <input
              id="event-image-file"
              type="file"
              accept="image/*"
              onChange={async (e) => {
                if (uploadingRef.current) return;
                const file = e.target.files?.[0];
                if (file) {
                  try {
                    uploadingRef.current = true;
                    const formData = new FormData();
                    formData.append("file", file);
                    
                    const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8085";
                    const ADMIN_BEARER = process.env.NEXT_PUBLIC_ADMIN_BEARER || "Bearer dev-super-admin-token";
                    
                    // Try via API gateway first, then fall back directly to media-service
                    const endpoints = [
                      `${API_URL}/media/upload`,
                      "http://localhost:8087/media/upload",
                    ];

                    let uploadedUrl: string | null = null;
                    let lastError: any = null;

                    for (const endpoint of endpoints) {
                      try {
                        const response = await fetch(endpoint, {
                          method: "POST",
                          body: formData,
                          headers: {
                            Authorization: ADMIN_BEARER,
                          },
                        });
                        if (response.ok) {
                          const data = await response.json();
                          uploadedUrl = data.url;
                          break;
                        } else {
                          const errorData = await response.json().catch(() => ({ error: "Upload failed" }));
                          lastError = errorData.error || response.statusText;
                        }
                      } catch (err) {
                        lastError = err;
                      }
                    }

                    if (uploadedUrl) {
                      onChange((p) => ({ ...p, imageUrl: uploadedUrl }));
                    } else {
                      alert(`Failed to upload image: ${lastError || "network error"}`);
                    }
                  } catch (error) {
                    console.error("Upload error:", error);
                    alert("Failed to upload image. Please check if the media service is running.");
                  } finally {
                    uploadingRef.current = false;
                    // reset input so selecting the same file again re-triggers change
                    e.target.value = "";
                  }
                }
              }}
              style={{ ...inputStyle, padding: "8px" }}
            />
            <span style={{ fontSize: "0.85em", color: "var(--muted)" }}>or</span>
            <input
              id="event-image-url"
              placeholder="https://..."
              value={eventForm.imageUrl || ""}
              onChange={(e) => onChange((p) => ({ ...p, imageUrl: e.target.value }))}
              style={{ ...inputStyle, flex: 1 }}
            />
          </div>
          {eventForm.imageUrl && (
            <img
              src={eventForm.imageUrl}
              alt="Preview"
              style={{
                maxWidth: "100%",
                maxHeight: 200,
                marginTop: 8,
                borderRadius: 8,
                objectFit: "cover",
              }}
              onError={(e) => {
                (e.target as HTMLImageElement).style.display = "none";
              }}
            />
          )}
        </label>
        <label>
          Price
          <input
            id="event-price"
            type="number"
            step="0.01"
            placeholder="Price"
            value={eventForm.price}
            onChange={(e) => onChange((p) => ({ ...p, price: Number(e.target.value) }))}
            style={inputStyle}
          />
        </label>
        <label style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <input
            id="event-public"
            type="checkbox"
            checked={eventForm.publicEvent}
            onChange={(e) => onChange((p) => ({ ...p, publicEvent: e.target.checked }))}
          />
          Public event
        </label>
        <div style={{ display: "flex", gap: 10, flexWrap: "wrap", marginTop: 4 }}>
          <button
            onClick={onSave}
            disabled={saving}
            style={{
              padding: "10px 14px",
              background: "linear-gradient(120deg, var(--accent-1), var(--accent-2), var(--accent-3))",
              color: "#1d130c",
              border: "none",
              borderRadius: 10,
              cursor: "pointer",
              boxShadow: "0 10px 30px rgba(194, 122, 72, 0.35)",
              minWidth: 150,
              fontWeight: 600,
            }}
          >
            {saving ? "Saving..." : selectedEventId ? "Save changes" : "Create"}
          </button>
          {selectedEventId && (
            <button
              onClick={onClear}
              style={{
                padding: "10px 14px",
                background: "transparent",
                color: "var(--foreground)",
                border: "1px solid var(--card-border)",
                borderRadius: 10,
                cursor: "pointer",
                minWidth: 150,
              }}
            >
              Clear selection
            </button>
          )}
          {selectedEventId && (
            <button
              onClick={onDelete}
              disabled={deleting}
              style={{
                padding: "10px 14px",
                background: "rgba(255,0,0,0.08)",
                color: "#ffb4b4",
                border: "1px solid rgba(255,0,0,0.25)",
                borderRadius: 10,
                cursor: "pointer",
                minWidth: 150,
              }}
            >
              {deleting ? "Deleting..." : "Delete event"}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

