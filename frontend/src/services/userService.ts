import api from "@/lib/api";
import type { UpdateUserProfilePayload, UserAddressResponse, UserProfileResponse } from "@/types/user";

export type { UpdateUserProfilePayload, UserAddressResponse, UserProfileResponse } from "@/types/user";

export const userService = {
    getMyProfile: () => api.get<UserProfileResponse>("/users/me"),
    getMyAddress: () => api.get<UserAddressResponse>("/users/me/address"),
    updateMyProfile: (data: UpdateUserProfilePayload) => api.patch<UserProfileResponse>("/users/me", data),
};
