package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.dto.ProfileInfo;

public interface IProfileService {

  ProfileInfo getProfile(String username);
}
