package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * InventoryRepository handles database access for Inventory objects.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find all inventories belonging to a specific party.
     *
     * @param partyId ID of the party
     * @return List of Inventory objects
     */
    List<Inventory> findByPartyId(Long partyId);
}
