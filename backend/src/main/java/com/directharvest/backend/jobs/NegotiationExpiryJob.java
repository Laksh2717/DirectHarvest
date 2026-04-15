package com.directharvest.backend.jobs;

import com.directharvest.backend.common.enums.NegotiationStatus;
import com.directharvest.backend.negotiations.entity.Negotiation;
import com.directharvest.backend.negotiations.entity.NegotiationEvent;
import com.directharvest.backend.negotiations.repository.NegotiationEventRepository;
import com.directharvest.backend.negotiations.repository.NegotiationRepository;
import com.directharvest.backend.common.enums.NegotiationEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

@Component
public class NegotiationExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(NegotiationExpiryJob.class);
    private static final EnumSet<NegotiationStatus> OPEN_STATUSES =
            EnumSet.of(NegotiationStatus.PENDING_BUYER, NegotiationStatus.PENDING_FARMER);

    private final NegotiationRepository negotiationRepository;
    private final NegotiationEventRepository negotiationEventRepository;
    private final boolean enabled;
    private final int batchSize;

    public NegotiationExpiryJob(
            NegotiationRepository negotiationRepository,
            NegotiationEventRepository negotiationEventRepository,
            @Value("${negotiation.expiry.enabled:true}") boolean enabled,
            @Value("${negotiation.expiry.batch-size:50}") int batchSize
    ) {
        this.negotiationRepository = negotiationRepository;
        this.negotiationEventRepository = negotiationEventRepository;
        this.enabled = enabled;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${negotiation.expiry.fixed-delay-ms:3600000}", initialDelayString = "${negotiation.expiry.initial-delay-ms:3600000}")
    @Transactional
    public void expireNegotiations() {
        if (!enabled) {
            return;
        }

        Instant now = Instant.now();
        List<Negotiation> dueItems = negotiationRepository
                .findByStatusInAndExpiresAtLessThanEqualOrderByExpiresAtAsc(OPEN_STATUSES, now)
                .stream()
                .limit(batchSize)
                .toList();

        for (Negotiation negotiation : dueItems) {
            negotiation.setStatus(NegotiationStatus.EXPIRED);
            negotiationRepository.save(negotiation);
            recordExpiredEvent(negotiation);
            log.info("Expired negotiation id={}", negotiation.getId());
        }
    }

    private void recordExpiredEvent(Negotiation negotiation) {
        NegotiationEvent event = new NegotiationEvent();
        event.setNegotiation(negotiation);
        event.setEventType(NegotiationEventType.EXPIRED);
        event.setOfferedPrice(negotiation.getOfferedPrice());
        event.setRequestedQuantity(negotiation.getRequestedQuantity());
        event.setStatusAfter(NegotiationStatus.EXPIRED);
        negotiationEventRepository.save(event);
    }
}

