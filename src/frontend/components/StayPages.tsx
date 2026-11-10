"use client";

import { useMemo, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Image from "next/image";
import { useQuery } from "@tanstack/react-query";
import { ArrowRight, ChevronLeft, ChevronRight, X } from "lucide-react";
import { api, formatMoney } from "@/lib/api";
import type { Comparison } from "@/types/api";
import { useCompare } from "@/components/SiteShell";
import { ApiErrorState, ButtonLink, EditorialImage, EmptyState, Eyebrow, LoadingState, StayCard, TextLink } from "@/components/ui";
import { DateRangeCalendar } from "@/components/DateRangeCalendar";

const categories = [
  { label: "All stays", value: "" }, { label: "Rooms", value: "ROOM" },
  { label: "Suites", value: "SUITE" }, { label: "Villas", value: "VILLA" },
];

export function StayListingPage() {
  const [category, setCategory] = useState("");
  const { ids, toggle } = useCompare();
  const rooms = useQuery({ queryKey: ["accommodations"], queryFn: () => api.accommodations() });
  const filtered = useMemo(() => (rooms.data ?? []).filter(room => !category || room.category === category), [rooms.data, category]);
  return <>
    <section className="page-hero"><EditorialImage src="https://images.unsplash.com/photo-1613490493576-7fde63acd811?auto=format&fit=crop&w=2200&q=85" alt="Private villa overlooking the cove" className="page-hero__image" /><div className="page-hero__copy"><Eyebrow light>THE PRIVATE COLLECTION</Eyebrow><h1>Find your<br />own horizon.</h1><p>Rooms, suites, and villas shaped by the quiet beauty of the coast.</p></div></section>
    <section className="page-intro"><div><Eyebrow>THE STAYS</Eyebrow><h2>Considered spaces.<br />A sense of elsewhere.</h2></div><p>Each stay is an invitation to slow down. Explore the collection by room, suite, or private villa, then find the one that feels like yours.</p></section>
    <div className="filter-row" role="group" aria-label="Filter accommodation categories">
      {categories.map(item => <button key={item.value} type="button" className={`filter-chip ${category === item.value ? "filter-chip--active" : ""}`} onClick={() => setCategory(item.value)} aria-pressed={category === item.value}>{item.label}</button>)}
    </div>
    <section className="stay-listing" aria-live="polite">
      {rooms.isLoading ? <LoadingState label="Gathering the collection…" /> : rooms.isError ? <ApiErrorState message={rooms.error.message} retry={() => void rooms.refetch()} /> : filtered.length === 0 ? <EmptyState title="No stays in this collection." copy="Try another category to discover more of the cove." href="/stay" cta="Explore all stays" /> : (
        <div className="stays-grid">{filtered.map((room, index) => <StayCard key={room.id} room={room} index={index} compareSlot={<label className="compare-check"><input type="checkbox" checked={ids.includes(room.id)} disabled={!ids.includes(room.id) && ids.length >= 3} onChange={() => toggle(room.id)} /> Compare</label>} />)}</div>
      )}
    </section>
  </>;
}

export function StayDetailPage() {
  const params = useParams<{ slug: string }>();
  const router = useRouter();
  const [lightbox, setLightbox] = useState<number | null>(null);
  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  const [adults, setAdults] = useState(2);
  const [children, setChildren] = useState(0);
  const query = useQuery({ queryKey: ["accommodation", params.slug], queryFn: () => api.accommodation(params.slug), enabled: Boolean(params.slug) });
  const room = query.data;
  const gallery = room?.gallery.length ? room.gallery : room ? [{ url: room.heroImage, altText: room.name, type: "HERO", displayOrder: 0 }] : [];

  const openLightbox = (index: number) => {
    setLightbox(index);
    document.body.style.overflow = "hidden";
  };
  const closeLightbox = () => {
    setLightbox(null);
    document.body.style.overflow = "";
  };
  const changeLightbox = (direction: number) => setLightbox(current => current === null ? null : (current + direction + gallery.length) % gallery.length);
  const bookDates = () => {
    if (!room) return;
    const params = new URLSearchParams({ accommodationTypeId: room.id, adults: String(adults), children: String(children) });
    if (checkIn) params.set("checkIn", checkIn);
    if (checkOut) params.set("checkOut", checkOut);
    router.push(`/book?${params.toString()}`);
  };
  if (query.isLoading) return <div className="booking-page"><LoadingState label="Opening your stay…" /></div>;
  if (query.isError || !room) return <div className="booking-page"><ApiErrorState message={query.error?.message ?? "This stay could not be found."} retry={() => void query.refetch()} /></div>;

  return <>
    <section className="room-detail-hero"><EditorialImage src={room.heroImage} alt={room.gallery[0]?.altText ?? room.name} priority className="room-detail-hero__image" /><div className="room-detail-hero__copy"><div><Eyebrow light>{room.category} · {room.viewType}</Eyebrow><h1>{room.name}</h1><p>{room.maxGuests} guests · {room.roomSizeSqFt.toLocaleString()} sq ft · {room.bedConfiguration}</p></div><div className="room-detail-hero__price"><strong>{formatMoney(room.startingNightlyRate, room.currency)} <small>/ NIGHT</small></strong><button className="button button--light" onClick={() => document.getElementById("availability")?.scrollIntoView({ behavior: "smooth" })}>Check availability <ArrowRight size={14} /></button></div></div></section>
    <section className="gallery-section"><div className="gallery-mosaic">{gallery.slice(0, 5).map((image, index) => <button key={`${image.url}-${index}`} className="gallery-mosaic__item" onClick={() => openLightbox(index)} aria-label={`Open photo ${index + 1}: ${image.altText}`}><Image src={image.url} alt={image.altText} fill sizes="(max-width: 700px) 50vw, 40vw" priority={index === 0} style={{ objectFit: "cover" }} />{index === 4 && gallery.length > 5 && <span className="gallery-mosaic__more">+{gallery.length - 5} photographs</span>}</button>)}</div></section>
    <section className="room-story"><div><Eyebrow>A PLACE TO CALL YOURS</Eyebrow><h2>Space to settle into the moment.</h2><p>{room.longDescription}</p><p>{room.shortDescription}</p><TextLink href="/the-cove">The Veloura Cove feeling</TextLink></div><EditorialImage src={gallery[1]?.url ?? room.heroImage} alt={gallery[1]?.altText ?? `${room.name} detail`} className="room-story__image" /></section>
    <div className="room-facts"><div className="room-fact"><span>Room to unwind</span><strong>{room.roomSizeSqFt.toLocaleString()} sq ft</strong></div><div className="room-fact"><span>Thoughtfully sleeps</span><strong>Up to {room.maxGuests}</strong></div><div className="room-fact"><span>Bed arrangement</span><strong>{room.bedConfiguration}</strong></div><div className="room-fact"><span>Your view</span><strong>{room.viewType}</strong></div></div>
    <section className="amenities-section"><Eyebrow>THE DETAILS THAT MATTER</Eyebrow><h2>Made for your kind of escape.</h2><div className="amenities-grid">{[...room.amenities, ...room.features].map((item, index) => <div key={`${item}-${index}`} className="amenity-item">{item}</div>)}</div></section>
    <section className="section-pad" id="availability"><div className="page-intro" style={{ padding: 0, marginBottom: 36 }}><div><Eyebrow>PLAN YOUR STAY</Eyebrow><h2>Let the dates find you.</h2></div><p>Availability is checked against the cove’s live accommodation inventory. The night of checkout is not counted as an occupied night.</p></div><div className="booking-search-card"><div className="booking-search-card__fields"><div className="field-row"><div className="field"><label htmlFor="detail-adults">Adults</label><select id="detail-adults" value={adults} onChange={event => setAdults(Number(event.target.value))}>{Array.from({ length: room.maxAdults }, (_, i) => <option key={i + 1} value={i + 1}>{i + 1}</option>)}</select></div><div className="field"><label htmlFor="detail-children">Children</label><select id="detail-children" value={children} onChange={event => setChildren(Number(event.target.value))}>{Array.from({ length: room.maxChildren + 1 }, (_, i) => <option key={i} value={i}>{i}</option>)}</select></div></div><p className="disclaimer-note">Dates shown in the resort’s calendar are local calendar dates. Your selected stay will be rechecked when you request a hold.</p></div><DateRangeCalendar slug={room.slug} adults={adults} childrenCount={children} checkIn={checkIn} checkOut={checkOut} onChange={(arrival, departure) => { setCheckIn(arrival); setCheckOut(departure); }} compact /></div>
    </section>
    <div className="sticky-booking"><span>From<strong>{formatMoney(room.startingNightlyRate, room.currency)} / night</strong></span><button className="button button--dark" onClick={bookDates}>Plan this stay <ArrowRight size={14} /></button></div>
    {lightbox !== null && gallery[lightbox] && <div className="gallery-lightbox" role="dialog" aria-modal="true" aria-label="Accommodation photo gallery" onKeyDown={event => { if (event.key === "Escape") closeLightbox(); if (event.key === "ArrowRight") changeLightbox(1); if (event.key === "ArrowLeft") changeLightbox(-1); }} tabIndex={-1} ref={element => element?.focus()}>
      <button onClick={() => changeLightbox(-1)} aria-label="Previous image"><ChevronLeft /></button><div className="gallery-lightbox__image"><Image src={gallery[lightbox].url} alt={gallery[lightbox].altText} fill sizes="90vw" style={{ objectFit: "contain" }} /><span className="gallery-lightbox__caption">{gallery[lightbox].altText} · {lightbox + 1} / {gallery.length}</span></div><button onClick={() => changeLightbox(1)} aria-label="Next image"><ChevronRight /></button><button className="gallery-lightbox__close" onClick={closeLightbox} aria-label="Close gallery"><X /></button>
    </div>}
  </>;
}

export function ComparePage() {
  const { ids, toggle, clear } = useCompare();
  const query = useQuery({ queryKey: ["comparison", ids], queryFn: () => api.compare(ids), enabled: ids.length >= 2 });
  if (ids.length < 2) return <div className="booking-page"><EmptyState title="A thoughtful choice takes comparison." copy="Select two or three stays from the collection to see their details side by side." href="/stay" cta="Explore the stays" /></div>;
  if (query.isLoading) return <div className="booking-page"><LoadingState label="Laying out the details…" /></div>;
  if (query.isError) return <div className="booking-page"><ApiErrorState message={query.error.message} retry={() => void query.refetch()} /></div>;
  const rows: { label: string; value: (room: Comparison) => React.ReactNode }[] = [
    { label: "Starting rate", value: room => <><strong>{formatMoney(room.startingNightlyRate, room.currency)}</strong> / night</> },
    { label: "Category", value: room => room.category },
    { label: "Room size", value: room => `${room.roomSizeSqFt.toLocaleString()} sq ft` },
    { label: "Maximum guests", value: room => `${room.maxGuests} guests` },
    { label: "Beds", value: room => room.bedConfiguration },
    { label: "View", value: room => room.viewType },
    { label: "Private pool", value: room => room.privatePool ? "Included" : "—" },
    { label: "Terrace / balcony", value: room => room.terrace ? "Included" : "—" },
    { label: "Breakfast", value: room => room.breakfastIncluded ? "Included" : "—" },
    { label: "Butler service", value: room => room.butlerService ? "Included" : "—" },
    { label: "Notable amenities", value: room => room.notableAmenities.join(", ") || "—" },
  ];
  return <><section className="page-intro" style={{ paddingTop: 140 }}><div><Eyebrow>THE COLLECTION, SIDE BY SIDE</Eyebrow><h2>Find the one that feels like yours.</h2></div><button className="text-button" onClick={clear}>Clear comparison</button></section><div className="compare-table-wrap"><table className="compare-table"><thead><tr><th>Compare stays</th>{query.data?.map(room => <th key={room.id} className="compare-table__head"><EditorialImage src={room.heroImage} alt={room.name} className="compare-table__image" /><h3>{room.name}</h3><button className="text-button" onClick={() => toggle(room.id)}><X size={12} /> Remove</button></th>)}</tr></thead><tbody>{rows.map(row => <tr key={row.label}><td>{row.label}</td>{query.data?.map(room => <td key={room.id}>{row.value(room)}</td>)}</tr>)}</tbody></table><div style={{ marginTop: 30 }}><ButtonLink href="/book">Find your stay</ButtonLink></div></div></>;
}
