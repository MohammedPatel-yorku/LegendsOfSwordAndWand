package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.service.IInnService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/campaign/{campaignId}/inn")
public class InnController {

  private final IInnService innService;

  /** Enter the inn for a specific campaign. */
  @PostMapping("/enter")
  public String enterInn(@PathVariable Long campaignId) {
    return innService.loadInnView(campaignId);
  }

  /** Buy an item for the campaign's party. */
  @PostMapping("/buy")
  public String buyItem(@PathVariable Long campaignId, @RequestParam Long itemId) {
    boolean success = innService.purchaseItem(campaignId, itemId);
    return success ? "Item purchase successful." : "Item purchase failed.";
  }

  /** Recruit a hero for the campaign's party. */
  @PostMapping("/recruit")
  public String recruitHero(@PathVariable Long campaignId, @RequestParam Long heroId) {
    boolean success = innService.recruitHero(campaignId, heroId);
    return success ? "Hero recruited successfully." : "Hero recruitment failed.";
  }

  /** Exit the inn for this campaign. */
  @PostMapping("/exit")
  public String exitInn(@PathVariable Long campaignId) {
    return innService.exitInn(campaignId);
  }
}
