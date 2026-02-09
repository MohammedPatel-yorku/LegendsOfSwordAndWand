package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password
    ) {
        boolean result = userService.registerUser(username, password);

        if (result) {
            return "Registration successful";
        }
        return "Registration unsuccessful";
    }

    @PostMapping("/login")
    public String loginUser(
            @RequestParam String username,
            @RequestParam String password
    ) {
        boolean result = userService.loginUser(username, password);

        if (result) {
            return "Login successful";
        }
        return "Login unsuccessful";
    }
}
