import { Button } from "@/components/ui/button";
import { ArrowRight, Tractor, ShoppingBasket } from "lucide-react";
import Link from "next/link";
import Image from "next/image";
import HeroBrowseLink from "@/components/landing/HeroBrowseLink";

const HeroSection = () => {
    return (
        <section id="hero" className="relative min-h-screen flex items-center justify-center overflow-hidden">
            <Image
                src="/hero-farm.jpg"
                alt="Lush green farmland at sunrise"
                className="absolute inset-0 w-full h-full object-cover"
                fill
                sizes="100vw"
                priority
            />
            <div className="absolute inset-0" style={{ background: "var(--hero-overlay)" }} />

            <div className="relative z-10 container mx-auto px-6 text-center">
                <p className="text-secondary font-medium tracking-widest uppercase text-sm mb-4 animate-fade-in">
                    Farm Fresh · No Middlemen · Fair Prices
                </p>
                <h1 className="text-4xl sm:text-5xl md:text-7xl font-extrabold text-primary-foreground leading-tight mb-6 animate-fade-up">
                    From Farm to
                    <span className="text-gradient-warm"> Your Table</span>
                </h1>
                <p className="text-lg md:text-xl text-primary-foreground/80 max-w-2xl mx-auto mb-10 animate-fade-up" style={{ animationDelay: "0.2s" }}>
                    Connect directly with local farmers. Buy fresh produce at fair prices.
                    No middlemen, no markup — just honest food from honest hands.
                </p>

                <div className="flex flex-col sm:flex-row gap-4 justify-center animate-fade-up" style={{ animationDelay: "0.4s" }}>
                    <Button asChild size="lg" className="bg-secondary text-secondary-foreground hover:bg-secondary/90 font-semibold text-base px-8 py-6 gap-2">
                        <Link href="/register/farmer">
                            <Tractor className="h-5 w-5" />
                            Register as Farmer
                            <ArrowRight className="h-4 w-4" />
                        </Link>
                    </Button>
                    <Button asChild size="lg" className="bg-primary-foreground text-foreground hover:bg-primary-foreground/90 font-semibold text-base px-8 py-6 gap-2">
                        <Link href="/register/buyer">
                            <ShoppingBasket className="h-5 w-5" />
                            Register as Buyer
                        </Link>
                    </Button>
                </div>

                <HeroBrowseLink />
            </div>
        </section>
    );
};

export default HeroSection;
