package com.university.project.legendsofswordandwand.battle.ability.chaos;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import com.university.project.legendsofswordandwand.battle.ability.AbilityHelper;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import java.util.ArrayList;
import java.util.List;

/**
 * An ability that launches a fireball hitting up to three enemies at once.
 *
 * <p>If the caster's {@link HybridClass} is {@code SORCERER}, all hits deal double damage.
 * Otherwise the standard calculated damage is applied.
 */
public class FireballAbility implements Ability {

  private final HybridClass hybridClass;

  /**
   * Constructs a {@code FireballAbility} for the given hybrid class.
   *
   * @param hybridClass the caster's hybrid class, used to determine the damage multiplier
   */
  public FireballAbility(HybridClass hybridClass) {
    this.hybridClass = hybridClass;
  }

  /**
   * Returns the mana cost of casting Fireball.
   *
   * @return {@code 30}
   */
  @Override
  public int getManaCost() {
    return 30;
  }

  /**
   * Executes the Fireball ability, dealing damage to up to three enemies.
   *
   * <p>If {@code target} is {@code null}, the first three enemies in the {@code enemies} list are
   * hit. Otherwise, {@code target} is hit first, followed by up to two additional enemies from the
   * list. The damage multiplier is {@code 2.0} for {@code SORCERER} and {@code 1.0} otherwise. Each
   * hit is logged to the {@link BattleState}.
   *
   * @param caster the {@link BattleUnit} casting the ability
   * @param target the primary {@link BattleUnit} to hit, or {@code null} to hit the first three
   *     enemies
   * @param allies the list of allied {@link BattleUnit}s (unused by this ability)
   * @param enemies the list of all enemy {@link BattleUnit}s
   * @param state the current {@link BattleState}, used for damage application and logging
   */
  @Override
  public void execute(
      BattleUnit caster,
      BattleUnit target,
      List<BattleUnit> allies,
      List<BattleUnit> enemies,
      BattleState state) {

    double multiplier = (hybridClass == HybridClass.SORCERER) ? 2.0 : 1.0;

    List<BattleUnit> hits;
    if (target == null) {
      hits = enemies.stream().limit(3).toList();
    } else {
      hits = new ArrayList<>();
      if (enemies.contains(target)) hits.add(target);
      for (BattleUnit unit : enemies) {
        if (hits.size() >= 3) break;
        if (unit.getBattleId() != target.getBattleId()) hits.add(unit);
      }
    }

    for (BattleUnit hit : hits) {
      int damage = (int) (AbilityHelper.calculateDamage(caster, hit) * multiplier);
      int hpBefore = hit.getHero().getHealth();
      AbilityHelper.applyDamage(caster.getHero(), hit, damage, state);
      int actual = hpBefore - hit.getHero().getHealth();

      state.log(
          "  🔥 Fireball hits "
              + hit.getHero().getName()
              + " for "
              + actual
              + " dmg → "
              + hit.getHero().getHealth()
              + " HP left");
    }
  }
}
