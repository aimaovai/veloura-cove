import type { Metadata } from "next";
import { ComparePage } from "@/components/StayPages";

export const metadata: Metadata = { title: "Compare Stays", description: "Compare accommodations at Veloura Cove side by side." };

export default function Page() { return <ComparePage />; }
