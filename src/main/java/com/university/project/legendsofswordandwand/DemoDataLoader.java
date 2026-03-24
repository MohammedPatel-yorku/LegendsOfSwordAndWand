package com.university.project.legendsofswordandwand;

import com.university.project.legendsofswordandwand.model.*;
import com.university.project.legendsofswordandwand.model.enums.*;
import com.university.project.legendsofswordandwand.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the in-memory H2 database with demo data when the {@code demo} profile is active.
 *
 * <p>Creates two users (player1 and player2) each with saved parties containing
 * heroes at various levels and hybrid classes, suitable for immediately testing
 * all use cases including PvP without manual setup.
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
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        seedItems();

        User player1 = createUser("player1", "password");
        User player2 = createUser("player2", "password");

        // player1 — saved party with hybrid classes ready for PvP
        Party p1Party = buildSavedParty(player1);
        addHero(p1Party, "Aldric",   HeroClass.WARRIOR, 8,  HybridClass.KNIGHT,  12, 12, 145, 66);
        addHero(p1Party, "Seraphel", HeroClass.ORDER,   6,  HybridClass.PALADIN, 7,  13, 130, 82);
        addHero(p1Party, "Vex",      HeroClass.CHAOS,   5,  HybridClass.INVOKER, 20, 7,  125, 85);
        addGold(p1Party, 3500);

        // player2 — saved party with different hybrid spread
        Party p2Party = buildSavedParty(player2);
        addHero(p2Party, "Mira",    HeroClass.MAGE,    7,  HybridClass.WIZARD,  8,  8,  135, 96);
        addHero(p2Party, "Drak",    HeroClass.WARRIOR, 9,  HybridClass.WARLOCK, 14, 11, 150, 68);
        addHero(p2Party, "Sylvara", HeroClass.ORDER,   5,  HybridClass.PROPHET, 7,  15, 125, 90);
        addGold(p2Party, 2800);

        System.out.println("=== Demo data loaded ===");
        System.out.println("player1 / password  — 3 heroes, 1 saved party");
        System.out.println("player2 / password  — 3 heroes, 1 saved party");
        System.out.println("Both accounts ready for PvP immediately.");
    }

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

    private User createUser(String username, String password) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();
        return userRepository.save(user);
    }

    private Party buildSavedParty(User owner) {
        Party party = Party.builder().owner(owner).build();
        party.setSaved(true);
        party = partyRepository.save(party);

        Inventory inv = Inventory.builder().party(party).build();
        inventoryRepository.save(inv);

        return party;
    }

    /**
     * Adds a hero directly with preset stats to the party, bypassing the normal
     * level-up flow so we can set exact levels and hybrid classes for demo purposes.
     */
    private void addHero(Party party, String name, HeroClass startingClass,
                         int level, HybridClass hybridClass,
                         int attack, int defense, int health, int mana) {

        Hero hero = Hero.builder()
                .name(name)
                .startingClass(startingClass)
                .party(party)
                .build();

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

    private void addGold(Party party, int amount) {
        party.setGold(amount);
        partyRepository.save(party);
    }
}