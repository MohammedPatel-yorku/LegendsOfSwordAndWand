package com.university.project.legendsofswordandwand.service.battle;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Item;
import java.util.List;

public interface IInnService {

  List<String> loadInnView(Long campaignId);

  List<Item> getShopItems();

  List<Hero> getAvailableRecruits(Long campaignId);

  boolean purchaseItem(Long campaignId, Long itemId);

  boolean recruitHero(Long campaignId, Long heroId);

  void cleanupTemporaryRecruits(Long campaignId);
}
