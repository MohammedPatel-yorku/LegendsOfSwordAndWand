package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.HeroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HeroService {

  private final HeroRepository heroRepository;

  public Hero createHeroForUser(User owner, String name, String heroClass) {
    Hero hero = new Hero(name, heroClass, 1, 100, 10, owner);

    return heroRepository.save(hero);
  }
}
