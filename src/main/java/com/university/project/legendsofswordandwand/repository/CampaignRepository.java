package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
  Optional<Campaign> findByOwnerAndActiveTrue(User owner);
}
