package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class PartyService {

  private final PartyRepository partyRepository;

  public Party createNewParty(User owner, Hero leader) {
    Party party = new Party();

    party.setOwner(owner);
    party.addHero(leader);
    leader.setParty(party);

    return partyRepository.save(party);
  }
}
