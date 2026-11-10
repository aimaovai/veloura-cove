import type { Metadata } from "next";
import { ManagePage } from "@/components/ManagePage";

export const metadata: Metadata = { title: "Manage Your Stay", description: "Privately review and cancel a simulated Veloura Cove reservation.", robots: { index: false, follow: false } };
export default function Page() { return <ManagePage />; }
