package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.enums.BattleStatus;
import java.io.Serializable;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BattleState implements Serializable {

  private List<BattleUnit> playerUnits = new ArrayList<>();
  private List<BattleUnit> enemyUnits = new ArrayList<>();

  private LinkedList<Long> turnQueue = new LinkedList<>();

  private Long activeUnitBattleId;

  private List<String> battleLog = new ArrayList<>();

  public void log(String message) {

    battleLog.add(message);
  }

  private Map<Long, Integer> shields = new HashMap<>();

  private Map<Long, Integer> stunned = new HashMap<>();

  private boolean playerTurn;
  private BattleStatus status = BattleStatus.IN_PROGRESS;
  private Long campaignId;
  private boolean pvp = false;
  private Long pvpInvitationId;
  private String pvpSenderUsername;
  private String pvpReceiverUsername;
  private Long pvpSenderPartyId;
  private Long pvpReceiverPartyId;

  public boolean isOver() {
    return status != BattleStatus.IN_PROGRESS;
  }

  public List<BattleUnit> getLivingPlayerHeroes() {
    return playerUnits.stream().filter(BattleUnit::isAlive).toList();
  }

  public List<BattleUnit> getLivingEnemyHeroes() {
    return enemyUnits.stream().filter(BattleUnit::isAlive).toList();
  }

  public BattleUnit findUnit(long battleId) {

    for (BattleUnit unit : playerUnits) if (unit.getBattleId() == battleId) return unit;
    for (BattleUnit unit : enemyUnits) if (unit.getBattleId() == battleId) return unit;

    return null;
  }

  public BattleUnit getActiveUnit() {
    return findUnit(activeUnitBattleId);
  }

  public boolean isStunned(Long heroId) {
    return stunned.getOrDefault(heroId, 0) > 0;
  }

  public void applyStun(Long heroId) {
    stunned.put(heroId, 1);
  }

  public void tickStuns() {
    stunned.replaceAll((id, turns) -> Math.max(0, turns - 1));
  }

  public int getShield(Long battleId) {
    return shields.getOrDefault(battleId, 0);
  }

  public void setShield(Long battleId, int amount) {
    shields.put(battleId, amount);
  }
}
