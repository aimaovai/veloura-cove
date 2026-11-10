"use client";

import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { resortImages } from "@/lib/assets";
import { ApiErrorState, ButtonLink, EditorialImage, Eyebrow, ExperienceCard, LoadingState, OfferPanel, ScrollHint, SectionHeading, StayCard, TextLink } from "@/components/ui";
import { BookingBar } from "@/components/BookingBar";

export function HomePage() {
  const stays = useQuery({ queryKey: ["home-stays"], queryFn: () => api.accommodations({ featured: true }) });
  const experiences = useQuery({ queryKey: ["home-experiences"], queryFn: () => api.experiences() });
  const offers = useQuery({ queryKey: ["home-offers"], queryFn: api.offers });
  const setting = useQuery({ queryKey: ["resort-settings"], queryFn: api.resortSettings });

  return <>
    <section className="hero">
      <EditorialImage src={resortImages.hero} alt="Quiet coastal resort villa beside the ocean at sunset" priority className="hero__image" />
      <div className="hero__veil" />
      <div className="hero__content"><Eyebrow light>VELOURA COVE · COASTAL SANCTUARY</Eyebrow><h1>Where the coast slows down.</h1><p>An intimate retreat where architecture, ocean, and stillness meet.</p><div className="hero__actions"><ButtonLink href="/the-cove" light>Explore the Cove</ButtonLink><TextLink href="/book">Reserve your stay</TextLink></div></div>
      <span className="hero__side-note">A QUIETER KIND OF ESCAPE · HAWAIʻI</span><ScrollHint />
    </section>
    <div className="booking-bar-wrap"><BookingBar /></div>

    <section className="intro-section" id="discover">
      <div className="intro-section__mark">21° 18′ 24.0″ N · 157° 49′ 42.0″ W</div>
      <div className="intro-section__copy"><Eyebrow>A PLACE APART</Eyebrow><h2>A quieter way to disappear.</h2><p>Set between volcanic cliffs and a hidden crescent of sand, Veloura Cove is imagined as a place for unhurried mornings, open horizons, and the rare luxury of having nowhere else to be.</p><TextLink href="/the-cove">The story of the cove</TextLink></div>
    </section>

    <section className="story-section">
      <EditorialImage src={resortImages.retreat} alt="An open-air suite with a calm garden and pool" className="story-section__image" />
      <div className="story-section__copy"><Eyebrow light>01 — ARRIVE</Eyebrow><h2>Let the outside in.</h2><p>A path through tropical gardens gives way to a private world of limestone, warm timber, and long views over the Pacific. Arrival is less a check-in than a gentle exhale.</p><TextLink href="/stay">Find your place</TextLink><div className="story-index"><span>01 ARRIVE</span><span>02 EXHALE</span><span>03 INDULGE</span><span>04 DISAPPEAR</span></div></div>
    </section>

    <section className="section-pad featured-stays">
      <div className="featured-stays__head"><SectionHeading label="THE ART OF STAYING" title={<>Spaces shaped<br />by the horizon.</>} copy="Six distinct expressions of coastal living, each with its own view of the cove." /><TextLink href="/stay">Discover all stays</TextLink></div>
      {stays.isLoading ? <LoadingState label="Discovering the stays…" /> : stays.isError ? <ApiErrorState message={stays.error.message} retry={() => void stays.refetch()} /> : (
        <div className="stays-grid">{(stays.data ?? []).slice(0, 4).map((room, index) => <StayCard key={room.id} room={room} index={index} />)}</div>
      )}
    </section>

    <section className="section-pad experience-feature">
      <div className="experience-feature__head"><SectionHeading label="A COVE OF POSSIBILITIES" title="Beyond the room." copy="Small moments, thoughtfully made. A slower rhythm, just beyond your door." /><TextLink href="/experiences">All experiences</TextLink></div>
      {experiences.isLoading ? <LoadingState /> : experiences.isError ? <ApiErrorState message={experiences.error.message} retry={() => void experiences.refetch()} /> : (
        <div className="experience-grid">{(experiences.data ?? []).filter(item => item.featured).slice(0, 3).map(item => <ExperienceCard item={item} key={item.slug} />)}</div>
      )}
    </section>

    <section className="dining-split">
      <div className="dining-split__copy"><Eyebrow>GATHER AROUND THE TABLE</Eyebrow><h2>A taste of<br />the extraordinary.</h2><p>Coastal ingredients, open-fire cooking, and a table that always has room for one more. At Veloura Cove, dining follows the light and the seasons.</p><ButtonLink href="/dining">Discover dining</ButtonLink></div>
      <EditorialImage src={resortImages.dining} alt="An intimate candlelit dinner featuring seasonal coastal cuisine" className="dining-split__image" />
    </section>

    <section className="wellness-banner">
      <EditorialImage src={resortImages.wellness} alt="A serene spa retreat surrounded by warm stone and water" className="wellness-banner__image" />
      <div className="wellness-banner__copy"><Eyebrow light>A MOMENT, JUST FOR YOU</Eyebrow><h2>Return to<br />yourself.</h2><p>Restorative rituals, quiet movement, and the luxury of listening to what you need.</p><ButtonLink href="/wellness" light>Explore wellness</ButtonLink></div>
    </section>

    {offers.isLoading ? <LoadingState label="Gathering an invitation…" /> : offers.isError ? <ApiErrorState message={offers.error.message} retry={() => void offers.refetch()} /> : offers.data?.[0] && <OfferPanel offer={offers.data[0]} />}

    <section className="closing-note"><Eyebrow>YOUR TIME, WELL SPENT</Eyebrow><h2>Some places stay with you.</h2><ButtonLink href="/book">Find your stay</ButtonLink></section>
    {setting.isError && <p className="sr-only" aria-live="polite">Contact details could not be loaded.</p>}
  </>;
}
