export type UserRole = "FARMER" | "BUYER";

export type SortDirection = "ASC" | "DESC";

export interface ApiFieldError {
    field?: string;
    message?: string;
}

export interface ApiErrorResponse {
    message?: string;
    fieldErrors?: ApiFieldError[];
}