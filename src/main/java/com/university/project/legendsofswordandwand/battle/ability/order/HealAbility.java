package com.university.project.legendsofswordandwand.battle.ability.order;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import java.util.Comparator;
import java.util.List;

/**
 * An ability that restores health to one or all allies depending on the caster's hybrid class.
 *
 * <p>{@code PRIEST} heals all allies at once. All other classes heal only the ally with
 * the lowest current health. {@code PROPHET} doubles the amount healed.
 */
public class HealAbility implements Ability {

    private final HybridClass hybridClass;

    /**
     * Constructs a {@code HealAbility} for the given hybrid class.
     *
     * @param hybridClass the caster's hybrid class, used to determine heal targets and amounts
     */
    public HealAbility(HybridClass hybridClass) {
        this.hybridClass = hybridClass;
    }

    /**
     * Returns the mana cost of casting Heal.
     *
     * @return {@code 35}
     */
    @Override
    public int getManaCost() {
        return 35;
    }

    /**
     * Executes the Heal ability, restoring health to one or all allies.
     *
     * <p>If the caster's {@link HybridClass} is {@code PRIEST}, all allies are healed.
     * Otherwise, only the ally with the lowest current health is healed. The heal amount
     * is 25% of the target's maximum health, doubled for {@code PROPHET}. Health is capped
     * at each hero's maximum. Each heal is logged to the {@link BattleState}.
     *
     * @param caster  the {@link BattleUnit} casting the ability
     * @param target  the intended target (unused by this ability)
     * @param allies  the list of allied {@link BattleUnit}s to potentially heal
     * @param enemies the list of enemy {@link BattleUnit}s (unused by this ability)
     * @param state   the current {@link BattleState}, used for logging
     */
    @Override
    public void execute(
            BattleUnit caster,
            BattleUnit target,
            List<BattleUnit> allies,
            List<BattleUnit> enemies,
            BattleState state) {

        double multiplier = (hybridClass == HybridClass.PROPHET) ? 2.0 : 1.0;

        if (hybridClass == HybridClass.PRIEST) {
            allies.forEach(u -> applyHeal(u.getHero(), multiplier, state));
        } else {
            allies.stream()
                    .min(Comparator.comparingInt(unit -> unit.getHero().getHealth()))
                    .ifPresent(unit -> applyHeal(unit.getHero(), multiplier, state));
        }
    }

    /**
     * Applies a heal to a single hero, restoring up to 25% of their maximum health.
     *
     * @param hero       the {@link HeroSnapshot} to heal
     * @param multiplier the damage multiplier to apply to the base heal amount
     * @param state      the current {@link BattleState}, used for logging
     */
    private void applyHeal(HeroSnapshot hero, double multiplier, BattleState state) {

        int heal = (int) (hero.getMaxHealth() * 0.25 * multiplier);
        int before = hero.getHealth();
        hero.setHealth(Math.min(hero.getMaxHealth(), hero.getHealth() + heal));
        int actual = hero.getHealth() - before;
        state.log(
                "  ✦ Heal restores "
                        + actual
                        + " HP to "
                        + hero.getName()
                        + " → "
                        + hero.getHealth()
                        + " HP");
    }
}