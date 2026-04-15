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
public class OrderAutoCompleteJob {

    private static final Logger log = LoggerFactory.getLogger(OrderAutoCompleteJob.class);

    private final OrderRepository orderRepository;
    private final boolean enabled;
    private final int batchSize;
    private final Duration autoCompleteWindow = Duration.ofDays(30);

    public OrderAutoCompleteJob(
            OrderRepository orderRepository,
            @Value("${orders.auto-complete.enabled:true}") boolean enabled,
            @Value("${orders.auto-complete.batch-size:50}") int batchSize
    ) {
        this.orderRepository = orderRepository;
        this.enabled = enabled;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${orders.auto-complete.fixed-delay-ms:86400000}", initialDelayString = "${orders.auto-complete.initial-delay-ms:86400000}")
    @Transactional
    public void autoCompleteOrders() {
        if (!enabled) {
            return;
        }

        Instant cutoff = Instant.now().minus(autoCompleteWindow);
        List<Order> dueItems = orderRepository
                .findByStatusInAndCreatedAtLessThanEqualOrderByCreatedAtAsc(List.of(OrderStatus.CONFIRMED, OrderStatus.ACTIVE), cutoff)
                .stream()
                .limit(batchSize)
                .toList();

        for (Order order : dueItems) {
            if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.ACTIVE) {
                continue;
            }
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(Instant.now());
            orderRepository.save(order);
            log.info("Auto-completed order id={}", order.getId());
        }
    }
}

