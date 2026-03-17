package com.university.project.legendsofswordandwand.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.HeroRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HeroServiceTest {

  @Mock HeroRepository heroRepository;
  @Mock PartyRepository partyRepository;

  @InjectMocks HeroService heroService;

  // TC-HS-01
  @Test
  void createBaseHero_warrior_hasCorrectBaseStats() {

    Party party = Party.builder().heroes(new ArrayList<>()).build();

    when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
    when(heroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    heroService.createBaseHeroForParty(1L, "Conan", HeroClass.WARRIOR);

    verify(heroRepository)
        .save(
            argThat(
                hero ->
                    hero.getName().equals("Conan")
                        && hero.getHeroClass() == HeroClass.WARRIOR
                        && hero.getLevel() == 1
                        && hero.getHealth() == 100
                        && hero.getDefense() == 5
                        && hero.getMana() == 50));
  }

  // TS-HS-02
  @Test
  void createBaseHero_invalidPartyId_throwsException() {

    when(partyRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        RuntimeException.class,
        () -> heroService.createBaseHeroForParty(99L, "Hero", HeroClass.MAGE));
  }

  // TC-HS-03
  @Test
  void createBaseHero_heroAddedToParty() {

    Party party = Party.builder().heroes(new ArrayList<>()).build();

    when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
    when(heroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    heroService.createBaseHeroForParty(1L, "Merlin", HeroClass.MAGE);

    assertEquals(1, party.getHeroes().size());
  }
}
