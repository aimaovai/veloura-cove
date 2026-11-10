import type { Metadata } from "next";
import { StayDetailPage } from "@/components/StayPages";

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params;
  const name = slug.split("-").map(word => `${word[0]?.toUpperCase() ?? ""}${word.slice(1)}`).join(" ");
  return { title: name || "Accommodation", description: `Discover ${name || "a private stay"} at the fictional Veloura Cove.` };
}

export default function Page() { return <StayDetailPage />; }
