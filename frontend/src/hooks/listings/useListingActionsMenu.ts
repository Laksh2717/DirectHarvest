import { useCallback, useEffect, useRef, useState } from "react";
import type { ListingActionsMenuPosition } from "@/types/listing";  

export function useListingActionsMenu() {
    const [isOpen, setIsOpen] = useState(false);
    const [menuPosition, setMenuPosition] = useState<ListingActionsMenuPosition | null>(null);
    const buttonRef = useRef<HTMLButtonElement | null>(null);
    const menuRef = useRef<HTMLDivElement | null>(null);

    const closeMenu = useCallback(() => {
        setIsOpen(false);
        setMenuPosition(null);
    }, []);

    const toggleMenu = useCallback((event: React.MouseEvent<HTMLButtonElement>) => {
        event.stopPropagation();

        if (isOpen) {
            closeMenu();
            return;
        }

        const rect = event.currentTarget.getBoundingClientRect();
        const menuMaxHeight = 280;
        const gap = 8;
        const spaceBelow = window.innerHeight - rect.bottom - gap;
        const placeAbove = spaceBelow < 220;
        const top = placeAbove ? Math.max(gap, rect.top - menuMaxHeight - gap) : rect.bottom + gap;
        const maxHeight = placeAbove
            ? Math.min(menuMaxHeight, Math.max(160, rect.top - gap * 2))
            : Math.min(menuMaxHeight, Math.max(160, spaceBelow));

        setMenuPosition({
            top,
            right: Math.max(gap, window.innerWidth - rect.right),
            maxHeight,
        });
        setIsOpen(true);
    }, [closeMenu, isOpen]);

    useEffect(() => {
        const handleOutsideClick = (event: MouseEvent) => {
            if (!menuRef.current && !buttonRef.current) {
                return;
            }

            if (
                event.target instanceof Node &&
                !menuRef.current?.contains(event.target) &&
                !buttonRef.current?.contains(event.target)
            ) {
                closeMenu();
            }
        };

        document.addEventListener("mousedown", handleOutsideClick);
        return () => {
            document.removeEventListener("mousedown", handleOutsideClick);
        };
    }, [closeMenu]);

    useEffect(() => {
        window.addEventListener("resize", closeMenu);
        return () => {
            window.removeEventListener("resize", closeMenu);
        };
    }, [closeMenu]);

    return {
        isOpen,
        menuPosition,
        buttonRef,
        menuRef,
        closeMenu,
        toggleMenu,
    };
}