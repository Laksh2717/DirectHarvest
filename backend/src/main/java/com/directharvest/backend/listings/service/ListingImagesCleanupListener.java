package com.directharvest.backend.listings.service;

import com.directharvest.backend.listings.event.ListingImagesCleanupRequestedEvent;
import com.directharvest.backend.shared.cloudinary.service.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ListingImagesCleanupListener {

    private static final Logger log = LoggerFactory.getLogger(ListingImagesCleanupListener.class);

    private final CloudinaryService cloudinaryService;
    private final CloudinaryDeleteOutboxService outboxService;

    public ListingImagesCleanupListener(
            CloudinaryService cloudinaryService,
            CloudinaryDeleteOutboxService outboxService
    ) {
        this.cloudinaryService = cloudinaryService;
        this.outboxService = outboxService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCleanupRequested(ListingImagesCleanupRequestedEvent event) {
        for (String publicId : event.publicIds()) {
            try {
                cloudinaryService.delete(publicId);
            } catch (Exception ex) {
                log.warn("Failed to delete stale Cloudinary asset after commit: {}", publicId);
                outboxService.enqueueIfAbsent(publicId, ex.getMessage());
            }
        }
    }
}

