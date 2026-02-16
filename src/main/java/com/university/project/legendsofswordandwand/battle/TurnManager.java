package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.Hero;
import org.springframework.stereotype.Component;
import java.util.LinkedList;
import java.util.List;

@Component
public class TurnManager {
    private LinkedList<Hero> activeQueue = new LinkedList<>();
    private LinkedList<Hero> waitingQueue = new LinkedList<>();

    public void initializeTurnOrder(List<Hero> allUnits) {
        activeQueue.clear();
        waitingQueue.clear();
        allUnits.sort((a, b) -> b.getLevel() != a.getLevel() ?
                b.getLevel() - a.getLevel() : b.getAttack() - a.getAttack());
        activeQueue.addAll(allUnits);
    }

    public Hero getNextUnit() {
        if (!activeQueue.isEmpty()) {
            return activeQueue.pollFirst();
        }
        return waitingQueue.pollFirst();
    }

    public void moveUnitToWait(Hero unit) {
        waitingQueue.addLast(unit);
    }
}