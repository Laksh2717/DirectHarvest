import Link from "next/link";
import Image from "next/image";
import { Sprout } from "lucide-react";
import { getAuthContent, getAuthRoleContent, getOppositeRole } from "@/lib/authContent";
import type { AuthPageShellProps } from "@/types/auth";

export default function AuthPageShell({ mode, role, children, footer }: AuthPageShellProps) {
    const content = getAuthContent(mode, role);
    const roleContent = getAuthRoleContent(role);
    const oppositeRole = getOppositeRole(role);
    const Icon = roleContent.icon;

    return (
        <div className="flex h-screen overflow-hidden">
            <div className="relative hidden flex-col justify-between p-10 lg:flex lg:w-1/2">
                <Image
                    src="/register-bg.jpg"
                    alt="Fresh farm produce"
                    className="absolute inset-0 h-full w-full object-cover"
                    fill
                    sizes="50vw"
                />
                <div className="absolute inset-0 bg-gradient-to-b from-black/60 via-black/30 to-black/80" />

                <div className="relative z-10">
                    <Link href="/" className="inline-flex items-center gap-2">
                        <Sprout className="h-8 w-8 text-primary" />
                        <span className="font-heading text-2xl font-bold text-white">DirectHarvest</span>
                    </Link>
                </div>

                <div className="relative z-10 space-y-6">
                    <div className="flex h-14 w-14 items-center justify-center rounded-2xl border border-white/25 bg-white/15 backdrop-blur-sm">
                        <Icon className={`h-7 w-7 ${roleContent.leftIconClassName}`} />
                    </div>
                    <h1 className="whitespace-pre-line font-heading text-4xl font-bold leading-tight text-white">{content.leftHeadline}</h1>
                    <p className="max-w-md text-lg text-white/80">{content.leftDescription}</p>
                </div>

                <div className="relative z-10 rounded-xl bg-black/40 px-4 py-3 backdrop-blur-sm">
                    <p className="text-sm font-medium text-white">
                        {content.switchPrompt}{" "}
                        <Link
                            href={`/${mode}/${oppositeRole}`}
                            className="font-bold text-yellow-300 underline underline-offset-2 hover:text-yellow-200"
                        >
                            {content.switchLinkLabel}
                        </Link>
                    </p>
                </div>
            </div>

            <div className="flex w-full flex-col overflow-y-auto bg-background lg:w-1/2">
                <div className="flex min-h-full flex-col justify-center px-6 py-8 sm:px-12 lg:px-16">
                    <div className="mb-6 lg:hidden">
                        <Link href="/" className="inline-flex items-center gap-2">
                            <Sprout className="h-7 w-7 text-primary" />
                            <span className="font-heading text-xl font-bold text-foreground">DirectHarvest</span>
                        </Link>
                    </div>

                    <div className="mx-auto w-full max-w-md space-y-4">
                        <div>
                            <div className="mb-2 flex items-center gap-3 lg:hidden">
                                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary/10">
                                    <Icon className={`h-5 w-5 ${roleContent.mobileIconClassName}`} />
                                </div>
                            </div>
                            <h2 className="font-heading text-xl font-bold text-foreground">{content.formHeadline}</h2>
                        </div>

                        {children}

                        <div className="space-y-1 text-center text-sm text-muted-foreground">{footer}</div>
                    </div>
                </div>
            </div>
        </div>
    );
}
