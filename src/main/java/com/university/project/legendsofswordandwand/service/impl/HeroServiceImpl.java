package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.HeroRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.service.IHeroService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Hero Object Service class. */
@Service
@Transactional
@RequiredArgsConstructor
public class HeroServiceImpl implements IHeroService {

  private final HeroRepository heroRepository;
  private final PartyRepository partyRepository;

  /**
   * Creates a new base stat (Level 1, 100 HP, 10 Attack) Hero for requesting Party.
   *
   * @param partyId ID of Party to create Hero Object for
   * @param selectedHeroName Name to assign to Hero
   * @param selectedHeroClass Hero Class to assign to Hero
   */
  @Override
  public void createBaseHeroForParty(
      Long partyId, String selectedHeroName, HeroClass selectedHeroClass) {
    Party party =
        partyRepository
            .findById(partyId)
            .orElseThrow(() -> new RuntimeException("Party Not Found"));

    Hero hero =
        Hero.builder()
            .name(selectedHeroName)
            .primaryClass(selectedHeroClass)
            .level(1)
            .health(100)
            .attack(5)
            .party(party)
            .build();

    party.getHeroes().add(hero);

    heroRepository.save(hero);
  }
}
