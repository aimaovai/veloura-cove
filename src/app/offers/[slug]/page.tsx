import type { Metadata } from "next";
import { OfferDetailPage } from "@/components/ContentPages";

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params;
  const name = slug.split("-").map(word => `${word[0]?.toUpperCase() ?? ""}${word.slice(1)}`).join(" ");
  return { title: name || "Stay Offer", description: `Explore the ${name || "Veloura Cove"} offer and its terms.` };
}
export default function Page() { return <OfferDetailPage />; }
