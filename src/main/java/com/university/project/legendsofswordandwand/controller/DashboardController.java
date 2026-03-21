package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.dto.response.DashboardInfo;
import com.university.project.legendsofswordandwand.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** MVC controller handling requests for the user dashboard. */
@Controller
@RequiredArgsConstructor
public class DashboardController {

  private final IUserService userService;

  /**
   * Serves the dashboard page, populating the model with the current user's summary information.
   *
   * <p>Retrieves dashboard data via {@link IUserService} and exposes the username, party status,
   * campaign status, party size, cumulative level, and gold to the view.
   *
   * @param authentication the current user's authentication
   * @param model the Spring MVC model
   * @return the logical view name for the dashboard, or a redirect to login if unauthenticated
   */
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
