import type { Metadata } from "next";
import { Eyebrow } from "@/components/ui";

export const metadata: Metadata = { title: "Terms", description: "Demo-use terms for the fictional Veloura Cove reservation website." };

export default function Page() {
  return <section className="legal-page"><Eyebrow>PORTFOLIO DEMONSTRATION</Eyebrow><h1>Made to be explored.</h1><p>Veloura Cove is a fictional luxury hospitality concept and software portfolio project. It has no real accommodation inventory, operating resort, or bookable experiences. All reservations and availability are demonstrations.</p><p>The payment simulation does not process a real payment, and displayed cancellation penalties or refunds are not financial transactions. Do not use this site to make real travel or event arrangements.</p><p>Descriptions, imagery, prices, contact details, offers, and experiences are demonstration content and may not correspond to real places or services.</p></section>;
}
