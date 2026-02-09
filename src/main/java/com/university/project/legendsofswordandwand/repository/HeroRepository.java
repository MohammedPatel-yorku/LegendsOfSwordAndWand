package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.Hero;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeroRepository extends JpaRepository<Hero, Long> {}
