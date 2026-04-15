package com.directharvest.backend.jobs;

import com.directharvest.backend.common.enums.OrderStatus;
import com.directharvest.backend.orders.entity.Order;
import com.directharvest.backend.orders.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class OrderActivationJob {

    private static final Logger log = LoggerFactory.getLogger(OrderActivationJob.class);

    private final OrderRepository orderRepository;
    private final boolean enabled;
    private final int batchSize;
    private final Duration activationWindow = Duration.ofHours(24);

    public OrderActivationJob(
            OrderRepository orderRepository,
            @Value("${orders.activation.enabled:true}") boolean enabled,
            @Value("${orders.activation.batch-size:50}") int batchSize
    ) {
        this.orderRepository = orderRepository;
        this.enabled = enabled;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${orders.activation.fixed-delay-ms:3600000}", initialDelayString = "${orders.activation.initial-delay-ms:3600000}")
    @Transactional
    public void activateOrders() {
        if (!enabled) {
            return;
        }

        Instant cutoff = Instant.now().minus(activationWindow);
        List<Order> dueItems = orderRepository
                .findByStatusAndCreatedAtLessThanEqualOrderByCreatedAtAsc(OrderStatus.CONFIRMED, cutoff)
                .stream()
                .limit(batchSize)
                .toList();

        for (Order order : dueItems) {
            if (order.getStatus() != OrderStatus.CONFIRMED) {
                continue;
            }
            order.setStatus(OrderStatus.ACTIVE);
            order.setActivatedAt(Instant.now());
            orderRepository.save(order);
            log.info("Activated order id={}", order.getId());
        }
    }
}

