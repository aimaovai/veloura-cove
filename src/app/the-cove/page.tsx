import type { Metadata } from "next";
import { CovePage } from "@/components/ContentPages";

export const metadata: Metadata = { title: "The Cove", description: "The story and coastal philosophy behind fictional Veloura Cove." };
export default function Page() { return <CovePage />; }
