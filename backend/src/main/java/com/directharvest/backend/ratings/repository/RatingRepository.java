package com.directharvest.backend.ratings.repository;

import com.directharvest.backend.ratings.entity.Rating;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    @EntityGraph(attributePaths = {"order", "listing", "rater", "ratedUser"})
    List<Rating> findAllByRatedUserIdOrderByCreatedAtDesc(Long ratedUserId);

    @EntityGraph(attributePaths = {"order", "listing", "rater", "ratedUser"})
    List<Rating> findAllByOrderIdIn(List<Long> orderIds);

    @EntityGraph(attributePaths = {"order", "listing", "rater", "ratedUser"})
    Optional<Rating> findByOrderId(Long orderId);

    boolean existsByOrderIdAndRaterId(Long orderId, Long raterId);

    @Query("""
        SELECT AVG(CAST(r.score AS double)) FROM Rating r
        WHERE r.ratedUser.id = :ratedUserId
        """)
    Optional<Double> findAverageScoreByRatedUserId(@Param("ratedUserId") Long ratedUserId);

    @Query("""
        SELECT COUNT(r) FROM Rating r
        WHERE r.ratedUser.id = :ratedUserId
        """)
    Long countByRatedUserId(@Param("ratedUserId") Long ratedUserId);
}

