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
    Long partyId = 1L;

    when(innService.loadInnView(partyId)).thenReturn("Inn loaded successfully");

    String result = innController.enterInn(partyId);

    assertEquals("Inn loaded successfully", result);
    verify(innService, times(1)).loadInnView(partyId);
  }

  // Buy item
  @Test
  void buyItem_whenSuccessful_shouldReturnSuccessMessage() {
    Long itemId = 2L;

    when(innService.purchaseItem(itemId)).thenReturn(true);

    String result = innController.buyItem(itemId);

    assertEquals("Item purchase successful.", result);
    verify(innService).purchaseItem(itemId);
  }

  // Fail to buy item
  @Test
  void buyItem_whenFailed_shouldReturnFailureMessage() {
    Long itemId = 2L;

    when(innService.purchaseItem(itemId)).thenReturn(false);

    String result = innController.buyItem(itemId);

    assertEquals("Item purchase failed.", result);
    verify(innService).purchaseItem(itemId);
  }

  // Recruit hero
  @Test
  void recruitHero_whenSuccessful_shouldReturnSuccessMessage() {
    Long partyId = 1L;
    Long heroId = 3L;

    when(innService.recruitHero(partyId, heroId)).thenReturn(true);

    String result = innController.recruitHero(partyId, heroId);

    assertEquals("Hero recruited successfully.", result);
    verify(innService).recruitHero(partyId, heroId);
  }

  // Fail to recruit hero
  @Test
  void recruitHero_whenFailed_shouldReturnFailureMessage() {
    Long partyId = 1L;
    Long heroId = 3L;

    when(innService.recruitHero(partyId, heroId)).thenReturn(false);

    String result = innController.recruitHero(partyId, heroId);

    assertEquals("Hero recruitment failed.", result);
    verify(innService).recruitHero(partyId, heroId);
  }

  // Exit inn
  @Test
  void exitInn_shouldReturnServiceResponse() {
    Long partyId = 1L;

    when(innService.exitInn(partyId)).thenReturn("Proceeding to next room");

    String result = innController.exitInn(partyId);

    assertEquals("Proceeding to next room", result);
    verify(innService).exitInn(partyId);
  }
}
