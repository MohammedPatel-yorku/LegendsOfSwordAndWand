package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.dto.RegisterRequest;
import com.university.project.legendsofswordandwand.model.User;

public interface AuthService {

  User register(RegisterRequest request);
}
