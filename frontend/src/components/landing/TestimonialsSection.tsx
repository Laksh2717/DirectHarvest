import { Star } from "lucide-react";
import ScrollReveal from "@/components/landing/ScrollReveal";

const testimonials = [
    {
        name: "Rajesh Kumar",
        role: "Wheat Farmer, Punjab",
        text: "DirectHarvest changed my life. I now earn 40% more by selling directly to restaurants and families. No more dealing with exploitative middlemen.",
        rating: 5,
    },
    {
        name: "Priya Sharma",
        role: "Restaurant Owner, Delhi",
        text: "The freshness of produce we get through DirectHarvest is unmatched. Our customers can taste the difference, and we save on procurement costs.",
        rating: 5,
    },
    {
        name: "Anand Patel",
        role: "Organic Farmer, Gujarat",
        text: "Finally a platform that values farmers. The direct connection with buyers means I can plan my crops better and reduce waste significantly.",
        rating: 5,
    },
];

const TestimonialsSection = () => {
    return (
        <section id="testimonials" className="py-24 bg-background">
            <div className="container mx-auto px-6">
                <ScrollReveal>
                    <div className="text-center mb-16">
                        <p className="text-secondary font-semibold tracking-widest uppercase text-sm mb-2">Real Stories</p>
                        <h2 className="text-3xl md:text-5xl font-bold text-foreground">
                            Trusted by Thousands
                        </h2>
                    </div>
                </ScrollReveal>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    {testimonials.map((t, index) => (
                        <ScrollReveal key={index}>
                            <div className="bg-card rounded-2xl p-8 border border-border shadow-[var(--shadow-card)] h-full">
                                <div className="flex gap-1 mb-4">
                                    {Array.from({ length: t.rating }).map((_, i) => (
                                        <Star key={i} className="h-5 w-5 fill-secondary text-secondary" />
                                    ))}
                                </div>
                                <p className="text-foreground/80 leading-relaxed mb-6 italic">&quot;{t.text}&quot;</p>
                                <div>
                                    <p className="font-bold text-foreground">{t.name}</p>
                                    <p className="text-sm text-muted-foreground">{t.role}</p>
                                </div>
                            </div>
                        </ScrollReveal>
                    ))}
                </div>
            </div>
        </section>
    );
};

export default TestimonialsSection;
