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

/**
 * MVC controller handling authentication-related requests, including login,
 * registration, and logout.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    /**
     * Serves the login page.
     *
     * @return the logical view name for the login page
     */
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    /**
     * Serves the registration page.
     *
     * @return the logical view name for the registration page
     */
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    /**
     * Handles user registration form submission.
     *
     * <p>On success, redirects to the login page. On failure, adds the error message
     * as a flash attribute and redirects back to the registration page.
     *
     * @param request            the {@link RegisterRequest} containing the submitted form data
     * @param redirectAttributes used to pass flash attributes across the redirect
     * @return a redirect to {@code /login} on success, or {@code /register} on failure
     */
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

    /**
     * Handles logout requests, guarding against logout during an active battle.
     *
     * <p>If an ongoing {@link BattleState} is found in the session, the logout is blocked
     * and an error message is flashed to the user. Otherwise, the security context is cleared,
     * the session is invalidated, and the user is redirected to the login page.
     *
     * @param session            the current {@link HttpSession}
     * @param redirectAttributes used to pass flash attributes across the redirect
     * @return a redirect to {@code /battle} with an error if a battle is in progress,
     *         or {@code /login?logout} on successful logout
     */
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