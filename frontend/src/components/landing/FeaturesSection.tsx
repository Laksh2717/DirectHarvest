import { TrendingUp, Users, Clock, BarChart3, Globe, HeartHandshake } from "lucide-react";
import ScrollReveal from "@/components/landing/ScrollReveal";

const features = [
    {
        icon: TrendingUp,
        title: "Fair Pricing",
        description: "Farmers earn more, buyers pay less. No middleman margins eating into profits.",
    },
    {
        icon: Users,
        title: "Direct Connection",
        description: "Build lasting relationships between producers and consumers for trust and quality.",
    },
    {
        icon: Clock,
        title: "Fresh Guaranteed",
        description: "Produce reaches you faster — harvested and available for pickup within hours, not days.",
    },
    {
        icon: BarChart3,
        title: "Market Insights",
        description: "Real-time pricing data helps farmers make smarter decisions about what to grow.",
    },
    {
        icon: Globe,
        title: "Local & Sustainable",
        description: "Reduce food miles and carbon footprint by sourcing from nearby farms.",
    },
    {
        icon: HeartHandshake,
        title: "Community First",
        description: "Strengthen local food systems and support the farming communities that feed us.",
    },
];

const FeaturesSection = () => {
    return (
        <section id="features" className="py-24 bg-accent/50">
            <div className="container mx-auto px-6">
                <ScrollReveal>
                    <div className="text-center mb-16">
                        <p className="text-secondary font-semibold tracking-widest uppercase text-sm mb-2">Why DirectHarvest</p>
                        <h2 className="text-3xl md:text-5xl font-bold text-foreground">
                            Benefits for Everyone
                        </h2>
                    </div>
                </ScrollReveal>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                    {features.map((feature, index) => (
                        <ScrollReveal key={index}>
                            <div className="group bg-card rounded-2xl p-8 border border-border hover:border-primary/30 transition-all duration-300 hover:shadow-[var(--shadow-elevated)] h-full">
                                <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-5 group-hover:bg-primary transition-colors">
                                    <feature.icon className="h-6 w-6 text-primary group-hover:text-primary-foreground transition-colors" />
                                </div>
                                <h3 className="text-xl font-bold text-foreground mb-2">{feature.title}</h3>
                                <p className="text-muted-foreground leading-relaxed">{feature.description}</p>
                            </div>
                        </ScrollReveal>
                    ))}
                </div>
            </div>
        </section>
    );
};

export default FeaturesSection;
