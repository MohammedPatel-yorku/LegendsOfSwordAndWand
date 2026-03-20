package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.Item;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

  Optional<Item> findByName(String name);
}
