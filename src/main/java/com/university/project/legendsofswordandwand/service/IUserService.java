package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.dto.DashboardInfo;

public interface IUserService {

  DashboardInfo getDashboardInfo(String username);
}
