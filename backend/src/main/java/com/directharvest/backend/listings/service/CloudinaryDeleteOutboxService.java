package com.directharvest.backend.listings.service;

import com.directharvest.backend.listings.entity.CloudinaryDeleteOutbox;
import com.directharvest.backend.listings.entity.CloudinaryDeleteOutboxStatus;
import com.directharvest.backend.listings.repository.CloudinaryDeleteOutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class CloudinaryDeleteOutboxService {

    private final CloudinaryDeleteOutboxRepository outboxRepository;
    private final int defaultMaxAttempts;
    private final long initialBackoffMs;
    private final long maxBackoffMs;

    public CloudinaryDeleteOutboxService(
            CloudinaryDeleteOutboxRepository outboxRepository,
            @Value("${cloudinary.delete-retry.max-attempts:5}") int defaultMaxAttempts,
            @Value("${cloudinary.delete-retry.initial-backoff-ms:30000}") long initialBackoffMs,
            @Value("${cloudinary.delete-retry.max-backoff-ms:900000}") long maxBackoffMs
    ) {
        this.outboxRepository = outboxRepository;
        this.defaultMaxAttempts = defaultMaxAttempts;
        this.initialBackoffMs = initialBackoffMs;
        this.maxBackoffMs = maxBackoffMs;
    }

    @Transactional
    public void enqueueIfAbsent(String publicId, String errorMessage) {
        Set<CloudinaryDeleteOutboxStatus> activeStatuses = Set.of(
                CloudinaryDeleteOutboxStatus.PENDING,
                CloudinaryDeleteOutboxStatus.PROCESSING
        );

        if (outboxRepository.existsByPublicIdAndStatusIn(publicId, activeStatuses)) {
            return;
        }

        CloudinaryDeleteOutbox outbox = new CloudinaryDeleteOutbox();
        outbox.setPublicId(publicId);
        outbox.setStatus(CloudinaryDeleteOutboxStatus.PENDING);
        outbox.setAttemptCount(0);
        outbox.setMaxAttempts(defaultMaxAttempts);
        outbox.setNextAttemptAt(Instant.now());
        outbox.setLastError(truncate(errorMessage));
        outboxRepository.save(outbox);
    }

    @Transactional(readOnly = true)
    public List<CloudinaryDeleteOutbox> fetchDuePending(int batchSize) {
        return outboxRepository.findByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
                CloudinaryDeleteOutboxStatus.PENDING,
                Instant.now(),
                PageRequest.of(0, batchSize)
        );
    }

    @Transactional
    public void markProcessing(CloudinaryDeleteOutbox outbox) {
        outbox.setStatus(CloudinaryDeleteOutboxStatus.PROCESSING);
        outboxRepository.save(outbox);
    }

    @Transactional
    public void markSucceeded(CloudinaryDeleteOutbox outbox) {
        outbox.setStatus(CloudinaryDeleteOutboxStatus.SUCCEEDED);
        outbox.setLastError(null);
        outbox.setNextAttemptAt(Instant.now());
        outboxRepository.save(outbox);
    }

    @Transactional
    public void markFailed(CloudinaryDeleteOutbox outbox, String errorMessage) {
        int attempts = outbox.getAttemptCount() + 1;
        outbox.setAttemptCount(attempts);
        outbox.setLastError(truncate(errorMessage));

        if (attempts >= outbox.getMaxAttempts()) {
            outbox.setStatus(CloudinaryDeleteOutboxStatus.DEAD);
            outbox.setNextAttemptAt(Instant.now());
        } else {
            outbox.setStatus(CloudinaryDeleteOutboxStatus.PENDING);
            outbox.setNextAttemptAt(Instant.now().plusMillis(resolveBackoffMs(attempts)));
        }

        outboxRepository.save(outbox);
    }

    private long resolveBackoffMs(int attempt) {
        long factor = 1L << Math.max(0, attempt - 1);
        long delay = initialBackoffMs * factor;
        return Math.min(delay, maxBackoffMs);
    }

    private String truncate(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        return errorMessage.length() <= 1000 ? errorMessage : errorMessage.substring(0, 1000);
    }
}

