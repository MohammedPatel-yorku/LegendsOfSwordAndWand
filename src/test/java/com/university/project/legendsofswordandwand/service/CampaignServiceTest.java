package com.university.project.legendsofswordandwand.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CampaignServiceTest {

  @Mock CampaignRepository campaignRepository;
  @Mock UserRepository userRepository;
  @Mock PartyService partyService;
  @Mock HeroService heroService;

  @InjectMocks CampaignService campaignService;

  private User mockUser;
  private Party mockParty;

  @BeforeEach
  void setUp() {

    mockUser = User.builder().username("testUser").password("password").build();

    mockParty = Party.builder().build();
  }

  // TC-CS-01
  @Test
  void startNewCampaign_validUsername_createsCampaign() {

    when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
    when(partyService.createPartyForUser(any())).thenReturn(mockParty);
    when(campaignRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    Campaign result = campaignService.startNewCampaign("testUser", "Arthas", HeroClass.WARRIOR);

    assertNotNull(result);
    assertTrue(result.isActive());
    assertEquals(1, result.getCurrentRoom());
    verify(heroService).createBaseHeroForParty(any(), eq("Arthas"), eq(HeroClass.WARRIOR));
  }

  // TS-CS-02
  @Test
  void startNewCampaign_unknownUsername_throwsException() {

    when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

    assertThrows(
        RuntimeException.class,
        () -> campaignService.startNewCampaign("ghost", "Arthas", HeroClass.WARRIOR));
  }

  // TS-CS-03
  @Test
  void startNewCampaign_returnsRepositoryResult() {

    when(campaignRepository.existsActiveCampaignByOwnerId(1L)).thenReturn(true);

    assertTrue(campaignService.hasActiveCampaign(1L));
  }

  // TS-CS-04
  @Test
  void startNewCampaign_campaignStartsAtRoomOne() {

    when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
    when(partyService.createPartyForUser(any())).thenReturn(mockParty);
    when(campaignRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    Campaign result = campaignService.startNewCampaign("testUser", "Gandalf", HeroClass.ORDER);

    assertEquals(1, result.getCurrentRoom());
  }
}
