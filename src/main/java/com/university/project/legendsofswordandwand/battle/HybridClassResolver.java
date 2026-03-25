package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import org.springframework.stereotype.Component;

/**
 * Spring component responsible for resolving the {@link HybridClass} that results from combining
 * two {@link HeroClass} values.
 *
 * <p>When both classes are the same, a pure specialisation is returned. When the classes differ,
 * the combination maps to a unique hybrid archetype.
 */
@Component
public class HybridClassResolver {

  /**
   * Resolves the {@link HybridClass} for the given pair of {@link HeroClass} values.
   *
   * <p>The resolution is order-independent: {@code resolve(ORDER, CHAOS)} and {@code resolve(CHAOS,
   * ORDER)} return the same result. If both classes are identical, the pure specialisation for that
   * class is returned:
   *
   * <ul>
   *   <li>{@code ORDER + ORDER} → {@code PRIEST}
   *   <li>{@code CHAOS + CHAOS} → {@code INVOKER}
   *   <li>{@code WARRIOR + WARRIOR} → {@code KNIGHT}
   *   <li>{@code MAGE + MAGE} → {@code WIZARD}
   * </ul>
   *
   * Cross-class combinations resolve as follows:
   *
   * <ul>
   *   <li>{@code ORDER + CHAOS} → {@code HERETIC}
   *   <li>{@code ORDER + WARRIOR} → {@code PALADIN}
   *   <li>{@code ORDER + MAGE} → {@code PROPHET}
   *   <li>{@code CHAOS + WARRIOR} → {@code ROGUE}
   *   <li>{@code CHAOS + MAGE} → {@code SORCERER}
   *   <li>{@code WARRIOR + MAGE} → {@code WARLOCK}
   * </ul>
   *
   * @param primary the hero's primary {@link HeroClass}
   * @param secondary the hero's secondary {@link HeroClass}
   * @return the resolved {@link HybridClass} * @throws IllegalArgumentException if the two classes
   *     do not map to any known hybrid * combination — this should never occur in practice as all
   *     valid {@link HeroClass} * pairings are explicitly handled
   */
  public HybridClass resolve(HeroClass primary, HeroClass secondary) {

    HeroClass a = primary.ordinal() <= secondary.ordinal() ? primary : secondary;
    HeroClass b = primary.ordinal() <= secondary.ordinal() ? secondary : primary;

    if (a == b) {
      return switch (a) {
        case ORDER -> HybridClass.PRIEST;
        case CHAOS -> HybridClass.INVOKER;
        case WARRIOR -> HybridClass.KNIGHT;
        case MAGE -> HybridClass.WIZARD;
      };
    }

    if (a == HeroClass.ORDER && b == HeroClass.CHAOS) return HybridClass.HERETIC;
    if (a == HeroClass.ORDER && b == HeroClass.WARRIOR) return HybridClass.PALADIN;
    if (a == HeroClass.ORDER && b == HeroClass.MAGE) return HybridClass.PROPHET;
    if (a == HeroClass.CHAOS && b == HeroClass.WARRIOR) return HybridClass.ROGUE;
    if (a == HeroClass.CHAOS && b == HeroClass.MAGE) return HybridClass.SORCERER;
    if (a == HeroClass.WARRIOR && b == HeroClass.MAGE) return HybridClass.WARLOCK;

    throw new IllegalArgumentException("Cannot hybridize the same class: " + primary);
  }
}
