package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.service.CampaignService;
import com.university.project.legendsofswordandwand.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/campaign")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final UserService userService;

    @PostMapping("/start")
    public Campaign startCampaign(@RequestParam Long userId, @RequestParam String heroName, @RequestParam String heroClass){
        User user = userService.findById(userId).orElseThrow(() -> new RuntimeException("Usr not found"));

        return campaignService.startNewCampaign(user, heroName, heroClass);
    }
}
