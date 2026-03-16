package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import org.springframework.stereotype.Component;

@Component
public class HybridClassResolver {

  public HybridClass resolve(HeroClass primary, HeroClass secondary) {

    HeroClass a = primary.ordinal() <= secondary.ordinal() ? primary : secondary;
    HeroClass b = primary.ordinal() <= secondary.ordinal() ? secondary : primary;

    if (a == HeroClass.ORDER && b == HeroClass.CHAOS) return HybridClass.HERETIC;
    if (a == HeroClass.ORDER && b == HeroClass.WARRIOR) return HybridClass.PALADIN;
    if (a == HeroClass.ORDER && b == HeroClass.MAGE) return HybridClass.PROPHET;
    if (a == HeroClass.CHAOS && b == HeroClass.WARRIOR) return HybridClass.ROGUE;
    if (a == HeroClass.CHAOS && b == HeroClass.MAGE) return HybridClass.SORCERER;
    if (a == HeroClass.WARRIOR && b == HeroClass.MAGE) return HybridClass.WARLOCK;

    throw new IllegalArgumentException("Cannot hybridize the same class: " + primary);
  }
}
