import { Suspense } from "react";
import type { Metadata } from "next";
import { BookingPage } from "@/components/BookingPage";

export const metadata: Metadata = { title: "Plan Your Stay", description: "Search live demo availability and plan a simulated Veloura Cove reservation.", robots: { index: false, follow: false } };

export default async function Page({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  const params = await searchParams;
  const value = (key: string) => typeof params[key] === "string" ? params[key] as string : undefined;
  const number = (key: string, fallback: number) => {
    const parsed = Number(value(key));
    return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback;
  };
  return <Suspense fallback={<div className="booking-page"><div className="loading-state">Preparing your stay…</div></div>}>
    <BookingPage initialCheckIn={value("checkIn")} initialCheckOut={value("checkOut")}
      initialAdults={number("adults", 2)} initialChildren={Number(value("children") ?? 0)}
      initialTypeId={value("accommodationTypeId")} />
  </Suspense>;
}
