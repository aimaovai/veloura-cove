import type { Metadata } from "next";
import { QueryProvider } from "@/components/QueryProvider";
import { SiteShell } from "@/components/SiteShell";
import "@/styles/globals.css";

export const metadata: Metadata = {
  metadataBase: new URL(process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000"),
  title: { default: "Veloura Cove — Where the coast slows down", template: "%s — Veloura Cove" },
  description: "An intimate coastal sanctuary where architecture, ocean, and stillness meet. Explore the fictional Veloura Cove resort.",
  referrer: "no-referrer",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>
        <QueryProvider>
          <SiteShell>{children}</SiteShell>
        </QueryProvider>
      </body>
    </html>
  );
}
