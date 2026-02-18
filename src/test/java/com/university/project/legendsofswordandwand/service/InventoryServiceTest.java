package com.university.project.legendsofswordandwand.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.university.project.legendsofswordandwand.model.Inventory;
import com.university.project.legendsofswordandwand.repository.InventoryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

  @Mock private InventoryRepository inventoryRepository;

  @InjectMocks private InventoryService inventoryService;

  // getInventoryByParty should return list from repository
  @Test
  void getInventoryByParty_shouldReturnInventoryList() {
    Long partyId = 1L;

    List<Inventory> mockInventoryList = List.of(new Inventory(), new Inventory());

    when(inventoryRepository.findByPartyId(partyId)).thenReturn(mockInventoryList);

    List<Inventory> result = inventoryService.getInventoryByParty(partyId);

    assertEquals(2, result.size());
    verify(inventoryRepository, times(1)).findByPartyId(partyId);
  }

  // purchaseItem should return true
  @Test
  void purchaseItem_shouldReturnTrue() {
    Long partyId = 1L;
    Long itemId = 2L;

    boolean result = inventoryService.purchaseItem(partyId, itemId);

    assertTrue(result);
  }

  // saveInventory should call repository save
  @Test
  void saveInventory_shouldCallRepositorySave() {
    Inventory inventory = new Inventory();

    when(inventoryRepository.save(inventory)).thenReturn(inventory);

    Inventory result = inventoryService.saveInventory(inventory);

    assertNotNull(result);
    verify(inventoryRepository, times(1)).save(inventory);
  }

  // saveInventory should throw exception if repository fails
  @Test
  void saveInventory_whenRepositoryFails_shouldThrowException() {
    Inventory inventory = new Inventory();

    when(inventoryRepository.save(inventory)).thenThrow(new RuntimeException("Database error"));

    assertThrows(RuntimeException.class, () -> inventoryService.saveInventory(inventory));
  }
}
