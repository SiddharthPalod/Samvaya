"use client";

import type { CSSProperties } from "react";
import { useEffect, useState } from "react";
import api from "@/lib/api";

type WebhookSubscription = {
  id: number;
  partnerId: string;
  url: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
};

type WebhookDelivery = {
  id: number;
  subscriptionId: number;
  domainEventId: string;
  domainEventType: string;
  payload: string;
  attempt: number;
  status: string;
  lastError: string | null;
  nextAttemptAt: string | null;
  createdAt: string;
  updatedAt: string;
};

type Props = {
  cardStyle: CSSProperties;
  inputStyle: CSSProperties;
};

export default function WebhookManagementCard({ cardStyle, inputStyle }: Props) {
  const [subscriptions, setSubscriptions] = useState<WebhookSubscription[]>([]);
  const [deliveries, setDeliveries] = useState<Map<number, WebhookDelivery[]>>(new Map());
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({ partnerId: "", url: "" });
  const [submitting, setSubmitting] = useState(false);
  const [selectedSubscription, setSelectedSubscription] = useState<number | null>(null);
  const [secret, setSecret] = useState<string | null>(null);

  useEffect(() => {
    fetchSubscriptions();
  }, []);

  const fetchSubscriptions = async () => {
    setLoading(true);
    setMessage("");
    try {
      const { data } = await api.get<WebhookSubscription[]>("/webhooks/list");
      setSubscriptions(data);
      for (const sub of data) {
        await fetchDeliveries(sub.id);
      }
    } catch (err: any) {
      setMessage(err?.response?.data?.message || "Failed to load webhooks");
    } finally {
      setLoading(false);
    }
  };

  const fetchDeliveries = async (subscriptionId: number) => {
    try {
      const { data } = await api.get<WebhookDelivery[]>(`/webhooks/deliveries/${subscriptionId}`, {
        params: { page: 0, size: 20 }
      });
      setDeliveries(prev => new Map(prev).set(subscriptionId, data));
    } catch (err) {
      console.error(`Failed to load deliveries for subscription ${subscriptionId}`, err);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setMessage("");
    try {
      const { data } = await api.post<{ id: number; secret: string }>("/webhooks/register", formData);
      setSecret(data.secret);
      setFormData({ partnerId: "", url: "" });
      setShowForm(false);
      setMessage("Webhook registered successfully");
      await fetchSubscriptions();
    } catch (err: any) {
      setMessage(err?.response?.data?.message || "Failed to register webhook");
    } finally {
      setSubmitting(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "SUCCESS":
        return "rgba(34, 197, 94, 0.2)";
      case "FAILED":
        return "rgba(239, 68, 68, 0.2)";
      case "PENDING":
      case "RETRYING":
        return "rgba(234, 179, 8, 0.2)";
      default:
        return "rgba(255, 255, 255, 0.05)";
    }
  };

  return (
    <div>
      {message && (
        <div className="glass card-border" style={{ ...cardStyle, marginBottom: 16 }}>
          {message}
        </div>
      )}

      <div className="glass card-border" style={cardStyle}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
          <h2 style={{ margin: 0, color: "var(--foreground)" }}>Webhook Subscriptions</h2>
          <button
            onClick={() => setShowForm(!showForm)}
            style={{
              padding: "8px 16px",
              borderRadius: 8,
              border: "1px solid var(--card-border)",
              background: "rgba(255,255,255,0.08)",
              color: "var(--foreground)",
              cursor: "pointer",
            }}
          >
            {showForm ? "Cancel" : "+ Register"}
          </button>
        </div>

        {secret && (
          <div
            style={{
              ...cardStyle,
              marginBottom: 16,
              background: "rgba(34, 197, 94, 0.1)",
              border: "1px solid rgba(34, 197, 94, 0.3)",
            }}
          >
            <h3 style={{ marginTop: 0, color: "var(--foreground)" }}>Webhook Secret</h3>
            <p style={{ fontSize: 12, color: "var(--muted)", marginBottom: 8 }}>
              Save this secret now - it won't be shown again
            </p>
            <code
              style={{
                display: "block",
                padding: 12,
                borderRadius: 8,
                background: "rgba(0,0,0,0.2)",
                color: "var(--foreground)",
                fontSize: 12,
                wordBreak: "break-all",
                fontFamily: "monospace",
              }}
            >
              {secret}
            </code>
            <button
              onClick={() => setSecret(null)}
              style={{
                marginTop: 8,
                padding: "6px 12px",
                borderRadius: 6,
                border: "1px solid var(--card-border)",
                background: "transparent",
                color: "var(--foreground)",
                cursor: "pointer",
                fontSize: 12,
              }}
            >
              Dismiss
            </button>
          </div>
        )}

        {showForm && (
          <form onSubmit={handleSubmit} style={{ marginBottom: 20 }}>
            <div style={{ marginBottom: 12 }}>
              <label style={{ display: "block", marginBottom: 6, fontSize: 14, color: "var(--foreground)" }}>
                Partner ID
              </label>
              <input
                type="text"
                value={formData.partnerId}
                onChange={(e) => setFormData({ ...formData, partnerId: e.target.value })}
                required
                style={inputStyle}
                placeholder="e.g., my-app-integration"
              />
            </div>
            <div style={{ marginBottom: 12 }}>
              <label style={{ display: "block", marginBottom: 6, fontSize: 14, color: "var(--foreground)" }}>
                Webhook URL
              </label>
              <input
                type="url"
                value={formData.url}
                onChange={(e) => setFormData({ ...formData, url: e.target.value })}
                required
                style={inputStyle}
                placeholder="https://your-app.com/webhooks/eventverse"
              />
            </div>
            <button
              type="submit"
              disabled={submitting}
              style={{
                padding: "10px 20px",
                borderRadius: 8,
                border: "none",
                background: "rgba(255,255,255,0.15)",
                color: "var(--foreground)",
                cursor: submitting ? "not-allowed" : "pointer",
                opacity: submitting ? 0.5 : 1,
              }}
            >
              {submitting ? "Registering..." : "Register Webhook"}
            </button>
          </form>
        )}

        {loading ? (
          <div style={{ padding: 20, textAlign: "center", color: "var(--muted)" }}>Loading...</div>
        ) : subscriptions.length === 0 ? (
          <div style={{ padding: 20, textAlign: "center", color: "var(--muted)" }}>
            No webhooks registered
          </div>
        ) : (
          <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
            {subscriptions.map((sub) => {
              const subDeliveries = deliveries.get(sub.id) || [];
              const isExpanded = selectedSubscription === sub.id;

              return (
                <div
                  key={sub.id}
                  style={{
                    ...cardStyle,
                    padding: 16,
                    border: "1px solid var(--card-border)",
                  }}
                >
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "start", marginBottom: 8 }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
                        <strong style={{ color: "var(--foreground)" }}>{sub.partnerId}</strong>
                        <span
                          style={{
                            fontSize: 11,
                            padding: "2px 8px",
                            borderRadius: 4,
                            background: sub.active ? "rgba(34, 197, 94, 0.2)" : "rgba(107, 114, 128, 0.2)",
                            color: sub.active ? "rgb(34, 197, 94)" : "rgb(107, 114, 128)",
                          }}
                        >
                          {sub.active ? "Active" : "Inactive"}
                        </span>
                      </div>
                      <p style={{ fontSize: 12, color: "var(--muted)", margin: 0, wordBreak: "break-all" }}>
                        {sub.url}
                      </p>
                      <p style={{ fontSize: 11, color: "var(--muted)", marginTop: 4 }}>
                        Created: {formatDate(sub.createdAt)}
                      </p>
                    </div>
                    <button
                      onClick={() => setSelectedSubscription(isExpanded ? null : sub.id)}
                      style={{
                        padding: 6,
                        borderRadius: 6,
                        border: "1px solid var(--card-border)",
                        background: "transparent",
                        color: "var(--foreground)",
                        cursor: "pointer",
                      }}
                    >
                      {isExpanded ? "▼" : "▶"}
                    </button>
                  </div>

                  {isExpanded && (
                    <div style={{ marginTop: 12, paddingTop: 12, borderTop: "1px solid var(--card-border)" }}>
                      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 8 }}>
                        <strong style={{ fontSize: 13, color: "var(--foreground)" }}>Delivery Logs</strong>
                        <button
                          onClick={() => fetchDeliveries(sub.id)}
                          style={{
                            fontSize: 11,
                            padding: "4px 8px",
                            borderRadius: 4,
                            border: "1px solid var(--card-border)",
                            background: "transparent",
                            color: "var(--muted)",
                            cursor: "pointer",
                          }}
                        >
                          Refresh
                        </button>
                      </div>
                      {subDeliveries.length === 0 ? (
                        <p style={{ fontSize: 12, color: "var(--muted)" }}>No deliveries yet</p>
                      ) : (
                        <div style={{ display: "flex", flexDirection: "column", gap: 8, maxHeight: 300, overflowY: "auto" }}>
                          {subDeliveries.map((delivery) => (
                            <div
                              key={delivery.id}
                              style={{
                                padding: 10,
                                borderRadius: 8,
                                background: getStatusColor(delivery.status),
                                border: "1px solid var(--card-border)",
                              }}
                            >
                              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
                                <span style={{ fontSize: 12, fontWeight: 600, color: "var(--foreground)" }}>
                                  {delivery.domainEventType}
                                </span>
                                <span style={{ fontSize: 11, color: "var(--muted)" }}>
                                  {formatDate(delivery.createdAt)}
                                </span>
                              </div>
                              <div style={{ fontSize: 11, color: "var(--muted)" }}>
                                <div>Event ID: {delivery.domainEventId}</div>
                                <div>Attempt: {delivery.attempt}</div>
                                <div>Status: {delivery.status}</div>
                                {delivery.lastError && (
                                  <div style={{ color: "rgba(239, 68, 68, 0.8)", marginTop: 4 }}>
                                    Error: {delivery.lastError}
                                  </div>
                                )}
                                {delivery.nextAttemptAt && delivery.status !== "SUCCESS" && (
                                  <div>Next attempt: {formatDate(delivery.nextAttemptAt)}</div>
                                )}
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
