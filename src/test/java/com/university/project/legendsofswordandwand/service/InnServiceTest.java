package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.repository.InventoryRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InnServiceTest {

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InnService innService;

    // loadInnView properly calls reviveAndHealParty and returns correct message
    @Test
    void loadInnView_shouldReturnPartyStatusMessage() {
        Long partyId = 1L;
        Party party = new Party();

        when(partyRepository.findById(partyId))
                .thenReturn(Optional.of(party));

        String result = innService.loadInnView(partyId);

        assertEquals("Party status displayed.", result);
        verify(partyRepository).findById(partyId);
    }

    // reviveAndHealParty throws exception if party not found
    @Test
    void reviveAndHealParty_whenPartyNotFound_shouldThrowException() {
        Long partyId = 99L;

        when(partyRepository.findById(partyId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> innService.reviveAndHealParty(partyId));

        assertEquals("Party not found.", exception.getMessage());
        verify(partyRepository).findById(partyId);
    }

    // purchaseItem should return true
    @Test
    void purchaseItem_shouldReturnTrue() {
        boolean result = innService.purchaseItem(1L);

        assertTrue(result);
    }

    // recruitHero should return true
    @Test
    void recruitHero_shouldReturnTrue() {
        boolean result = innService.recruitHero(1L, 2L);

        assertTrue(result);
    }

    // exitInn should return correct message
    @Test
    void exitInn_shouldReturnNextRoomMessage() {
        String result = innService.exitInn(1L);

        assertEquals("Proceed to next room.", result);
    }
}
