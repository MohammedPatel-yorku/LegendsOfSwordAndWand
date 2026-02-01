package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);
}
