import { describe, expect, it } from "vitest";
import { displayDate, formatMoney, nightsBetween } from "./api";
import { addDays, localToday } from "./dates";

describe("resort-local booking date helpers", () => {
  it("adds calendar days across month and daylight-saving boundaries", () => {
    expect(addDays("2027-01-30", 3)).toBe("2027-02-02");
    expect(addDays("2026-03-08", 1)).toBe("2026-03-09");
  });

  it("uses exclusive checkout when calculating stay nights", () => {
    expect(nightsBetween("2027-06-10", "2027-06-13")).toBe(3);
    expect(nightsBetween("2027-06-10", "2027-06-11")).toBe(1);
  });

  it("formats a calendar date without converting through UTC midnight", () => {
    expect(displayDate("2027-01-03", { month: "short", day: "numeric", year: "numeric" })).toBe("Jan 3, 2027");
  });

  it("uses currency minor units in price display", () => {
    expect(formatMoney(1234.5, "USD")).toBe("$1,234.50");
  });

  it("returns an ISO local date for the resort date picker minimum", () => {
    expect(localToday()).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });
});
