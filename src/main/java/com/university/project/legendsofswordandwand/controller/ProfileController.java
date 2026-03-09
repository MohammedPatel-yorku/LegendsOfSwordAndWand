package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.dto.ProfileInfo;
import com.university.project.legendsofswordandwand.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

  private final ProfileService profileService;

  @GetMapping
  public String profilePage(Authentication authentication, Model model) {

    if (authentication == null) return "redirect:/login";

    ProfileInfo profile = profileService.getProfile(authentication.getName());
    model.addAttribute("profile", profile);

    return "profile";
  }
}
