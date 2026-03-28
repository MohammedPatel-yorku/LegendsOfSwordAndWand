package com.university.project.legendsofswordandwand.model.enums;

/** Represents the current outcome state of a battle. */
public enum BattleStatus {
  /** The battle is still ongoing; neither side has been eliminated. */
  IN_PROGRESS,
  /** All enemy units have been defeated; the player wins. */
  PLAYER_WIN,
  /** All player units have been defeated; the player loses. */
  PLAYER_LOSE
}
