export const getApiErrorMessage = (error: unknown, fallbackMessage: string) => {
    if (error && typeof error === "object" && "response" in error) {
        const axiosError = error as { response?: { data?: { message?: string } } };
        return axiosError.response?.data?.message ?? fallbackMessage;
    }

    return fallbackMessage;
};
