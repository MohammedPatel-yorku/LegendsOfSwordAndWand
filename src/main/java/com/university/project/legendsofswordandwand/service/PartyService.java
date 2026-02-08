package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Party Object Service class. */
@Service
@Transactional
@RequiredArgsConstructor
public class PartyService {

  private final PartyRepository partyRepository;
  private final UserRepository userRepository;

  /**
   * Creates a new Party for User to use in a newly created Campaign.
   *
   * @param userId ID of User to create Party for
   * @return Created and saved Party
   */
  public Party createPartyForUser(Long userId) {
    User owner =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));

    Party party = Party.builder().owner(owner).build();
    owner.getParties().add(party);

    return partyRepository.save(party);
  }
}
