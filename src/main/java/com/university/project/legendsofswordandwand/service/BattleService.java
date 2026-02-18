package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Hero;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BattleService {

  public void fight(Hero a, Hero b) {
    while (a.getHealth() > 0 && b.getHealth() > 0) {
      b.setHealth(b.getHealth() - a.getAttack());
      if (b.getHealth() <= 0) break;

      a.setHealth(a.getHealth() - b.getAttack());
    }
  }
}
