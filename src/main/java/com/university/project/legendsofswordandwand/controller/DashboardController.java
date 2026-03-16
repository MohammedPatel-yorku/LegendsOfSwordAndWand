package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.dto.response.DashboardInfo;
import com.university.project.legendsofswordandwand.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

  private final IUserService userService;

  @GetMapping("/dashboard")
  public String dashboardPage(Authentication authentication, Model model) {

    if (authentication == null) return "redirect:/login";

    DashboardInfo info = userService.getDashboardInfo(authentication.getName());
    model.addAttribute("username", info.username());
    model.addAttribute("hasParty", info.hasParty());
    model.addAttribute("hasCampaign", info.hasCampaign());
    model.addAttribute("partySize", info.partySize());
    model.addAttribute("cumulativeLevel", info.cumulativeLevel());
    model.addAttribute("gold", info.gold());

    return "dashboard";
  }
}
