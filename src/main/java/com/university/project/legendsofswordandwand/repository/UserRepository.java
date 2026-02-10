package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository Interface for User entities. Provides database access methods for User persistence and
 * retrieval.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Retrieves a user by their username.
   *
   * @param username the username to search for
   * @return the matching User or null if no user exists
   */
  User findByUsername(String username);
}
