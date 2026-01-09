import type { CSSProperties } from "react";
import type {
  AnalyticsSummary,
  RealtimeEventSnapshot,
  TopEvent,
} from "../types";

type Props = {
  summary: AnalyticsSummary | null;
  realtime: RealtimeEventSnapshot | null;
  onRefresh: () => void;
  loading: boolean;
  cardStyle: CSSProperties;
};

const statBox = (label: string, value: string, secondary?: string) => (
  <div
    style={{
      padding: 12,
      borderRadius: 12,
      border: "1px solid var(--card-border)",
      background: "rgba(255,255,255,0.03)",
    }}
  >
    <div style={{ color: "var(--muted)", fontSize: 12, marginBottom: 4 }}>{label}</div>
    <div style={{ fontSize: 22, fontWeight: 700, color: "var(--foreground)" }}>{value}</div>
    {secondary && <div style={{ color: "var(--muted)", fontSize: 12 }}>{secondary}</div>}
  </div>
);

const renderTopEvents = (topEvents: TopEvent[]) => (
  <table style={{ width: "100%", borderCollapse: "collapse", marginTop: 8 }}>
    <thead>
      <tr style={{ textAlign: "left", color: "var(--muted)", fontSize: 12 }}>
        <th style={{ padding: "6px 0" }}>Event</th>
        <th>Revenue</th>
        <th>Tickets</th>
      </tr>
    </thead>
    <tbody>
      {topEvents.map((t) => (
        <tr key={t.eventId} style={{ borderTop: "1px solid var(--card-border)" }}>
          <td style={{ padding: "8px 0" }}>{t.title || t.eventId}</td>
          <td>${(t.revenue ?? 0).toLocaleString()}</td>
          <td>{t.tickets ?? 0}</td>
        </tr>
      ))}
    </tbody>
  </table>
);

export default function AnalyticsDashboardCard({
  summary,
  realtime,
  onRefresh,
  loading,
  cardStyle,
}: Props) {
  const lastRevenue = summary?.dailyRevenue?.at(-1)?.revenue ?? 0;
  const lastDau = summary?.dailyActiveUsers?.at(-1)?.dau ?? 0;

  return (
    <section className="glass card-border" style={cardStyle}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div>
          <h2 style={{ margin: 0, color: "var(--foreground)" }}>Analytics</h2>
          <p style={{ margin: 0, color: "var(--muted)" }}>Batch + realtime overview</p>
        </div>
        <button
          onClick={onRefresh}
          disabled={loading}
          style={{
            padding: "8px 12px",
            borderRadius: 10,
            border: "1px solid var(--card-border)",
            background: "rgba(255,255,255,0.05)",
            color: "var(--foreground)",
            cursor: loading ? "not-allowed" : "pointer",
          }}
        >
          {loading ? "Refreshing..." : "Refresh"}
        </button>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 12, marginTop: 16 }}>
        {statBox("Revenue (today)", `$${lastRevenue.toLocaleString()}`)}
        {statBox("DAU (today)", `${lastDau}`)}
        {statBox(
          "Realtime",
          realtime ? `${realtime.tickets} tickets` : "Select event",
          realtime ? `$${(realtime.revenue ?? 0).toLocaleString()} revenue` : undefined
        )}
      </div>

      <div style={{ marginTop: 20 }}>
        <h3 style={{ margin: "0 0 8px", color: "var(--foreground)" }}>Top events (last 7d)</h3>
        {summary?.topEvents?.length ? (
          renderTopEvents(summary.topEvents)
        ) : (
          <div style={{ color: "var(--muted)" }}>No events yet.</div>
        )}
      </div>
    </section>
  );
}
