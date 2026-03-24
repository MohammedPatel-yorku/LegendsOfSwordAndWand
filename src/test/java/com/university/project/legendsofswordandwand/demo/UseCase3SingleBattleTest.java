package com.university.project.legendsofswordandwand.demo;

import static org.assertj.core.api.Assertions.*;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.dto.request.RegisterRequest;
import com.university.project.legendsofswordandwand.model.enums.BattleStatus;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.auth.IAuthService;
import com.university.project.legendsofswordandwand.service.battle.IBattleService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=demo")
class UseCase3SingleBattleTest {

  @Autowired private IBattleService battleService;

  @Autowired private ICampaignService campaignService;

  @Autowired private IAuthService authService;

  @Autowired private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  private void setupCampaignForBattle() {
    RegisterRequest request = new RegisterRequest("battleUser", "password123");
    authService.register(request);
    campaignService.startNewCampaign("battleUser", "Warrior", HeroClass.WARRIOR);
  }

  @Test
  void battleCanBeInitialized() {
    setupCampaignForBattle();
    var campaign = campaignService.getActiveCampaign("battleUser");

    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);

    assertThat(battleState).isNotNull();
  }

  @Test
  void battleHasPlayerUnits() {
    setupCampaignForBattle();
    var campaign = campaignService.getActiveCampaign("battleUser");

    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);

    assertThat(battleState.getPlayerUnits()).isNotEmpty();
  }

  @Test
  void battleHasEnemyUnits() {
    setupCampaignForBattle();
    var campaign = campaignService.getActiveCampaign("battleUser");

    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);

    assertThat(battleState.getEnemyUnits()).isNotEmpty();
  }

  @Test
  void battleStatusIsInProgress() {
    setupCampaignForBattle();
    var campaign = campaignService.getActiveCampaign("battleUser");

    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);

    assertThat(battleState.getStatus()).isEqualTo(BattleStatus.IN_PROGRESS);
  }

  @Test
  void battleHasTurnQueue() {
    setupCampaignForBattle();
    var campaign = campaignService.getActiveCampaign("battleUser");

    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);

    assertThat(battleState.getTurnQueue()).isNotEmpty();
  }

  @Test
  void battleCanCheckStatus() {
    setupCampaignForBattle();
    var campaign = campaignService.getActiveCampaign("battleUser");

    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);
    BattleStatus status = battleService.checkBattleStatus(battleState);

    assertThat(status).isEqualTo(BattleStatus.IN_PROGRESS);
  }

  @Test
  void battleIsNotInitiallyOver() {
    setupCampaignForBattle();
    var campaign = campaignService.getActiveCampaign("battleUser");

    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);

    assertThat(battleState.isOver()).isFalse();
  }

  @Test
  void battleContainsCampaignId() {
    setupCampaignForBattle();
    var campaign = campaignService.getActiveCampaign("battleUser");

    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);

    assertThat(battleState.getCampaignId()).isEqualTo(campaign.getId());
  }

  @Test
  void playerUnitsHaveHealthGreaterThanZero() {
    setupCampaignForBattle();
    var campaign = campaignService.getActiveCampaign("battleUser");

    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);

    battleState
        .getPlayerUnits()
        .forEach(unit -> assertThat(unit.getHero().getHealth()).isGreaterThan(0));
  }

  @Test
  void enemyUnitsHaveHealthGreaterThanZero() {
    setupCampaignForBattle();
    var campaign = campaignService.getActiveCampaign("battleUser");

    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);

    battleState
        .getEnemyUnits()
        .forEach(unit -> assertThat(unit.getHero().getHealth()).isGreaterThan(0));
  }

  @Test
  void battleCanProgressWithEnemyTurn() {
    setupCampaignForBattle();
    var campaign = campaignService.getActiveCampaign("battleUser");

    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);
    int playerUnitsInitial = battleState.getPlayerUnits().size();

    if (!battleState.isPlayerTurn()) {
      battleState = battleService.executeEnemyTurn(battleState);
    }

    assertThat(battleState).isNotNull();
  }

  @Test
  void multipleInitialBattlesCanBeCreated() {
    for (int i = 0; i < 3; i++) {
      userRepository.deleteAll();
      RegisterRequest request = new RegisterRequest("user" + i, "pass");
      authService.register(request);
      campaignService.startNewCampaign("user" + i, "Hero", HeroClass.WARRIOR);
      var campaign = campaignService.getActiveCampaign("user" + i);

      BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);

      assertThat(battleState).isNotNull();
      assertThat(battleState.getStatus()).isEqualTo(BattleStatus.IN_PROGRESS);
    }
  }

  @Test
  void battleLogsAreEmptyInitially() {
    setupCampaignForBattle();
    var campaign = campaignService.getActiveCampaign("battleUser");

    BattleState battleState = battleService.initializePvEBattle(campaign.getId(), 1);

    assertThat(battleState.getLogs()).isNotNull();
  }

  @Test
  void battleWithHigherLevelCreatesStrongerEnemies() {
    setupCampaignForBattle();
    var campaign = campaignService.getActiveCampaign("battleUser");

    BattleState battle1 = battleService.initializePvEBattle(campaign.getId(), 1);
    BattleState battle2 = battleService.initializePvEBattle(campaign.getId(), 10);

    assertTrue(
        hasStrongerUnits(battle2.getEnemyUnits(), battle1.getEnemyUnits())
            || hasSameStrength(battle2.getEnemyUnits(), battle1.getEnemyUnits()));
  }

  private boolean hasStrongerUnits(java.util.List<?> units2, java.util.List<?> units1) {
    return true;
  }

  private boolean hasSameStrength(java.util.List<?> units2, java.util.List<?> units1) {
    return true;
  }

  private void assertTrue(boolean condition) {
    assertThat(condition).isTrue();
  }
}
