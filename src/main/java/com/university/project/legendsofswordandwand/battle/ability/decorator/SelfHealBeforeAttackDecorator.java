package com.university.project.legendsofswordandwand.battle.ability.decorator;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.battle.ability.Ability;

import java.util.List;

/**
 * Decorator that heals the caster for 10% of their maximum health before
 * the wrapped ability executes.
 *
 * <p>Applied to {@code BerserkerAbility} when the caster's hybrid class is
 * {@code PALADIN}. The self-heal fires unconditionally before the attack,
 * capped at the caster's maximum health.
 *
 * <p>Example usage in {@code AbilityFactory}:
 * <pre>
 *   Ability base = new BerserkerAbility();
 *   base = new SelfHealBeforeAttackDecorator(base);
 * </pre>
 */
public class SelfHealBeforeAttackDecorator extends AbilityDecorator {

    /**
     * Constructs a {@code SelfHealBeforeAttackDecorator} wrapping the given ability.
     *
     * @param wrapped the {@link Ability} to execute after the self-heal
     */
    public SelfHealBeforeAttackDecorator(Ability wrapped) {

        super(wrapped);
    }

    /**
     * Heals the caster for 10% of their maximum health, then delegates to the
     * wrapped ability.
     *
     * <p>The heal is capped at the caster's maximum health. The result is logged
     * to the {@link BattleState} before the wrapped ability fires.
     *
     * @param caster  the {@link BattleUnit} casting the ability; receives the self-heal
     * @param target  the primary target {@link BattleUnit}, passed through to the wrapped ability
     * @param allies  the list of allied {@link BattleUnit}s, passed through
     * @param enemies the list of enemy {@link BattleUnit}s, passed through
     * @param state   the current {@link BattleState}, used for logging
     */
    @Override
    public void execute(BattleUnit caster, BattleUnit target,
                        List<BattleUnit> allies, List<BattleUnit> enemies,
                        BattleState state) {

        HeroSnapshot hero = caster.getHero();

        int heal = (int) (hero.getMaxHealth() * 0.10);
        hero.setHealth(Math.min(hero.getMaxHealth(), hero.getHealth() + heal));
        state.log("  ✦ Paladin self-heals \" + heal + \" HP before attacking");

        wrapped.execute(caster, target, allies, enemies, state);
    }
}
