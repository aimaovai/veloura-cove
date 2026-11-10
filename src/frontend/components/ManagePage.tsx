"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ArrowRight, ShieldCheck, X } from "lucide-react";
import { useEffect, useState } from "react";
import { api, displayDate, formatMoney, nightsBetween } from "@/lib/api";
import { ApiErrorState, ButtonLink, EmptyState, Eyebrow, LoadingState } from "@/components/ui";
import type { Cancellation } from "@/types/api";

export function ManagePage() {
  const [token, setToken] = useState("");
  const [ready, setReady] = useState(false);
  const [confirmCancel, setConfirmCancel] = useState(false);
  const [cancellation, setCancellation] = useState<Cancellation | null>(null);
  const queryClient = useQueryClient();
  const settings = useQuery({ queryKey: ["resort-settings"], queryFn: api.resortSettings });
  useEffect(() => {
    setToken(sessionStorage.getItem("vc-management-token") ?? "");
    setReady(true);
  }, []);

  const reservation = useQuery({
    queryKey: ["managed-reservation"],
    queryFn: () => api.managedReservation(token),
    enabled: Boolean(token),
    retry: false,
  });
  const cancel = useMutation({
    mutationFn: () => api.cancelReservation(token),
    onSuccess: result => {
      setCancellation(result);
      setConfirmCancel(false);
      void queryClient.invalidateQueries({ queryKey: ["managed-reservation"] });
    },
  });

  if (!ready) return <div className="booking-page"><LoadingState label="Opening your private reservation…" /></div>;
  if (!token) return <div className="booking-page"><EmptyState title="Your private link is in this session." copy="To protect guest details, reservation access is not embedded in a public URL. Open the management page in the browser session where you completed the demo booking." href="/book" cta="Plan a stay" /></div>;
  if (reservation.isLoading) return <div className="booking-page"><LoadingState label="Retrieving your reservation…" /></div>;
  if (reservation.isError || !reservation.data) return <div className="booking-page"><ApiErrorState message={reservation.error?.message ?? "The reservation could not be retrieved."} retry={() => void reservation.refetch()} /></div>;
  const stay = reservation.data;
  const canCancel = stay.status === "CONFIRMED" || stay.status === "HELD";
  const policyHours = settings.data?.cancellationWindowHours ?? 72;

  return <div className="booking-page"><div className="booking-page__heading"><Eyebrow><ShieldCheck size={12} /> PRIVATE RESERVATION MANAGEMENT</Eyebrow><h1>Your stay, in one place.</h1><p>This private session link uses a secure management token. Keep it to yourself.</p></div>
    <div className="manage-card"><span className="manage-card__status">{stay.status.replaceAll("_", " ")}</span><h2>{stay.accommodationName}</h2>
      {stay.confirmationNumber && <div className="booking-summary__line"><span>Confirmation</span><strong>{stay.confirmationNumber}</strong></div>}
      <div className="booking-summary__line"><span>Arrival</span><strong>{displayDate(stay.checkIn)}</strong></div>
      <div className="booking-summary__line"><span>Departure</span><strong>{displayDate(stay.checkOut)}</strong></div>
      <div className="booking-summary__line"><span>Length of stay</span><strong>{nightsBetween(stay.checkIn, stay.checkOut)} nights</strong></div>
      <div className="booking-summary__line"><span>Guests</span><strong>{stay.adults} adults · {stay.children} children</strong></div>
      {stay.guestName && <div className="booking-summary__line"><span>Lead guest</span><strong>{stay.guestName}</strong></div>}
      {stay.specialRequests && <div className="booking-summary__line"><span>Special requests</span><strong>{stay.specialRequests}</strong></div>}
      <div className="booking-summary__line"><span>Room subtotal</span><strong>{formatMoney(stay.price.roomSubtotal, stay.price.currency)}</strong></div>
      <div className="booking-summary__line"><span>Resort fee</span><strong>{formatMoney(stay.price.resortFee, stay.price.currency)}</strong></div>
      <div className="booking-summary__line"><span>Taxes</span><strong>{formatMoney(stay.price.taxes, stay.price.currency)}</strong></div>
      <div className="booking-summary__line booking-summary__total"><span>Total estimate</span><strong>{formatMoney(stay.price.total, stay.price.currency)}</strong></div>
      <p className="disclaimer-note">Cancellation within {policyHours} hours of arrival may show a simulated penalty. Any simulated refund is not a real financial transaction.</p>
      {cancellation && <div className="inquiry-success" role="status"><strong>Reservation cancelled.</strong> Simulated refund: {formatMoney(cancellation.simulatedRefund, stay.price.currency)} · Simulated penalty: {formatMoney(cancellation.simulatedPenalty, stay.price.currency)}. No actual payment or refund was processed.</div>}
      {cancel.isError && <p className="form-error" role="alert">{cancel.error.message}</p>}
      <div className="form-actions"><ButtonLink href="/book">Plan another stay</ButtonLink>{canCancel && !cancellation && <button className="button button--outline" onClick={() => setConfirmCancel(true)}>Request cancellation <X size={14} /></button>}</div>
      <p className="disclaimer-note">Veloura Cove is fictional. This page displays data from a reservation-management demonstration, not a real booking.</p>
    </div>
    {confirmCancel && <div className="cancel-dialog" role="presentation" onMouseDown={event => { if (event.target === event.currentTarget) setConfirmCancel(false); }}><div className="cancel-dialog__panel" role="dialog" aria-modal="true" aria-labelledby="cancel-title" onKeyDown={event => { if (event.key === "Escape") setConfirmCancel(false); }}><Eyebrow>CANCELLATION POLICY</Eyebrow><h3 id="cancel-title">Are you sure you want to cancel?</h3><p>Cancellation at least {policyHours} hours before check-in is eligible for a free cancellation. Inside that window, the first room night may be shown as a simulated penalty. Any refund shown is simulated only.</p><div className="form-actions"><button className="text-button" onClick={() => setConfirmCancel(false)}>Keep my stay</button><button className="button button--dark" disabled={cancel.isPending} onClick={() => cancel.mutate()}>{cancel.isPending ? "Cancelling…" : "Confirm cancellation"} <ArrowRight size={14} /></button></div></div></div>}
  </div>;
}
