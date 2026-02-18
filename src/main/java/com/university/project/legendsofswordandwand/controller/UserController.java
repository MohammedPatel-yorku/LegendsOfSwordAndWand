package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Rest Controller class mapped with '/user' to handle User requests. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

  private final UserService userService;

  /**
   * Registers User. Mapped to POST '/register'.
   *
   * @param username Username to register User with
   * @param password Password to register User with
   * @return Registration success as boolean
   */
  @PostMapping("/register")
  public String registerUser(@RequestParam String username, @RequestParam String password) {
    boolean result = userService.registerUser(username, password);

    if (result) {
      return "Registration successful";
    }
    return "Registration unsuccessful";
  }

  /**
   * Logs in User. Mapped to POST '/login'.
   *
   * @param username Username to log in with
   * @param password Password to log in with
   * @return Login success as boolean
   */
  @PostMapping("/login")
  public String loginUser(@RequestParam String username, @RequestParam String password) {
    boolean result = userService.loginUser(username, password);

    if (result) {
      return "Login successful";
    }
    return "Login unsuccessful";
  }
}
