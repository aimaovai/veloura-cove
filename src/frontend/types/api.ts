export type Category = "ROOM" | "SUITE" | "VILLA";
export type ExperienceCategory = "WELLNESS" | "DINING" | "ADVENTURE" | "ROMANCE" | "WATER" | "CULTURE";
export type CelebrationType = "ANNIVERSARY" | "HONEYMOON" | "BIRTHDAY" | "NONE";

export interface ImageDto {
  url: string;
  altText: string;
  type: string;
  displayOrder: number;
}

export interface Accommodation {
  id: string;
  slug: string;
  name: string;
  shortDescription: string;
  longDescription: string;
  category: Category;
  maxGuests: number;
  maxAdults: number;
  maxChildren: number;
  bedConfiguration: string;
  roomSizeSqFt: number;
  startingNightlyRate: number;
  currency: string;
  viewType: string;
  featured: boolean;
  heroImage: string;
  gallery: ImageDto[];
  amenities: string[];
  features: string[];
  checkInTime: string;
  checkOutTime: string;
}

export interface Comparison {
  id: string;
  name: string;
  category: Category;
  startingNightlyRate: number;
  currency: string;
  roomSizeSqFt: number;
  maxGuests: number;
  bedConfiguration: string;
  viewType: string;
  privatePool: boolean;
  terrace: boolean;
  breakfastIncluded: boolean;
  butlerService: boolean;
  notableAmenities: string[];
  heroImage: string;
}

export interface PriceLine {
  date: string;
  nightlyRate: number;
}

export interface PriceQuote {
  nightlyCharges: PriceLine[];
  roomSubtotal: number;
  resortFee: number;
  taxes: number;
  total: number;
  currency: string;
}

export interface Availability {
  accommodation: Accommodation;
  totalUnits: number;
  unitsRemaining: number;
  nightlyStartingRate: number;
  estimatedPrice: PriceQuote;
  status: "AVAILABLE" | "LIMITED" | "SOLD_OUT";
}

export interface CalendarDay {
  date: string;
  available: boolean;
  remainingInventory: number;
  nightlyRate: number;
  minimumStay: number;
  arrivalAllowed: boolean;
  departureAllowed: boolean;
}

export interface Hold {
  holdId: string;
  expiresAt: string;
  accommodation: Accommodation;
  checkIn: string;
  checkOut: string;
  adults: number;
  children: number;
  price: PriceQuote;
}

export interface ReservationCreated {
  reservationId: string;
  confirmationNumber: string | null;
  managementToken: string | null;
  status: string;
  accommodationName: string;
  checkIn: string;
  checkOut: string;
  adults: number;
  children: number;
  price: PriceQuote;
}

export interface PaymentSimulation {
  reservationId: string;
  status: string;
  outcome: string;
  paymentReference: string | null;
  confirmationNumber: string | null;
  message: string;
}

export interface ManagedReservation {
  confirmationNumber: string | null;
  accommodationName: string;
  checkIn: string;
  checkOut: string;
  adults: number;
  children: number;
  status: string;
  guestName: string | null;
  specialRequests: string | null;
  price: PriceQuote;
}

export interface Cancellation {
  status: string;
  freeCancellation: boolean;
  simulatedPenalty: number;
  simulatedRefund: number;
  message: string;
}

export interface Offer {
  slug: string;
  name: string;
  description: string;
  heroImage: string;
  discountType: string;
  discountValue: number;
  minimumNights: number;
  validFrom: string | null;
  validUntil: string | null;
  terms: string;
  applicableAccommodationSlugs: string[];
}

export interface Experience {
  slug: string;
  name: string;
  category: ExperienceCategory;
  shortDescription: string;
  longDescription: string;
  durationMinutes: number;
  startingPrice: number;
  currency: string;
  heroImage: string;
  featured: boolean;
  gallery: ImageDto[];
}

export interface ResortSettings {
  resortName: string;
  phone: string;
  email: string;
  address: string;
  checkInTime: string;
  checkOutTime: string;
  currency: string;
  resortFeePerNight: number;
  taxPercentage: number;
  cancellationWindowHours: number;
  holdDurationMinutes: number;
  maxStayNights: number;
}

export interface ApiError {
  timestamp: string;
  status: number;
  code: string;
  message: string;
  fieldErrors: { field: string; message: string }[];
}
