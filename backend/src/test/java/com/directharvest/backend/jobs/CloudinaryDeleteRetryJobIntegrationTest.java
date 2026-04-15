package com.directharvest.backend.jobs;

import com.directharvest.backend.listings.entity.CloudinaryDeleteOutbox;
import com.directharvest.backend.listings.entity.CloudinaryDeleteOutboxStatus;
import com.directharvest.backend.listings.event.ListingImagesCleanupRequestedEvent;
import com.directharvest.backend.listings.repository.CloudinaryDeleteOutboxRepository;
import com.directharvest.backend.listings.service.ListingImagesCleanupListener;
import com.directharvest.backend.shared.cloudinary.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = "cloudinary.delete-retry.enabled=true")
class CloudinaryDeleteRetryJobIntegrationTest {

    @Autowired
    private ListingImagesCleanupListener cleanupListener;

    @Autowired
    private CloudinaryDeleteRetryJob retryJob;

    @Autowired
    private CloudinaryDeleteOutboxRepository outboxRepository;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        reset(cloudinaryService);
    }

    @Test
    void listenerEnqueuesOutbox_whenCloudinaryDeleteFails() {
        doThrow(new RuntimeException("temporary outage")).when(cloudinaryService).delete("dh/listing/stale-1");

        cleanupListener.onCleanupRequested(new ListingImagesCleanupRequestedEvent(Set.of("dh/listing/stale-1")));

        assertThat(outboxRepository.count()).isEqualTo(1);
        CloudinaryDeleteOutbox saved = outboxRepository.findAll().getFirst();
        assertThat(saved.getPublicId()).isEqualTo("dh/listing/stale-1");
        assertThat(saved.getStatus()).isEqualTo(CloudinaryDeleteOutboxStatus.PENDING);
        assertThat(saved.getAttemptCount()).isEqualTo(0);
    }

    @Test
    void retryJobMarksSucceeded_whenDeleteEventuallyWorks() {
        CloudinaryDeleteOutbox item = new CloudinaryDeleteOutbox();
        item.setPublicId("dh/listing/retry-ok");
        item.setStatus(CloudinaryDeleteOutboxStatus.PENDING);
        item.setAttemptCount(0);
        item.setMaxAttempts(3);
        item.setNextAttemptAt(Instant.now().minusSeconds(1));
        outboxRepository.save(item);

        doNothing().when(cloudinaryService).delete("dh/listing/retry-ok");

        retryJob.processRetries();

        CloudinaryDeleteOutbox updated = outboxRepository.findById(item.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(CloudinaryDeleteOutboxStatus.SUCCEEDED);
        assertThat(updated.getAttemptCount()).isEqualTo(0);
        verify(cloudinaryService).delete("dh/listing/retry-ok");
    }

    @Test
    void retryJobMarksDead_whenMaxAttemptsReached() {
        CloudinaryDeleteOutbox item = new CloudinaryDeleteOutbox();
        item.setPublicId("dh/listing/retry-dead");
        item.setStatus(CloudinaryDeleteOutboxStatus.PENDING);
        item.setAttemptCount(0);
        item.setMaxAttempts(1);
        item.setNextAttemptAt(Instant.now().minusSeconds(1));
        outboxRepository.save(item);

        doThrow(new RuntimeException("cloudinary timeout")).when(cloudinaryService).delete("dh/listing/retry-dead");

        retryJob.processRetries();

        CloudinaryDeleteOutbox updated = outboxRepository.findById(item.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(CloudinaryDeleteOutboxStatus.DEAD);
        assertThat(updated.getAttemptCount()).isEqualTo(1);
        assertThat(updated.getLastError()).contains("timeout");
    }
}

