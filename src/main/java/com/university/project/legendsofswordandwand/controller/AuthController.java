package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.dto.request.RegisterRequest;
import com.university.project.legendsofswordandwand.service.auth.IAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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

  @PostMapping("/logout-check")
  public String logoutCheck(HttpSession session, RedirectAttributes redirectAttributes) {
    BattleState state = (BattleState) session.getAttribute("battleState");
    if (state != null && !state.isOver()) {
      redirectAttributes.addFlashAttribute("error", "You cannot log out during a battle.");
      return "redirect:/battle";
    }
    SecurityContextHolder.clearContext();
    session.invalidate();
    return "redirect:/login?logout";
  }
}
