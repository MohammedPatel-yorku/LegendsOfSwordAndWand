package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.battle.HeroStatCalculator;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.HeroRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.service.IHeroService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Hero Object Service class. */
@Service
@Transactional
@RequiredArgsConstructor
public class HeroServiceImpl implements IHeroService {

  private final HeroRepository heroRepository;
  private final PartyRepository partyRepository;
  private final HeroStatCalculator heroStatCalculator;

  /**
   * Creates a new base stat (Level 1, 100 HP, 10 Attack) Hero for requesting Party.
   *
   * @param partyId ID of Party to create Hero Object for
   * @param selectedHeroName Name to assign to Hero
   * @param selectedHeroClass Hero Class to assign to Hero
   */
  @Override
  public void createBaseHeroForParty(
      Long partyId, String selectedHeroName, HeroClass selectedHeroClass) {
    Party party =
        partyRepository
            .findById(partyId)
            .orElseThrow(() -> new RuntimeException("Party Not Found"));

    Hero hero =
        Hero.builder().name(selectedHeroName).startingClass(selectedHeroClass).party(party).build();

    incrementClassLevel(hero, selectedHeroClass);

    party.getHeroes().add(hero);
    heroRepository.save(hero);
  }

  @Override
  public Hero levelUp(Long heroId, HeroClass chosenClass) {

    Hero hero =
        heroRepository.findById(heroId).orElseThrow(() -> new RuntimeException("Hero Not Found"));

    if (hero.getLevel() >= 20) throw new RuntimeException("Hero is already at max level");

    if (!isLevelUpPending(heroId))
      throw new RuntimeException("Hero does not have enough XP to level up");

    HeroClass effectiveChoice = hero.isHybrid() ? hero.getPrimaryClass() : chosenClass;

    incrementClassLevel(hero, effectiveChoice);

    if (!hero.isHybrid()
        && hero.getPrimaryClass() == null
        && getClassLevel(hero, chosenClass) == 5) {
      hero.setPrimaryClass(chosenClass);
    }

    if (!hero.isHybrid()
        && hero.getPrimaryClass() != null
        && hero.getPrimaryClass() != chosenClass
        && getClassLevel(hero, chosenClass) == 5) {
      hero.setSecondaryClass(chosenClass);
      hero.setHybrid(true);
      hero.setHybridClass(
          heroStatCalculator.resolveHybridClass(hero.getPrimaryClass(), chosenClass));
    }

    heroStatCalculator.applyLevelUp(hero, effectiveChoice);

    return heroRepository.save(hero);
  }

  @Override
  public Hero addExperience(Long heroId, int amount) {

    Hero hero =
        heroRepository.findById(heroId).orElseThrow(() -> new RuntimeException("Hero not found"));

    hero.setExperience(hero.getExperience() + amount);

    return heroRepository.save(hero);
  }

  @Override
  public boolean isLevelUpPending(Long heroId) {

    Hero hero =
        heroRepository.findById(heroId).orElseThrow(() -> new RuntimeException("Hero not found"));

    return hero.getLevel() < 20 && hero.getExperience() >= hero.getExperienceToNextLevel();
  }

  @Override
  public Optional<Hero> findById(Long heroId) {

    return heroRepository.findById(heroId);
  }

  @Override
  public Hero save(Hero hero) {

    return heroRepository.save(hero);
  }

  private void incrementClassLevel(Hero hero, HeroClass heroClass) {

    switch (heroClass) {
      case ORDER -> hero.setOrderLevels(hero.getOrderLevels() + 1);
      case CHAOS -> hero.setChaosLevels(hero.getChaosLevels() + 1);
      case WARRIOR -> hero.setWarriorLevels(hero.getWarriorLevels() + 1);
      case MAGE -> hero.setMageLevels(hero.getMageLevels() + 1);
    }
  }

  private int getClassLevel(Hero hero, HeroClass heroClass) {

    return switch (heroClass) {
      case ORDER -> hero.getOrderLevels();
      case CHAOS -> hero.getChaosLevels();
      case WARRIOR -> hero.getWarriorLevels();
      case MAGE -> hero.getMageLevels();
    };
  }
}
