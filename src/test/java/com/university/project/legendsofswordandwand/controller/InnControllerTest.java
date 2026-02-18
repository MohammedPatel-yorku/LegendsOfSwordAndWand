package com.university.project.legendsofswordandwand.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.university.project.legendsofswordandwand.service.InnService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InnControllerTest {

  @Mock private InnService innService;

  @InjectMocks private InnController innController;

  // Enter inn
  @Test
  void enterInn_shouldReturnInnView() {
    Long campaignId = 1L;

    when(innService.loadInnView(campaignId)).thenReturn("Inn loaded successfully");

    String result = innController.enterInn(campaignId);

    assertEquals("Inn loaded successfully", result);
    verify(innService, times(1)).loadInnView(campaignId);
  }

  // Buy item
  @Test
  void buyItem_whenSuccessful_shouldReturnSuccessMessage() {
    Long campaignId = 1L;
    Long itemId = 2L;

    when(innService.purchaseItem(campaignId, itemId)).thenReturn(true);

    String result = innController.buyItem(campaignId, itemId);

    assertEquals("Item purchase successful.", result);
    verify(innService).purchaseItem(campaignId, itemId);
  }

  // Fail to buy item
  @Test
  void buyItem_whenFailed_shouldReturnFailureMessage() {
    Long campaignId = 1L;
    Long itemId = 2L;

    when(innService.purchaseItem(campaignId, itemId)).thenReturn(false);

    String result = innController.buyItem(campaignId, itemId);

    assertEquals("Item purchase failed.", result);
    verify(innService).purchaseItem(campaignId, itemId);
  }

  // Recruit hero
  @Test
  void recruitHero_whenSuccessful_shouldReturnSuccessMessage() {
    Long campaignId = 1L;
    Long heroId = 3L;

    when(innService.recruitHero(campaignId, heroId)).thenReturn(true);

    String result = innController.recruitHero(campaignId, heroId);

    assertEquals("Hero recruited successfully.", result);
    verify(innService).recruitHero(campaignId, heroId);
  }

  // Fail to recruit hero
  @Test
  void recruitHero_whenFailed_shouldReturnFailureMessage() {
    Long campaignId = 1L;
    Long heroId = 3L;

    when(innService.recruitHero(campaignId, heroId)).thenReturn(false);

    String result = innController.recruitHero(campaignId, heroId);

    assertEquals("Hero recruitment failed.", result);
    verify(innService).recruitHero(campaignId, heroId);
  }

  // Exit inn
  @Test
  void exitInn_shouldReturnServiceResponse() {
    Long campaignId = 1L;

    when(innService.exitInn(campaignId)).thenReturn("Proceeding to next room");

    String result = innController.exitInn(campaignId);

    assertEquals("Proceeding to next room", result);
    verify(innService).exitInn(campaignId);
  }
}
