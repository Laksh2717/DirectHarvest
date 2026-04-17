import axios from "axios";
import type { AxiosError, InternalAxiosRequestConfig } from "axios";
import { sessionService } from "@/services/sessionService";
import { clearAuthSession, persistAuthSession } from "@/lib/api/authSession";
import type { AuthResponse } from "@/types/auth";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

const api = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true, // always send cookies with every request
    headers: {
        "Content-Type": "application/json",
    },
});

const refreshClient = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    },
});

let refreshPromise: Promise<AuthResponse> | null = null;

const authPaths = ["/auth/login", "/auth/register", "/auth/refresh", "/auth/logout", "/auth/google"];

const isAuthPath = (url?: string) =>
    Boolean(url && authPaths.some((path) => url.includes(path)));

const refreshAccessToken = async () => {
    if (!refreshPromise) {
        refreshPromise = refreshClient
            .post<AuthResponse>("/auth/refresh")
            .then((response) => {
                persistAuthSession(response.data);
                return response.data;
            })
            .catch((error) => {
                clearAuthSession();
                throw error;
            })
            .finally(() => {
                refreshPromise = null;
            });
    }

    return refreshPromise;
};

api.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
    if (isAuthPath(config.url)) {
        return config;
    }

    // Add cache control headers to prevent stale data in production
    config.headers["Cache-Control"] = "no-cache, no-store, must-revalidate";

    if (sessionService.getActiveRole() && sessionService.shouldRefreshAccessToken()) {
        await refreshAccessToken();
    }

    return config;
});

api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const originalRequest = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;

        if (!originalRequest || isAuthPath(originalRequest.url) || error.response?.status !== 401 || originalRequest._retry) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;

        try {
            await refreshAccessToken();
            return api(originalRequest);
        } catch {
            return Promise.reject(error);
        }
    },
);

export default api;
