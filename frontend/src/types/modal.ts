import { FarmerListingCardState } from "@/hooks/listings/useFarmerListingCard";
import type { ListingResponse } from "@/services/listingService";
import { FormEventHandler, ReactNode } from "react";
import { NegotiationResponse } from "./offer";
import { FieldErrors, SubmitHandler, UseFormHandleSubmit, UseFormRegister } from "react-hook-form";
import { CancelOrderFormValues } from "./order";
import { UpdateUserProfilePayload } from "./user";

export type AddQuantityFormValues = {
  newQuantity: string;
};

export type UseAddQuantityModalParams = {
  listing: ListingResponse;
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
};

export type ActionType = 'makeInactive' | 'delete' | undefined;

export type UseConfirmationModalParams = {
  listingId: number;
  actionType: ActionType;
  onConfirm: () => void;
  onCancel: () => void;
};

export type UpdateDetailsFormData = {
  cropName: string;
  description: string;
  street: string;
  city: string;
  state: string;
  pincode: string;
};

export type UseUpdateDetailsModalParams = {
  listing: ListingResponse;
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
};

export type UseUpdateImagesModalParams = {
  listing: ListingResponse;
  onClose: () => void;
  onSuccess?: () => void;
};

export interface ImagePreview {
  id: number;
  url: string;
  isPrimary: boolean;
}

export type UpdatePriceFormValues = {
  newPrice: string;
};

export type UseUpdatePriceModalParams = {
  listing: ListingResponse;
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
};

export interface ConfirmationModalProps {
  isOpen: boolean;
  title: string;
  message: string;
  listingId: number;
  onConfirm: () => void;
  onCancel: () => void;
  actionType?: 'makeInactive' | 'delete';
}

export type ListingAddQuantityModalProps = {
    listing: ListingResponse;
    addQtyForm: FarmerListingCardState["addQtyForm"];
    actionLoading: FarmerListingCardState["actions"]["actionLoading"];
    onCancel: () => void;
};

export type ListingConfirmationModalProps = {
    open: boolean;
    title: string;
    description: string;
    confirmText: string;
    loading: boolean;
    icon: ReactNode;
    onCancel: () => void;
    onConfirm: () => void;
};

export type ListingUpdateDetailsModalProps = {
    listing: ListingResponse;
    updateDetailsForm: FarmerListingCardState["updateDetailsForm"];
    actionLoading: FarmerListingCardState["actions"]["actionLoading"];
    onCancel: () => void;
};

export type ListingUpdateImagesModalProps = {
    listing: ListingResponse;
    imageState: FarmerListingCardState["imageState"];
    actionLoading: FarmerListingCardState["actions"]["actionLoading"];
    onCancel: () => void;
};

export type ListingUpdatePriceModalProps = {
    listing: ListingResponse;
    updatePriceForm: FarmerListingCardState["updatePriceForm"];
    actionLoading: FarmerListingCardState["actions"]["actionLoading"];
    onCancel: () => void;
};

export type ModalLayoutProps = {
    open: boolean;
    title: string;
    onClose: () => void;
    children: ReactNode;
    closeAriaLabel?: string;
    backdropAriaLabel?: string;
    maxWidth?: "sm" | "md" | "lg" | "xl";
    onBackdropClick?: () => void;
    closeButtonDisabled?: boolean;
};

export type NegotiationAcceptModalProps = {
    negotiation: NegotiationResponse;
    currentPrice: number;
    currentQty: number;
    onClose: () => void;
    actionLoading: "accept" | "reject" | "counter" | null;
    onConfirm: () => void;
};

export type CounterFormValues = {
    counterPrice: string;
    counterQty: string;
};

export type NegotiationCounterModalProps = {
    negotiation: NegotiationResponse;
    currentPrice: number;
    currentQty: number;
    availableQuantity: number | null;
    onClose: () => void;
    actionLoading: "accept" | "reject" | "counter" | null;
    registerCounter: UseFormRegister<CounterFormValues>;
    handleCounterFormSubmit: UseFormHandleSubmit<CounterFormValues>;
    counterErrors: FieldErrors<CounterFormValues>;
    isCounterSubmitting: boolean;
    onSubmit: (data: { counterPrice: string; counterQty: string }) => void;
};

export type RejectFormValues = {
    rejectReason: string;
};

export type NegotiationRejectModalProps = {
    negotiation: NegotiationResponse;
    currentPrice: number;
    currentQty: number;
    onClose: () => void;
    actionLoading: "accept" | "reject" | "counter" | null;
    registerReject: UseFormRegister<RejectFormValues>;
    handleRejectSubmit: UseFormHandleSubmit<RejectFormValues>;
    isRejectSubmitting: boolean;
    onSubmit: (data: { rejectReason: string }) => void;
    errors?: FieldErrors<RejectFormValues>;
};

export type OrderCancelModalProps = {
    open: boolean;
    loading: boolean;
    isSubmitting: boolean;
    onClose: () => void;
    onSubmit: FormEventHandler<HTMLFormElement>;
    registerCancel: UseFormRegister<CancelOrderFormValues>;
};

export type OrderRatingModalProps = {
    open: boolean;
    orderDisplayId: string;
    selectedRating: number;
    submitting: boolean;
    onClose: () => void;
    onSelectRating: (rating: number) => void;
    onSubmit: () => void;
};

export type UserProfileUpdateModalProps = {
    open: boolean;
    isSubmitting: boolean;
    register: UseFormRegister<UpdateUserProfilePayload>;
    handleSubmit: UseFormHandleSubmit<UpdateUserProfilePayload>;
    onSubmit: SubmitHandler<UpdateUserProfilePayload>;
    errors: FieldErrors<UpdateUserProfilePayload>;
    onClose: () => void;
};
