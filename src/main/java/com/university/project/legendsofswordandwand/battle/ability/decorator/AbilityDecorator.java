package com.university.project.legendsofswordandwand.battle.ability.decorator;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import java.util.List;

/**
 * Abstract base class for the Decorator pattern applied to {@link Ability}.
 *
 * <p>Wraps an existing {@link Ability} and delegates all calls to it by default. Concrete
 * subclasses override {@link #execute} to add behaviour before or after the wrapped ability fires,
 * without modifying the wrapped class itself.
 *
 * <p>Because {@code AbilityDecorator} implements {@link Ability}, decorators are fully
 * interchangeable with base abilities and can be stacked arbitrarily.
 */
public abstract class AbilityDecorator implements Ability {

  /** The ability being wrapped and extended by this decorator. */
  protected Ability wrapped;

  /**
   * Constructs an {@code AbilityDecorator} around the given ability.
   *
   * @param wrapped the {@link Ability} to wrap; must not be {@code null}
   */
  protected AbilityDecorator(Ability wrapped) {

    this.wrapped = wrapped;
  }

  /**
   * Returns the mana cost of the wrapped ability.
   *
   * @return the mana cost delegated to the wrapped {@link Ability}
   */
  @Override
  public int getManaCost() {

    return wrapped.getManaCost();
  }

  /**
   * Delegates execution to the wrapped ability.
   *
   * <p>Subclasses should call {@code super.execute(...)} or {@code wrapped.execute(...)} at the
   * appropriate point to preserve the wrapped ability's behaviour.
   *
   * @param caster the {@link BattleUnit} casting the ability
   * @param target the primary target {@link BattleUnit}, may be {@code null}
   * @param allies the list of allied {@link BattleUnit}s
   * @param enemies the list of enemy {@link BattleUnit}s
   * @param state the current {@link BattleState}
   */
  @Override
  public void execute(
      BattleUnit caster,
      BattleUnit target,
      List<BattleUnit> allies,
      List<BattleUnit> enemies,
      BattleState state) {

    wrapped.execute(caster, target, allies, enemies, state);
  }
}
