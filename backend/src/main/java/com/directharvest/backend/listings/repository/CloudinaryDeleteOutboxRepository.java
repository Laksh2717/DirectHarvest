package com.directharvest.backend.listings.repository;

import com.directharvest.backend.listings.entity.CloudinaryDeleteOutbox;
import com.directharvest.backend.listings.entity.CloudinaryDeleteOutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface CloudinaryDeleteOutboxRepository extends JpaRepository<CloudinaryDeleteOutbox, Long> {

    boolean existsByPublicIdAndStatusIn(String publicId, Collection<CloudinaryDeleteOutboxStatus> statuses);

    List<CloudinaryDeleteOutbox> findByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
            CloudinaryDeleteOutboxStatus status,
            Instant nextAttemptAt,
            Pageable pageable
    );
}

