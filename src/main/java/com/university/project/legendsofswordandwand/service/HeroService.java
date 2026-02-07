package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.HeroRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Hero Object Service class. */
@Service
@Transactional
@RequiredArgsConstructor
public class HeroService {

  private final HeroRepository heroRepository;
  private final UserRepository userRepository;

  /**
   * Creates a new base stat (Level 1, 100 HP, 10 Attack) Hero for User.
   *
   * @param userId ID of User to create Hero Object for
   * @param selectedHeroName Name to assign to Hero
   * @param selectedHeroClass Hero Class to assign to Hero
   * @return Newly created and saved Hero Object
   */
  public Hero createBaseHeroForUser(
      Long userId, String selectedHeroName, HeroClass selectedHeroClass) {
    User owner = userRepository.findById(userId).orElseThrow();

    Hero hero = new Hero();
    hero.setName(selectedHeroName);
    hero.setHeroClass(selectedHeroClass);
    hero.setLevel(1);
    hero.setHealth(100);
    hero.setAttack(10);
    hero.setOwner(owner);

    owner.getHeroes().add(hero);

    return heroRepository.save(hero);
  }
}
