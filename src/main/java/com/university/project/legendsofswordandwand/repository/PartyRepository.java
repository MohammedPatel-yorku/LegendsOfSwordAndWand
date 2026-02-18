package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository Interface for Party entities. Provides database access methods for Party persistence
 * and retrieval.
 */
public interface PartyRepository extends JpaRepository<Party, Long> {

    @Query("SELECT p FROM Party p WHERE p.campaign.id = :campaignId AND p.active = true")
    Optional<Party> findActivePartyByCampaignId(@Param("campaignId") Long campaignId);
}
