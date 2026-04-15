package com.directharvest.backend.jobs;

import com.directharvest.backend.listings.entity.CloudinaryDeleteOutbox;
import com.directharvest.backend.listings.service.CloudinaryDeleteOutboxService;
import com.directharvest.backend.shared.cloudinary.service.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CloudinaryDeleteRetryJob {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryDeleteRetryJob.class);

    private final CloudinaryDeleteOutboxService outboxService;
    private final CloudinaryService cloudinaryService;
    private final boolean enabled;
    private final int batchSize;

    public CloudinaryDeleteRetryJob(
            CloudinaryDeleteOutboxService outboxService,
            CloudinaryService cloudinaryService,
            @Value("${cloudinary.delete-retry.enabled:true}") boolean enabled,
            @Value("${cloudinary.delete-retry.batch-size:25}") int batchSize
    ) {
        this.outboxService = outboxService;
        this.cloudinaryService = cloudinaryService;
        this.enabled = enabled;
        this.batchSize = batchSize;
    }

    @Scheduled(
            fixedDelayString = "${cloudinary.delete-retry.fixed-delay-ms:30000}",
            initialDelayString = "${cloudinary.delete-retry.initial-delay-ms:30000}"
    )
    public void processRetries() {
        if (!enabled) {
            return;
        }

        List<CloudinaryDeleteOutbox> dueItems = outboxService.fetchDuePending(batchSize);
        for (CloudinaryDeleteOutbox item : dueItems) {
            processSingle(item);
        }
    }

    public void processSingle(CloudinaryDeleteOutbox item) {
        try {
            outboxService.markProcessing(item);
            cloudinaryService.delete(item.getPublicId());
            outboxService.markSucceeded(item);
        } catch (Exception ex) {
            outboxService.markFailed(item, ex.getMessage());
            log.warn("Cloudinary delete retry failed for public_id={}", item.getPublicId());
        }
    }
}

