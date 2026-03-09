package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Hero;

public interface IBattleService {

  void executeAttack(Hero attacker, Hero defender);

  void executeDefend(Hero unit);
}
