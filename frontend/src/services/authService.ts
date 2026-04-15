import api from "@/lib/api";
import { clearAuthSession, persistAuthSession } from "@/lib/api/authSession";
import type { AuthResponse, GoogleLoginPayload, LoginPayload, RegisterPayload } from "@/types/auth";

export type { GoogleLoginPayload, LoginPayload, RegisterPayload } from "@/types/auth";

export const authService = {
    register: (data: RegisterPayload) =>
        api.post("/auth/register", data),
    login: async (data: LoginPayload) => {
        const response = await api.post<AuthResponse>("/auth/login", data);
        persistAuthSession(response.data);
        return response;
    },
    googleLogin: async (data: GoogleLoginPayload) => {
        const response = await api.post<AuthResponse>("/auth/google", data);
        persistAuthSession(response.data);
        return response;
    },
    refresh: async () => {
        const response = await api.post<AuthResponse>("/auth/refresh");
        persistAuthSession(response.data);
        return response;
    },
    logout: () =>
        api.post("/auth/logout").finally(() => {
            clearAuthSession();
        }),
};
