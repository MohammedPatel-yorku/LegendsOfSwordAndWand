package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Rest Controller class mapped with '/campaign' to handle campaign requests. */
@RestController
@RequestMapping("/campaign")
@RequiredArgsConstructor
public class CampaignController {

  private final CampaignService campaignService;

  /**
   * Starts new campaign, mapped to POST '/start'.
   *
   * @param userId ID of User that Campaign belongs to
   * @param heroName Name to give to starting Hero
   * @param heroClass Hero Class to assign to starting Hero
   * @return New Campaign Object
   */
  @PostMapping("/start")
  public Campaign startCampaign(
      @RequestParam Long userId, @RequestParam String heroName, @RequestParam HeroClass heroClass) {
    return campaignService.startNewCampaign(userId, heroName, heroClass);
  }
}
