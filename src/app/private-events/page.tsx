import type { Metadata } from "next";
import { InquiryPage } from "@/components/ContentPages";

export const metadata: Metadata = { title: "Private Events & Inquiries", description: "Send an event or resort inquiry to the Veloura Cove demonstration API." };
export default function Page() { return <InquiryPage />; }
