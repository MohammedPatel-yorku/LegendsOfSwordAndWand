package com.university.project.legendsofswordandwand.service;

public interface IInnService {

  String loadInnView(Long campaignId);

  boolean purchaseItem(Long campaignId, Long itemId);

  boolean recruitHero(Long campaignId, Long heroId);

  String exitInn(Long campaignId);
}
