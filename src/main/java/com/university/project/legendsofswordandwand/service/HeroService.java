package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.HeroRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class HeroService {

  private final HeroRepository heroRepository;
  private final UserRepository userRepository;

  public Hero createHeroForUser(Long userId, String name, String heroClass) {
    User owner = userRepository.findById(userId).orElseThrow();

    Hero hero = new Hero(name, heroClass, 1, 100, 10, owner);
    owner.getHeroes().add(hero);

    return heroRepository.save(hero);
  }
}
