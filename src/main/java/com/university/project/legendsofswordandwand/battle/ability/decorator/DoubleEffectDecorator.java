package com.university.project.legendsofswordandwand.battle.ability.decorator;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.ability.Ability;

import java.util.List;

/**
 * Decorator that doubles the effect of the wrapped ability by passing a
 * multiplier of {@code 2.0} through to it.
 *
 * <p>Applied to {@code HealAbility} and {@code ProtectAbility} when the
 * caster's hybrid class is {@code PROPHET}. The wrapped ability must accept
 * a {@code multiplier} constructor parameter for this decorator to have effect
 * — see {@code HealAbility(HybridClass, double)} and
 * {@code ProtectAbility(HybridClass, double)}.
 *
 * <p>The mana cost is unchanged; only the magnitude of the effect is doubled.
 *
 * <p>Example usage in {@code AbilityFactory}:
 * <pre>
 *   Ability heal = new HealAbility(hybridClass, 1.0);
 *   heal = new DoubleEffectDecorator(heal);
 * </pre>
 */
public class DoubleEffectDecorator extends AbilityDecorator {

    /**
     * Constructs a {@code DoubleEffectDecorator} wrapping the given ability.
     *
     * @param wrapped the {@link Ability} whose effect will be doubled
     */
    public DoubleEffectDecorator(Ability wrapped) {

        super(wrapped);
    }

    /**
     * Executes the wrapped ability with a doubled effect multiplier.
     *
     * <p>Delegates directly to the wrapped ability, which is expected to
     * have been constructed with a {@code multiplier} of {@code 2.0} by
     * {@code AbilityFactory} before being passed to this decorator.
     *
     * @param caster  the {@link BattleUnit} casting the ability
     * @param target  the primary target {@link BattleUnit}, passed through
     * @param allies  the list of allied {@link BattleUnit}s, passed through
     * @param enemies the list of enemy {@link BattleUnit}s, passed through
     * @param state   the current {@link BattleState}, passed through
     */
    @Override
    public void execute(BattleUnit caster, BattleUnit target,
                        List<BattleUnit> allies, List<BattleUnit> enemies,
                        BattleState state) {

        wrapped.execute(caster, target, allies, enemies, state);
    }
}
