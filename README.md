# ⚔️ Legends of Sword and Wand

> A tactical role-playing game built with Java 17 and Spring Boot — fight your way through dungeons, level up a party of heroes, and challenge other players in PvP battles.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Design Patterns](#design-patterns)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Use Cases & Features](#use-cases--features)
- [Game Mechanics](#game-mechanics)
- [Code Smells — Deliverable 3 Analysis](#code-smells--deliverable-3-analysis)
- [How to Find Code Smells with MetricsTree](#how-to-find-code-smells-with-metricsreloaded--metricstree)
- [Contributors](#contributors)

---

## Overview

*Legends of Sword and Wand* is a single-player or hot-seat two-player tactical RPG inspired by Dungeons & Dragons. Players form a party of heroes from four classes — **Order**, **Chaos**, **Warrior**, and **Mage** — level them up through a 30-room dungeon campaign (PvE), and face off against other players' saved parties (PvP).

Key highlights:
- Full turn-based combat with abilities, status effects, and hybrid class promotions
- Persistent campaign state stored in a MySQL database
- Thymeleaf-rendered web UI served by Spring Boot MVC
- Hot-seat PvP via invitation system
- Hall of Fame leaderboard and PvP league standings

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.2 |
| Web | Spring MVC + Thymeleaf |
| Security | Spring Security (form login, BCrypt) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL |
| Build | Maven |
| Utilities | Lombok |
| Testing | JUnit 5 + AssertJ |

---

## Architecture

The application follows a layered MVC architecture with clear separation of concerns:

```
┌──────────────────────────────────────────────┐
│              Thymeleaf Views (HTML)           │
└────────────────────┬─────────────────────────┘
                     │
┌────────────────────▼─────────────────────────┐
│             Controllers (Spring MVC)          │
│  AuthController, BattleController,            │
│  CampaignController, InnController,           │
│  PvPController, ProfileController, ...        │
└────────────────────┬─────────────────────────┘
                     │
┌────────────────────▼─────────────────────────┐
│            Service Layer (Business Logic)     │
│  battle/  campaign/  hero/  party/            │
│  inventory/  user/  pvp/  auth/               │
└──────┬──────────────────────────┬────────────┘
       │                          │
┌──────▼──────────┐  ┌────────────▼────────────┐
│  Battle Engine  │  │   Repository Layer       │
│  BattleState    │  │   (Spring Data JPA)      │
│  BattleUnit     │  │   CampaignRepository     │
│  AbilityExecutor│  │   HeroRepository         │
│  EnemyGenerator │  │   PartyRepository, ...   │
│  HeroStatCalc.  │  └────────────┬────────────┘
└─────────────────┘               │
                     ┌────────────▼────────────┐
                     │        MySQL DB          │
                     └─────────────────────────┘
```

**Package organization:**

- `controller/` — HTTP request handlers, session management, model population
- `service/` — Business logic, split by domain (battle, campaign, hero, party, user, pvp, auth, inventory)
- `battle/` — Self-contained battle engine (state machine, abilities, enemy generation, stat calculation)
- `model/` — JPA entities (Hero, Party, Campaign, User, Item, Inventory, PvPInvitation)
- `model/enums/` — HeroClass, HybridClass, ActionType, BattleStatus, RoomType, InvitationStatus
- `repository/` — Spring Data JPA interfaces
- `config/` — Spring Security configuration
- `dto/` — Response DTOs for profile and campaign views

---

## Design Patterns

The project implements **six design patterns** as required:

### 1. Strategy — `ClassBonusStrategy`
**Where:** `battle/strategy/`  
**Classes:** `ClassBonusStrategy` (interface), `OrderBonusStrategy`, `ChaosBonusStrategy`, `WarriorBonusStrategy`, `MageBonusStrategy`  
**How it works:** Each hero class's per-level stat bonuses are encapsulated in their own strategy class. `HeroStatCalculator` holds a `List<ClassBonusStrategy>` injected by Spring and dynamically selects the right strategy at runtime. Adding a new class requires only a new strategy — no changes to the calculator.

### 2. Decorator — `AbilityDecorator`
**Where:** `battle/ability/decorator/`  
**Classes:** `AbilityDecorator` (abstract), `DoubleEffectDecorator`, `StunDecorator`, `SelfHealBeforeAttackDecorator`  
**How it works:** Ability effects are layered at runtime by wrapping a base `Ability` with decorators. For example, the Knight's Berserker Attack is the base Berserker ability wrapped with a `StunDecorator`, which adds the 50% stun chance on top without modifying the base class.

### 3. Factory — `AbilityFactory`
**Where:** `battle/ability/AbilityFactory.java`  
**How it works:** Centralizes the creation of `Ability` objects for each hero class and hybrid class combination. Controllers and services request abilities by class/hybrid type rather than constructing them directly, keeping object creation decoupled from usage.

### 4. Template Method — `BattleInitializer`
**Where:** `battle/initializer/`  
**Classes:** `BattleInitializer` (abstract), `PvEBattleInitializer`, `PvPBattleInitializer`  
**How it works:** The abstract `BattleInitializer` defines the skeleton of battle setup (build unit lists, set up the turn queue, configure state). PvE and PvP subclasses override the steps that differ — enemy generation vs. loading two saved parties — while the shared sequencing is inherited.

### 5. Strategy (Enemy AI) — `decideEnemyAction` archetypes
**Where:** `BattleServiceImpl.decideEnemyAction()`  
**How it works:** Enemy behavior is differentiated by archetype (glass cannon, tank, swift, brute, balanced), mirroring the Strategy pattern to give each enemy type a distinct combat personality without a proliferation of subclasses.

### 6. Dependency Injection / IoC (Spring)
**Where:** Throughout all `@Service`, `@Component`, `@Controller` classes  
**How it works:** Spring's IoC container wires all collaborators through constructor injection (via Lombok `@RequiredArgsConstructor`). Services depend on interfaces (`IBattleService`, `ICampaignService`, etc.), not concrete implementations, enabling easy substitution and testability — a direct application of the Dependency Inversion principle.

---

## Project Structure

```
LegendsOfSwordAndWand/
├── src/
│   ├── main/
│   │   ├── java/com/university/project/legendsofswordandwand/
│   │   │   ├── LegendsOfSwordAndWandApplication.java
│   │   │   ├── DemoDataLoader.java
│   │   │   ├── battle/
│   │   │   │   ├── ability/
│   │   │   │   │   ├── chaos/        (ChainLightningAbility, FireballAbility)
│   │   │   │   │   ├── decorator/    (AbilityDecorator, DoubleEffect, Stun, SelfHeal)
│   │   │   │   │   ├── mage/         (ReplenishAbility)
│   │   │   │   │   ├── order/        (FireShieldAbility, HealAbility, ProtectAbility)
│   │   │   │   │   ├── warrior/      (BerserkerAbility)
│   │   │   │   │   ├── Ability.java
│   │   │   │   │   ├── AbilityFactory.java
│   │   │   │   │   └── AbilityHelper.java
│   │   │   │   ├── initializer/
│   │   │   │   │   ├── BattleInitializer.java
│   │   │   │   │   ├── PvEBattleInitializer.java
│   │   │   │   │   └── PvPBattleInitializer.java
│   │   │   │   ├── strategy/
│   │   │   │   │   ├── ClassBonusStrategy.java
│   │   │   │   │   ├── ChaosBonusStrategy.java
│   │   │   │   │   ├── MageBonusStrategy.java
│   │   │   │   │   ├── OrderBonusStrategy.java
│   │   │   │   │   └── WarriorBonusStrategy.java
│   │   │   │   ├── AbilityExecutor.java
│   │   │   │   ├── BattleState.java
│   │   │   │   ├── BattleUnit.java
│   │   │   │   ├── DamageCalculator.java
│   │   │   │   ├── EnemyGenerator.java
│   │   │   │   ├── HeroSnapshot.java
│   │   │   │   ├── HeroStatCalculator.java
│   │   │   │   └── HybridClassResolver.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── BattleController.java
│   │   │   │   ├── CampaignController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── InnController.java
│   │   │   │   ├── ProfileController.java
│   │   │   │   ├── PvPController.java
│   │   │   │   └── RankingsController.java
│   │   │   ├── dto/
│   │   │   │   ├── request/   (RegisterRequest)
│   │   │   │   └── response/  (CampaignViewInfo, CompleteCampaignInfo, DashboardInfo, ...)
│   │   │   ├── model/
│   │   │   │   ├── enums/     (ActionType, BattleStatus, HeroClass, HybridClass, ...)
│   │   │   │   ├── Campaign.java
│   │   │   │   ├── Hero.java
│   │   │   │   ├── Inventory.java
│   │   │   │   ├── Item.java
│   │   │   │   ├── Party.java
│   │   │   │   ├── PvPInvitation.java
│   │   │   │   └── User.java
│   │   │   ├── repository/
│   │   │   │   ├── CampaignRepository.java
│   │   │   │   ├── HeroRepository.java
│   │   │   │   ├── InventoryRepository.java
│   │   │   │   ├── ItemRepository.java
│   │   │   │   ├── PartyRepository.java
│   │   │   │   ├── PvPInvitationRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   └── service/
│   │   │       ├── auth/       (IAuthService, AuthServiceImpl)
│   │   │       ├── battle/     (IBattleService, IInnService, BattleServiceImpl, InnServiceImpl)
│   │   │       ├── campaign/   (ICampaignService, ICampaignProgressService, impls)
│   │   │       ├── hero/       (IHeroService, HeroServiceImpl)
│   │   │       ├── inventory/  (IInventoryService, InventoryServiceImpl)
│   │   │       ├── party/      (IPartyService, IPartyManagementService, impls)
│   │   │       ├── pvp/        (IPvPService, PvPServiceImpl)
│   │   │       ├── security/   (CustomUserDetailsService)
│   │   │       └── user/       (IProfileService, IUserService, impls)
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/
│   │           ├── auth/       (login.html, register.html)
│   │           ├── battle/     (battle.html, inn.html, result.html)
│   │           ├── campaign/   (campaign.html, new-campaign.html, inn.html, complete.html)
│   │           ├── fragments/  (layout.html, navbar.html, card.html)
│   │           ├── profile/    (profile.html)
│   │           └── dashboard.html
│   └── test/
│       └── java/.../LegendsOfSwordAndWandApplicationTests.java
└── pom.xml
```

---

## Getting Started

### Prerequisites

- **Java 17+** — [Download](https://adoptium.net/)
- **Maven 3.8+** — [Download](https://maven.apache.org/)
- **MySQL 8.0+** — [Download](https://dev.mysql.com/downloads/)

### Database Setup

```sql
CREATE DATABASE legends_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'legends_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON legends_db.* TO 'legends_user'@'localhost';
FLUSH PRIVILEGES;
```

### Clone the Repository

```bash
git clone https://github.com/your-org/LegendsOfSwordAndWand.git
cd LegendsOfSwordAndWand
```

---

## Configuration

The application reads database credentials from environment variables. Set the following before running:

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | JDBC connection URL | `jdbc:mysql://localhost:3306/legends_db` |
| `DB_USER` | Database username | `legends_user` |
| `DB_PASS` | Database password | `your_password` |

**On Linux/macOS:**
```bash
export DB_URL=jdbc:mysql://localhost:3306/legends_db
export DB_USER=legends_user
export DB_PASS=your_password
```

**On Windows (PowerShell):**
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/legends_db"
$env:DB_USER="legends_user"
$env:DB_PASS="your_password"
```

The app runs with the `demo` profile by default (`spring.profiles.active=demo`), which seeds initial item data via `DemoDataLoader`.

Hibernate is configured to auto-create/update the schema on startup (`spring.jpa.hibernate.ddl-auto=update` — verify in your profile-specific properties).

---

## Running the Application

```bash
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean package -DskipTests
java -jar target/LegendsOfSwordAndWand-0.0.1-SNAPSHOT.jar
```

The application starts on **http://localhost:8080**.

**Default pages:**
- `http://localhost:8080/login` — Login page
- `http://localhost:8080/register` — New account
- `http://localhost:8080/dashboard` — Main hub (after login)

---

## Use Cases & Features

| # | Use Case | Endpoint(s) |
|---|---|---|
| UC1 | Create profile & log in | `GET/POST /register`, `GET/POST /login` |
| UC2 | Start a new PvE campaign | `GET/POST /campaign/new`, `GET /campaign` |
| UC3 | Single battle (PvE) | `GET /battle`, `POST /battle/action`, `GET /battle/result` |
| UC4 | Interact with the Inn | `GET /inn`, `POST /inn/buy`, `POST /inn/recruit` |
| UC5 | Exit the PvE campaign | `POST /campaign/exit` |
| UC6 | Continue an incomplete campaign | `GET /campaign` (auto-loads active campaign) |
| UC7 | PvP invitation & battle | `POST /pvp/invite`, `POST /pvp/accept`, `GET /battle` |
| — | View profile & rankings | `GET /profile`, `GET /rankings` |
| — | Level up heroes | `POST /campaign/level-up`, `POST /battle/result/level-up` |

---

## Game Mechanics

### Hero Classes & Stats

Every hero starts at Level 1 with: **5 ATK / 5 DEF / 100 HP / 50 Mana**. Each level-up grants **+1 ATK / +1 DEF / +5 HP / +2 Mana** before class bonuses.

| Class | Per-Level Bonus | Abilities |
|---|---|---|
| **Order** | +5 Mana, +2 DEF | Protect (25 mana), Heal (35 mana) |
| **Chaos** | +3 ATK, +5 HP | Fireball (30 mana), Chain Lightning (40 mana) |
| **Warrior** | +2 ATK, +3 DEF | Berserker Attack (60 mana) |
| **Mage** | +5 Mana, +1 ATK | Replenish (80 mana) |

### Hybrid Classes

Reaching Level 5 in two different classes unlocks a **Hybrid Class** with combined bonuses and unique passive abilities. There are 10 possible hybrids (e.g., Order+Warrior = **Paladin**, Chaos+Mage = **Sorcerer**, Warrior+Mage = **Warlock**).

### Battle System

- Units act in order of **level (descending)**, then **attack (descending)** as tiebreaker
- Each turn: **Attack**, **Defend** (+10 HP, +5 Mana), **Wait** (FIFO at end of round), or **Cast**
- Damage formula: `attacker.attack - defender.defense`
- Shields, stuns, and fire shields are tracked per unit in `BattleState`
- Battle ends when all units on one side reach 0 HP

### Experience & Gold

- XP per enemy: `Exp(L) = 50 * L`
- Gold per enemy: `G(L) = 75 * L`
- XP threshold to level up: `Exp(L) = Exp(L-1) + 500 + 75*L + 20*L²`
- Loss penalty: −30% XP in current level, −10% gold

### PvE Campaign

- 30 rooms total; each room is **Battle** or **Inn** (probability shifts with cumulative party level)
- Scoring at completion: `100 × hero levels + 10 × gold + 0.5 × item_cost × 10` per bought item

### Inn

- Full party revive and heal on arrival
- Shop: Bread, Cheese, Steak (HP), Water, Juice, Wine (Mana), Elixir (full revive)
- Hero recruitment available in the first 10 rooms (1–4 random recruits of level 1–4)

---

## Code Smells — Deliverable 3 Analysis

> This section documents 10 instances of code smells found in the project, spanning at least 5 distinct types. For each smell, the location, problem, and recommended refactoring are described.

---

### Smell 1 — Duplicated Code: Enemy Turn Loop (Type: Code Duplication)

**Location:** `BattleController.java` — `battlePage()` method (lines ~65–72) and `action()` method (lines ~125–132)

**The code:**
```java
// Appears identically in BOTH battlePage() and action()
int safetyLimit = 50;
while (!state.isOver() && !state.isPlayerTurn() && safetyLimit-- > 0) {
    state = battleService.executeEnemyTurn(state);
}
if (!state.isOver() && !state.isPlayerTurn()) {
    state.setStatus(battleService.checkBattleStatus(state));
}
```

**Why it's a smell:** This exact 5-line block is copy-pasted between two methods. Any change to the enemy turn loop (e.g., the safety limit, the fallback logic) must be updated in both places. If one copy is changed and the other is not, a subtle bug is introduced.

**Refactoring — Extract Method:** Move the block into a private helper in `BattleController`:
```java
private BattleState processEnemyTurns(BattleState state) {
    int safetyLimit = 50;
    while (!state.isOver() && !state.isPlayerTurn() && safetyLimit-- > 0) {
        state = battleService.executeEnemyTurn(state);
    }
    if (!state.isOver() && !state.isPlayerTurn()) {
        state.setStatus(battleService.checkBattleStatus(state));
    }
    return state;
}
```
Then both call sites become a single line: `state = processEnemyTurns(state);`

---

### Smell 2 — Magic Numbers (Type: Magic Numbers / Primitive Obsession)

**Location:** `BattleServiceImpl.java` — `awardBattleRewards()`, `applyBattleLoss()`; `HeroStatCalculator.java` — `calculateExpThreshold()` and `applyBaseGain()`; `InnServiceImpl.java` — `getAvailableRecruits()`

**The code (multiple locations):**
```java
// BattleServiceImpl - XP and gold formulas
int totalXp = state.getEnemyUnits().stream().mapToInt(u -> 50 * u.getHero().getLevel()).sum();
int gold = state.getEnemyUnits().stream().mapToInt(u -> 75 * u.getHero().getLevel()).sum();

// applyBattleLoss - XP threshold re-computed inline
int prevThreshold = u.getHero().getExperienceToNextLevel()
    - (500 + 75 * u.getHero().getLevel() + 20 * u.getHero().getLevel() * u.getHero().getLevel());
int penalty = (int) (xpInCurrentLevel * 0.30);
// ...
partyManagementService.deductGold(party.getId(), (int) (party.getGold() * 0.10));

// HeroStatCalculator
hero.setLevel(hero.getLevel() + 1);
hero.setHealth(hero.getHealth() + 5);
hero.setMaxHealth(hero.getMaxHealth() + 5);
hero.setMana(hero.getMana() + 2);
hero.setMaxMana(hero.getMaxMana() + 2);
```

**Why it's a smell:** Raw numbers like `50`, `75`, `500`, `0.30`, `0.10`, `5`, `2` are spread across multiple files with no explanation. If the game designer changes "XP per enemy level" from 50 to 60, every location must be found and updated manually. This is a maintenance and consistency risk.

**Refactoring — Replace Magic Number with Named Constant:** Create a `GameConstants` class:
```java
public final class GameConstants {
    public static final int XP_PER_ENEMY_LEVEL = 50;
    public static final int GOLD_PER_ENEMY_LEVEL = 75;
    public static final double LOSS_XP_PENALTY_RATE = 0.30;
    public static final double LOSS_GOLD_PENALTY_RATE = 0.10;
    public static final int BASE_HP_PER_LEVEL = 5;
    public static final int BASE_MANA_PER_LEVEL = 2;
    public static final int EXP_BASE = 500;
    public static final int EXP_LINEAR_COEFF = 75;
    public static final int EXP_QUADRATIC_COEFF = 20;
}
```

---

### Smell 3 — Duplicated XP Threshold Formula (Type: Code Duplication)

**Location:** `BattleServiceImpl.applyBattleLoss()` and `InnServiceImpl.getAvailableRecruits()`

**The code:**
```java
// In BattleServiceImpl.applyBattleLoss()
int prevThreshold = u.getHero().getExperienceToNextLevel()
    - (500 + 75 * u.getHero().getLevel() + 20 * u.getHero().getLevel() * u.getHero().getLevel());

// In InnServiceImpl.getAvailableRecruits()
int prevThreshold = h.getExperienceToNextLevel()
    - (500 + 75 * level + 20 * level * level);
```

**Why it's a smell:** The XP threshold step formula `500 + 75*L + 20*L²` is duplicated in two different services in two different packages. `HeroStatCalculator` already contains `calculateExpThreshold()` but it is `private`. If the formula changes (e.g., a game balance update), it must be changed in three places and any mismatch causes invisible bugs where heroes gain or lose the wrong amount of XP.

**Refactoring — Extract Method + Make Public:** Expose `calculateExpThreshold()` as a public static utility method (or make `HeroStatCalculator.getExpStepForLevel(int level)` public), then replace both inline computations:
```java
// Before (duplicated):
int prevThreshold = h.getExperienceToNextLevel() - (500 + 75 * level + 20 * level * level);

// After (single source of truth):
int prevThreshold = h.getExperienceToNextLevel() - HeroStatCalculator.getExpStepForLevel(level);
```

---

### Smell 4 — Feature Envy: Business Logic in `BattleController` (Type: Feature Envy)

**Location:** `BattleController.result()` method (~40 lines of business logic in a controller)

**The code:**
```java
boolean rewardsAlreadyGiven = Boolean.TRUE.equals(session.getAttribute("rewardsGiven"));
if (!rewardsAlreadyGiven && state.getStatus() == BattleStatus.PLAYER_WIN) {
    Map<String, Object> rewards = battleService.awardBattleRewards(state);
    model.addAttribute("rewardGold", rewards.get("gold"));
    model.addAttribute("rewardRecipients", rewards.get("recipients"));
    session.setAttribute("rewardsGiven", true);
} else if (state.getStatus() == BattleStatus.PLAYER_WIN) { ... }

if (!rewardsAlreadyGiven && state.getStatus() == BattleStatus.PLAYER_LOSE) {
    battleService.applyBattleLoss(state);
    session.setAttribute("rewardsGiven", true);
}

List<Hero> levelUpHeroes = state.getPlayerUnits().stream()
    .filter(u -> u.isAlive() && u.getHero().getId() != null)
    .filter(u -> heroService.isLevelUpPending(u.getHero().getId()))
    .map(u -> heroService.findById(u.getHero().getId()).orElse(null))
    .filter(Objects::nonNull)
    .toList();
```

**Why it's a smell:** The controller is making decisions about when to apply rewards, filtering hero lists, and checking level-up states — all of which are business concerns that belong in `IBattleService`. A controller should only call a service and put the result into the model. This violates the Single Responsibility Principle and GRASP's "Controller" pattern, and makes the logic untestable without spinning up an HTTP context.

**Refactoring — Move Method to Service:** Add a `BattleResultDTO getBattleResult(BattleState state)` method to `IBattleService` that returns all display data. The controller becomes a thin delegate:
```java
BattleResultDTO result = battleService.prepareBattleResult(state);
model.addAttribute("rewardGold", result.getGold());
model.addAttribute("levelUpHeroes", result.getLevelUpHeroes());
```

---

### Smell 5 — Switch on String (Type: Switch Statements / Primitive Obsession)

**Location:** `BattleServiceImpl.decideEnemyAction()` — a `switch` on `actor.getHero().getName()` (the enemy's name string)

**The code:**
```java
switch (name) {
    case "Skeleton", "Witch", "Shadow" -> { /* glass cannon behavior */ }
    case "Orc", "Dark Knight"          -> { /* brute behavior */ }
    case "Goblin", "Vampire"           -> { /* swift behavior */ }
    case "Troll"                       -> { /* tank behavior */ }
    default                            -> { /* balanced behavior */ }
}
```

**Why it's a smell:** Enemy behavior is coupled to a string match on the enemy's display name. This is fragile — renaming an enemy (e.g., "Dark Knight" → "Death Knight" for a graphical update) silently breaks their AI because the switch no longer matches. Meanwhile, `EnemyGenerator` already has an `EnemyType` enum with `Archetype` data attached, but that information is lost once the `Hero` object is created. This is also a violation of the Open/Closed Principle: adding a new enemy type requires modifying the switch statement.

**Refactoring — Replace Type Code with Strategy / Move Behavior to Enum:** Store the enemy's archetype on the `Hero` entity (or use a `BattleUnit` wrapper field). Then introduce a `EnemyAIStrategy` interface and attach one per archetype. The switch disappears entirely:
```java
// BattleUnit gets: EnemyBehavior behavior;
actor.getBehavior().decideAction(actor, targets, state);
```

---

### Smell 6 — Long Method: `innPage()` in `InnController` (Type: Long Method)

**Location:** `InnController.innPage()` — approximately 60 lines of mixed access control, session management, DB loading, and model population

**Why it's a smell:** The method does at least five distinct things: (1) validates access conditions, (2) decides whether to heal the party, (3) generates or reloads recruits from the session, (4) refreshes the campaign entity, and (5) populates 8+ model attributes. A method should do one thing. This method is difficult to test, difficult to read, and difficult to modify without risking regressions in any of the other concerns it handles.

**Refactoring — Extract Method:** Break into focused private helpers:
```java
private boolean isInnAccessPermitted(Campaign c, HttpSession session) { ... }
private List<Hero> loadOrRefreshRecruits(Long campaignId, HttpSession session) { ... }
private void populateInnModel(Model model, Campaign c, List<Hero> recruits, HttpSession session) { ... }
```

The main `innPage()` then reads as a clear sequence of steps.

---

### Smell 7 — Large Class: `BattleServiceImpl` (Type: Large Class / God Class)

**Location:** `BattleServiceImpl.java` — 532 lines

**Why it's a smell:** `BattleServiceImpl` is responsible for: (1) initializing PvE battles, (2) initializing PvP battles, (3) executing player actions, (4) executing enemy AI turns, (5) running the turn queue, (6) awarding post-battle rewards, (7) applying loss penalties, (8) updating PvP win/loss records, and (9) restoring heroes after PvP. This violates the Single Responsibility Principle. The class has too many reasons to change — a change to the reward system, the AI, or the PvP logic all touch the same file. MetricsTree will show this class with high WMC (Weighted Methods per Class) and high coupling.

**Refactoring — Extract Class:** Split into at minimum:
- `BattleEngineService` — turn execution, advance turn, defend, attack
- `BattleRewardService` — XP/gold distribution, loss penalty
- `PvPResultService` — PvP win/loss recording and hero restoration

---

### Smell 8 — Silent Exception Swallowing (Type: Inappropriate Error Handling)

**Location:** `CampaignController`, `BattleController`, `InnController` — all use the same pattern

**The code:**
```java
try {
    // ... 10-30 lines of business logic
} catch (Exception e) {
    return "redirect:/campaign";  // or /dashboard, /battle
}
```

**Why it's a smell:** Catching the root `Exception` type and silently redirecting hides all errors — whether it's a `NullPointerException` from a bug, a `DataAccessException` from a DB failure, or a legitimate `RuntimeException` thrown intentionally (like "Campaign already in progress"). Developers debugging a broken flow will see a redirect with no indication of what went wrong. This makes maintenance very difficult.

**Refactoring — Use Spring's `@ExceptionHandler` or Log the Exception:** At minimum, log the error before redirecting. Better: define specific exception types and handle them appropriately:
```java
} catch (IllegalStateException e) {
    redirectAttributes.addFlashAttribute("error", e.getMessage());
    return "redirect:/campaign";
} catch (Exception e) {
    log.error("Unexpected error in campaign flow", e); // at least log it!
    return "redirect:/campaign";
}
```

---

### Smell 9 — Data Clump: Recruit Stat Initialization (Type: Data Clumps)

**Location:** `InnServiceImpl.getAvailableRecruits()` — the lambda inside the `IntStream.range()` block

**The code:**
```java
switch (cls) {
    case ORDER   -> h.setOrderLevels(level);
    case CHAOS   -> h.setChaosLevels(level);
    case WARRIOR -> h.setWarriorLevels(level);
    case MAGE    -> h.setMageLevels(level);
}
for (int lvl = 1; lvl < level; lvl++) {
    heroStatCalculator.applyLevelUp(h, cls);
}
heroStatCalculator.applyClassBonusOnly(h, cls);
h.setLevel(level);
// XP threshold re-computed inline again here...
```

**Why it's a smell:** This group of operations — set class levels, apply level-ups, apply class bonus, set level, compute XP — is a coherent unit of behavior that represents "initialize a hero to a given level in a given class." The same cluster of data and operations already appears in `HeroServiceImpl.createBaseHeroForParty()`. This data clump should be extracted into a factory or builder method, ideally in `HeroServiceImpl` itself (e.g., `createHeroAtLevel(partyId, name, cls, level)`).

---

### Smell 10 — Inappropriate Intimacy: `BattleController` Accessing Internal Session Keys Across Methods (Type: Inappropriate Intimacy)

**Location:** `BattleController.java` and `InnController.java` — both define the constant `LAST_RESULT_KEY = "lastBattleResult"` independently

**The code:**
```java
// In BattleController.java:
private static final String LAST_RESULT_KEY = "lastBattleResult";

// In InnController.java:
private static final String LAST_RESULT_KEY = "lastBattleResult";
```

**Why it's a smell:** The same session attribute key string is defined separately in two controllers. Both controllers depend on this shared contract — `BattleController` writes it, `InnController` reads it — but the coupling is implicit and undocumented. If one controller renames the key and the other doesn't, the inn access guard silently breaks. This is a hidden coupling between two classes that should not know about each other's internals.

**Refactoring — Extract Constant to a Shared Class:** Define all session keys in one place:
```java
public final class SessionKeys {
    public static final String BATTLE_STATE    = "battleState";
    public static final String LAST_RESULT     = "lastBattleResult";
    public static final String REWARDS_GIVEN   = "rewardsGiven";
    public static final String INN_RECRUITS    = "innRecruits";
    public static final String HEAL_SUMMARY    = "healSummary";
}
```
Both controllers import `SessionKeys.LAST_RESULT` — the contract is now explicit and centralized.

---

## How to Find Code Smells with MetricsReloaded / MetricsTree

The deliverable requires using a metrics tool to justify code smell detection. Here is a step-by-step guide for **IntelliJ IDEA**, which your project uses.

### Option A: MetricsReloaded (Recommended for this project)

**Install:**
1. Open IntelliJ → `File → Settings → Plugins`
2. Search for **MetricsReloaded**, install it, restart IntelliJ

**Run:**
1. Open your project in IntelliJ
2. From the menu: `Analyze → Calculate Metrics...`
3. Select **Whole project** in the scope dropdown
4. Click **OK**

**Key metrics to look at and what they reveal:**

| Metric | Abbreviation | What to look for | Smell it reveals |
|---|---|---|---|
| Weighted Methods per Class | WMC | High values (>20) | Large Class / God Class |
| Lines of Code | LOC | Methods >30 lines | Long Method |
| Cyclomatic Complexity | CC | Methods >10 | Long Method, Switch Statements |
| Coupling Between Objects | CBO | Classes >7 dependencies | Feature Envy, inappropriate coupling |
| Lack of Cohesion in Methods | LCOM | High values | Large Class, misplaced responsibilities |
| Depth of Inheritance | DIT | >4 | Speculative generality |
| Number of Parameters | NP | Methods >4 params | Long Parameter List |

**Interpreting your results:**
- `BattleServiceImpl` will show **high WMC and LOC** → confirms Smell 7 (Large Class)
- `BattleController.result()` and `InnController.innPage()` will show **high CC** → confirms Smells 4 and 6 (Feature Envy, Long Method)
- `decideEnemyAction()` will show **very high CC** (every `case` adds 1) → confirms Smell 5 (Switch on String)

### Option B: MetricsTree

**Install:**
1. `File → Settings → Plugins` → Search **MetricsTree** → Install → Restart

**Run:**
1. Right-click the project root in the Project panel
2. Select `Analyze → MetricsTree → Calculate Metrics for Project`
3. Results appear in a tree view organized by package → class → method

**MetricsTree also supports Lanza-Marinescu quality metrics**, which map directly to well-known refactoring candidates:
- **God Class** detector: high WMC + high TCC (Tight Class Cohesion)
- **Feature Envy** detector: high LAA (Locality of Attribute Accesses)
- **Data Class** detector: low WMC + high number of accessors

### How to produce the metrics table for your report

Export from MetricsReloaded: `Analyze → Calculate Metrics → Export as CSV`, then paste into your SDD table. Structure it as:

```
Package                    | Class               | Method              | LOC | CC | CBO | WMC
battle.impl                | BattleServiceImpl   | —                   | 532 | —  | 8   | 45
battle.impl                | BattleServiceImpl   | decideEnemyAction   | 40  | 12 | —   | —
battle.impl                | BattleServiceImpl   | applyBattleLoss     | 28  | 4  | —   | —
controller                 | BattleController    | result              | 55  | 8  | —   | —
controller                 | InnController       | innPage             | 60  | 10 | —   | —
```

Use these numbers directly in your code smell justifications: *"Method `decideEnemyAction()` has a Cyclomatic Complexity of 12 and 40 LOC, indicating a Switch Statement smell."*

---

## Contributors

| Name | GitHub |
|---|---|
| *(Add your names here)* | *(Add links)* |

---

*CSSD2203 / DIGT3141 — Winter 2026 Group Project*  
*Algonquin College*