package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.enums.HeroClass;

public interface IHeroService {

  void createBaseHeroForParty(Long partyId, String heroName, HeroClass heroClass);
}
