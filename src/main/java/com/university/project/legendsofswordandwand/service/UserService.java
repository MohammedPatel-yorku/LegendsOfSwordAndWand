package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
//made with the help of AI
/**
 * service layer for handling logic related to users
 * including registration, login, and operations
 * use case: User Registration and Login
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * constructs a UserService with the needed dependencies
     *
     * @param userRepository repository used to persist and retrieve users
     * @param passwordEncoder encoder used to hash and verify passwords
     */
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * saves a user to the database
     * method exists to keep unit testing requirements
     * and delegates directly to the UserRepository
     * @param user the user to be saved
     * @return the saved user entity
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * registers a new user by validating input, hashing the password,
     * and the user.
     * @param username the username entered by the user
     * @param password the unhashed password entered by the user
     * @return true if registration is successful false otherwise
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
     * authenticates a user by verifying the provided password against
     * the stored hashed password
     * @param username the username entered by the user
     * @param password the raw password entered by the user
     * @return true if login is successful false otherwise
     */
    public boolean loginUser(String username, String password) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return false;
        }

  private final UserRepository userRepository;

  public User createUser(String username, String password) {
    User user = new User(username, password);

    return userRepository.save(user);
  }

  public User login(String username, String password) {
    return userRepository
        .findByUsername(username)
        .filter(user -> user.getPassword().equals(password))
        .orElseThrow(() -> new RuntimeException("Invalid credentials"));
  }

  public User save(User user) {
    return userRepository.save(user);
  }

        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * validates registration credentials.
     * @param username the username to validate
     * @param password the password to validate
     * @return true if both username and password are non-null and non-empty
     */
    private boolean validate(String username, String password) {
        return username != null && password != null
                && !username.isEmpty()
                && !password.isEmpty();
    }
}
