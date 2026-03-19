package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.service.user.IProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rankings")
@RequiredArgsConstructor
public class RankingsController {

  private final IProfileService profileService;

  @GetMapping
  public String rankingsPage(Authentication authentication, Model model) {
    if (authentication == null) return "redirect:/login";
    model.addAttribute("entries", profileService.getHallOfFame());
    model.addAttribute("username", authentication.getName());
    return "rankings";
  }
}
