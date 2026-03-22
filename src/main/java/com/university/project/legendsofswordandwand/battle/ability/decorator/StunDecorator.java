package com.university.project.legendsofswordandwand.battle.ability.decorator;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.ability.Ability;

import java.util.List;
import java.util.Random;

/**
 * Decorator that gives the wrapped ability a 50% chance to stun the primary
 * target for one turn after it executes.
 *
 * <p>Applied to {@code BerserkerAbility} when the caster's hybrid class is
 * {@code KNIGHT}. The stun is only applied if the target is non-null and
 * still alive after the wrapped ability fires.
 *
 * <p>Example usage in {@code AbilityFactory}:
 * <pre>
 *   Ability base = new BerserkerAbility();
 *   base = new StunDecorator(base, random);
 * </pre>
 */
public class StunDecorator extends AbilityDecorator {

    /** Used to determine whether the stun triggers on each execution. */
    private final Random random;

    /**
     * Constructs a {@code StunDecorator} wrapping the given ability.
     *
     * @param wrapped the {@link Ability} to execute before attempting the stun
     * @param random  the {@link Random} instance used for the 50% stun roll
     */
    public StunDecorator(Ability wrapped, Random random) {

        super(wrapped);
        this.random = random;
    }

    /**
     * Executes the wrapped ability, then attempts to stun the primary target.
     *
     * <p>The stun has a 50% chance of triggering. It is only applied if
     * {@code target} is non-null and the target is still alive after the
     * wrapped ability resolves. A stun causes the affected unit to skip
     * its next turn.
     *
     * @param caster  the {@link BattleUnit} casting the ability
     * @param target  the primary target; stun candidate if alive after the attack
     * @param allies  the list of allied {@link BattleUnit}s, passed through
     * @param enemies the list of enemy {@link BattleUnit}s, passed through
     * @param state   the current {@link BattleState}, used for stun tracking and logging
     */
    @Override
    public void execute(BattleUnit caster, BattleUnit target,
                        List<BattleUnit> allies, List<BattleUnit> enemies,
                        BattleState state) {

        wrapped.execute(caster, target, allies, enemies, state);

        if (target != null && target.isAlive() && random.nextBoolean()) {
            state.applyStun(target.getBattleId());
            state.log("  ★ Knight stuns " + target.getHero().getName() + "!");
        }
    }
}
