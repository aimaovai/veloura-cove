import Image from "next/image";
import Link from "next/link";
import { ArrowDown, ArrowRight, ArrowUpRight } from "lucide-react";
import { formatMoney } from "@/lib/api";
import type { Accommodation, Experience, Offer } from "@/types/api";

export function EditorialImage({ src, alt, className = "", priority = false, position = "center" }: {
  src: string; alt: string; className?: string; priority?: boolean; position?: string;
}) {
  return <div className={`editorial-image ${className}`}><Image src={src} alt={alt} fill priority={priority} sizes="(max-width: 760px) 100vw, 65vw" style={{ objectFit: "cover", objectPosition: position }} /></div>;
}

export function Eyebrow({ children, light = false }: { children: React.ReactNode; light?: boolean }) {
  return <span className={`eyebrow ${light ? "eyebrow--light" : ""}`}>{children}</span>;
}

export function SectionHeading({ label, title, copy, light = false }: { label: string; title: React.ReactNode; copy?: string; light?: boolean }) {
  return <div className={`section-heading ${light ? "section-heading--light" : ""}`}><Eyebrow light={light}>{label}</Eyebrow><h2>{title}</h2>{copy && <p>{copy}</p>}</div>;
}

export function ButtonLink({ href, children, light = false, outline = false }: { href: string; children: React.ReactNode; light?: boolean; outline?: boolean }) {
  return <Link className={`button ${light ? "button--light" : "button--dark"} ${outline ? "button--outline" : ""}`} href={href}>{children}<ArrowRight size={15} /></Link>;
}

export function TextLink({ href, children }: { href: string; children: React.ReactNode }) {
  return <Link className="text-link" href={href}>{children}<ArrowUpRight size={15} /></Link>;
}

export function ScrollHint() {
  return <a className="scroll-hint" href="#discover"><span>SCROLL TO DISCOVER</span><ArrowDown size={14} /></a>;
}

export function StayCard({ room, index = 0, compareSlot }: { room: Accommodation; index?: number; compareSlot?: React.ReactNode }) {
  return (
    <article className={`stay-card ${index % 2 ? "stay-card--offset" : ""}`}>
      <Link href={`/stay/${room.slug}`} className="stay-card__image-link" aria-label={`Explore ${room.name}`}>
        <EditorialImage src={room.heroImage} alt={room.gallery[0]?.altText ?? room.name} className="stay-card__image" />
        <span className="stay-card__number">0{index + 1}</span>
        <span className="stay-card__image-arrow"><ArrowUpRight size={18} /></span>
      </Link>
      <div className="stay-card__meta"><span>{room.category} · {room.viewType}</span><span>UP TO {room.maxGuests} GUESTS</span></div>
      <div className="stay-card__title-row"><Link href={`/stay/${room.slug}`}><h3>{room.name}</h3></Link>{compareSlot}</div>
      <p className="stay-card__copy">{room.shortDescription}</p>
      <div className="stay-card__bottom"><span>From <strong>{formatMoney(room.startingNightlyRate, room.currency)}</strong> / night</span><Link href={`/stay/${room.slug}`} className="text-link">Discover the stay <ArrowRight size={14} /></Link></div>
    </article>
  );
}

export function ExperienceCard({ item }: { item: Experience }) {
  return <Link className="experience-card" href={`/experiences/${item.slug}`}>
    <EditorialImage src={item.heroImage} alt={item.name} className="experience-card__image" />
    <div className="experience-card__overlay"><span className="eyebrow eyebrow--light">{item.category} · {Math.round(item.durationMinutes / 60)} HOUR{item.durationMinutes >= 120 ? "S" : ""}</span><h3>{item.name}</h3><span className="experience-card__link">Explore experience <ArrowUpRight size={16} /></span></div>
  </Link>;
}

export function OfferPanel({ offer }: { offer: Offer }) {
  return <section className="offer-panel">
    <EditorialImage src={offer.heroImage} alt={offer.name} className="offer-panel__image" />
    <div className="offer-panel__copy"><Eyebrow>AN INVITATION TO STAY A LITTLE LONGER</Eyebrow><h2>{offer.name}</h2><p>{offer.description}</p><p className="offer-panel__terms">{offer.minimumNights} night minimum · {offer.terms}</p><ButtonLink href={`/offers/${offer.slug}`}>Discover the offer</ButtonLink></div>
  </section>;
}

export function LoadingState({ label = "Gathering the details…" }: { label?: string }) {
  return <div className="loading-state" role="status"><span className="loading-mark" /><span>{label}</span></div>;
}

export function ApiErrorState({ message, retry }: { message: string; retry?: () => void }) {
  return <div className="api-error" role="alert"><Eyebrow>A MOMENT, PLEASE</Eyebrow><p>{message}</p>{retry && <button className="text-button" onClick={retry}>Try again <ArrowRight size={14} /></button>}</div>;
}

export function EmptyState({ title, copy, href = "/book", cta = "Plan a stay" }: { title: string; copy: string; href?: string; cta?: string }) {
  return <div className="empty-state"><span className="empty-state__mark">VC</span><h3>{title}</h3><p>{copy}</p><ButtonLink href={href}>{cta}</ButtonLink></div>;
}

export function OfferCard({ offer }: { offer: Offer }) {
  return <article className="offer-card"><Link href={`/offers/${offer.slug}`}><EditorialImage src={offer.heroImage} alt={offer.name} className="offer-card__image" /><span className="eyebrow">{offer.minimumNights} NIGHT{offer.minimumNights > 1 ? "S" : ""} MINIMUM</span><h3>{offer.name}</h3><p>{offer.description}</p><span className="text-link">Explore offer <ArrowRight size={14} /></span></Link></article>;
}
