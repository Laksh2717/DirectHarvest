"use client";

import { Sprout } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useState, useEffect, useMemo, useSyncExternalStore } from "react";
import Link from "next/link";
import { sessionService } from "@/services/sessionService";

const smoothScroll = (e: React.MouseEvent<HTMLAnchorElement>, id: string) => {
    e.preventDefault();
    const el = document.getElementById(id);
    if (el) el.scrollIntoView({ behavior: "smooth" });
};

type NavbarProps = {
    forceSolid?: boolean;
};

const Navbar = ({ forceSolid = false }: NavbarProps) => {
    const [scrolled, setScrolled] = useState(false);
    const activeRole = useSyncExternalStore(
        sessionService.subscribe,
        () => sessionService.getActiveRole(),
        () => null,
    );
    const isLoggedIn = activeRole === "FARMER" || activeRole === "BUYER";
    const dashboardHref = useMemo(() => {
        if (activeRole === "FARMER") {
            return "/farmer";
        }
        return "/buyer";
    }, [activeRole]);

    const isSolid = forceSolid || scrolled;

    useEffect(() => {
        const onScroll = () => setScrolled(window.scrollY > 60);
        window.addEventListener("scroll", onScroll);
        return () => window.removeEventListener("scroll", onScroll);
    }, []);

    return (
        <nav
            className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${isSolid
                ? "bg-background/90 backdrop-blur-lg border-b border-border shadow-sm"
                : "bg-transparent border-b border-transparent"
                }`}
        >
            <div className="container mx-auto px-6 h-16 flex items-center justify-between">
                <Link href="/" className="flex items-center gap-2 cursor-pointer">
                    <Sprout className={`h-7 w-7 transition-colors ${isSolid ? "text-primary" : "text-secondary"}`} />
                    <span className={`font-heading text-xl font-bold transition-colors ${isSolid ? "text-foreground" : "text-primary-foreground"}`}>
                        DirectHarvest
                    </span>
                </Link>

                <div className="hidden md:flex items-center gap-8">
                    {[
                        { id: "how-it-works", label: "How It Works" },
                        { id: "features", label: "Features" },
                        { id: "testimonials", label: "Testimonials" },
                    ].map((link) => (
                        <a
                            key={link.id}
                            href={`#${link.id}`}
                            onClick={(e) => smoothScroll(e, link.id)}
                            className={`text-sm font-medium transition-colors ${isSolid ? "text-muted-foreground hover:text-foreground" : "text-primary-foreground/70 hover:text-primary-foreground"
                                }`}
                        >
                            {link.label}
                        </a>
                    ))}
                </div>

                <div className="flex items-center gap-3">
                    {isLoggedIn ? (
                        <Button asChild size="sm" className={`font-medium ${isSolid ? "bg-primary text-primary-foreground hover:bg-primary/90" : "bg-secondary text-secondary-foreground hover:bg-secondary/90"}`}>
                            <Link href={dashboardHref}>Go to Dashboard</Link>
                        </Button>
                    ) : (
                        <>
                            <Link
                                href="/browse"
                                className={`text-sm font-medium transition-colors ${isSolid ? "text-muted-foreground hover:text-foreground" : "text-primary-foreground/70 hover:text-primary-foreground"}`}
                            >
                                Browse Products
                            </Link>
                            <Button asChild variant="ghost" size="sm" className={`transition-colors ${isSolid ? "text-foreground" : "text-primary-foreground hover:bg-primary-foreground/10"}`}>
                                <Link href="/login/farmer">Login</Link>
                            </Button>
                            <Button asChild size="sm" className={`font-medium ${isSolid ? "bg-primary text-primary-foreground hover:bg-primary/90" : "bg-secondary text-secondary-foreground hover:bg-secondary/90"}`}>
                                <Link href="/register/farmer">Get Started</Link>
                            </Button>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
