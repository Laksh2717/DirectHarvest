package com.directharvest.backend.listings.repository;

import com.directharvest.backend.common.enums.ListingStatus;
import com.directharvest.backend.listings.entity.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    @EntityGraph(attributePaths = "images")
    Optional<Listing> findWithImagesById(Long id);

    @EntityGraph(attributePaths = "images")
    Optional<Listing> findWithImagesByIdAndFarmerId(Long id, Long farmerId);

    @EntityGraph(attributePaths = "images")
    List<Listing> findAllByFarmerIdOrderByCreatedAtDesc(Long farmerId);

    @EntityGraph(attributePaths = "images")
    List<Listing> findAllByFarmerIdAndStatusOrderByCreatedAtDesc(Long farmerId, ListingStatus status);

    @Query("select l from Listing l where l.farmer.id = :farmerId and l.status = :status and l.quantity > 0 order by l.quantity asc")
    Optional<Listing> findFirstByFarmerIdAndStatusOrderByQuantityAsc(Long farmerId, ListingStatus status);

    @EntityGraph(attributePaths = "images")
    List<Listing> findAllByFarmerIdAndStatusInOrderByCreatedAtDesc(Long farmerId, List<ListingStatus> statuses);

    @EntityGraph(attributePaths = "images")
    @Query("select l from Listing l where l.quantity > 0 order by l.createdAt desc limit 10")
    List<Listing> findTop10ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "images")
    @Query("""
        select l from Listing l 
        where l.quantity > 0 and (
            lower(l.cropName) like lower(concat('%', :cropName, '%')) or
            lower(l.description) like lower(concat('%', :description, '%')) or
            lower(l.street) like lower(concat('%', :street, '%')) or
            lower(l.city) like lower(concat('%', :city, '%')) or
            lower(l.state) like lower(concat('%', :state, '%'))
        )
        order by l.createdAt desc
        """)
    List<Listing> findByCropNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrStreetContainingIgnoreCaseOrCityContainingIgnoreCaseOrStateContainingIgnoreCaseOrderByCreatedAtDesc(
            String cropName,
            String description,
            String street,
            String city,
            String state
    );

    // @EntityGraph(attributePaths = {"images", "farmer"})
    // @Query("select l from Listing l where l.quantity > 0")
    // Page<Listing> findAllForBrowse(Pageable pageable);

    // @EntityGraph(attributePaths = {"images", "farmer"})
    // @Query("select l from Listing l where l.quantity > 0 and lower(l.cropName) like lower(concat('%', :cropName, '%'))")
    // Page<Listing> findByCropNameContainingIgnoreCase(String cropName, Pageable pageable);

    // 1. Update the derived query for crop search
    @EntityGraph(attributePaths = {"images", "farmer"})
    Page<Listing> findByStatusAndCropNameContainingIgnoreCase(ListingStatus status, String cropName, Pageable pageable);

    // 2. Update the custom query for 'findAll'
    @EntityGraph(attributePaths = {"images", "farmer"})
    @Query("select l from Listing l where l.status = com.directharvest.backend.common.enums.ListingStatus.ACTIVE and l.quantity > 0")
    Page<Listing> findAllForBrowse(Pageable pageable);

    // 3. Update the search query (if you use this for the browse feature)
    @EntityGraph(attributePaths = {"images", "farmer"})
    @Query("select l from Listing l where l.status = com.directharvest.backend.common.enums.ListingStatus.ACTIVE and l.quantity > 0 and lower(l.cropName) like lower(concat('%', :cropName, '%'))")
    Page<Listing> findByCropNameContainingIgnoreCase(String cropName, Pageable pageable);

    @Query("""
        SELECT COUNT(l) FROM Listing l
        WHERE l.farmer.id = :farmerId AND l.status = :status AND l.quantity > 0
        """)
    Long countByFarmerIdAndStatus(Long farmerId, ListingStatus status);
}

