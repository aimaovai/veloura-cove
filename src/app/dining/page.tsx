import type { Metadata } from "next";
import { DiningPage } from "@/components/ContentPages";

export const metadata: Metadata = { title: "Dining", description: "Imagine coastal dining and sunset meals at the fictional Veloura Cove." };
export default function Page() { return <DiningPage />; }
