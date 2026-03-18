package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.battle.ability.Ability;
import com.university.project.legendsofswordandwand.battle.ability.AbilityFactory;
import com.university.project.legendsofswordandwand.battle.ability.AbilityHelper;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class AbilityExecutor {

    private final AbilityFactory abilityFactory;
    private final Random random = new Random();

    public void executeAbility(
            BattleUnit caster,
            BattleUnit target,
            List<BattleUnit> allies,
            List<BattleUnit> enemies,
            BattleState state,
            int abilityIndex) {

        HeroClass effectiveClass = (caster.getHero().getPrimaryClass() != null)
                ? caster.getHero().getPrimaryClass()
                : caster.getHero().getStartingClass();
        HybridClass hybridClass = caster.getHero().getHybridClass();

        Ability ability = abilityFactory.resolve(effectiveClass, hybridClass, abilityIndex);

        if (caster.getHero().getMana() < ability.getManaCost())
            throw new IllegalStateException(
                    caster.getHero().getName() + " does not have enough mana");

        caster.getHero().setMana(caster.getHero().getMana() - ability.getManaCost());
        ability.execute(caster, target, allies, enemies, state);
    }

    public void maybeSneak(BattleUnit attacker, BattleUnit defender, BattleState state) {

        if (random.nextBoolean()) {

            int bonus = AbilityHelper.calculateDamage(attacker, defender) / 2;
            AbilityHelper.applyDamage(attacker.getHero(), defender, bonus, state);
        }
    }

    public void applyManaBurn(BattleUnit defender) {

        int burn = (int) (defender.getHero().getMaxMana() * 0.10);
        defender.getHero().setMana(Math.max(0, defender.getHero().getMana() - burn));
    }
}
