import type { Metadata } from "next";
import { Eyebrow } from "@/components/ui";

export const metadata: Metadata = { title: "Privacy", description: "Privacy information for the fictional Veloura Cove portfolio demonstration." };

export default function Page() {
  return <section className="legal-page"><Eyebrow>PORTFOLIO DEMONSTRATION</Eyebrow><h1>A note on privacy.</h1><p>Veloura Cove is a fictional resort website. Do not submit real personal or sensitive information through its forms. Reservation and inquiry requests are stored by the demonstration backend in its configured database so the application can demonstrate booking workflows.</p><p>Reservation-management access is represented by a private token kept in the current browser session. Avoid sharing it. This site does not collect payment card data, use a real payment provider, or send confirmation emails.</p><p>This portfolio project does not represent an operating hotel or provide a real hospitality service.</p></section>;
}
