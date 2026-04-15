import { Sprout } from "lucide-react";

const Footer = () => {
    return (
        <footer className="bg-foreground py-16">
            <div className="container mx-auto px-6">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-12">
                    <div className="md:col-span-2">
                        <div className="flex items-center gap-2 mb-4">
                            <Sprout className="h-7 w-7 text-secondary" />
                            <span className="font-heading text-xl font-bold text-background">DirectHarvest</span>
                        </div>
                        <p className="text-background/60 max-w-sm leading-relaxed">
                            Empowering farmers and connecting them directly with buyers for a fairer, fresher food ecosystem.
                        </p>
                    </div>

                    <div>
                        <h4 className="font-bold text-background mb-4">Platform</h4>
                        <ul className="space-y-3 text-background/60">
                            <li><a href="#" className="hover:text-secondary transition-colors">How It Works</a></li>
                            <li><a href="#" className="hover:text-secondary transition-colors">For Farmers</a></li>
                            <li><a href="#" className="hover:text-secondary transition-colors">For Buyers</a></li>
                            <li><a href="#" className="hover:text-secondary transition-colors">Pricing</a></li>
                        </ul>
                    </div>

                    <div>
                        <h4 className="font-bold text-background mb-4">Company</h4>
                        <ul className="space-y-3 text-background/60">
                            <li><a href="#" className="hover:text-secondary transition-colors">About Us</a></li>
                            <li><a href="#" className="hover:text-secondary transition-colors">Contact</a></li>
                            <li><a href="#" className="hover:text-secondary transition-colors">Careers</a></li>
                            <li><a href="#" className="hover:text-secondary transition-colors">Blog</a></li>
                        </ul>
                    </div>
                </div>

                <div className="border-t border-background/10 mt-12 pt-8 text-center">
                    <p className="text-background/40 text-sm">
                        © 2026 DirectHarvest. All rights reserved. Built with ❤️ for farmers.
                    </p>
                </div>
            </div>
        </footer>
    );
};

export default Footer;
