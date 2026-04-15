import { ShoppingBasket, Tractor } from "lucide-react";
import type { AuthMode, AuthRole, AuthRoleContent } from "@/types/auth";

const AUTH_CONTENT: Record<AuthRole, AuthRoleContent> = {
    farmer: {
        icon: Tractor,
        leftIconClassName: "text-yellow-300",
        mobileIconClassName: "text-primary",
        byMode: {
            login: {
                leftHeadline: "Welcome back,\nFarmer!",
                leftDescription: "Login to manage your produce listings and connect with buyers.",
                formHeadline: "Login as a Farmer",
                switchPrompt: "Login as a Buyer?",
                switchLinkLabel: "Login as Buyer",
                footerPrompt: "Don\'t have an account?",
                footerLinkLabel: "Register as Farmer",
            },
            register: {
                leftHeadline: "Sell your produce\ndirectly to buyers",
                leftDescription: "Join thousands of farmers who earn more by cutting out the middleman.",
                formHeadline: "Register as a Farmer to get started",
                switchPrompt: "Want to register as a Buyer?",
                switchLinkLabel: "Register as Buyer",
                footerPrompt: "Already have an account?",
                footerLinkLabel: "Login as Farmer",
            },
        },
    },
    buyer: {
        icon: ShoppingBasket,
        leftIconClassName: "text-yellow-300",
        mobileIconClassName: "text-secondary",
        byMode: {
            login: {
                leftHeadline: "Welcome back,\nBuyer!",
                leftDescription: "Login to browse fresh produce and place orders directly from farms.",
                formHeadline: "Login as a Buyer",
                switchPrompt: "Login as a Farmer?",
                switchLinkLabel: "Login as Farmer",
                footerPrompt: "Don\'t have an account?",
                footerLinkLabel: "Register as Buyer",
            },
            register: {
                leftHeadline: "Get fresh produce\nstraight from farms",
                leftDescription: "Access farm-fresh produce at better prices with complete transparency.",
                formHeadline: "Register as a Buyer to get started",
                switchPrompt: "Want to register as a Farmer?",
                switchLinkLabel: "Register as Farmer",
                footerPrompt: "Already have an account?",
                footerLinkLabel: "Login as Buyer",
            },
        },
    },
};

export const getAuthRole = (value: string | undefined): AuthRole => (value === "farmer" ? "farmer" : "buyer");

export const getOppositeRole = (role: AuthRole): AuthRole => (role === "farmer" ? "buyer" : "farmer");

export const getAuthRoleContent = (role: AuthRole) => AUTH_CONTENT[role];

export const getAuthContent = (mode: AuthMode, role: AuthRole) => AUTH_CONTENT[role].byMode[mode];
