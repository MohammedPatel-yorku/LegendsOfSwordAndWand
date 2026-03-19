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

@Component
public class AbilityFactory {

  private final Random random = new Random();

  public Ability resolve(HeroClass heroClass, HybridClass hybridClass, int abilityIndex) {

    return switch (heroClass) {
      case ORDER -> resolveOrder(hybridClass, abilityIndex);
      case CHAOS -> resolveChaos(hybridClass, abilityIndex);
      case WARRIOR -> new BerserkerAbility(hybridClass, random);
      case MAGE -> new ReplenishAbility(hybridClass);
    };
  }

  private Ability resolveOrder(HybridClass hybridClass, int abilityIndex) {

    if (abilityIndex == 0) {

      return hybridClass == HybridClass.HERETIC
          ? new FireShieldAbility()
          : new ProtectAbility(hybridClass);
    }

    return new HealAbility(hybridClass);
  }

  private Ability resolveChaos(HybridClass hybridClass, int abilityIndex) {

    return abilityIndex == 0
        ? new FireballAbility(hybridClass)
        : new ChainLightningAbility(hybridClass, random);
  }
}
