package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Party;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository Interface for Campaign entities. Provides database access methods for Campaign
 * persistence and retrieval.
 */
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

  @Query("SELECT c.party FROM Campaign c WHERE c.id = :campaignId AND c.active = true")
  Optional<Party> findActivePartyByCampaignId(@Param("campaignId") Long campaignId);

  @Query("SELECT COUNT(c) > 0 FROM Campaign c WHERE c.owner.id = :userId AND c.active = true")
  boolean existsActiveCampaignByOwnerId(@Param("userId") Long userId);

  @Query("SELECT c FROM Campaign c WHERE c.owner.id = :userId ORDER BY c.score DESC")
  List<Campaign> findAllByOwnerIdOrderByScoreDesc(@Param("userId") Long userId);
}
