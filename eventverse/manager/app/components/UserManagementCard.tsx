"use client";

import type { CSSProperties } from "react";
import { UserListItem } from "../types";

type Props = {
  userForm: { email: string; password: string };
  onChange: (updater: (prev: { email: string; password: string }) => { email: string; password: string }) => void;
  onCreate: () => Promise<void>;
  onUpdate: () => Promise<void>;
  onDelete: () => Promise<void>;
  userSaving: boolean;
  userMessage: string;
  users: UserListItem[];
  usersLoading: boolean;
  onRefresh: () => void;
  onSelectUser: (user: UserListItem) => void;
  inputStyle: CSSProperties;
  cardStyle: CSSProperties;
};

export default function UserManagementCard({
  userForm,
  onChange,
  onCreate,
  onUpdate,
  onDelete,
  userSaving,
  userMessage,
  users,
  usersLoading,
  onRefresh,
  onSelectUser,
  inputStyle,
  cardStyle,
}: Props) {
  return (
    <section style={{ ...cardStyle }} className="glass card-border">
      <h2 style={{ marginTop: 0, marginBottom: 10 }}>Manage users</h2>
      {userMessage && (
        <div
          style={{
            marginBottom: 12,
            padding: 10,
            borderRadius: 10,
            border: "1px solid var(--card-border)",
            background: "rgba(255,255,255,0.04)",
          }}
        >
          {userMessage}
        </div>
      )}
      <div style={{ display: "grid", gap: 12, maxWidth: 420 }}>
        <label>
          Email
          <input
            type="email"
            placeholder="user@example.com"
            value={userForm.email}
            onChange={(e) => onChange((p) => ({ ...p, email: e.target.value }))}
            style={inputStyle}
          />
        </label>
        <label>
          Password
          <input
            type="password"
            placeholder="Password"
            value={userForm.password}
            onChange={(e) => onChange((p) => ({ ...p, password: e.target.value }))}
            style={inputStyle}
          />
        </label>
        <button
          onClick={onCreate}
          disabled={userSaving}
          style={{
            padding: "10px 12px",
            background: "linear-gradient(120deg, var(--accent-1), var(--accent-2))",
            color: "#1d130c",
            border: "none",
            borderRadius: 10,
            cursor: "pointer",
            boxShadow: "0 10px 30px rgba(232, 195, 158, 0.35)",
            minWidth: 150,
          }}
        >
          {userSaving ? "Saving..." : "Create user"}
        </button>
        {users.length > 0 && (
          <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
            <button
              onClick={onUpdate}
              disabled={userSaving}
              style={{
                padding: "10px 12px",
                background: "linear-gradient(120deg, var(--accent-2), var(--accent-3))",
                color: "#1d130c",
                border: "none",
                borderRadius: 10,
                cursor: "pointer",
                boxShadow: "0 10px 30px rgba(194, 122, 72, 0.35)",
                minWidth: 140,
              }}
            >
              {userSaving ? "Saving..." : "Update selected"}
            </button>
            <button
              onClick={onDelete}
              disabled={userSaving}
              style={{
                padding: "10px 12px",
                background: "rgba(255,0,0,0.08)",
                color: "#ffb4b4",
                border: "1px solid rgba(255,0,0,0.25)",
                borderRadius: 10,
                cursor: "pointer",
                minWidth: 140,
              }}
            >
              {userSaving ? "Deleting..." : "Delete selected"}
            </button>
          </div>
        )}
      </div>
      <div style={{ marginTop: 16, display: "grid", gap: 8 }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 8 }}>
          <h3 style={{ margin: 0 }}>Users</h3>
          <button
            onClick={onRefresh}
            disabled={usersLoading}
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
            {usersLoading ? "Refreshing..." : "Refresh"}
          </button>
        </div>
        {users.length === 0 ? (
          <div style={{ color: "var(--muted)" }}>
            {usersLoading ? "Loading users..." : "No users yet."}
          </div>
        ) : (
          <div
            style={{
              border: "1px solid var(--card-border)",
              borderRadius: 12,
              overflow: "hidden",
            }}
          >
            {users.map((u, idx) => (
              <div
                key={`${u.email}-${idx}`}
                style={{
                  display: "grid",
                  gridTemplateColumns: "1fr 120px 180px",
                  padding: "10px 12px",
                  background: idx % 2 === 0 ? "rgba(255,255,255,0.02)" : "transparent",
                  cursor: "pointer",
                }}
                onClick={() => onSelectUser(u)}
              >
                <div style={{ color: "var(--foreground)" }}>{u.email}</div>
                <div style={{ color: "var(--muted)" }}>{u.id ? `ID: ${u.id}` : "pending id"}</div>
                <div style={{ color: "var(--muted)", fontFamily: "monospace", fontSize: 12 }}>
                  {u.passwordHash || "•••"}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

