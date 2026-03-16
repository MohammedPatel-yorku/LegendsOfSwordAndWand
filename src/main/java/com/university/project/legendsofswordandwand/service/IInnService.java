package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Item;
import java.util.List;

public interface IInnService {

  void loadInnView(Long campaignId);

  List<Item> getShopItems();

  List<Hero> getAvailableRecruits(Long campaignId);

  boolean purchaseItem(Long campaignId, Long itemId);

  boolean recruitHero(Long campaignId, Long heroId);
}
