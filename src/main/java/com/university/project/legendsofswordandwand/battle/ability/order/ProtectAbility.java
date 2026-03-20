package com.university.project.legendsofswordandwand.battle.ability.order;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import java.util.List;

/**
 * An ability that applies a protective shield to all unshielded allies.
 *
 * <p>Each unshielded ally receives a shield worth 10% of their maximum health.
 * {@code PROPHET} doubles the shield amount. Allies that are already shielded are unaffected.
 */
public class ProtectAbility implements Ability {

    private final HybridClass hybridClass;

    /**
     * Constructs a {@code ProtectAbility} for the given hybrid class.
     *
     * @param hybridClass the caster's hybrid class, used to determine the shield multiplier
     */
    public ProtectAbility(HybridClass hybridClass) {
        this.hybridClass = hybridClass;
    }

    /**
     * Returns the mana cost of casting Protect.
     *
     * @return {@code 25}
     */
    @Override
    public int getManaCost() {
        return 25;
    }

    /**
     * Executes the Protect ability, applying a shield to all unshielded allies.
     *
     * <p>Each ally without an existing shield receives a shield equal to 10% of their maximum
     * health, doubled if the caster's {@link HybridClass} is {@code PROPHET}. Allies that
     * already have a shield active are skipped. Each outcome is logged to the {@link BattleState}.
     *
     * @param caster  the {@link BattleUnit} casting the ability
     * @param target  the intended target (unused by this ability)
     * @param allies  the list of allied {@link BattleUnit}s to potentially shield
     * @param enemies the list of enemy {@link BattleUnit}s (unused by this ability)
     * @param state   the current {@link BattleState}, used for shield tracking and logging
     */
    @Override
    public void execute(
            BattleUnit caster,
            BattleUnit target,
            List<BattleUnit> allies,
            List<BattleUnit> enemies,
            BattleState state) {

        double multiplier = (hybridClass == HybridClass.PROPHET) ? 2.0 : 1.0;

        for (BattleUnit ally : allies) {

            int shield = (int) (ally.getHero().getMaxHealth() * 0.10 * multiplier);

            if (state.getShield(ally.getBattleId()) == 0) {
                state.setShield(ally.getBattleId(), shield);
                state.log("  ✦ Protect shields " + ally.getHero().getName() + " for " + shield + " HP");
            } else {
                state.log(
                        "  ✦ Protect has no effect — " + ally.getHero().getName() + " is already shielded");
            }
        }
    }
}