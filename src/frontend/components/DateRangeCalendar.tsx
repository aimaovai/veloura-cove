"use client";

import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft, ArrowRight, CalendarDays, RotateCcw } from "lucide-react";
import { api, displayDate } from "@/lib/api";
import type { CalendarDay } from "@/types/api";

const today = () => {
  const date = new Date();
  return localISO(new Date(date.getFullYear(), date.getMonth(), date.getDate(), 12));
};

function localISO(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

function shiftMonth(date: Date, count: number) {
  return new Date(date.getFullYear(), date.getMonth() + count, 1, 12);
}

function monthDays(month: Date) {
  const start = new Date(month.getFullYear(), month.getMonth(), 1, 12);
  const offset = start.getDay();
  const count = new Date(month.getFullYear(), month.getMonth() + 1, 0, 12).getDate();
  const cells: (Date | null)[] = Array.from({ length: offset }, () => null);
  for (let day = 1; day <= count; day++) cells.push(new Date(month.getFullYear(), month.getMonth(), day, 12));
  while (cells.length % 7) cells.push(null);
  return cells;
}

export function DateRangeCalendar({ slug, adults, childrenCount, checkIn, checkOut, onChange, onComplete, compact = false }: {
  slug?: string;
  adults: number;
  childrenCount: number;
  checkIn: string;
  checkOut: string;
  onChange: (checkIn: string, checkOut: string) => void;
  onComplete?: () => void;
  compact?: boolean;
}) {
  const [month, setMonth] = useState(() => {
    if (checkIn) {
      const [year, value, day] = checkIn.split("-").map(Number);
      return new Date(year, value - 1, day, 12);
    }
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1, 12);
  });
  const firstOfMonth = localISO(new Date(month.getFullYear(), month.getMonth(), 1, 12));
  const startDate = firstOfMonth < today() ? today() : firstOfMonth;
  const endDate = localISO(new Date(month.getFullYear(), month.getMonth() + 2, 1, 12));
  const calendarQuery = useQuery({
    queryKey: ["calendar", slug, startDate, endDate, adults, childrenCount],
    queryFn: () => api.calendar(slug!, { startDate, endDate, adults, children: childrenCount }),
    enabled: Boolean(slug),
  });
  const data = useMemo(() => new Map((calendarQuery.data ?? []).map(day => [day.date, day])), [calendarQuery.data]);
  const numberOfMonths = compact ? 1 : 2;
  const minDate = today();

  const choose = (value: string, availability?: CalendarDay) => {
    if (!availability || value < minDate) return;
    if (!checkIn || checkOut || value <= checkIn) {
      if (!availability.arrivalAllowed) return;
      onChange(value, "");
      return;
    }
    if (!availability.departureAllowed) return;
    onChange(checkIn, value);
    onComplete?.();
  };

  return (
    <div className={`calendar-shell ${compact ? "calendar-shell--compact" : ""}`}>
      <div className="calendar-header">
        <div><span className="eyebrow">SELECT YOUR STAY</span><h3>{checkIn && checkOut ? `${displayDate(checkIn, { month: "short", day: "numeric" })} — ${displayDate(checkOut, { month: "short", day: "numeric" })}` : checkIn ? "Choose your departure" : "Choose your arrival"}</h3></div>
        <div className="calendar-nav">
          <button type="button" onClick={() => setMonth(current => shiftMonth(current, -1))} aria-label="Previous month" disabled={localISO(shiftMonth(month, -1)) < localISO(new Date(new Date().getFullYear(), new Date().getMonth(), 1, 12))}><ArrowLeft size={15} /></button>
          <button type="button" onClick={() => setMonth(current => shiftMonth(current, 1))} aria-label="Next month"><ArrowRight size={15} /></button>
        </div>
      </div>
      {calendarQuery.isLoading && <div className="calendar-state" role="status"><span className="loading-mark" />Checking cove availability…</div>}
      {calendarQuery.isError && <p className="calendar-error" role="alert">Availability could not be reached. Please try again.</p>}
      {!slug && <p className="calendar-error">Choose an accommodation to see live nightly availability.</p>}
      {calendarQuery.data && <div className="calendar-months">
        {Array.from({ length: numberOfMonths }, (_, index) => {
          const current = shiftMonth(month, index);
          const cells = monthDays(current);
          return <div key={localISO(current)} className={`calendar-month ${index ? "calendar-month--second" : ""}`}>
            <h4>{new Intl.DateTimeFormat("en-US", { month: "long", year: "numeric" }).format(current)}</h4>
            <div className="calendar-week" aria-hidden="true">{["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"].map(day => <span key={day}>{day}</span>)}</div>
            <div className="calendar-days">
              {cells.map((date, cellIndex) => {
                if (!date) return <span key={`empty-${cellIndex}`} className="calendar-day calendar-day--outside" aria-hidden="true" />;
                const value = localISO(date);
                const info = data.get(value);
                const isStart = value === checkIn;
                const isEnd = value === checkOut;
                const between = checkIn && checkOut && value > checkIn && value < checkOut;
                const notAvailableForArrival = !checkIn || checkOut || value <= checkIn;
                const disabled = value < minDate || !info || (notAvailableForArrival && !info.arrivalAllowed) || (!notAvailableForArrival && !info.departureAllowed);
                return <button key={value} type="button" className={`calendar-day ${!info?.available ? "calendar-day--unavailable" : ""} ${isStart ? "calendar-day--range-start calendar-day--selected" : ""} ${isEnd ? "calendar-day--range-end calendar-day--selected" : ""} ${between ? "calendar-day--between" : ""}`} onClick={() => choose(value, info)} disabled={disabled} aria-pressed={isStart || isEnd} aria-label={`${displayDate(value, { weekday: "long", month: "long", day: "numeric" })}${info ? `, ${info.available ? `${info.remainingInventory} rooms remaining` : "no arrival inventory"}` : ", availability loading"}`}>
                  <span>{date.getDate()}</span>
                  {info && <small className="calendar-day__rate">{info.available ? `$${Math.round(info.nightlyRate)}` : "—"}</small>}
                </button>;
              })}
            </div>
          </div>;
        })}
      </div>}
      {calendarQuery.data && <div className="calendar-legend"><span><i /> Available arrival</span><span>— No arrival inventory</span><span>Checkout can be on a booked night</span></div>}
      <div className="calendar-actions">
        <button type="button" className="text-button" onClick={() => onChange("", "")}><RotateCcw size={13} /> Clear dates</button>
        {onComplete && checkIn && checkOut && <button type="button" className="button button--dark" onClick={onComplete}>Use these dates <CalendarDays size={14} /></button>}
      </div>
    </div>
  );
}
