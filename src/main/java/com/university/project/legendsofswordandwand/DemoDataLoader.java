package com.university.project.legendsofswordandwand;

import com.university.project.legendsofswordandwand.model.*;
import com.university.project.legendsofswordandwand.model.enums.*;
import com.university.project.legendsofswordandwand.repository.*;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with demo data when the {@code demo} profile is active.
 *
 * <p>Creates two users (player1 and player2). player1 has:
 *
 * <ul>
 *   <li>One saved party with hybrid heroes — ready for PvP immediately
 *   <li>One active campaign at room 29 — one click from completion, with inventory items seeded
 * </ul>
 *
 * player2 has one saved party with hybrid heroes, also ready for PvP.
 *
 * <p>With the use of AI
 */
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoDataLoader implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PartyRepository partyRepository;
  private final HeroRepository heroRepository;
  private final InventoryRepository inventoryRepository;
  private final ItemRepository itemRepository;
  private final CampaignRepository campaignRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) {

    seedItems();

    User player1 = createUser("player1", "password");
    User player2 = createUser("player2", "password");

    // ── player1: saved party for PvP ────────────────────────────────────────
    Party p1SavedParty = buildParty(player1, true);
    addHero(p1SavedParty, "Aldric",   HeroClass.WARRIOR, 8, HybridClass.KNIGHT,  12, 12, 145, 66);
    addHero(p1SavedParty, "Seraphel", HeroClass.ORDER,   6, HybridClass.PALADIN,  7, 13, 130, 82);
    addHero(p1SavedParty, "Vex",      HeroClass.CHAOS,   5, HybridClass.INVOKER, 20,  7, 125, 85);
    addGold(p1SavedParty, 3500);

    // ── player1: active campaign at room 29 (one room from the end) ─────────
    Party p1CampaignParty = buildParty(player1, false);
    addHero(p1CampaignParty, "Gareth", HeroClass.WARRIOR, 9, HybridClass.KNIGHT,  14, 13, 160, 70);
    addHero(p1CampaignParty, "Lyra",   HeroClass.MAGE,    7, HybridClass.WIZARD,   9,  9, 140, 98);
    addHero(p1CampaignParty, "Orion",  HeroClass.ORDER,   6, HybridClass.PROPHET,  8, 14, 135, 88);
    addGold(p1CampaignParty, 4200);
    addInventoryItems(p1CampaignParty);

    Campaign campaign =
            Campaign.builder()
                    .owner(player1)
                    .party(p1CampaignParty)
                    .currentRoom(29)
                    .active(true)
                    .build();
    campaign.setHasVisitedInn(true);
    campaignRepository.save(campaign);

    // ── player2: saved party for PvP ────────────────────────────────────────
    Party p2Party = buildParty(player2, true);
    addHero(p2Party, "Mira",    HeroClass.MAGE,    7, HybridClass.WIZARD,   8,  8, 135, 96);
    addHero(p2Party, "Drak",    HeroClass.WARRIOR, 9, HybridClass.WARLOCK, 14, 11, 150, 68);
    addHero(p2Party, "Sylvara", HeroClass.ORDER,   5, HybridClass.PROPHET,  7, 15, 125, 90);
    addGold(p2Party, 2800);

    System.out.println("=== Demo data loaded ===");
    System.out.println("player1 / password  — PvP party + active campaign at room 29/30");
    System.out.println("player2 / password  — PvP party ready");
    System.out.println("Both accounts ready for PvP immediately.");
  }

  /** Seeds the item catalogue if it is empty. */
  private void seedItems() {
    if (itemRepository.count() > 0) return;
    itemRepository.save(Item.bread());
    itemRepository.save(Item.cheese());
    itemRepository.save(Item.steak());
    itemRepository.save(Item.water());
    itemRepository.save(Item.juice());
    itemRepository.save(Item.wine());
    itemRepository.save(Item.elixir());
  }

  /**
   * Creates and persists a new user with an encoded password.
   *
   * @param username the desired username
   * @param password the plain-text password to encode
   * @return the saved {@link User}
   */
  private User createUser(String username, String password) {
    User user =
            User.builder().username(username).password(passwordEncoder.encode(password)).build();
    return userRepository.save(user);
  }

  /**
   * Creates a party for the given user with an associated empty inventory.
   *
   * @param owner the user who owns this party
   * @param saved {@code true} for a saved PvP-eligible party; {@code false} for a campaign party
   * @return the saved {@link Party}
   */
  private Party buildParty(User owner, boolean saved) {
    Party party = Party.builder().owner(owner).build();
    party.setSaved(saved);
    party = partyRepository.save(party);
    inventoryRepository.save(Inventory.builder().party(party).build());
    return party;
  }

  /**
   * Seeds a handful of items into the campaign party's inventory so the inn inventory panel is
   * non-empty when player1 logs in at room 29.
   *
   * @param party the campaign party whose inventory to populate
   */
  private void addInventoryItems(Party party) {
    List<Inventory> inventories = inventoryRepository.findByPartyId(party.getId());
    Inventory inv =
            inventories.isEmpty()
                    ? inventoryRepository.save(Inventory.builder().party(party).build())
                    : inventories.get(0);

    // Resolve item IDs from the already-seeded catalogue
    itemRepository.findByName("Bread").ifPresent(i -> inv.getItemIds().add(i.getId()));
    itemRepository.findByName("Steak").ifPresent(i -> inv.getItemIds().add(i.getId()));
    itemRepository.findByName("Juice").ifPresent(i -> inv.getItemIds().add(i.getId()));
    itemRepository.findByName("Elixir").ifPresent(i -> inv.getItemIds().add(i.getId()));
    inventoryRepository.save(inv);
  }

  /**
   * Adds a hero directly with preset stats to the party, bypassing the normal level-up flow so
   * exact levels and hybrid classes can be set for demo purposes.
   *
   * @param party        the party to add the hero to
   * @param name         the hero's display name
   * @param startingClass the hero's base class
   * @param level        the hero's level
   * @param hybridClass  the hero's hybrid class
   * @param attack       the hero's attack stat
   * @param defense      the hero's defense stat
   * @param health       the hero's HP (also sets maxHealth)
   * @param mana         the hero's mana (also sets maxMana)
   */
  private void addHero(
          Party party,
          String name,
          HeroClass startingClass,
          int level,
          HybridClass hybridClass,
          int attack,
          int defense,
          int health,
          int mana) {

    Hero hero = Hero.builder().name(name).startingClass(startingClass).party(party).build();

    hero.setLevel(level);
    hero.setHybridClass(hybridClass);
    hero.setHybrid(true);
    hero.setPrimaryClass(startingClass);
    hero.setAttack(attack);
    hero.setDefense(defense);
    hero.setHealth(health);
    hero.setMaxHealth(health);
    hero.setMana(mana);
    hero.setMaxMana(mana);
    hero.setExperience(0);
    hero.setExperienceToNextLevel(9999);
    hero.setTemporary(false);

    heroRepository.save(hero);
    party.getHeroes().add(hero);
    partyRepository.save(party);
  }

  /**
   * Sets the gold amount on the given party.
   *
   * @param party  the party to update
   * @param amount the gold amount to set
   */
  private void addGold(Party party, int amount) {
    party.setGold(amount);
    partyRepository.save(party);
  }
}