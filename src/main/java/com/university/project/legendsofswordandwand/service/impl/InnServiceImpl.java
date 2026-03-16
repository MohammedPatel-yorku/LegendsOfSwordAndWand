package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Item;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.HeroRepository;
import com.university.project.legendsofswordandwand.repository.ItemRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.service.IInnService;
import com.university.project.legendsofswordandwand.service.IInventoryService;
import com.university.project.legendsofswordandwand.service.IPartyService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** InventoryService handles business logic for Inventory objects. */
@Service
@RequiredArgsConstructor
public class InnServiceImpl implements IInnService {

  private final IPartyService partyService;
  private final IInventoryService inventoryService;
  private final ItemRepository itemRepository;
  private final Random random = new Random();
  private final PartyRepository partyRepository;
  private final HeroRepository heroRepository;

  @Override
  public void loadInnView(Long campaignId) {
    partyService.reviveAndHealParty(campaignId);
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

    Party party = partyService.getActiveParty(campaignId);

    if (party.getHeroes().size() >= 5) return Collections.emptyList();

    int count = 1 + random.nextInt(3);
    HeroClass[] classes = HeroClass.values();

    return java.util.stream.IntStream.range(0, count)
        .mapToObj(
            i -> {
              HeroClass heroClass = classes[random.nextInt(classes.length)];
              int level = 1 + random.nextInt(4);

              return Hero.builder()
                  .name(generateRecruitName())
                  .startingClass(heroClass)
                  .party(party)
                  .build();
            })
        .toList();
  }

  @Override
  public boolean purchaseItem(Long campaignId, Long itemId) {

    Party party = partyService.getActiveParty(campaignId);
    Item item =
        itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));

    if (party.getGold() < item.getCost()) return false;

    party.setGold(party.getGold() + item.getCost());
    partyRepository.save(party);
    return true;
  }

  @Override
  public boolean recruitHero(Long campaignId, Long heroId) {

    Party party = partyService.getActiveParty(campaignId);
    if (party.getHeroes().size() >= 5) throw new RuntimeException("Party is full");

    Hero hero =
        heroRepository.findById(heroId).orElseThrow(() -> new RuntimeException("Hero not found"));

    int cost = hero.getLevel() == 1 ? 0 : hero.getLevel() * 200;
    if (party.getGold() < cost) throw new RuntimeException("Not enough gold");

    party.setGold(party.getGold() - cost);
    party.getHeroes().add(hero);
    partyRepository.save(party);
    return true;
  }

  private String generateRecruitName() {

    String[] names = {
      "Aldric", "Seraphine", "Corvus", "Mira", "Theron",
      "Isolde", "Gareth", "Lyra", "Dorian", "Elara"
    };

    return names[random.nextInt(names.length)];
  }
}
