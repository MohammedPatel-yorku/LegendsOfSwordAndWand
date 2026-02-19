package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * service layer for handling logic related to users
 * including registration and login
 * use case: User Registration and Login
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * constructs a UserService with the needed dependencies
     */
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * saves a user to the database (used for testing)
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * registers a new user
     */
    public String registerUser(String username, String password) {

        if (!validate(username, password)) {
            return "username and password must not be empty.";
        }

        // check duplicate username
        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            return "username already exists. please choose another.";
        }

        // hash password
        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(username, hashedPassword);
        userRepository.save(user);

        return "registration successful.";
    }

  /**
   * Authenticates a User by verifying the provided password against the persisted hashed password.
   *
   * @param username Username entered by the user
   * @param password Password entered by the user
   * @return true if login is successful, false otherwise
   */
  public boolean loginUser(String username, String password) {
    User user = userRepository.findByUsername(username).orElse(null);

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

  public User createUser(String username, String password) {
    String hashedPassword = passwordEncoder.encode(password);
    User user = User.builder().username(username).password(hashedPassword).build();
    return userRepository.save(user);
  }
}
