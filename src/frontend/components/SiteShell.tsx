"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { ArrowRight, Menu, X } from "lucide-react";
import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";

type CompareContextValue = { ids: string[]; toggle: (id: string) => void; clear: () => void };
const CompareContext = createContext<CompareContextValue | null>(null);

export function useCompare() {
  const value = useContext(CompareContext);
  if (!value) throw new Error("useCompare must be used within SiteShell");
  return value;
}

const links = [
  { label: "Stay", href: "/stay" },
  { label: "Experiences", href: "/experiences" },
  { label: "Dining", href: "/dining" },
  { label: "Wellness", href: "/wellness" },
  { label: "The Cove", href: "/the-cove" },
  { label: "Offers", href: "/offers" },
];

export function SiteShell({ children }: { children: React.ReactNode }) {
  const path = usePathname();
  const home = path === "/";
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [ids, setIds] = useState<string[]>([]);
  const [compareOpen, setCompareOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const menuToggleRef = useRef<HTMLButtonElement>(null);
  const { data: rooms } = useQuery({ queryKey: ["accommodations"], queryFn: () => api.accommodations() });

  useEffect(() => {
    const stored = localStorage.getItem("vc-compare");
    if (stored) {
      try { setIds((JSON.parse(stored) as string[]).slice(0, 3)); } catch { localStorage.removeItem("vc-compare"); }
    }
    const onScroll = () => setScrolled(window.scrollY > 50);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  const clear = useCallback(() => {
    setIds([]);
    localStorage.removeItem("vc-compare");
  }, []);
  const toggle = useCallback((id: string) => {
    setIds(current => {
      const next = current.includes(id) ? current.filter(item => item !== id) : current.length < 3 ? [...current, id] : current;
      localStorage.setItem("vc-compare", JSON.stringify(next));
      return next;
    });
  }, []);
  const compareValue = useMemo(() => ({ ids, toggle, clear }), [ids, toggle, clear]);
  const compareRooms = rooms?.filter(room => ids.includes(room.id)) ?? [];

  useEffect(() => { setMenuOpen(false); }, [path]);
  useEffect(() => {
    if (!menuOpen) {
      document.body.style.overflow = "";
      return;
    }
    document.body.style.overflow = "hidden";
    menuRef.current?.querySelector<HTMLAnchorElement>("a[href]")?.focus();
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setMenuOpen(false);
        window.requestAnimationFrame(() => menuToggleRef.current?.focus());
        return;
      }
      if (event.key !== "Tab" || !menuRef.current) return;
      const focusable = Array.from(menuRef.current.querySelectorAll<HTMLAnchorElement>("a[href]"));
      if (focusable.length === 0) return;
      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => {
      window.removeEventListener("keydown", handleKeyDown);
      document.body.style.overflow = "";
    };
  }, [menuOpen]);

  return (
    <CompareContext.Provider value={compareValue}>
      <a className="skip-link" href="#main-content">Skip to content</a>
      <header className={`site-header ${home && !scrolled && !menuOpen ? "site-header--transparent" : ""} ${scrolled || menuOpen ? "site-header--scrolled" : ""}`}>
        <Link className="wordmark" href="/" aria-label="Veloura Cove home">
          <span className="wordmark__name">VELOURA COVE</span>
          <span className="wordmark__sub">COASTAL SANCTUARY</span>
        </Link>
        <nav className="desktop-nav" aria-label="Main navigation">
          {links.map(item => <Link key={item.href} href={item.href} className={path.startsWith(item.href) ? "nav-link nav-link--active" : "nav-link"}>{item.label}</Link>)}
        </nav>
        <div className="header-actions">
          <Link className="header-book" href="/book">Book your stay <ArrowRight size={14} /></Link>
          <button ref={menuToggleRef} className="menu-toggle" onClick={() => setMenuOpen(open => !open)} aria-label={menuOpen ? "Close menu" : "Open menu"} aria-expanded={menuOpen} aria-controls="mobile-menu">
            {menuOpen ? <X size={21} /> : <Menu size={21} />}
          </button>
        </div>
      </header>
      <div id="mobile-menu" ref={menuRef} className={`mobile-menu ${menuOpen ? "mobile-menu--open" : ""}`} aria-hidden={!menuOpen}>
        <span className="eyebrow">DISCOVER VELOURA COVE</span>
        {links.map((item, index) => <Link key={item.href} href={item.href} tabIndex={menuOpen ? 0 : -1}><span className="mobile-menu__index">0{index + 1}</span>{item.label}<ArrowRight size={19} /></Link>)}
        <Link className="button button--dark mobile-menu__book" href="/book" tabIndex={menuOpen ? 0 : -1}>Find your stay <ArrowRight size={15} /></Link>
      </div>
      <main id="main-content">{children}</main>
      <footer className="footer">
        <div className="footer__top">
          <div className="footer__brand"><Link className="wordmark wordmark--footer" href="/"><span className="wordmark__name">VELOURA COVE</span><span className="wordmark__sub">WHERE THE COAST SLOWS DOWN</span></Link><p>An intimate coastal sanctuary,<br />imagined for a slower kind of escape.</p></div>
          <div className="footer__column"><span className="eyebrow">EXPLORE</span><Link href="/stay">The stays</Link><Link href="/experiences">Experiences</Link><Link href="/dining">Dining</Link><Link href="/wellness">Wellness</Link></div>
          <div className="footer__column"><span className="eyebrow">PLAN YOUR STAY</span><Link href="/offers">Offers & packages</Link><Link href="/book">Availability</Link><Link href="/private-events">Private events</Link><Link href="/the-cove">The Cove</Link></div>
          <div className="footer__column"><span className="eyebrow">GET IN TOUCH</span><Link href="/private-events">Make an inquiry <ArrowRight size={13} /></Link><a href="mailto:stay@velouracove.example">stay@velouracove.example</a><Link href="/manage">Manage a stay</Link><Link href="/privacy">Privacy</Link><Link href="/terms">Terms</Link></div>
        </div>
        <div className="footer__bottom">
          <span>© {new Date().getFullYear()} VELOURA COVE · A FICTIONAL RESORT</span>
          <p>Veloura Cove is a fictional luxury hospitality concept developed as a portfolio demonstration. All reservations and payments are simulated.</p>
        </div>
      </footer>
      {ids.length > 0 && (
        <div className={`compare-tray ${compareOpen ? "compare-tray--open" : ""}`}>
          <button className="compare-tray__trigger" onClick={() => setCompareOpen(open => !open)} aria-expanded={compareOpen}>
            <span className="compare-tray__count">{ids.length}</span> stays selected <span className="compare-tray__show">{compareOpen ? "Close" : "Compare"}</span>
          </button>
          {compareOpen && <div className="compare-tray__panel">
            {compareRooms.map(room => <div key={room.id} className="compare-tray__room"><span>{room.name}</span><button aria-label={`Remove ${room.name} from comparison`} onClick={() => toggle(room.id)}>×</button></div>)}
            {ids.length < 2 && <p>Select one more stay to compare.</p>}
            <div className="compare-tray__actions"><button className="text-button" onClick={clear}>Clear all</button>{ids.length > 1 && <Link className="button button--dark" href="/compare">Compare stays <ArrowRight size={14} /></Link>}</div>
          </div>}
        </div>
      )}
    </CompareContext.Provider>
  );
}
