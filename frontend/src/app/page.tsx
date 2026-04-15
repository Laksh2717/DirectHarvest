import Navbar from "@/components/landing/Navbar";
import HeroSection from "@/components/landing/HeroSection";
import HowItWorks from "@/components/landing/HowItWorks";
import FeaturesSection from "@/components/landing/FeaturesSection";
import TestimonialsSection from "@/components/landing/TestimonialsSection";
import CTASection from "@/components/landing/CTASection";
import Footer from "@/components/landing/Footer";

const SectionDivider = ({ from, to }: { from: string; to: string }) => (
  <div
    className="h-16"
    style={{
      background: `linear-gradient(to bottom, ${from}, ${to})`,
    }}
  />
);

export default function Home() {
  return (
    <div className="min-h-screen">
      <Navbar />
      <HeroSection />
      <SectionDivider from="hsl(30 25% 12%)" to="hsl(42 33% 96%)" />
      <HowItWorks />
      <SectionDivider from="hsl(42 33% 96%)" to="hsl(145 35% 92% / 0.5)" />
      <FeaturesSection />
      <SectionDivider from="hsl(145 35% 92% / 0.5)" to="hsl(42 33% 96%)" />
      <TestimonialsSection />
      <SectionDivider from="hsl(42 33% 96%)" to="hsl(145 55% 28% / 0.3)" />
      <CTASection />
      <Footer />
    </div>
  );
}
