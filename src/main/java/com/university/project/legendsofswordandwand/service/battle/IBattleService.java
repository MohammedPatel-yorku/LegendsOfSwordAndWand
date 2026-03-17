package com.university.project.legendsofswordandwand.service.battle;

import com.university.project.legendsofswordandwand.model.Hero;

public interface IBattleService {

  void executeAttack(Hero attacker, Hero defender);

  void executeDefend(Hero unit);
}
