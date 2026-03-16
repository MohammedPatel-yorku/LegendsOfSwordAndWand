package com.university.project.legendsofswordandwand.service.party.impl;

import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.party.IPartyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Party Object Service class. */
@Service
@Transactional
@RequiredArgsConstructor
class PartyServiceImpl implements IPartyService {

  private final PartyRepository partyRepository;
  private final UserRepository userRepository;

  /** Creates a new Party for User to use in a newly created Campaign. */
  @Override
  public Party createPartyForUser(Long userId) {
    User owner =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));

    Party party = Party.builder().owner(owner).build();

    owner.getParties().add(party);
    return partyRepository.save(party);
  }

  @Override
  public void deleteParty(Long partyId) {

    Party party =
        partyRepository
            .findById(partyId)
            .orElseThrow(() -> new RuntimeException("Party not found"));

    partyRepository.delete(party);
  }
}
