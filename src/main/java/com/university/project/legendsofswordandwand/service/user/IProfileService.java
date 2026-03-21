package com.university.project.legendsofswordandwand.service.user;

import com.university.project.legendsofswordandwand.dto.response.HallOfFameEntry;
import com.university.project.legendsofswordandwand.dto.response.ProfileInfo;
import com.university.project.legendsofswordandwand.dto.response.PvPStandingEntry;

import java.util.List;

public interface IProfileService {

  ProfileInfo getProfile(String username);

  List<HallOfFameEntry> getHallOfFame();

  List<PvPStandingEntry> getPvPStandings();
}
