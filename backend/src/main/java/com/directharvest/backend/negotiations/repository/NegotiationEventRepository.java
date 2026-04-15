package com.directharvest.backend.negotiations.repository;

import com.directharvest.backend.negotiations.entity.NegotiationEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NegotiationEventRepository extends JpaRepository<NegotiationEvent, Long> {

    @EntityGraph(attributePaths = {"actor"})
    List<NegotiationEvent> findAllByNegotiationIdOrderByCreatedAtAsc(Long negotiationId);
}

