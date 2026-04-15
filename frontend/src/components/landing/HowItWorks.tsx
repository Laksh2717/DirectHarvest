import { Handshake, Leaf, PackageCheck } from "lucide-react";
import ScrollReveal from "@/components/landing/ScrollReveal";

const steps = [
    {
        icon: Leaf,
        step: "01",
        title: "Farmers List Produce",
        description: "Farmers upload their fresh produce with pricing and availability directly on the platform.",
    },
    {
        icon: Handshake,
        step: "02",
        title: "Buyers Browse & Order",
        description: "Individuals and companies browse listings, compare prices, and place orders directly with farmers.",
    },
    {
        icon: PackageCheck,
        step: "03",
        title: "Buyer Picks Up",
        description: "Buyers arrange pickup or their own delivery — cutting costs and ensuring freshness on their schedule.",
    },
];

const HowItWorks = () => {
    return (
        <section id="how-it-works" className="py-24 bg-background">
            <div className="container mx-auto px-6">
                <ScrollReveal>
                    <div className="text-center mb-16">
                        <p className="text-secondary font-semibold tracking-widest uppercase text-sm mb-2">Simple Process</p>
                        <h2 className="text-3xl md:text-5xl font-bold text-foreground">
                            How It Works
                        </h2>
                    </div>
                </ScrollReveal>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    {steps.map((item, index) => (
                        <ScrollReveal key={index} className={`delay-${index}`}>
                            <div className="relative group bg-card rounded-2xl p-8 border border-border hover:border-primary/30 transition-all duration-300 hover:shadow-[var(--shadow-elevated)] h-full">
                                <span className="text-6xl font-extrabold text-muted/80 absolute top-4 right-6 group-hover:text-accent transition-colors">
                                    {item.step}
                                </span>
                                <div className="w-14 h-14 rounded-xl bg-accent flex items-center justify-center mb-5">
                                    <item.icon className="h-7 w-7 text-accent-foreground" />
                                </div>
                                <h3 className="text-xl font-bold text-foreground mb-3">{item.title}</h3>
                                <p className="text-muted-foreground leading-relaxed">{item.description}</p>
                            </div>
                        </ScrollReveal>
                    ))}
                </div>
            </div>
        </section>
    );
};

export default HowItWorks;
