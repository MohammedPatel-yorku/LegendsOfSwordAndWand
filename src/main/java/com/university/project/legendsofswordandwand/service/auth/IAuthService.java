package com.university.project.legendsofswordandwand.service.auth;

import com.university.project.legendsofswordandwand.dto.request.RegisterRequest;
import com.university.project.legendsofswordandwand.model.User;

/** Service interface defining the contract for user authentication operations. */
public interface IAuthService {

  /**
   * Registers a new user account using the provided credentials. The password is stored in
   * encrypted form.
   *
   * @param request the {@link RegisterRequest} containing the desired username and password
   * @return the newly created and persisted {@link User}
   * @throws RuntimeException if the username is already taken
   */
  User register(RegisterRequest request);
}
