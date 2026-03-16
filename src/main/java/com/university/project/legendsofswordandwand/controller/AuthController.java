package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.dto.request.RegisterRequest;
import com.university.project.legendsofswordandwand.service.auth.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

  private final IAuthService authService;

  @GetMapping("/login")
  public String loginPage() {
    return "auth/login";
  }

  @GetMapping("/register")
  public String registerPage() {
    return "auth/register";
  }

  @PostMapping("/register")
  public String register(RegisterRequest request, RedirectAttributes redirectAttributes) {

    try {
      authService.register(request);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/register";
    }

    return "redirect:/login";
  }
}
