package com.university.project.legendsofswordandwand.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RegisterRequest {

  private String username;
  private String password;
}
