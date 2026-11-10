import type {
  Accommodation, ApiError, Availability, CalendarDay, Cancellation, Comparison,
  Experience, Hold, ManagedReservation, Offer, PaymentSimulation, PriceQuote,
  ReservationCreated, ResortSettings,
} from "@/types/api";

const API_BASE = (process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1").replace(/\/$/, "");

export class ApiRequestError extends Error {
  status: number;
  code: string;
  fieldErrors: ApiError["fieldErrors"];

  constructor(error: ApiError, status: number) {
    super(error.message || "The request could not be completed.");
    this.name = "ApiRequestError";
    this.status = status;
    this.code = error.code || "API_ERROR";
    this.fieldErrors = error.fieldErrors ?? [];
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...init,
      headers: { Accept: "application/json", ...(init?.body ? { "Content-Type": "application/json" } : {}), ...init?.headers },
      cache: "no-store",
    });
  } catch {
    throw new Error("We could not reach the resort service. Check that the API is running, then try again.");
  }
  if (!response.ok) {
    const body = await response.json().catch(() => null) as ApiError | null;
    throw new ApiRequestError(body ?? {
      timestamp: new Date().toISOString(), status: response.status, code: "API_ERROR",
      message: "The request could not be completed.", fieldErrors: [],
    }, response.status);
  }
  return response.json() as Promise<T>;
}

const query = (params: Record<string, string | number | boolean | undefined>) => {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) if (value !== undefined) search.set(key, String(value));
  return search.toString();
};

export const api = {
  accommodations: (params: { category?: string; guests?: number; featured?: boolean } = {}) =>
    request<Accommodation[]>(`/accommodation-types?${query(params)}`),
  accommodation: (slug: string) => request<Accommodation>(`/accommodation-types/${encodeURIComponent(slug)}`),
  compare: (ids: string[]) => request<Comparison[]>(`/accommodation-types/compare?${ids.map(id => `ids=${encodeURIComponent(id)}`).join("&")}`),
  availability: (params: { checkIn: string; checkOut: string; adults: number; children: number; accommodationTypeId?: string }) =>
    request<Availability[]>(`/availability?${query(params)}`),
  calendar: (slug: string, params: { startDate: string; endDate: string; adults: number; children: number }) =>
    request<CalendarDay[]>(`/accommodation-types/${encodeURIComponent(slug)}/availability-calendar?${query(params)}`),
  hold: (body: { accommodationTypeId: string; checkIn: string; checkOut: string; adults: number; children: number }) =>
    request<Hold>("/reservation-holds", { method: "POST", body: JSON.stringify(body) }),
  reservation: (body: Record<string, unknown>, idempotencyKey: string) =>
    request<ReservationCreated>("/reservations", { method: "POST", headers: { "Idempotency-Key": idempotencyKey }, body: JSON.stringify(body) }),
  payment: (id: string, result: "SUCCESS" | "DECLINED", idempotencyKey: string) =>
    request<PaymentSimulation>(`/reservations/${encodeURIComponent(id)}/payment-simulation`, {
      method: "POST", headers: { "Idempotency-Key": idempotencyKey }, body: JSON.stringify({ result }),
    }),
  managedReservation: (token: string) =>
    request<ManagedReservation>(`/reservations/manage/${encodeURIComponent(token)}`),
  cancelReservation: (token: string) =>
    request<Cancellation>(`/reservations/manage/${encodeURIComponent(token)}/cancel`, { method: "POST" }),
  offers: () => request<Offer[]>("/offers"),
  offer: (slug: string) => request<Offer>(`/offers/${encodeURIComponent(slug)}`),
  experiences: (category?: string) =>
    request<Experience[]>(`/experiences${category ? `?category=${encodeURIComponent(category)}` : ""}`),
  experience: (slug: string) => request<Experience>(`/experiences/${encodeURIComponent(slug)}`),
  resortSettings: () => request<ResortSettings>("/resort-settings"),
  inquiry: (body: Record<string, unknown>) =>
    request<{ inquiryId: string; status: string; createdAt: string }>("/inquiries", { method: "POST", body: JSON.stringify(body) }),
};

export function formatMoney(amount: number, currency = "USD") {
  return new Intl.NumberFormat("en-US", { style: "currency", currency }).format(amount);
}

export function nightsBetween(checkIn: string, checkOut: string) {
  const [iy, im, id] = checkIn.split("-").map(Number);
  const [oy, om, od] = checkOut.split("-").map(Number);
  return Math.round((Date.UTC(oy, om - 1, od) - Date.UTC(iy, im - 1, id)) / 86_400_000);
}

export function displayDate(date: string, options: Intl.DateTimeFormatOptions = { month: "short", day: "numeric", year: "numeric" }) {
  const [year, month, day] = date.split("-").map(Number);
  return new Intl.DateTimeFormat("en-US", options).format(new Date(year, month - 1, day, 12));
}

export type { PriceQuote };
