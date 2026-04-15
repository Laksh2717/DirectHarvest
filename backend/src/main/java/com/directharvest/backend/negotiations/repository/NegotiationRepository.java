package com.directharvest.backend.negotiations.repository;

import com.directharvest.backend.common.enums.NegotiationStatus;
import com.directharvest.backend.negotiations.entity.Negotiation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {

    @EntityGraph(attributePaths = {"listing", "buyer", "farmer"})
    Optional<Negotiation> findDetailsById(Long id);

    @EntityGraph(attributePaths = {"listing", "buyer", "farmer"})
    List<Negotiation> findAllByBuyerIdOrFarmerIdOrderByUpdatedAtDesc(Long buyerId, Long farmerId);

        @EntityGraph(attributePaths = {"listing", "buyer", "farmer"})
            @Query("""
                SELECT n
                FROM Negotiation n
                WHERE (n.buyer.id = :userId OR n.farmer.id = :userId)
                  AND n.status IN :statuses
                ORDER BY n.updatedAt DESC
                """)
            List<Negotiation> findAllByParticipantIdAndStatusInOrderByUpdatedAtDesc(
                @Param("userId") Long userId,
                @Param("statuses") Collection<NegotiationStatus> statuses
            );

    @EntityGraph(attributePaths = {"listing", "buyer", "farmer", "events", "events.actor"})
    List<Negotiation> findAllByListingIdAndBuyerIdAndFarmerIdOrderByCreatedAtAsc(Long listingId, Long buyerId, Long farmerId);

    boolean existsByListingIdAndBuyerIdAndStatusIn(Long listingId, Long buyerId, Collection<NegotiationStatus> statuses);

    boolean existsByListingIdAndStatusIn(Long listingId, Collection<NegotiationStatus> statuses);

        @Query("""
                        SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END
                        FROM Negotiation n
                        WHERE n.status IN :statuses
                            AND (n.buyer.id = :userId OR n.farmer.id = :userId)
                        """)
        boolean existsActiveByParticipantId(@Param("userId") Long userId, @Param("statuses") Collection<NegotiationStatus> statuses);

    List<Negotiation> findByStatusInAndExpiresAtLessThanEqualOrderByExpiresAtAsc(Collection<NegotiationStatus> statuses, Instant now);

    @Query("""
        SELECT COUNT(n) FROM Negotiation n
        WHERE (n.buyer.id = :userId OR n.farmer.id = :userId)
          AND n.status IN :statuses
        """)
    Long countByParticipantIdAndStatusIn(Long userId, Collection<NegotiationStatus> statuses);
}

