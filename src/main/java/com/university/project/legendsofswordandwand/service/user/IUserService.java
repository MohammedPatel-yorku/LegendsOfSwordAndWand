package com.university.project.legendsofswordandwand.service.user;

import com.university.project.legendsofswordandwand.dto.response.DashboardInfo;

public interface IUserService {

  DashboardInfo getDashboardInfo(String username);

  Long getUserIdByUsername(String username);
}
