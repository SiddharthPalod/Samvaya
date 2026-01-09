"use client";

export type EventPayload = {
  title: string;
  description: string;
  city: string;
  time: string;
  capacity: number;
  organizerId: number;
  venue?: string;
  category?: string;
  imageUrl?: string;
  publicEvent: boolean;
  price: number;
};

export type EventResponse = EventPayload & {
  id: number;
  publicEvent: boolean;
};

export type Inventory = {
  eventId: number;
  totalSeats: number;
  availableSeats: number;
};

export type UserListItem = {
  id: number | null;
  email: string;
  passwordHash?: string;
  createdAt?: string;
};

export type DailyRevenuePoint = { day: string; revenue: number };
export type DailyActiveUsersPoint = { day: string; dau: number };
export type TopEvent = { eventId: string; title: string; revenue: number; tickets: number };
export type AnalyticsSummary = {
  dailyRevenue: DailyRevenuePoint[];
  dailyActiveUsers: DailyActiveUsersPoint[];
  topEvents: TopEvent[];
};
export type RealtimeEventSnapshot = { eventId: string; tickets: number; revenue: number };

