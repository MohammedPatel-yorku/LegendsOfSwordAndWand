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

## Contributors

| Name             | GitHub |
|------------------|---|
| *Mohammed Patel* | *https://github.com/MohammedPatel-yorku* |

---

*CSSD2203 — Winter 2026 Group Project*  
*York University*

Report Generated with the use of generative AI