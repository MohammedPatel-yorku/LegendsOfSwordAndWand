package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CampaignService {

  private final CampaignRepository campaignRepository;
  private final HeroService heroService;

  public Campaign startNewCampaign(User owner, String heroClass, String heroName) {

    Campaign campaign = new Campaign(owner);
    campaignRepository.save(campaign);

    heroService.createHeroForUser(owner.getId(), heroName, heroClass);

    return campaign;
  }
}
