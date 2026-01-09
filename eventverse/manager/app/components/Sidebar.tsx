"use client";

import type { CSSProperties } from "react";

type Props = {
  activeTab: "events" | "users" | "webhooks" | "analytics";
  onChange: (tab: "events" | "users" | "webhooks" | "analytics") => void;
  cardStyle: CSSProperties;
};

export default function Sidebar({ activeTab, onChange, cardStyle }: Props) {
  const baseBtn: CSSProperties = {
    padding: "10px 12px",
    textAlign: "left",
    borderRadius: 10,
    border: "1px solid var(--card-border)",
    color: "var(--foreground)",
    cursor: "pointer",
    background: "transparent",
  };

  const navStyle: CSSProperties = {
    ...cardStyle,
    position: "sticky",
    top: 24,
    padding: 16,
    gap: 10,
    display: "flex",
    flexDirection: "column",
  };

  return (
    <nav className="glass card-border" style={navStyle}>
      <button
        onClick={() => onChange("events")}
        style={{
          ...baseBtn,
          background: activeTab === "events" ? "rgba(255,255,255,0.08)" : "transparent",
        }}
      >
        Event management
      </button>
      <button
        onClick={() => onChange("users")}
        style={{
          ...baseBtn,
          background: activeTab === "users" ? "rgba(255,255,255,0.08)" : "transparent",
        }}
      >
        User management
      </button>
      <button
        onClick={() => onChange("webhooks")}
        style={{
          ...baseBtn,
          background: activeTab === "webhooks" ? "rgba(255,255,255,0.08)" : "transparent",
        }}
      >
        Webhook management
      </button>
      <button
        onClick={() => onChange("analytics")}
        style={{
          ...baseBtn,
          background: activeTab === "analytics" ? "rgba(255,255,255,0.08)" : "transparent",
        }}
      >
        Analytics dashboard
      </button>
    </nav>
  );
}

