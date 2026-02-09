package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * repository interface for User entities provides database access methods for user persistence and
 * retrieval
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * gets a user by their username
   *
   * @param username the username to search for
   * @return the matching User or null if no user exists
   */
  User findByUsername(String username);
}
