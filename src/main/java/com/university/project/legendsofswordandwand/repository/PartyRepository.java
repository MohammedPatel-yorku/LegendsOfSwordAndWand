package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Interface for Party entities. Provides database access methods for Party persistence
 * and retrieval.
 */
public interface PartyRepository extends JpaRepository<Party, Long> {}
