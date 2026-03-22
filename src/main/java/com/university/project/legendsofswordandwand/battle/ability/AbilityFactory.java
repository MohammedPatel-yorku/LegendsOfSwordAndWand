package com.university.project.legendsofswordandwand.battle.ability;

import com.university.project.legendsofswordandwand.battle.ability.chaos.ChainLightningAbility;
import com.university.project.legendsofswordandwand.battle.ability.chaos.FireballAbility;
import com.university.project.legendsofswordandwand.battle.ability.mage.ReplenishAbility;
import com.university.project.legendsofswordandwand.battle.ability.order.FireShieldAbility;
import com.university.project.legendsofswordandwand.battle.ability.order.HealAbility;
import com.university.project.legendsofswordandwand.battle.ability.order.ProtectAbility;
import com.university.project.legendsofswordandwand.battle.ability.warrior.BerserkerAbility;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import java.util.Random;
import org.springframework.stereotype.Component;

/**
 * Factory responsible for constructing {@link Ability} instances based on a hero's {@link
 * HeroClass}, {@link HybridClass}, and ability slot index.
 *
 * <p>Registered as a Spring {@code @Component} for dependency injection.
 */
@Component
public class AbilityFactory {

  private final Random random = new Random();

  /**
   * Resolves and returns the appropriate {@link Ability} for the given hero configuration.
   *
   * <ul>
   *   <li>{@code ORDER} — delegates to {@link #resolveOrder(HybridClass, int)}
   *   <li>{@code CHAOS} — delegates to {@link #resolveChaos(HybridClass, int)}
   *   <li>{@code WARRIOR} — always returns a {@link BerserkerAbility}
   *   <li>{@code MAGE} — always returns a {@link ReplenishAbility}
   * </ul>
   *
   * @param heroClass the hero's primary class, determining which ability group to use
   * @param hybridClass the hero's hybrid class, used to further specialise the ability
   * @param abilityIndex the ability slot index ({@code 0} for the first ability, {@code 1} for the
   *     second)
   * @return the resolved {@link Ability} instance
   */
  public Ability resolve(HeroClass heroClass, HybridClass hybridClass, int abilityIndex) {

    return switch (heroClass) {
      case ORDER -> resolveOrder(hybridClass, abilityIndex);
      case CHAOS -> resolveChaos(hybridClass, abilityIndex);
      case WARRIOR -> new BerserkerAbility(hybridClass, random);
      case MAGE -> new ReplenishAbility(hybridClass);
    };
  }

  /**
   * Resolves an {@link Ability} for an {@code ORDER} class hero.
   *
   * <p>For ability slot {@code 0}: returns {@link FireShieldAbility} if the hybrid class is {@code
   * HERETIC}, otherwise returns {@link ProtectAbility}. For all other slots, returns {@link
   * HealAbility}.
   *
   * @param hybridClass the hero's hybrid class
   * @param abilityIndex the ability slot index
   * @return the resolved {@link Ability} for the {@code ORDER} class
   */
  private Ability resolveOrder(HybridClass hybridClass, int abilityIndex) {

    if (abilityIndex == 0) {
      return hybridClass == HybridClass.HERETIC
          ? new FireShieldAbility()
          : new ProtectAbility(hybridClass);
    }

    return new HealAbility(hybridClass);
  }

  /**
   * Resolves an {@link Ability} for a {@code CHAOS} class hero.
   *
   * <p>Returns {@link FireballAbility} for ability slot {@code 0}, or {@link ChainLightningAbility}
   * for all other slots.
   *
   * @param hybridClass the hero's hybrid class
   * @param abilityIndex the ability slot index
   * @return the resolved {@link Ability} for the {@code CHAOS} class
   */
  private Ability resolveChaos(HybridClass hybridClass, int abilityIndex) {

    return abilityIndex == 0
        ? new FireballAbility(hybridClass)
        : new ChainLightningAbility(hybridClass, random);
  }
}
