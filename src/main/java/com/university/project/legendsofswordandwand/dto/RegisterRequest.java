package com.university.project.legendsofswordandwand.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RegisterRequest {

  private String username;
  private String password;
}
