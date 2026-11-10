import type { Metadata } from "next";
import { ExperienceDetailPage } from "@/components/ContentPages";

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params;
  const name = slug.split("-").map(word => `${word[0]?.toUpperCase() ?? ""}${word.slice(1)}`).join(" ");
  return { title: name || "Experience", description: `Explore ${name || "a signature experience"} at Veloura Cove.` };
}
export default function Page() { return <ExperienceDetailPage />; }
