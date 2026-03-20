package com.university.project.legendsofswordandwand.service.hero.impl;

import com.university.project.legendsofswordandwand.battle.HeroStatCalculator;
import com.university.project.legendsofswordandwand.battle.HybridClassResolver;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.HeroRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Hero Object Service class. */
@Service
@Transactional
@RequiredArgsConstructor
class HeroServiceImpl implements IHeroService {

    private final HeroRepository heroRepository;
    private final PartyRepository partyRepository;
    private final HeroStatCalculator heroStatCalculator;
    private final HybridClassResolver hybridClassResolver;

    /**
     * Creates a new base stat (Level 1, 100 HP, 10 Attack) Hero for requesting Party.
     *
     * @param partyId           ID of Party to create Hero Object for
     * @param selectedHeroName  name to assign to the hero
     * @param selectedHeroClass hero class to assign to the hero
     * @throws RuntimeException if the party is not found
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

        heroStatCalculator.applyClassBonusOnly(hero, selectedHeroClass);

        party.getHeroes().add(hero);
        heroRepository.save(hero);
    }

    /**
     * Applies a level-up to the given hero using the chosen class.
     *
     * <p>For hybrid heroes, the primary class is always used regardless of {@code chosenClass}.
     * A hero becomes hybrid when they reach level 5 in a second class after already having a
     * primary class set, at which point the {@link HybridClassResolver} assigns their hybrid class.
     *
     * @param heroId      the ID of the hero to level up
     * @param chosenClass the {@link HeroClass} the player selected for this level-up
     * @return the updated and saved {@link Hero}
     * @throws RuntimeException if the hero is not found, is already at max level, or has
     *                          insufficient XP
     */
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
                && getClassLevel(hero, chosenClass) == 5) {
            hero.setSecondaryClass(chosenClass);
            hero.setHybrid(true);
            hero.setHybridClass(hybridClassResolver.resolve(hero.getPrimaryClass(), chosenClass));
        }

        heroStatCalculator.applyLevelUp(hero, effectiveChoice);

        return heroRepository.save(hero);
    }

    /**
     * Adds experience points to the given hero and persists the change.
     *
     * @param heroId the ID of the hero to award experience to
     * @param amount the amount of experience to add
     * @return the updated and saved {@link Hero}
     * @throws RuntimeException if the hero is not found
     */
    @Override
    public Hero addExperience(Long heroId, int amount) {

        Hero hero =
                heroRepository.findById(heroId).orElseThrow(() -> new RuntimeException("Hero not found"));

        hero.setExperience(hero.getExperience() + amount);

        return heroRepository.save(hero);
    }

    /**
     * Returns {@code true} if the hero has enough experience to level up and has not reached
     * the maximum level of 20.
     *
     * @param heroId the ID of the hero to check
     * @return {@code true} if a level-up is pending
     * @throws RuntimeException if the hero is not found
     */
    @Override
    public boolean isLevelUpPending(Long heroId) {

        Hero hero =
                heroRepository.findById(heroId).orElseThrow(() -> new RuntimeException("Hero not found"));

        return hero.getLevel() < 20 && hero.getExperience() >= hero.getExperienceToNextLevel();
    }

    /**
     * Finds a hero by their ID.
     *
     * @param heroId the ID of the hero to find
     * @return an {@link Optional} containing the hero if found, or empty otherwise
     */
    @Override
    public Optional<Hero> findById(Long heroId) {
        return heroRepository.findById(heroId);
    }

    /**
     * Persists the given hero entity.
     *
     * @param hero the {@link Hero} to save
     * @return the saved {@link Hero}
     */
    @Override
    public Hero save(Hero hero) {
        return heroRepository.save(hero);
    }

    /**
     * Deletes the hero with the given ID.
     *
     * @param heroId the ID of the hero to delete
     */
    @Override
    public void delete(Long heroId) {
        heroRepository.deleteById(heroId);
    }

    /**
     * Increments the class-specific level counter for the given hero and class by one.
     *
     * @param hero      the {@link Hero} to update
     * @param heroClass the {@link HeroClass} whose level counter to increment
     */
    private void incrementClassLevel(Hero hero, HeroClass heroClass) {
        switch (heroClass) {
            case ORDER -> hero.setOrderLevels(hero.getOrderLevels() + 1);
            case CHAOS -> hero.setChaosLevels(hero.getChaosLevels() + 1);
            case WARRIOR -> hero.setWarriorLevels(hero.getWarriorLevels() + 1);
            case MAGE -> hero.setMageLevels(hero.getMageLevels() + 1);
        }
    }

    /**
     * Returns the number of levels the hero has accumulated in the given class.
     *
     * @param hero      the {@link Hero} to query
     * @param heroClass the {@link HeroClass} to look up
     * @return the number of levels in the specified class
     */
    private int getClassLevel(Hero hero, HeroClass heroClass) {
        return switch (heroClass) {
            case ORDER -> hero.getOrderLevels();
            case CHAOS -> hero.getChaosLevels();
            case WARRIOR -> hero.getWarriorLevels();
            case MAGE -> hero.getMageLevels();
        };
    }
}