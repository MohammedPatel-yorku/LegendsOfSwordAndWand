package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// made with the help of AI
/** User Object Service class. */
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Saves a User to the database.
   *
   * @param user User to be saved
   * @return the saved User entity
   */
  public User save(User user) {
    return userRepository.save(user);
  }

  /**
   * Registers a new User by validating input, hashing the password, and persisting the User.
   *
   * @param username Username entered by the User
   * @param password Unhashed password entered by the User
   * @return true if registration is successful, false otherwise
   */
  public boolean registerUser(String username, String password) {
    if (!validate(username, password)) {
      return false;
    }

    String hashedPassword = passwordEncoder.encode(password);
    User user = new User(username, hashedPassword);
    userRepository.save(user);

    return true;
  }

  /**
   * Authenticates a User by verifying the provided password against the persisted hashed password.
   *
   * @param username Username entered by the user
   * @param password Password entered by the user
   * @return true if login is successful, false otherwise
   */
  public boolean loginUser(String username, String password) {
    User user = userRepository.findByUsername(username);

    if (user == null) {
      return false;
    }
    return passwordEncoder.matches(password, user.getPassword());
  }

  /**
   * Validates registration credentials.
   *
   * @param username Username to validate
   * @param password Password to validate
   * @return true if both username and password are non-null and non-empty
   */
  private boolean validate(String username, String password) {
    return username != null && password != null && !username.isEmpty() && !password.isEmpty();
  }
}
