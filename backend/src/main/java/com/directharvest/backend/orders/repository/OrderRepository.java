package com.directharvest.backend.orders.repository;

import com.directharvest.backend.orders.entity.Order;
import com.directharvest.backend.common.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"listing", "negotiation", "buyer", "farmer"})
    List<Order> findAllByBuyerIdOrFarmerIdOrderByUpdatedAtDesc(Long buyerId, Long farmerId);

    @EntityGraph(attributePaths = {"listing", "negotiation", "buyer", "farmer"})
    @Query("""
        SELECT o
        FROM Order o
        WHERE (o.buyer.id = :buyerId OR o.farmer.id = :farmerId)
          AND o.status IN :statuses
        ORDER BY o.updatedAt DESC
        """)
    List<Order> findAllByBuyerIdOrFarmerIdAndStatusInOrderByUpdatedAtDesc(
        @Param("buyerId") Long buyerId,
        @Param("farmerId") Long farmerId,
        @Param("statuses") List<OrderStatus> statuses
    );

    @EntityGraph(attributePaths = {"listing", "negotiation", "negotiation.events", "buyer", "farmer"})
    Optional<Order> findDetailsById(Long id);

    boolean existsByNegotiationId(Long negotiationId);

    List<Order> findByStatusAndCreatedAtLessThanEqualOrderByCreatedAtAsc(OrderStatus status, Instant createdAt);

    List<Order> findByStatusInAndCreatedAtLessThanEqualOrderByCreatedAtAsc(List<OrderStatus> statuses, Instant createdAt);

    List<Order> findByStatusAndActivatedAtLessThanEqualOrderByActivatedAtAsc(OrderStatus status, Instant activatedAt);

    @Query("""
        SELECT COUNT(o) FROM Order o
        WHERE o.farmer.id = :farmerId AND o.status = :status
        """)
    Long countByFarmerIdAndStatus(@Param("farmerId") Long farmerId, @Param("status") OrderStatus status);

    @Query("""
        SELECT COUNT(o) FROM Order o
        WHERE o.buyer.id = :buyerId AND o.status = :status
        """)
    Long countByBuyerIdAndStatus(@Param("buyerId") Long buyerId, @Param("status") OrderStatus status);

        @Query("""
            SELECT YEAR(COALESCE(o.completedAt, o.createdAt)), COALESCE(SUM(o.agreedPrice * o.agreedQuantity), 0)
            FROM Order o
            WHERE o.farmer.id = :farmerId
                AND o.status = com.directharvest.backend.common.enums.OrderStatus.COMPLETED
            GROUP BY YEAR(COALESCE(o.completedAt, o.createdAt))
            ORDER BY YEAR(COALESCE(o.completedAt, o.createdAt))
            """)
        List<Object[]> sumCompletedAmountByFarmerGroupedByYear(@Param("farmerId") Long farmerId);

        @Query("""
            SELECT MONTH(COALESCE(o.completedAt, o.createdAt)), COALESCE(SUM(o.agreedPrice * o.agreedQuantity), 0)
            FROM Order o
            WHERE o.farmer.id = :farmerId
                AND o.status = com.directharvest.backend.common.enums.OrderStatus.COMPLETED
                AND YEAR(COALESCE(o.completedAt, o.createdAt)) = :year
            GROUP BY MONTH(COALESCE(o.completedAt, o.createdAt))
            ORDER BY MONTH(COALESCE(o.completedAt, o.createdAt))
            """)
        List<Object[]> sumCompletedAmountByFarmerGroupedByMonth(@Param("farmerId") Long farmerId, @Param("year") Integer year);

        @Query("""
            SELECT YEAR(COALESCE(o.completedAt, o.createdAt)), COALESCE(SUM(o.agreedPrice * o.agreedQuantity), 0)
            FROM Order o
            WHERE o.buyer.id = :buyerId
                AND o.status = com.directharvest.backend.common.enums.OrderStatus.COMPLETED
            GROUP BY YEAR(COALESCE(o.completedAt, o.createdAt))
            ORDER BY YEAR(COALESCE(o.completedAt, o.createdAt))
            """)
        List<Object[]> sumCompletedAmountByBuyerGroupedByYear(@Param("buyerId") Long buyerId);

        @Query("""
            SELECT MONTH(COALESCE(o.completedAt, o.createdAt)), COALESCE(SUM(o.agreedPrice * o.agreedQuantity), 0)
            FROM Order o
            WHERE o.buyer.id = :buyerId
                AND o.status = com.directharvest.backend.common.enums.OrderStatus.COMPLETED
                AND YEAR(COALESCE(o.completedAt, o.createdAt)) = :year
            GROUP BY MONTH(COALESCE(o.completedAt, o.createdAt))
            ORDER BY MONTH(COALESCE(o.completedAt, o.createdAt))
            """)
        List<Object[]> sumCompletedAmountByBuyerGroupedByMonth(@Param("buyerId") Long buyerId, @Param("year") Integer year);
}

