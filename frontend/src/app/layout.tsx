import type { Metadata } from "next";
import { Toaster } from "sonner";
import GoogleOAuthProviderWrapper from "@/components/providers/GoogleOAuthProviderWrapper";
import "./globals.css";

export const metadata: Metadata = {
  title: "DirectHarvest - From Farm to Your Table",
  description: "Connect directly with local farmers. Buy fresh produce at fair prices. No middlemen, no markup — just honest food from honest hands.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="min-h-full flex flex-col">
        <GoogleOAuthProviderWrapper>{children}</GoogleOAuthProviderWrapper>
        <Toaster richColors position="top-right" />
      </body>
    </html>
  );
}
