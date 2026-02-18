package com.university.project.legendsofswordandwand.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InnServiceTest {

    @Mock
    private PartyService partyService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InnService innService;

    // loadInnView should call reviveAndHealParty and return correct message
    @Test
    void loadInnView_shouldReturnPartyStatusMessage() {
        Long campaignId = 1L;

        String result = innService.loadInnView(campaignId);

        assertEquals("Party status displayed.", result);
        verify(partyService).reviveAndHealParty(campaignId);
    }

    // reviveAndHealParty should throw exception if PartyService throws exception
    @Test
    void reviveAndHealParty_whenPartyNotFound_shouldThrowException() {
        Long campaignId = 99L;

        doThrow(new RuntimeException("Party not found."))
                .when(partyService).reviveAndHealParty(campaignId);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> innService.loadInnView(campaignId));

        assertEquals("Party not found.", exception.getMessage());
        verify(partyService).reviveAndHealParty(campaignId);
    }

    // purchaseItem should return true
    @Test
    void purchaseItem_shouldReturnTrue() {
        Long campaignId = 1L;
        Long itemId = 2L;

        when(inventoryService.purchaseItem(campaignId, itemId))
                .thenReturn(true);

        boolean result = innService.purchaseItem(campaignId, itemId);

        assertTrue(result);
        verify(inventoryService).purchaseItem(campaignId, itemId);
    }

    // recruitHero should return true
    @Test
    void recruitHero_shouldReturnTrue() {
        Long campaignId = 1L;
        Long heroId = 2L;

        boolean result = innService.recruitHero(campaignId, heroId);

        assertTrue(result);
        verify(partyService).recruitHero(campaignId, heroId);
    }

    // exitInn should return correct message
    @Test
    void exitInn_shouldReturnNextRoomMessage() {
        String result = innService.exitInn(1L);

        assertEquals("Proceed to next room.", result);
    }
}
