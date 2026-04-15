import BrowseProductsPage from "@/components/browse/BrowseProductsPage";
import Navbar from "@/components/landing/Navbar";
import Footer from "@/components/landing/Footer";

export const metadata = {
    title: "Browse Products - Direct Harvest",
    description: "Discover fresh crops from local farmers",
};

export default function GuestBrowsePage() {
    return (
        <div className="min-h-screen">
            <Navbar forceSolid />

            <main>
                <section className="pt-24 pb-1">
                    <div className="container mx-auto px-6">
                        <h1 className="text-3xl font-extrabold text-foreground sm:text-4xl">
                            Browse Products
                        </h1>
                    </div>
                </section>

                <section className="bg-background py-10">
                    <div className="container mx-auto px-6">
                        <BrowseProductsPage />
                    </div>
                </section>
            </main>

            <Footer />
        </div>
    );
}
