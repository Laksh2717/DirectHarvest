import { useEffect } from "react";
import { useForm } from "react-hook-form";
import type { ActionFormValues, UseNegotiationActionFormsParams } from "@/types/offer";

export function useNegotiationActionForms({ open, rejectOpen, counterOpen, negotiation, availableQuantity }: UseNegotiationActionFormsParams) {
    const {
        register: registerReject,
        handleSubmit: handleRejectSubmit,
        reset: resetRejectForm,
        formState: { isSubmitting: isRejectSubmitting },
    } = useForm<Pick<ActionFormValues, "rejectReason">>({
        defaultValues: { rejectReason: "" },
    });

    const {
        register: registerCounter,
        handleSubmit: handleCounterFormSubmit,
        reset: resetCounterForm,
        formState: { errors: counterErrors, isSubmitting: isCounterSubmitting },
    } = useForm<Pick<ActionFormValues, "counterPrice" | "counterQty">>({
        defaultValues: { counterPrice: "", counterQty: "" },
    });

    useEffect(() => {
        if (!open) {
            resetRejectForm({ rejectReason: "" });
            resetCounterForm({ counterPrice: "", counterQty: "" });
        }
    }, [open, resetCounterForm, resetRejectForm]);

    useEffect(() => {
        if (rejectOpen) {
            resetRejectForm({ rejectReason: "" });
        }
    }, [rejectOpen, resetRejectForm]);

    useEffect(() => {
        if (counterOpen && negotiation) {
            resetCounterForm({
                counterPrice: String(negotiation.offeredPrice),
                counterQty: String(negotiation.requestedQuantity),
            });
        }
    }, [counterOpen, negotiation, resetCounterForm, availableQuantity]);

    return {
        registerReject,
        handleRejectSubmit,
        isRejectSubmitting,
        registerCounter,
        handleCounterFormSubmit,
        counterErrors,
        isCounterSubmitting,
    };
}