"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery } from "@tanstack/react-query";
import { AlertTriangle, ArrowLeft, ArrowRight, Clock3, LockKeyhole } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { ApiRequestError, api, displayDate, formatMoney, nightsBetween } from "@/lib/api";
import { addDays, localToday } from "@/lib/dates";
import { ApiErrorState, ButtonLink, EditorialImage, EmptyState, Eyebrow, LoadingState } from "@/components/ui";
import type { Availability, Hold, PaymentSimulation, ReservationCreated } from "@/types/api";

const guestSchema = z.object({
  firstName: z.string().trim().min(1, "Enter your first name.").max(100),
  lastName: z.string().trim().min(1, "Enter your last name.").max(100),
  email: z.string().trim().email("Enter a valid email address.").max(254),
  phone: z.string().trim().min(1, "Enter a phone number.").max(50),
  country: z.string().trim().min(1, "Enter your country.").max(100),
  estimatedArrivalTime: z.string().max(40).optional(),
  specialRequests: z.string().max(2000).optional(),
  celebrationType: z.enum(["NONE", "BIRTHDAY", "ANNIVERSARY", "HONEYMOON"]).optional(),
});
type GuestForm = z.infer<typeof guestSchema>;

type Stage = "search" | "guest" | "payment" | "confirmation";

function randomKey() {
  return crypto.randomUUID();
}

function BookingSummary({ title, room, checkIn, checkOut, adults, childrenCount, price }: {
  title: string; room?: string; checkIn: string; checkOut: string; adults: number; childrenCount: number;
  price?: Hold["price"];
}) {
  return <aside className="booking-summary"><Eyebrow>YOUR STAY, AT A GLANCE</Eyebrow><h3>{title}</h3>
    {room && <div className="booking-summary__line"><span>Stay</span><strong>{room}</strong></div>}
    {checkIn && checkOut && <><div className="booking-summary__line"><span>Arrive</span><strong>{displayDate(checkIn)}</strong></div><div className="booking-summary__line"><span>Depart</span><strong>{displayDate(checkOut)}</strong></div><div className="booking-summary__line"><span>Length of stay</span><strong>{nightsBetween(checkIn, checkOut)} nights</strong></div></>}
    <div className="booking-summary__line"><span>Guests</span><strong>{adults} adult{adults !== 1 ? "s" : ""}{childrenCount ? `, ${childrenCount} child${childrenCount !== 1 ? "ren" : ""}` : ""}</strong></div>
    {price && <>
      {price.nightlyCharges.map(line => <div key={line.date} className="booking-summary__line"><span>{displayDate(line.date, { month: "short", day: "numeric" })} · nightly rate</span><strong>{formatMoney(line.nightlyRate, price.currency)}</strong></div>)}
      <div className="booking-summary__line"><span>Room subtotal</span><strong>{formatMoney(price.roomSubtotal, price.currency)}</strong></div>
      <div className="booking-summary__line"><span>Resort fee</span><strong>{formatMoney(price.resortFee, price.currency)}</strong></div>
      <div className="booking-summary__line"><span>Taxes</span><strong>{formatMoney(price.taxes, price.currency)}</strong></div>
      <div className="booking-summary__line booking-summary__total"><span>Total estimate</span><strong>{formatMoney(price.total, price.currency)}</strong></div>
      <p className="disclaimer-note">A final total from the resort pricing service. No offer discount is applied unless shown above.</p>
    </>}
  </aside>;
}

export function BookingPage({ initialCheckIn, initialCheckOut, initialAdults, initialChildren, initialTypeId }: {
  initialCheckIn?: string; initialCheckOut?: string; initialAdults?: number; initialChildren?: number; initialTypeId?: string;
}) {
  const [checkIn, setCheckIn] = useState(initialCheckIn || addDays(localToday(), 14));
  const [checkOut, setCheckOut] = useState(initialCheckOut || addDays(localToday(), 17));
  const [adults, setAdults] = useState(initialAdults || 2);
  const [children, setChildren] = useState(initialChildren || 0);
  const [typeId, setTypeId] = useState(initialTypeId ?? "");
  const [searched, setSearched] = useState(false);
  const [stage, setStage] = useState<Stage>("search");
  const [hold, setHold] = useState<Hold | null>(null);
  const [created, setCreated] = useState<ReservationCreated | null>(null);
  const [payment, setPayment] = useState<PaymentSimulation | null>(null);
  const [remainingSeconds, setRemainingSeconds] = useState(0);
  const [inlineError, setInlineError] = useState("");
  const creationKey = useRef<string | null>(null);
  const paymentKeys = useRef<Record<string, string>>({});
  const settings = useQuery({ queryKey: ["resort-settings"], queryFn: api.resortSettings });
  const search = useQuery({
    queryKey: ["availability", checkIn, checkOut, adults, children, typeId],
    queryFn: () => api.availability({ checkIn, checkOut, adults, children, accommodationTypeId: typeId || undefined }),
    enabled: searched && stage === "search" && Boolean(checkIn && checkOut && checkOut > checkIn),
  });
  const holdMutation = useMutation({ mutationFn: (selected: Availability) => api.hold({
    accommodationTypeId: selected.accommodation.id, checkIn, checkOut, adults, children,
  }), onSuccess: value => { setHold(value); setRemainingSeconds(Math.max(0, Math.floor((new Date(value.expiresAt).getTime() - Date.now()) / 1000))); setStage("guest"); setInlineError(""); }, onError: error => setInlineError(error.message) });
  const reservationMutation = useMutation({ mutationFn: ({ values, key }: { values: GuestForm; key: string }) =>
    api.reservation({ ...values, holdId: hold!.holdId }, key),
    onSuccess: value => {
      if (value.managementToken) sessionStorage.setItem("vc-management-token", value.managementToken);
      setCreated(value);
      setStage("payment");
      setInlineError("");
    },
    onError: error => setInlineError(error.message),
  });
  const paymentMutation = useMutation({
    mutationFn: ({ result, key }: { result: "SUCCESS" | "DECLINED"; key: string }) =>
      api.payment(created!.reservationId, result, key),
    onSuccess: value => {
      setPayment(value);
      setInlineError("");
      if (value.status === "CONFIRMED" && value.confirmationNumber) setStage("confirmation");
    },
    onError: error => {
      setInlineError(error.message);
      if (error instanceof ApiRequestError && error.code === "HOLD_EXPIRED") {
        setRemainingSeconds(0);
      }
    },
  });
  const form = useForm<GuestForm>({
    resolver: zodResolver(guestSchema),
    defaultValues: { firstName: "", lastName: "", email: "", phone: "", country: "", estimatedArrivalTime: "", specialRequests: "", celebrationType: "NONE" },
  });

  useEffect(() => {
    if (!hold || stage === "search" || stage === "confirmation") return;
    const updateCountdown = () => setRemainingSeconds(Math.max(0, Math.floor((new Date(hold.expiresAt).getTime() - Date.now()) / 1000)));
    updateCountdown();
    const interval = window.setInterval(updateCountdown, 1000);
    return () => window.clearInterval(interval);
  }, [hold, stage]);

  const submitSearch = (event: React.FormEvent) => {
    event.preventDefault();
    if (!checkIn || !checkOut || checkOut <= checkIn) { setInlineError("Choose a check-out date after check-in."); return; }
    const nights = nightsBetween(checkIn, checkOut);
    if (nights > (settings.data?.maxStayNights ?? 30)) { setInlineError(`Choose a stay of ${settings.data?.maxStayNights ?? 30} nights or fewer.`); return; }
    setInlineError("");
    setSearched(true);
  };

  const startHold = (item: Availability) => {
    setInlineError("");
    holdMutation.mutate(item);
  };

  const submitGuest = form.handleSubmit(values => {
    if (!hold || remainingSeconds <= 0) { setInlineError("This temporary hold has expired. Check availability again to continue."); return; }
    if (!creationKey.current) creationKey.current = randomKey();
    reservationMutation.mutate({ values, key: creationKey.current });
  });

  const simulate = (result: "SUCCESS" | "DECLINED") => {
    if (!created) return;
    paymentKeys.current[result] ??= randomKey();
    paymentMutation.mutate({ result, key: paymentKeys.current[result] });
  };

  return <div className="booking-page">
    <div className="booking-page__heading"><Eyebrow>YOUR TIME AT THE COVE</Eyebrow><h1>{stage === "confirmation" ? "A moment to remember." : stage === "payment" ? "A final, simple step." : "Let your stay begin."}</h1><p>Veloura Cove is a fictional resort. Reservation and payment interactions are demonstrations only.</p></div>
    <div className="reservation-steps" aria-label="Reservation progress">
      {["Search", "Guest details", "Demo payment", "Confirmation"].map((item, index) => {
        const active = ["search", "guest", "payment", "confirmation"].indexOf(stage) === index;
        const complete = ["search", "guest", "payment", "confirmation"].indexOf(stage) > index;
        return <div key={item} className={`reservation-step ${active || complete ? "reservation-step--active" : ""}`}><span className="reservation-step__number">{complete ? "✓" : index + 1}</span>{item}</div>;
      })}
    </div>

    {stage === "search" && <>
      <div className="booking-search-card">
        <form className="booking-search-card__fields" onSubmit={submitSearch}>
          <div className="field-row"><div className="field"><label htmlFor="book-check-in">Check-in</label><input id="book-check-in" type="date" min={localToday()} value={checkIn} onChange={event => setCheckIn(event.target.value)} required /></div><div className="field"><label htmlFor="book-check-out">Check-out</label><input id="book-check-out" type="date" min={checkIn || localToday()} value={checkOut} onChange={event => setCheckOut(event.target.value)} required /></div></div>
          <div className="field-row"><div className="field"><label htmlFor="book-adults">Adults</label><select id="book-adults" value={adults} onChange={event => setAdults(Number(event.target.value))}>{Array.from({ length: 12 }, (_, i) => <option value={i + 1} key={i}>{i + 1} adult{i ? "s" : ""}</option>)}</select></div><div className="field"><label htmlFor="book-children">Children</label><select id="book-children" value={children} onChange={event => setChildren(Number(event.target.value))}>{Array.from({ length: 9 }, (_, i) => <option value={i} key={i}>{i} children</option>)}</select></div></div>
          {typeId && <div className="field"><span className="field-label">Selected stay</span><div className="selected-room-note">Your selected accommodation will be checked against live inventory. <button type="button" className="text-button" onClick={() => setTypeId("")}>Search all stays</button></div></div>}
          {inlineError && <p className="form-error" role="alert">{inlineError}</p>}
          <button type="submit" className="button button--dark">Search availability <ArrowRight size={14} /></button>
        </form>
        <div className="booking-search-card__aside"><Eyebrow>THE RIGHT TIME, JUST FOR YOU</Eyebrow><h2 className="serif">Begin with a date.</h2><p className="muted">Every search checks actual accommodation inventory. Your nightly breakdown and total estimate come directly from the resort pricing service.</p><div className="booking-search-card__policy"><Clock3 size={15} /><span>Choose your dates and guests to see stays that are actually available.</span></div></div>
      </div>
      {searched && <section className="booking-results" aria-live="polite">
        {search.isFetching ? <LoadingState label="Checking the cove for you…" /> : search.isError ? <ApiErrorState message={search.error.message} retry={() => void search.refetch()} /> : (search.data ?? []).filter(item => item.unitsRemaining > 0).length === 0 ? <EmptyState title="The cove is fully reserved for those dates." copy="Try a different arrival, explore another stay, or search a later date." href="/stay" cta="Explore other stays" /> : <>
          <div className="booking-results__head"><div><Eyebrow>{displayDate(checkIn)} — {displayDate(checkOut)} · {nightsBetween(checkIn, checkOut)} NIGHTS</Eyebrow><h2>Stays available for you.</h2></div><button className="text-button" onClick={() => setSearched(false)}><ArrowLeft size={13} /> Change dates</button></div>
          {(search.data ?? []).filter(item => item.unitsRemaining > 0).map(item => <article className="booking-result" key={item.accommodation.id}>
            <EditorialImage src={item.accommodation.heroImage} alt={item.accommodation.name} className="booking-result__image" />
            <div className="booking-result__info"><Eyebrow>{item.accommodation.category} · {item.accommodation.viewType}</Eyebrow><h3>{item.accommodation.name}</h3><p>{item.accommodation.shortDescription}</p><p>{item.unitsRemaining} of {item.totalUnits} accommodations remain · {item.status.toLowerCase()}</p></div>
            <div className="booking-result__price"><strong>{formatMoney(item.estimatedPrice.total, item.estimatedPrice.currency)}</strong><span>Estimated total · {nightsBetween(checkIn, checkOut)} nights</span><button className="button button--dark" disabled={holdMutation.isPending} onClick={() => startHold(item)}>{holdMutation.isPending ? "Holding…" : "Select this stay"} <ArrowRight size={14} /></button></div>
          </article>)}
          <p className="disclaimer-note">These are estimates from the current pricing service; all availability and price are checked again when a hold is requested. No offer has been applied.</p>
        </>}
      </section>}
    </>}

    {stage === "guest" && hold && <>
      <div className="booking-flow booking-flow__layout"><form className="booking-flow__main" onSubmit={submitGuest} noValidate>
        <div className={`hold-countdown ${remainingSeconds <= 0 ? "hold-countdown--expired" : ""}`} role="timer"><Clock3 size={15} />{remainingSeconds > 0 ? <>Your stay is held temporarily for <strong>{String(Math.floor(remainingSeconds / 60)).padStart(2, "0")}:{String(remainingSeconds % 60).padStart(2, "0")}</strong>.</> : <>This hold has expired. Please recheck availability.</>}</div>
        <Eyebrow>STEP 02 · YOUR DETAILS</Eyebrow><h2>Who’s joining us?</h2><p>Share the details for the lead guest. No payment card information is requested or stored.</p>
        <div className="form-grid">
          <div className="field"><label htmlFor="guest-first">First name</label><input id="guest-first" autoComplete="given-name" {...form.register("firstName")} />{form.formState.errors.firstName && <p className="field-error">{form.formState.errors.firstName.message}</p>}</div>
          <div className="field"><label htmlFor="guest-last">Last name</label><input id="guest-last" autoComplete="family-name" {...form.register("lastName")} />{form.formState.errors.lastName && <p className="field-error">{form.formState.errors.lastName.message}</p>}</div>
          <div className="field"><label htmlFor="guest-email">Email</label><input id="guest-email" type="email" autoComplete="email" {...form.register("email")} />{form.formState.errors.email && <p className="field-error">{form.formState.errors.email.message}</p>}</div>
          <div className="field"><label htmlFor="guest-phone">Phone</label><input id="guest-phone" type="tel" autoComplete="tel" {...form.register("phone")} />{form.formState.errors.phone && <p className="field-error">{form.formState.errors.phone.message}</p>}</div>
          <div className="field"><label htmlFor="guest-country">Country</label><input id="guest-country" autoComplete="country-name" {...form.register("country")} />{form.formState.errors.country && <p className="field-error">{form.formState.errors.country.message}</p>}</div>
          <div className="field"><label htmlFor="guest-arrival">Estimated arrival time · optional</label><input id="guest-arrival" type="time" {...form.register("estimatedArrivalTime")} /></div>
          <div className="field"><label htmlFor="guest-celebration">Celebration · optional</label><select id="guest-celebration" {...form.register("celebrationType")}><option value="NONE">None</option><option value="BIRTHDAY">Birthday</option><option value="ANNIVERSARY">Anniversary</option><option value="HONEYMOON">Honeymoon</option></select></div>
          <div className="field form-grid__full"><label htmlFor="guest-requests">Special requests · optional</label><textarea id="guest-requests" maxLength={2000} {...form.register("specialRequests")} placeholder="Anything that would make your stay more comfortable…" />{form.formState.errors.specialRequests && <p className="field-error">{form.formState.errors.specialRequests.message}</p>}</div>
        </div>
        {inlineError && <p className="form-error" role="alert">{inlineError}</p>}
        <div className="form-actions"><button type="button" className="text-button" onClick={() => { setHold(null); setStage("search"); setSearched(true); }}><ArrowLeft size={13} /> Check another stay</button><button type="submit" className="button button--dark" disabled={reservationMutation.isPending || remainingSeconds <= 0}>{reservationMutation.isPending ? "Saving details…" : "Review & continue"} <ArrowRight size={14} /></button></div>
      </form><div className="booking-flow__aside"><BookingSummary title="Your reservation" room={hold.accommodation.name} checkIn={hold.checkIn} checkOut={hold.checkOut} adults={hold.adults} childrenCount={hold.children} price={hold.price} /></div></div>
    </>}

    {stage === "payment" && hold && created && <>
      <div className="booking-flow booking-flow__layout"><div className="booking-flow__main"><Eyebrow>STEP 03 · FINAL REVIEW</Eyebrow><h2>Everything in its place.</h2><p>Review your stay before continuing. This is a portfolio demonstration; no real reservation is made.</p>
        <div className="booking-summary"><div className="booking-summary__line"><span>Lead guest</span><strong>{form.getValues("firstName")} {form.getValues("lastName")}</strong></div><div className="booking-summary__line"><span>Email</span><strong>{form.getValues("email")}</strong></div><div className="booking-summary__line"><span>Stay</span><strong>{created.accommodationName}</strong></div><div className="booking-summary__line"><span>Dates</span><strong>{displayDate(created.checkIn)} – {displayDate(created.checkOut)}</strong></div><div className="booking-summary__line"><span>Guest count</span><strong>{created.adults} adults · {created.children} children</strong></div>{form.getValues("specialRequests") && <div className="booking-summary__line"><span>Special requests</span><strong>{form.getValues("specialRequests")}</strong></div>}</div>
        <div className="demo-payment" style={{ marginTop: 23 }}><span className="demo-payment__label"><LockKeyhole size={13} /> Demo payment simulation</span><p>No card details are collected. Choose a simulated result to see the reservation lifecycle; no real transaction will occur.</p><div className="demo-payment__buttons"><button disabled={paymentMutation.isPending || remainingSeconds <= 0} onClick={() => simulate("SUCCESS")}>{paymentMutation.isPending ? "Submitting…" : "Simulate success"}</button><button disabled={paymentMutation.isPending || remainingSeconds <= 0} onClick={() => simulate("DECLINED")}>Simulate decline</button></div></div>
        {payment?.outcome === "DECLINED" && <p className="booking-payment-declined" role="status"><AlertTriangle size={14} /> The demo payment was declined. Your hold remains active; you can safely try the success simulation.</p>}
        {remainingSeconds <= 0 && <p className="form-error" role="alert">Your hold has expired. Search availability again to restart this step.</p>}
        {inlineError && <p className="form-error" role="alert">{inlineError}</p>}
        <div className="form-actions"><button type="button" className="text-button" onClick={() => setStage("guest")}><ArrowLeft size={13} /> Back to guest details</button></div>
        <p className="disclaimer-note">The cove’s cancellation window is {settings.data?.cancellationWindowHours ?? 72} hours before check-in. Any refunds or penalties in this demo are simulated only.</p>
      </div><div className="booking-flow__aside"><BookingSummary title="Your reservation" room={hold.accommodation.name} checkIn={hold.checkIn} checkOut={hold.checkOut} adults={hold.adults} childrenCount={hold.children} price={hold.price} /></div></div>
    </>}

    {stage === "confirmation" && created && payment && <>
      <div className="confirmation-card"><div className="confirmation-card__seal">VC</div><Eyebrow>CONFIRMATION · DEMONSTRATION ONLY</Eyebrow><h1>Your escape<br />is reserved.</h1><p>Wonderful, {form.getValues("firstName") || "traveler"}. Your simulated reservation has been confirmed by the Veloura Cove API.</p>
        <div className="confirmation-card__number"><span>Confirmation number</span><strong>{payment.confirmationNumber}</strong></div>
        <BookingSummary title="Stay details" room={created.accommodationName} checkIn={created.checkIn} checkOut={created.checkOut} adults={created.adults} childrenCount={created.children} price={created.price} />
        <p className="disclaimer-note">No real inventory is reserved, no payment was processed, and no confirmation email was sent.</p>
        {typeof window !== "undefined" && sessionStorage.getItem("vc-management-token") ? <ButtonLink href="/manage">View reservation details <ArrowRight size={14} /></ButtonLink> : <p className="disclaimer-note">This reservation can only be managed in the current session because the private access token was not retained.</p>}
      </div>
    </>}
  </div>;
}
