package com.university.project.legendsofswordandwand.service.auth;

import com.university.project.legendsofswordandwand.dto.request.RegisterRequest;
import com.university.project.legendsofswordandwand.model.User;

public interface IAuthService {

  User register(RegisterRequest request);
}
