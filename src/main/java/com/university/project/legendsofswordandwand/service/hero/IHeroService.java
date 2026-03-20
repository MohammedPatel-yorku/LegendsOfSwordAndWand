package com.university.project.legendsofswordandwand.service.hero;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import java.util.Optional;

public interface IHeroService {

  void createBaseHeroForParty(Long partyId, String heroName, HeroClass heroClass);

  Hero levelUp(Long heroId, HeroClass chosenClass);

  Hero addExperience(Long heroId, int amount);

  boolean isLevelUpPending(Long heroId);

  Optional<Hero> findById(Long heroId);

  Hero save(Hero hero);

  void delete(Long heroId);
}
