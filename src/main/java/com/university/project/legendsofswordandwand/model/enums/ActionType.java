package com.university.project.legendsofswordandwand.model.enums;

/** Represents the four actions a unit may take on their turn in battle. */
public enum ActionType {
  /** Performs a physical attack on a chosen target. */
  ATTACK,
  /** Forfeits the turn, restoring +10 HP and +5 mana to the unit. */
  DEFEND,
  /** Postpones the unit's action to the end of the round (FIFO). */
  WAIT,
  /** Activates the unit's special ability, consuming mana. */
  CAST
}
