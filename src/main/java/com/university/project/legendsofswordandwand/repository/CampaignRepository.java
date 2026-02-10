package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Interface for Campaign entities. Provides database access methods for Campaign
 * persistence and retrieval.
 */
public interface CampaignRepository extends JpaRepository<Campaign, Long> {}
