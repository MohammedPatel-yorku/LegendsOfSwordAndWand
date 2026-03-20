package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;

public class PvPBattleInitializer extends BattleInitializer {

    private final Party senderParty;
    private final Party receiverParty;
    private final Long invitationId;
    private final UserRepository userRepository;

    public PvPBattleInitializer(Party senderParty, Party receiverParty,
                                Long invitationId, UserRepository userRepository) {

        this.senderParty = senderParty;
        this.receiverParty = receiverParty;
        this.invitationId = invitationId;
        this.userRepository = userRepository;
    }

    @Override
    protected List<BattleUnit> buildPlayerUnits() {

        List<BattleUnit> units = new ArrayList<>();
        long id = 1L;

        for (Hero hero : senderParty.getHeroes())
            if (!hero.isTemporary())
                units.add(new BattleUnit(id++, new HeroSnapshot(hero), false));
        return units;
    }

    @Override
    protected List<BattleUnit> buildEnemyUnits() {

        List<BattleUnit> units = new ArrayList<>();
        long id = -1L;

        for (Hero hero : receiverParty.getHeroes())
            if (!hero.isTemporary())
                units.add(new BattleUnit(id--, new HeroSnapshot(hero), true));
        return units;
    }

    @Override
    public void onBattleEnd(BattleState state) {

        boolean senderWon = state.getStatus() ==
                com.university.project.legendsofswordandwand.model.enums.BattleStatus.PLAYER_WIN;

        String winnerUsername = senderWon
                ? senderParty.getOwner().getUsername()
                : receiverParty.getOwner().getUsername();
        String loserUsername = senderWon
                ? receiverParty.getOwner().getUsername()
                : senderParty.getOwner().getUsername();

        userRepository.findByUsername(winnerUsername).ifPresent(u -> {
            u.setPvpWins(u.getPvpWins() + 1);
            userRepository.save(u);
        });
        userRepository.findByUsername(loserUsername).ifPresent(u -> {
            u.setPvpLosses(u.getPvpLosses() + 1);
            userRepository.save(u);
        });
    }
}