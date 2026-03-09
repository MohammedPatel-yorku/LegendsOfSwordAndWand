package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.dto.DashboardInfo;
import com.university.project.legendsofswordandwand.dto.RegisterRequest;
import com.university.project.legendsofswordandwand.service.IAuthService;
import com.university.project.legendsofswordandwand.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

  private final IAuthService authService;
  private final IUserService userService;

  @GetMapping("/login")
  public String loginPage() {
    return "login";
  }

  @GetMapping("/register")
  public String registerPage() {
    return "register";
  }

  @PostMapping("/register")
  public String register(RegisterRequest request) {

    try {
      authService.register(request);
    } catch (Exception e) {
      return "redirect:/register?error";
    }

    return "redirect:/login";
  }

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
