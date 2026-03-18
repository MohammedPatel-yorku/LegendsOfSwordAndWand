package com.university.project.legendsofswordandwand.service.battle.impl;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Inventory;
import com.university.project.legendsofswordandwand.model.Item;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.InventoryRepository;
import com.university.project.legendsofswordandwand.repository.ItemRepository;
import com.university.project.legendsofswordandwand.service.battle.IInnService;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import com.university.project.legendsofswordandwand.service.party.IPartyManagementService;
import jakarta.transaction.Transactional;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
class InnServiceImpl implements IInnService {

  private final IPartyManagementService partyManagementService;
  private final ItemRepository itemRepository;
  private final Random random = new Random();
  private final IHeroService heroService;
  private final InventoryRepository inventoryRepository;

  @Override
  public List<String> loadInnView(Long campaignId) {
    return partyManagementService.reviveAndHealPartyWithSummary(campaignId);
  }

  @Override
  public List<Item> getShopItems() {
    List<Item> items = itemRepository.findAll();
    if (items.isEmpty()) {
      items = itemRepository.saveAll(Arrays.asList(
              Item.bread(), Item.cheese(), Item.steak(),
              Item.water(), Item.juice(), Item.wine(), Item.elixir()));
    }
    return items;
  }

  @Override
  public List<Hero> getAvailableRecruits(Long campaignId) {
    // Clean up any leftover temps first
    cleanupTemporaryRecruits(campaignId);

    Party party = partyManagementService.getActiveParty(campaignId);

    long permanentCount = party.getHeroes().stream()
            .filter(h -> !h.isTemporary())
            .count();

    if (permanentCount >= 5) return Collections.emptyList();

    int count = 1 + random.nextInt(3);
    HeroClass[] classes = HeroClass.values();

    List<Hero> recruits = java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> Hero.builder()
                    .name(generateRecruitName())
                    .startingClass(classes[random.nextInt(classes.length)])
                    .party(party)
                    .build())
            .toList();

    recruits.forEach(h -> {
      h.setTemporary(true);
      heroService.save(h);
    });

    return recruits;
  }

  @Override
  public boolean purchaseItem(Long campaignId, Long itemId) {
    Party party = partyManagementService.getActiveParty(campaignId);
    Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item not found"));

    if (party.getGold() < item.getCost()) return false;

    partyManagementService.deductGold(party.getId(), item.getCost());

    // Add to inventory
    Inventory inventory = party.getInventory();
    if (inventory == null) {
      inventory = Inventory.builder().party(party).build();
    }
    inventory.getItemIds().add(itemId);
    inventoryRepository.save(inventory);
    return true;
  }

  @Override
  public boolean recruitHero(Long campaignId, Long heroId) {
    Party party = partyManagementService.getActiveParty(campaignId);

    long permanentCount = party.getHeroes().stream()
            .filter(h -> !h.isTemporary())
            .count();
    if (permanentCount >= 5) throw new RuntimeException("Party is full");

    Hero hero = heroService.findById(heroId)
            .orElseThrow(() -> new RuntimeException("Hero not found"));

    int cost = hero.getLevel() == 1 ? 0 : hero.getLevel() * 200;
    if (party.getGold() < cost) throw new RuntimeException("Not enough gold");

    // Mark permanent before saving so they survive next inn visit cleanup
    hero.setTemporary(false);
    heroService.save(hero);

    partyManagementService.deductGold(party.getId(), cost);
    return true;
  }

  @Override
  public void cleanupTemporaryRecruits(Long campaignId) {
    Party party = partyManagementService.getActiveParty(campaignId);
    List<Hero> temps = party.getHeroes().stream()
            .filter(Hero::isTemporary)
            .toList();
    temps.forEach(h -> heroService.delete(h.getId()));
  }

  private String generateRecruitName() {
    String[] names = {
            "Aldric", "Seraphine", "Corvus", "Mira", "Theron",
            "Isolde", "Gareth", "Lyra", "Dorian", "Elara"
    };
    return names[random.nextInt(names.length)];
  }
}