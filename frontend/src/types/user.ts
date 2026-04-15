export interface UserProfileResponse {
    name: string;
    email: string;
    street: string;
    city: string;
    state: string;
    pincode: string;
    averageRating: number | null;
    ratingCount: number | null;
}

export interface UpdateUserProfilePayload {
    name: string;
    email: string;
    street: string;
    city: string;
    state: string;
    pincode: string;
}

export interface UserAddressResponse {
    street: string;
    city: string;
    state: string;
    pincode: string;
}


export interface UserProfileSectionProps {
    role: "farmer" | "buyer";
}

export interface FieldItemProps {
    label: string;
    value: string;
    shouldCapitalize?: boolean;
}