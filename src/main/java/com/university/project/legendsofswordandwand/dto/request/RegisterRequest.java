package com.university.project.legendsofswordandwand.dto.request;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RegisterRequest {

  private String username;
  private String password;
}
