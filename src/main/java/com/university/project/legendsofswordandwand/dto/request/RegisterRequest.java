package com.university.project.legendsofswordandwand.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO carrying the credentials submitted via the registration form. */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
  /** The desired username for the new account. Must be unique across all players. */
  private String username;

  /** The plain-text password that will be encoded before storage. */
  private String password;
}
