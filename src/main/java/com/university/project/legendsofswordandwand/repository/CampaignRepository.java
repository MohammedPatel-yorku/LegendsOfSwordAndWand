package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Optional<Campaign> findByOwnerAndActiveTrue(User owner);
}
