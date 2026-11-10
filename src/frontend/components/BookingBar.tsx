"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowRight } from "lucide-react";
import { localToday } from "@/lib/dates";

export function BookingBar({ accommodationTypeId }: { accommodationTypeId?: string }) {
  const router = useRouter();
  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  const [adults, setAdults] = useState(2);
  const [children, setChildren] = useState(0);

  const search = () => {
    if (!checkIn || !checkOut || checkOut <= checkIn) return;
    const params = new URLSearchParams({ checkIn, checkOut, adults: String(adults), children: String(children) });
    if (accommodationTypeId) params.set("accommodationTypeId", accommodationTypeId);
    router.push(`/book?${params.toString()}`);
  };

  return <form className="booking-bar" onSubmit={event => { event.preventDefault(); search(); }}>
    <div className="booking-bar__field"><label htmlFor="bar-check-in">Check in</label><input id="bar-check-in" type="date" min={localToday()} value={checkIn} onChange={event => setCheckIn(event.target.value)} required /></div>
    <div className="booking-bar__field"><label htmlFor="bar-check-out">Check out</label><input id="bar-check-out" type="date" min={checkIn || localToday()} value={checkOut} onChange={event => setCheckOut(event.target.value)} required /></div>
    <div className="booking-bar__field"><label htmlFor="bar-guests">Guests</label><select id="bar-guests" value={`${adults}-${children}`} onChange={event => { const [a, c] = event.target.value.split("-").map(Number); setAdults(a); setChildren(c); }}><option value="1-0">1 adult</option><option value="2-0">2 adults</option><option value="2-1">2 adults, 1 child</option><option value="2-2">2 adults, 2 children</option><option value="4-0">4 adults</option><option value="4-2">4 adults, 2 children</option></select></div>
    <button className="button button--dark" type="submit">Search availability <ArrowRight size={14} /></button>
  </form>;
}
