package com.university.project.legendsofswordandwand.service.battle.impl;

import com.university.project.legendsofswordandwand.battle.HeroStatCalculator;
import com.university.project.legendsofswordandwand.model.*;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
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
  private final HeroStatCalculator heroStatCalculator;
  private final CampaignRepository campaignRepository;

  @Override
  public List<String> loadInnView(Long campaignId) {
    return partyManagementService.reviveAndHealPartyWithSummary(campaignId);
  }

  @Override
  public List<Item> getShopItems() {
    List<Item> items = itemRepository.findAll();
    if (items.isEmpty()) {
      items =
          itemRepository.saveAll(
              Arrays.asList(
                  Item.bread(),
                  Item.cheese(),
                  Item.steak(),
                  Item.water(),
                  Item.juice(),
                  Item.wine(),
                  Item.elixir()));
    }
    return items;
  }

  @Override
  public List<Hero> getAvailableRecruits(Long campaignId) {
    cleanupTemporaryRecruits(campaignId);

    Party party = partyManagementService.getActiveParty(campaignId);

    // Spec: recruits only available in first 10 rooms
    Campaign campaign =
        campaignRepository
            .findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
    if (campaign.getCurrentRoom() > 10) return Collections.emptyList();

    long permanentCount = party.getHeroes().stream().filter(h -> !h.isTemporary()).count();
    if (permanentCount >= 5) return Collections.emptyList();

    int count = 1 + random.nextInt(3);
    HeroClass[] classes = HeroClass.values();

    List<Hero> recruits =
        java.util.stream.IntStream.range(0, count)
            .mapToObj(
                i -> {
                  HeroClass cls = classes[random.nextInt(classes.length)];
                  // Spec: random level between 1-4
                  int level = 1 + random.nextInt(4);

                  Hero h =
                      Hero.builder()
                          .name(generateRecruitName())
                          .startingClass(cls)
                          .party(party)
                          .build();

                  switch (cls) {
                    case ORDER -> h.setOrderLevels(level);
                    case CHAOS -> h.setChaosLevels(level);
                    case WARRIOR -> h.setWarriorLevels(level);
                    case MAGE -> h.setMageLevels(level);
                  }

                  // Apply base level gains for levels 2-4
                  for (int lvl = 1; lvl < level; lvl++) {
                    heroStatCalculator.applyLevelUp(h, cls);
                  }

                  // Apply class bonus for level 1
                  heroStatCalculator.applyClassBonusOnly(h, cls);

                  h.setLevel(level);
                  return h;
                })
            .toList();

    recruits.forEach(
        h -> {
          h.setTemporary(true);
          heroService.save(h);
        });

    return recruits;
  }

  @Override
  public boolean purchaseItem(Long campaignId, Long itemId) {
    Party party = partyManagementService.getActiveParty(campaignId);
    Item item =
        itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));

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

    long permanentCount = party.getHeroes().stream().filter(h -> !h.isTemporary()).count();
    if (permanentCount >= 5) throw new RuntimeException("Party is full");

    Hero hero =
        heroService.findById(heroId).orElseThrow(() -> new RuntimeException("Hero not found"));

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
    List<Hero> temps = party.getHeroes().stream().filter(Hero::isTemporary).toList();
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
