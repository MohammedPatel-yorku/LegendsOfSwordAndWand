package com.university.project.legendsofswordandwand.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.university.project.legendsofswordandwand.dto.response.DashboardInfo;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock UserRepository userRepository;
  @Mock CampaignService campaignService;

  @InjectMocks UserService userService;

  private User mockUser;

  @BeforeEach
  void setUp() {
    mockUser = User.builder().username("testUser").password("password").build();
  }

  // TC-US-01
  @Test
  void getDashboardInfo_userWithNoParties_returnsFalseFlags() {

    when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
    when(campaignService.hasActiveCampaign(any())).thenReturn(false);

    DashboardInfo dashboardInfo = userService.getDashboardInfo("testUser");

    assertFalse(dashboardInfo.hasParty());
    assertFalse(dashboardInfo.hasCampaign());
    assertEquals("testUser", dashboardInfo.username());
  }

  // TC-US-02
  @Test
  void getDashboardInfo_unknownUser_throwsException() {

    when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> userService.getDashboardInfo("ghost"));
  }
}
