package com.university.project.legendsofswordandwand.model.enums;

/**
 * Represents the ten hybrid classes a hero can unlock by reaching Level 5 in two distinct base
 * classes. Each hybrid combines the stat growth and abilities of both parent classes and grants a
 * unique passive effect.
 *
 * <p>Hybrid class is determined by the combination of the hero's primary and secondary classes (the
 * order they reached Level 5). The mapping is:
 *
 * <ul>
 *   <li>Order + Order → {@link #PRIEST} — Heal now applies to all friendly units
 *   <li>Order + Chaos → {@link #HERETIC} — Can cast Fire Shield in place of Protect
 *   <li>Order + Warrior → {@link #PALADIN} — Berserker Attack heals the Paladin before striking
 *   <li>Order + Mage → {@link #PROPHET} — Friendly spells double their effect
 *   <li>Chaos + Chaos → {@link #INVOKER} — Chain Lightning deals 50% per subsequent target
 *   <li>Chaos + Warrior → {@link #ROGUE} — 50% chance of a bonus Sneak Attack on every attack
 *   <li>Chaos + Mage → {@link #SORCERER} — Fireball deals double damage to all targets
 *   <li>Warrior + Warrior → {@link #KNIGHT} — Berserker Attack has 50% chance to stun targets
 *   <li>Warrior + Mage → {@link #WARLOCK} — Burns 10% of target's mana on every attack
 *   <li>Mage + Mage → {@link #WIZARD} — Replenish costs only 40 mana
 * </ul>
 */
public enum HybridClass {
  /** Order + Order. Heal now applies to all friendly units. */
  PRIEST,
  /** Order + Chaos. Can cast Fire Shield in place of Protect. */
  HERETIC,
  /** Order + Warrior. Berserker Attack heals the Paladin before striking. */
  PALADIN,
  /** Order + Mage. All friendly spells (Protect, Heal, Replenish) double their effect. */
  PROPHET,
  /** Chaos + Chaos. Chain Lightning deals 50% damage to every subsequent target hit. */
  INVOKER,
  /** Chaos + Warrior. 50% chance of an additional Sneak Attack on every basic attack. */
  ROGUE,
  /** Chaos + Mage. Fireball deals double damage to all affected units. */
  SORCERER,
  /** Warrior + Warrior. Berserker Attack has a 50% chance to stun each unit hit. */
  KNIGHT,
  /** Warrior + Mage. Burns 10% of the target's total mana on every basic attack. */
  WARLOCK,
  /** Mage + Mage. Replenish costs only 40 mana instead of 80. */
  WIZARD
}
