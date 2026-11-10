import type { Metadata } from "next";
import { WellnessPage } from "@/components/ContentPages";

export const metadata: Metadata = { title: "Wellness", description: "Discover restful rituals and wellness experiences at Veloura Cove." };
export default function Page() { return <WellnessPage />; }
