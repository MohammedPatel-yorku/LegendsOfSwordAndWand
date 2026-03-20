package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.dto.response.ProfileInfo;
import com.university.project.legendsofswordandwand.service.user.IProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * MVC controller handling requests for the user profile page.
 */
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IProfileService profileService;

    /**
     * Serves the profile page, populating the model with the current user's profile information.
     *
     * @param authentication the current user's authentication
     * @param model          the Spring MVC model
     * @return the logical view name for the profile page, or a redirect to login if unauthenticated
     */
    @GetMapping
    public String profilePage(Authentication authentication, Model model) {

        if (authentication == null) return "redirect:/login";

        ProfileInfo profile = profileService.getProfile(authentication.getName());
        model.addAttribute("profile", profile);

        return "profile/profile";
    }
}