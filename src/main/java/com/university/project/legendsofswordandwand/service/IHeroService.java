package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;

public interface IHeroService {

  void createBaseHeroForParty(Long partyId, String heroName, HeroClass heroClass);

  Hero levelUp(Long heroId, HeroClass chosenClass);

  Hero addExperience(Long heroId, int amount);

  boolean isLevelUpPending(Long heroId);
}
