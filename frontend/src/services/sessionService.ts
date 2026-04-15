import type { UserRole } from "@/types/common";

const ACTIVE_ROLE_KEY = "directharvest_active_role";
const ACTIVE_NAME_KEY = "directharvest_active_name";
const ACCESS_TOKEN_EXPIRES_AT_KEY = "directharvest_access_token_expires_at";
const REFRESH_TOKEN_EXPIRES_AT_KEY = "directharvest_refresh_token_expires_at";
const SESSION_CHANGE_EVENT = "directharvest-session-change";

const parseRefreshBufferMs = () => {
    const rawValue = process.env.NEXT_PUBLIC_ACCESS_TOKEN_REFRESH_BUFFER_MS;
    const parsed = rawValue ? Number(rawValue) : NaN;
    if (Number.isFinite(parsed) && parsed >= 0) {
        return parsed;
    }
    return 30_000;
};

const ACCESS_TOKEN_REFRESH_BUFFER_MS = parseRefreshBufferMs();

const emitSessionChange = () => {
    if (typeof window === "undefined") return;
    window.dispatchEvent(new Event(SESSION_CHANGE_EVENT));
};

const formatDisplayName = (name: string) =>
    name
        .trim()
        .split(/\s+/)
        .filter(Boolean)
        .map((part) => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
        .join(" ");

const isExpiringSoon = (expiresAt: string | null, bufferMs = 30_000) => {
    if (!expiresAt) return false;
    const expiryTime = Date.parse(expiresAt);
    if (Number.isNaN(expiryTime)) return false;
    return expiryTime - Date.now() <= bufferMs;
};

export const sessionService = {
    setAuthSession(params: {
        role: UserRole;
        name: string;
        accessTokenExpiresAt: string;
        refreshTokenExpiresAt: string;
    }) {
        if (typeof window === "undefined") return;
        window.localStorage.setItem(ACTIVE_ROLE_KEY, params.role);
        window.localStorage.setItem(ACTIVE_NAME_KEY, params.name);
        window.localStorage.setItem(ACCESS_TOKEN_EXPIRES_AT_KEY, params.accessTokenExpiresAt);
        window.localStorage.setItem(REFRESH_TOKEN_EXPIRES_AT_KEY, params.refreshTokenExpiresAt);
        emitSessionChange();
    },
    setActiveRole(role: UserRole) {
        if (typeof window === "undefined") return;
        window.localStorage.setItem(ACTIVE_ROLE_KEY, role);
        emitSessionChange();
    },
    setActiveName(name: string) {
        if (typeof window === "undefined") return;
        window.localStorage.setItem(ACTIVE_NAME_KEY, name);
        emitSessionChange();
    },
    getActiveRole(): UserRole | null {
        if (typeof window === "undefined") return null;
        const role = window.localStorage.getItem(ACTIVE_ROLE_KEY);
        return role === "FARMER" || role === "BUYER" ? role : null;
    },
    getActiveName(): string | null {
        if (typeof window === "undefined") return null;
        return window.localStorage.getItem(ACTIVE_NAME_KEY);
    },
    getFormattedActiveName(): string | null {
        const name = this.getActiveName();
        return name ? formatDisplayName(name) : null;
    },
    getAccessTokenExpiresAt(): string | null {
        if (typeof window === "undefined") return null;
        return window.localStorage.getItem(ACCESS_TOKEN_EXPIRES_AT_KEY);
    },
    getRefreshTokenExpiresAt(): string | null {
        if (typeof window === "undefined") return null;
        return window.localStorage.getItem(REFRESH_TOKEN_EXPIRES_AT_KEY);
    },
    shouldRefreshAccessToken(bufferMs = ACCESS_TOKEN_REFRESH_BUFFER_MS): boolean {
        if (typeof window === "undefined") return false;
        return isExpiringSoon(window.localStorage.getItem(ACCESS_TOKEN_EXPIRES_AT_KEY), bufferMs);
    },
    clearActiveRole() {
        if (typeof window === "undefined") return;
        window.localStorage.removeItem(ACTIVE_ROLE_KEY);
        window.localStorage.removeItem(ACTIVE_NAME_KEY);
        window.localStorage.removeItem(ACCESS_TOKEN_EXPIRES_AT_KEY);
        window.localStorage.removeItem(REFRESH_TOKEN_EXPIRES_AT_KEY);
        emitSessionChange();
    },
    subscribe(listener: () => void) {
        if (typeof window === "undefined") {
            return () => undefined;
        }

        const handleStorage = (event: StorageEvent) => {
            if (event.key === ACTIVE_ROLE_KEY || event.key === ACTIVE_NAME_KEY || event.key === null) {
                listener();
            }
        };

        window.addEventListener("storage", handleStorage);
        window.addEventListener(SESSION_CHANGE_EVENT, listener);

        return () => {
            window.removeEventListener("storage", handleStorage);
            window.removeEventListener(SESSION_CHANGE_EVENT, listener);
        };
    },
};
