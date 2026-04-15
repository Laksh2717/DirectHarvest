"use client";

import { createPortal } from "react-dom";
import { Button } from "@/components/ui/button";
import type { ListingActionsMenuProps } from "@/types/listing";

export default function ListingActionsMenu({
    status,
    isOpen,
    menuPosition,
    menuRef,
    onAddQty,
    onUpdatePrice,
    onUpdateDetails,
    onUpdateImages,
    onMakeInactive,
    onDelete,
}: ListingActionsMenuProps) {
    if (!isOpen || !menuPosition || typeof document === "undefined") {
        return null;
    }

    return createPortal(
        <div
            ref={menuRef}
            className="fixed z-999 w-48 rounded-xl border border-border bg-card p-2 shadow-2xl"
            style={{
                top: menuPosition.top,
                right: menuPosition.right,
            }}
        >
            <div className="max-h-72 overflow-y-auto pr-1" style={{ maxHeight: menuPosition.maxHeight }}>
                <div className="flex flex-col gap-1">
                    {status === "OUT_OF_STOCK" ? (
                        <>
                            <Button type="button" variant="ghost" className="justify-start text-xs hover:bg-muted" onClick={onAddQty}>
                                Add Quantity
                            </Button>
                            <Button type="button" variant="ghost" className="justify-start text-xs hover:bg-muted" onClick={onMakeInactive}>
                                Make Inactive
                            </Button>
                        </>
                    ) : status === "ACTIVE" ? (
                        <>
                            <Button type="button" variant="ghost" className="justify-start text-xs hover:bg-muted" onClick={onAddQty}>
                                Add Quantity
                            </Button>
                            <Button type="button" variant="ghost" className="justify-start text-xs hover:bg-muted" onClick={onUpdatePrice}>
                                Update Price
                            </Button>
                            <Button type="button" variant="ghost" className="justify-start text-xs hover:bg-muted" onClick={onUpdateDetails}>
                                Update Details
                            </Button>
                            <Button type="button" variant="ghost" className="justify-start text-xs hover:bg-muted" onClick={onUpdateImages}>
                                Update Images
                            </Button>
                            <Button type="button" variant="ghost" className="justify-start text-xs hover:bg-muted" onClick={onMakeInactive}>
                                Make Inactive
                            </Button>
                            <Button type="button" variant="ghost" className="justify-start text-xs hover:bg-muted text-destructive" onClick={onDelete}>
                                Delete Listing
                            </Button>
                        </>
                    ) : null}
                </div>
            </div>
        </div>,
        document.body,
    );
}