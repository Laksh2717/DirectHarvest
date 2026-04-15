import { sessionService } from "@/services/sessionService";
import type { AuthResponse } from "@/types/auth";

export const persistAuthSession = (response: AuthResponse | undefined) => {
    if (response?.role && response?.name && response?.accessTokenExpiresAt && response?.refreshTokenExpiresAt) {
        sessionService.setAuthSession({
            role: response.role,
            name: response.name,
            accessTokenExpiresAt: response.accessTokenExpiresAt,
            refreshTokenExpiresAt: response.refreshTokenExpiresAt,
        });
    }
};

export const clearAuthSession = () => {
    sessionService.clearActiveRole();
};
