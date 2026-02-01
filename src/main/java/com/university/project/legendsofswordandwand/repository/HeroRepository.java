package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeroRepository extends JpaRepository<Hero, Long> {
  List<Hero> findByOwner(User owner);
}
