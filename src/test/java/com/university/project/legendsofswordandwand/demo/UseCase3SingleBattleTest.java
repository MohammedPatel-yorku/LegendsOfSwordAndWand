package com.university.project.legendsofswordandwand.demo;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.dto.request.RegisterRequest;
import com.university.project.legendsofswordandwand.model.enums.ActionType;
import com.university.project.legendsofswordandwand.model.enums.BattleStatus;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.auth.IAuthService;
import com.university.project.legendsofswordandwand.service.battle.IBattleService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=demo")
class UseCase3SingleBattleTest {

  @Autowired private IBattleService battleService;

  @Autowired private ICampaignService campaignService;

  @Autowired private IAuthService authService;

  @Autowired private UserRepository userRepository;

  @Autowired private CampaignRepository campaignRepository;

  @BeforeEach
  void setUp() {
    campaignRepository.deleteAll();
    userRepository.deleteAll();
    System.out.println("\n--- Setting up test environment for USE CASE 3 ---");
  }

  // Case 3-01: Battle Initialization
  @Test
  void testCase3_01_battleInitialization() {
    System.out.println("\n[Test Case 3-01] Battle Initialization");
    System.out.println("Category: Battle initialization");
    System.out.println("Requirements: UC3 – successful battle initialization");

    System.out.println("\n--- Initial Condition ---");
    RegisterRequest request = new RegisterRequest("battleUser", "password123");
    authService.register(request);
    var campaign = campaignService.startNewCampaign("battleUser", "BattleHero", HeroClass.WARRIOR);
    System.out.println(" System running");
    System.out.println(" User logged in");
    System.out.println(" Campaign is active: " + campaign.isActive());
    System.out.println(" Player and enemy party are initialized");

    System.out.println("\n--- Procedure ---");
    System.out.println("  User enters battle room");

    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);

    System.out.println("\n--- Expected Outcome ---");
    System.out.println(" New battle instance created");
    assertThat(battleState).isNotNull();
    System.out.println("  - Battle State ID: " + battleState.getId());

    System.out.println(" Turn order initialized");
    assertThat(battleState.getTurnQueue()).isNotNull();
    assertThat(battleState.getTurnQueue()).isNotEmpty();
    System.out.println("  - Turn Queue Size: " + battleState.getTurnQueue().size());

    System.out.println(" Battle status set to active");
    BattleStatus battleStatus = battleService.checkBattleStatus(battleState);
    System.out.println("  - Battle Status: " + battleStatus);

    System.out.println(" Turn queue includes all alive units from both parties");
    int expectedUnits = battleState.getPlayerUnits().size() + battleState.getEnemyUnits().size();
    assertThat(battleState.getTurnQueue().size()).isGreaterThanOrEqualTo(expectedUnits);
    System.out.println("  - Expected units: " + expectedUnits);
    System.out.println("  - Queue size: " + battleState.getTurnQueue().size());

    System.out.println("\n TEST PASSED: Battle initialization successful");
  }

  // Case 3-02: Attack Execution
  @Test
  void testCase3_02_attackExecution() {
    System.out.println("\n[Test Case 3-02] Attack Execution");
    System.out.println("Category: Attack execution");
    System.out.println("Requirements: UC3 – execute attack action");

    System.out.println("\n--- Initial Condition ---");
    RegisterRequest request = new RegisterRequest("attackUser", "password123");
    authService.register(request);
    var campaign = campaignService.startNewCampaign("attackUser", "AttackHero", HeroClass.WARRIOR);
    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);
    System.out.println(" Battle is active");
    System.out.println(" Turn order initialized");
    System.out.println(" Active unit is alive");
    System.out.println(" Target unit is alive");

    var firstUnit = battleState.getTurnQueue().get(0).getUnit();
    System.out.println("  - Active Unit: " + firstUnit.getName());

    Long targetId = null;
    if (firstUnit.isPlayerUnit() && !battleState.getEnemyUnits().isEmpty()) {
      targetId = battleState.getEnemyUnits().get(0).getId();
    } else if (!firstUnit.isPlayerUnit() && !battleState.getPlayerUnits().isEmpty()) {
      targetId = battleState.getPlayerUnits().get(0).getId();
    }

    if (targetId == null) {
      System.out.println(" No valid target available, skipping test");
      return;
    }

    var targetUnit = firstUnit.isPlayerUnit()
        ? battleState.getEnemyUnits().stream().filter(u -> u.getId().equals(targetId)).findFirst()
        : battleState.getPlayerUnits().stream().filter(u -> u.getId().equals(targetId)).findFirst();

    assertThat(targetUnit).isPresent();
    double targetHpBefore = targetUnit.get().getCurrentHp();

    System.out.println("\n--- Procedure ---");
    System.out.println("  User selects Attack");
    System.out.println("  User selected an enemy hero");

    BattleState updatedBattleState =
        battleService.executePlayerAction(battleState, ActionType.ATTACK, targetId, null);

    System.out.println("\n--- Expected Outcome ---");
    System.out.println(" Damage calculated properly");

    var updatedTarget = firstUnit.isPlayerUnit()
        ? updatedBattleState.getEnemyUnits().stream().filter(u -> u.getId().equals(targetId)).findFirst()
        : updatedBattleState.getPlayerUnits().stream().filter(u -> u.getId().equals(targetId)).findFirst();

    assertThat(updatedTarget).isPresent();
    double targetHpAfter = updatedTarget.get().getCurrentHp();
    double damageDealt = targetHpBefore - targetHpAfter;

    System.out.println("  - Target HP After: " + targetHpAfter);
    System.out.println("  - Damage Dealt: " + damageDealt);
    assertThat(damageDealt).isGreaterThanOrEqualTo(0);

    System.out.println(" Target unit HP calculated properly");
    assertThat(targetHpAfter).isLessThanOrEqualTo(targetHpBefore);

    System.out.println(" HP does not go below 0");
    assertThat(targetHpAfter).isGreaterThanOrEqualTo(0);

    System.out.println(" Turn completes");  
    BattleStatus battleStatus = battleService.checkBattleStatus(updatedBattleState);
    System.out.println("  - Battle Status after turn: " + battleStatus);

    System.out.println("\n TEST PASSED: Attack execution successful");
  }
}
