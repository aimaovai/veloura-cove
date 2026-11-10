"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery } from "@tanstack/react-query";
import { ArrowRight } from "lucide-react";
import { useParams } from "next/navigation";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { api, formatMoney } from "@/lib/api";
import { resortImages } from "@/lib/assets";
import { ApiErrorState, ButtonLink, EditorialImage, Eyebrow, ExperienceCard, LoadingState, OfferCard, SectionHeading } from "@/components/ui";
import type { ExperienceCategory } from "@/types/api";

const experienceCategories: { label: string; value: ExperienceCategory | "" }[] = [
  { label: "All experiences", value: "" }, { label: "Wellness", value: "WELLNESS" },
  { label: "Dining", value: "DINING" }, { label: "Adventure", value: "ADVENTURE" },
  { label: "Romance", value: "ROMANCE" }, { label: "Water", value: "WATER" },
];

export function ExperiencesPage() {
  const [category, setCategory] = useState<ExperienceCategory | "">("");
  const query = useQuery({ queryKey: ["experiences", category], queryFn: () => api.experiences(category || undefined) });
  return <>
    <section className="page-hero"><EditorialImage src={resortImages.coast} alt="Pacific coastline around the cove" className="page-hero__image" /><div className="page-hero__copy"><Eyebrow light>A LITTLE MORE COVE</Eyebrow><h1>Make room<br />for wonder.</h1><p>Thoughtful ways to meet this place — on the water, at the table, and in a quieter state of mind.</p></div></section>
    <section className="page-intro"><div><Eyebrow>CURATED MOMENTS</Eyebrow><h2>The best memories have room to unfold.</h2></div><p>Find an experience that feels like you. Activities shown here are descriptive demonstrations; this website does not make standalone experience reservations.</p></section>
    <div className="filter-row" role="group" aria-label="Filter resort experiences">{experienceCategories.map(item => <button key={item.value} className={`filter-chip ${category === item.value ? "filter-chip--active" : ""}`} onClick={() => setCategory(item.value)} aria-pressed={category === item.value}>{item.label}</button>)}</div>
    {query.isLoading ? <LoadingState /> : query.isError ? <ApiErrorState message={query.error.message} retry={() => void query.refetch()} /> :
      <div className="experience-grid-page">{(query.data ?? []).map(item => <ExperienceCard item={item} key={item.slug} />)}</div>}
  </>;
}

export function ExperienceDetailPage() {
  const { slug } = useParams<{ slug: string }>();
  const query = useQuery({ queryKey: ["experience", slug], queryFn: () => api.experience(slug), enabled: Boolean(slug) });
  if (query.isLoading) return <div className="booking-page"><LoadingState label="Finding your experience…" /></div>;
  if (query.isError || !query.data) return <div className="booking-page"><ApiErrorState message={query.error?.message ?? "This experience could not be found."} retry={() => void query.refetch()} /></div>;
  const item = query.data;
  return <>
    <section className="experience-detail"><EditorialImage src={item.heroImage} alt={item.name} priority className="experience-detail__image" /><div className="experience-detail__copy"><Eyebrow>{item.category} · SIGNATURE EXPERIENCE</Eyebrow><h1>{item.name}</h1><p>{item.longDescription}</p><div className="experience-detail__fact"><span>Duration<strong>{Math.floor(item.durationMinutes / 60)}h{item.durationMinutes % 60 ? ` ${item.durationMinutes % 60}m` : ""}</strong></span><span>Starting from<strong>{formatMoney(item.startingPrice, item.currency)} / guest</strong></span></div><p className="disclaimer-note">Experience descriptions and prices are demonstration content. Experience bookings are not available through this site.</p><ButtonLink href="/book">Plan your stay</ButtonLink></div></section>
    {item.gallery.length > 1 && <section className="section-pad"><SectionHeading label="A CLOSER LOOK" title="The feeling of being here." /><div className="experience-grid-page">{item.gallery.slice(0, 3).map((image, index) => <EditorialImage key={`${image.url}-${index}`} src={image.url} alt={image.altText} className="experience-card" />)}</div></section>}
  </>;
}

export function OffersPage() {
  const query = useQuery({ queryKey: ["offers"], queryFn: api.offers });
  return <>
    <section className="page-hero"><EditorialImage src={resortImages.retreat} alt="An open-air suite with a private terrace" className="page-hero__image" /><div className="page-hero__copy"><Eyebrow light>AN INVITATION, EXTENDED</Eyebrow><h1>Stay for<br />the feeling.</h1><p>Considered packages for the moments you want to make a little more of.</p></div></section>
    <section className="page-intro"><div><Eyebrow>OFFERS & PACKAGES</Eyebrow><h2>Every reason to linger.</h2></div><p>Current packages and terms from the resort service. An offer is not applied to your booking estimate unless the backend includes it in the returned price.</p></section>
    {query.isLoading ? <LoadingState label="Gathering the invitations…" /> : query.isError ? <ApiErrorState message={query.error.message} retry={() => void query.refetch()} /> : <div className="offer-list">{(query.data ?? []).map(offer => <OfferCard key={offer.slug} offer={offer} />)}</div>}
  </>;
}

export function OfferDetailPage() {
  const { slug } = useParams<{ slug: string }>();
  const query = useQuery({ queryKey: ["offer", slug], queryFn: () => api.offer(slug), enabled: Boolean(slug) });
  if (query.isLoading) return <div className="booking-page"><LoadingState label="Opening this offer…" /></div>;
  if (query.isError || !query.data) return <div className="booking-page"><ApiErrorState message={query.error?.message ?? "This offer could not be found."} retry={() => void query.refetch()} /></div>;
  const offer = query.data;
  return <section className="offer-detail"><EditorialImage src={offer.heroImage} alt={offer.name} priority className="offer-detail__image" /><div className="offer-detail__copy"><Eyebrow>AN INVITATION TO THE COVE</Eyebrow><h1>{offer.name}</h1><p>{offer.description}</p><div className="offer-detail__terms"><span>Stay details</span><p>{offer.minimumNights} night minimum</p>{offer.validFrom && <p>Valid from {offer.validFrom}</p>}{offer.validUntil && <p>Valid through {offer.validUntil}</p>}{offer.applicableAccommodationSlugs.length > 0 && <p>Eligible stays: {offer.applicableAccommodationSlugs.join(", ")}</p>}</div><p className="disclaimer-note">{offer.terms} This displayed offer is not automatically applied to the reservation estimate.</p><ButtonLink href="/book">Explore available stays</ButtonLink></div></section>;
}

export function DiningPage() {
  const venues = [
    { name: "MAERA", descriptor: "CONTEMPORARY COASTAL", headline: "A table set by the tides.", copy: "Seasonal ingredients from island growers and fishers meet a thoughtful, ever-changing menu. Gather for a long lunch or let the evening unfold at its own pace.", image: resortImages.dining },
    { name: "SOLA", descriptor: "OPEN-FIRE DINING", headline: "The horizon, served warm.", copy: "An open kitchen, the glow of the hearth, and the first stars above the cove. Sola celebrates elemental cooking in an atmosphere made for lingering.", image: resortImages.coast },
    { name: "THE COVE BAR", descriptor: "COCKTAILS · SMALL PLATES", headline: "One more, as the sun goes down.", copy: "Find a seat where the last light rests on the water. A relaxed gathering place for fresh pours, small plates, and the unhurried end of day.", image: resortImages.retreat },
  ];
  return <>
    <section className="page-hero"><EditorialImage src={resortImages.dining} alt="The warmth and atmosphere of a coastal resort dining room" className="page-hero__image" /><div className="page-hero__copy"><Eyebrow light>GATHER AROUND THE TABLE</Eyebrow><h1>Made for<br />the appetite.</h1><p>Rooted in the coast. Guided by the season. Always worth slowing down for.</p></div></section>
    <section className="page-intro"><div><Eyebrow>THE TABLE AT VELOURA</Eyebrow><h2>From the first light to the last pour.</h2></div><p>Dining is part of the landscape here — intimate, generous, and shaped by the ingredients of the islands.</p></section>
    {venues.map((venue, index) => <section key={venue.name} className={`editorial-block ${index % 2 ? "editorial-block--reverse" : ""}`}><EditorialImage src={venue.image} alt={`${venue.name} dining atmosphere`} className="editorial-block__image" /><div className="editorial-block__copy"><Eyebrow>{venue.descriptor}</Eyebrow><h2>{venue.headline}</h2><p>{venue.copy}</p><p className="disclaimer-note">Venue details are fictional concepts. Dining hours and restaurant reservations are not configured in this demonstration.</p><ButtonLink href="/private-events">Enquire about dining</ButtonLink></div></section>)}
  </>;
}

export function WellnessPage() {
  const experiences = useQuery({ queryKey: ["wellness-experiences"], queryFn: () => api.experiences("WELLNESS") });
  return <>
    <section className="page-hero"><EditorialImage src={resortImages.wellness} alt="Warm tranquil spa water and natural stone" className="page-hero__image" /><div className="page-hero__copy"><Eyebrow light>A RETURN TO YOURSELF</Eyebrow><h1>Make space<br />for stillness.</h1><p>Restore your rhythm through quiet rituals, mindful movement, and a little more room to breathe.</p></div></section>
    <section className="editorial-block"><EditorialImage src={resortImages.retreat} alt="Peaceful resort retreat looking toward the garden" className="editorial-block__image" /><div className="editorial-block__copy"><Eyebrow>THE WELLNESS GARDEN</Eyebrow><h2>Leave the hurry at the water’s edge.</h2><p>A restorative concept for softer days: private treatments, warm stone, quiet gardens, and movement that meets you where you are.</p><p className="disclaimer-note">This fictional wellness concept does not make or confirm treatment appointments.</p><ButtonLink href="/private-events">Make a wellness inquiry</ButtonLink></div></section>
    <section className="section-pad experience-feature"><SectionHeading label="RESTORE & RECONNECT" title="A softer kind of ritual." copy="Explore wellness experiences listed by the resort service. Bookings are not available through this demonstration." />{experiences.isLoading ? <LoadingState /> : experiences.isError ? <ApiErrorState message={experiences.error.message} retry={() => void experiences.refetch()} /> : <div className="experience-grid" style={{ marginTop: 38 }}>{(experiences.data ?? []).map(item => <div key={item.slug}><ExperienceCard item={item} /></div>)}</div>}</section>
  </>;
}

export function CovePage() {
  return <>
    <section className="page-hero"><EditorialImage src={resortImages.coast} alt="A secluded stretch of Pacific coastline and water" className="page-hero__image" /><div className="page-hero__copy"><Eyebrow light>THE COVE, AS IMAGINED</Eyebrow><h1>Somewhere<br />between here<br />and elsewhere.</h1><p>A fictional coastal sanctuary dreamed into being as a portfolio hospitality experience.</p></div></section>
    <section className="editorial-block"><EditorialImage src={resortImages.architecture} alt="Natural textures and considered architecture" className="editorial-block__image" /><div className="editorial-block__copy"><Eyebrow>A PLACE APART</Eyebrow><h2>Architecture that listens to the landscape.</h2><p>In our imagined cove, low forms follow the contours of the coast. Warm stone, pale timber, and open-air living make space for the ocean to become part of every room.</p><p>Veloura Cove is a fictional resort concept. It has no real physical location or operating hospitality business.</p><ButtonLink href="/stay">Explore the stays</ButtonLink></div></section>
    <section className="feature-list">{[{ n: "01", title: "A slower rhythm", copy: "Days are shaped by the light, not the clock." }, { n: "02", title: "Room to be present", copy: "Intimate spaces encourage time well spent together." }, { n: "03", title: "Of the coast", copy: "An imaginative retreat inspired by island textures and the Pacific." }].map(item => <article className="feature-list__item" key={item.n}><span>{item.n} / OUR INTENTION</span><h3>{item.title}</h3><p>{item.copy}</p></article>)}</section>
    <section className="closing-note"><Eyebrow>A FICTIONAL RESORT, THOUGHTFULLY MADE</Eyebrow><h2>Where the coast slows down.</h2><ButtonLink href="/book">Find your stay</ButtonLink></section>
  </>;
}

const inquirySchema = z.object({
  name: z.string().trim().min(1, "Please enter your name.").max(150),
  email: z.string().trim().email("Please enter a valid email address.").max(254),
  phone: z.string().max(50).optional(),
  eventDate: z.string().optional(),
  partySize: z.number().int().min(1).max(10000).optional(),
  inquiryType: z.enum(["WEDDING", "PRIVATE_EVENT", "CORPORATE_RETREAT", "GENERAL", "TRANSPORTATION"]),
  message: z.string().trim().min(1, "Please tell us a little more.").max(3000),
});
type InquiryForm = z.infer<typeof inquirySchema>;

export function InquiryPage() {
  const [success, setSuccess] = useState("");
  const form = useForm<InquiryForm>({ resolver: zodResolver(inquirySchema), defaultValues: { name: "", email: "", phone: "", eventDate: "", inquiryType: "GENERAL", message: "" } });
  const mutation = useMutation({
    mutationFn: (values: InquiryForm) => api.inquiry({ ...values, eventDate: values.eventDate || null, partySize: values.partySize || null }),
    onSuccess: response => { setSuccess(`Your inquiry has been received (reference ${response.inquiryId.slice(0, 8)}). This is an inquiry only, not a reservation.`); form.reset(); },
  });
  const submit = form.handleSubmit(values => { setSuccess(""); mutation.mutate(values); });

  return <>
    <section className="page-hero"><EditorialImage src={resortImages.retreat} alt="Quiet private retreat set within the coast" className="page-hero__image" /><div className="page-hero__copy"><Eyebrow light>LET’S BEGIN A CONVERSATION</Eyebrow><h1>For the moments<br />worth gathering.</h1><p>An intimate celebration, a retreat with intention, or simply a question about the cove.</p></div></section>
    <section className="page-intro"><div><Eyebrow>PRIVATE EVENTS & INQUIRIES</Eyebrow><h2>A thoughtful beginning.</h2></div><p>Share a little about what you have in mind. This form sends an inquiry to the demonstration API; it does not reserve a date, venue, or accommodation.</p></section>
    <section className="section-pad" style={{ paddingTop: 38 }}><div className="inquiry-form-section">
      {success && <div className="inquiry-success" role="status">{success}</div>}
      <form className="inquiry-form" onSubmit={submit} noValidate>
        <div className="field"><label htmlFor="inquiry-name">Your name</label><input id="inquiry-name" autoComplete="name" {...form.register("name")} />{form.formState.errors.name && <p className="field-error">{form.formState.errors.name.message}</p>}</div>
        <div className="field"><label htmlFor="inquiry-email">Email</label><input id="inquiry-email" type="email" autoComplete="email" {...form.register("email")} />{form.formState.errors.email && <p className="field-error">{form.formState.errors.email.message}</p>}</div>
        <div className="field"><label htmlFor="inquiry-phone">Phone · optional</label><input id="inquiry-phone" type="tel" {...form.register("phone")} /></div>
        <div className="field"><label htmlFor="inquiry-type">I’m asking about</label><select id="inquiry-type" {...form.register("inquiryType")}><option value="GENERAL">A general question</option><option value="WEDDING">An intimate wedding</option><option value="PRIVATE_EVENT">A private event</option><option value="CORPORATE_RETREAT">A corporate retreat</option><option value="TRANSPORTATION">Private transportation</option></select></div>
        <div className="field"><label htmlFor="inquiry-date">Event date · optional</label><input id="inquiry-date" type="date" min="2026-01-01" {...form.register("eventDate")} /></div>
        <div className="field"><label htmlFor="inquiry-size">Party size · optional</label><input id="inquiry-size" type="number" min="1" max="10000" {...form.register("partySize", { setValueAs: value => value === "" ? undefined : Number(value) })} /></div>
        <div className="field inquiry-form__full"><label htmlFor="inquiry-message">A little more about it</label><textarea id="inquiry-message" maxLength={3000} rows={6} {...form.register("message")} />{form.formState.errors.message && <p className="field-error">{form.formState.errors.message.message}</p>}</div>
        {mutation.isError && <p className="form-error inquiry-form__full" role="alert">{mutation.error.message}</p>}
        <div className="inquiry-form__full"><button type="submit" className="button button--dark" disabled={mutation.isPending}>{mutation.isPending ? "Sending your inquiry…" : "Send an inquiry"} <ArrowRight size={14} /></button></div>
        <p className="disclaimer-note inquiry-form__full">A message does not create a booking or confirm any availability. Veloura Cove is a fictional portfolio project.</p>
      </form>
    </div></section>
  </>;
}
