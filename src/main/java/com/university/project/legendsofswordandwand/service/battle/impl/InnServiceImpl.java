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

/**
 * Default implementation of {@link IInnService}, handling inn visit logic including party healing,
 * shop purchases, hero recruitment, and temporary recruit cleanup.
 */
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

  /**
   * Revives and heals all heroes in the party and returns a summary of the healing applied.
   *
   * @param campaignId the ID of the campaign whose party to heal
   * @return a list of log strings describing the healing applied to each hero
   */
  @Override
  public List<String> loadInnView(Long campaignId) {
    return partyManagementService.reviveAndHealPartyWithSummary(campaignId);
  }

  /**
   * Returns all available shop items, seeding the database with default items if none exist.
   *
   * @return the list of available {@link Item}s for purchase
   */
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

  /**
   * Generates and persists a list of temporary hero recruits available for the current inn visit.
   *
   * <p>Recruits are only available within the first 10 rooms and when the party has fewer than 5
   * permanent heroes. Between 1 and 3 recruits are generated with random classes and levels between
   * 1 and 4. Stats are computed by applying level-up and class bonus logic. All recruits are marked
   * as temporary and saved to the database.
   *
   * <p>Any leftover temporary recruits from a previous visit are cleaned up first.
   *
   * @param campaignId the ID of the campaign whose party to generate recruits for
   * @return the list of generated temporary {@link Hero} recruits, or an empty list if unavailable
   */
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
                  if (level > 1) {
                    int prevThreshold =
                        h.getExperienceToNextLevel() - (500 + 75 * level + 20 * level * level);
                    h.setExperience(Math.max(0, prevThreshold));
                  }
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

  /**
   * Purchases a shop item for the party's campaign inventory, deducting the item's cost from gold.
   *
   * @param campaignId the ID of the campaign whose party is making the purchase
   * @param itemId the ID of the {@link Item} to purchase
   * @return {@code true} if the purchase succeeded, {@code false} if the party had insufficient
   *     gold
   * @throws RuntimeException if the item is not found
   */
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

  /**
   * Recruits a temporary hero into the party permanently, deducting the recruitment cost from gold.
   *
   * <p>Level 1 recruits are free. Higher-level recruits cost {@code (level - 1) * 200} gold. The
   * hero is marked as permanent before saving to ensure they survive the next cleanup.
   *
   * @param campaignId the ID of the campaign whose party is recruiting
   * @param heroId the ID of the temporary {@link Hero} to recruit
   * @return {@code true} on success
   * @throws RuntimeException if the party is full, the hero is not found, or gold is insufficient
   */
  @Override
  public boolean recruitHero(Long campaignId, Long heroId) {
    Party party = partyManagementService.getActiveParty(campaignId);

    long permanentCount = party.getHeroes().stream().filter(h -> !h.isTemporary()).count();
    if (permanentCount >= 5) throw new RuntimeException("Party is full");

    Hero hero =
        heroService.findById(heroId).orElseThrow(() -> new RuntimeException("Hero not found"));

    int cost = hero.getLevel() == 1 ? 0 : (hero.getLevel() - 1) * 200;
    if (party.getGold() < cost) throw new RuntimeException("Not enough gold");

    // Mark permanent before saving so they survive next inn visit cleanup
    hero.setTemporary(false);
    heroService.save(hero);

    partyManagementService.deductGold(party.getId(), cost);
    return true;
  }

  /**
   * Deletes all temporary (unrecruited) heroes from the party.
   *
   * <p>Should be called at the end of each inn visit to remove heroes the player chose not to
   * recruit.
   *
   * @param campaignId the ID of the campaign whose temporary recruits to clean up
   */
  @Override
  public void cleanupTemporaryRecruits(Long campaignId) {
    Party party = partyManagementService.getActiveParty(campaignId);
    List<Hero> temps = party.getHeroes().stream().filter(Hero::isTemporary).toList();
    temps.forEach(h -> heroService.delete(h.getId()));
  }

  /**
   * Generates a random name for a recruit from a fixed pool of fantasy names.
   *
   * @return a randomly selected recruit name
   */
  private String generateRecruitName() {
    String[] names = {
      "Aldric", "Seraphine", "Corvus", "Mira", "Theron",
      "Isolde", "Gareth", "Lyra", "Dorian", "Elara"
    };
    return names[random.nextInt(names.length)];
  }
}
