package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.service.user.IProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** MVC controller handling requests for the hall of fame rankings page. */
@Controller
@RequestMapping("/rankings")
@RequiredArgsConstructor
public class RankingsController {

  private final IProfileService profileService;

  /**
   * Serves the rankings page, populating the model with hall of fame entries and the current user's
   * username.
   *
   * @param authentication the current user's authentication
   * @param model the Spring MVC model
   * @return the logical view name for the rankings page, or a redirect to login if unauthenticated
   */
  @GetMapping
  public String rankingsPage(Authentication authentication, Model model) {
    if (authentication == null) return "redirect:/login";
    model.addAttribute("entries", profileService.getHallOfFame());
    model.addAttribute("username", authentication.getName());
    return "rankings";
  }
}
