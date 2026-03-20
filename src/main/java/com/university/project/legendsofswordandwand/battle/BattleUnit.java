package com.university.project.legendsofswordandwand.battle;

import java.io.Serializable;
import lombok.Getter;

@Getter
public class BattleUnit implements Serializable {

  private final long battleId;
  private final HeroSnapshot hero;
  private final boolean enemy;

  public BattleUnit(long battleId, HeroSnapshot hero, boolean enemy) {

    this.battleId = battleId;
    this.hero = hero;
    this.enemy = enemy;
  }

  public boolean isAlive() {
    return hero.getHealth() > 0;
  }
}
