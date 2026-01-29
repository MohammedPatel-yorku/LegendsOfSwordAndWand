package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private UserRepository userRepository;
}
