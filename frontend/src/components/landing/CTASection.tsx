import { Button } from "@/components/ui/button";
import { Tractor, ShoppingBasket, ArrowRight } from "lucide-react";
import ScrollReveal from "@/components/landing/ScrollReveal";
import Link from "next/link";

const CTASection = () => {
    return (
        <section className="py-24 bg-primary">
            <div className="container mx-auto px-6 text-center">
                <ScrollReveal>
                    <h2 className="text-3xl md:text-5xl font-bold text-primary-foreground mb-4">
                        Ready to Go Direct?
                    </h2>
                    <p className="text-primary-foreground/80 text-lg max-w-xl mx-auto mb-10">
                        Join thousands of farmers and buyers already benefiting from a fairer, fresher food marketplace.
                    </p>

                    <div className="flex flex-col sm:flex-row gap-4 justify-center">
                        <Button asChild size="lg" className="bg-secondary text-secondary-foreground hover:bg-secondary/90 font-semibold text-base px-8 py-6 gap-2">
                            <Link href="/register/farmer">
                                <Tractor className="h-5 w-5" />
                                Join as Farmer
                                <ArrowRight className="h-4 w-4" />
                            </Link>
                        </Button>
                        <Button asChild size="lg" className="bg-primary-foreground text-foreground hover:bg-primary-foreground/90 font-semibold text-base px-8 py-6 gap-2">
                            <Link href="/register/buyer">
                                <ShoppingBasket className="h-5 w-5" />
                                Join as Buyer
                            </Link>
                        </Button>
                    </div>

                    <div className="mt-16 grid grid-cols-1 sm:grid-cols-3 gap-8 max-w-3xl mx-auto">
                        {[
                            { value: "10,000+", label: "Active Farmers" },
                            { value: "50,000+", label: "Happy Buyers" },
                            { value: "₹2Cr+", label: "Farmer Earnings" },
                        ].map((stat, i) => (
                            <div key={i}>
                                <p className="text-3xl md:text-4xl font-bold text-secondary">{stat.value}</p>
                                <p className="text-primary-foreground/70 text-sm mt-1">{stat.label}</p>
                            </div>
                        ))}
                    </div>
                </ScrollReveal>
            </div>
        </section>
    );
};

export default CTASection;
