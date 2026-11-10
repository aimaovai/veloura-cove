import type { Metadata } from "next";
import { OffersPage } from "@/components/ContentPages";

export const metadata: Metadata = { title: "Offers & Packages", description: "Discover current demo offers and packages at Veloura Cove." };
export default function Page() { return <OffersPage />; }
