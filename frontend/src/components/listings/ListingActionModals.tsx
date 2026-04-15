import ListingAddQuantityModal from "@/components/modals/ListingAddQuantityModal";
import ListingUpdatePriceModal from "@/components/modals/ListingUpdatePriceModal";
import ListingUpdateDetailsModal from "@/components/modals/ListingUpdateDetailsModal";
import ListingUpdateImagesModal from "@/components/modals/ListingUpdateImagesModal";
import ListingConfirmationModal from "@/components/modals/ListingConfirmationModal";
import { AlertTriangle } from "lucide-react";
import { toTitleCase } from "@/lib/formatters";
import type { ListingActionModalsProps } from "@/types/listing";

export default function ListingActionModals({ listing, state }: ListingActionModalsProps) {
    const { addQtyForm, updatePriceForm, updateDetailsForm, imageState, modals, actions } = state;

    return (
        <>
            <ListingAddQuantityModal
                listing={listing}
                addQtyForm={addQtyForm}
                actionLoading={actions.actionLoading}
                onCancel={modals.closeAllActionUi}
            />

            <ListingUpdatePriceModal
                listing={listing}
                updatePriceForm={updatePriceForm}
                actionLoading={actions.actionLoading}
                onCancel={modals.closeAllActionUi}
            />

            <ListingUpdateDetailsModal
                listing={listing}
                updateDetailsForm={updateDetailsForm}
                actionLoading={actions.actionLoading}
                onCancel={modals.closeAllActionUi}
            />

            <ListingUpdateImagesModal
                listing={listing}
                imageState={imageState}
                actionLoading={actions.actionLoading}
                onCancel={modals.closeAllActionUi}
            />

            <ListingConfirmationModal
                open={modals.isInactiveConfirmOpen}
                title={`Make Inactive - ${toTitleCase(listing.cropName)}`}
                description="This will mark the listing as inactive."
                confirmText="Make Inactive"
                loading={modals.confirmLoading === "inactive"}
                icon={<AlertTriangle className="h-5 w-5" />}
                onCancel={modals.closeAllActionUi}
                onConfirm={actions.handleConfirmInactive}
            />

            <ListingConfirmationModal
                open={modals.isDeleteConfirmOpen}
                title={`Delete Listing - ${toTitleCase(listing.cropName)}`}
                description="This will permanently delete the listing and cannot be undone."
                confirmText="Delete Listing"
                loading={modals.confirmLoading === "delete"}
                icon={<AlertTriangle className="h-5 w-5" />}
                onCancel={modals.closeAllActionUi}
                onConfirm={actions.handleConfirmDelete}
            />
        </>
    );
}