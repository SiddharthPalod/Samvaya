import "./globals.css";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Samvaya Manager",
  description: "Super-admin console for events, pricing, and inventory",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}

