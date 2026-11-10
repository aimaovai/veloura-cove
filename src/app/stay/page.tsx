import type { Metadata } from "next";
import { StayListingPage } from "@/components/StayPages";

export const metadata: Metadata = { title: "The Stays", description: "Explore private rooms, ocean view suites, and villas at the fictional Veloura Cove." };

export default function Page() { return <StayListingPage />; }
