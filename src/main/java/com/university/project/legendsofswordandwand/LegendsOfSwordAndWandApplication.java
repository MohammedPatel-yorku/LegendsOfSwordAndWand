package com.university.project.legendsofswordandwand;

import com.university.project.legendsofswordandwand.controller.CampaignController;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class LegendsOfSwordAndWandApplication {

  public static void main(String[] args) {
    SpringApplication.run(LegendsOfSwordAndWandApplication.class, args);
  }

  @Bean
  @Profile("dev")
  CommandLineRunner demo(CampaignController campaignController) {
    return args -> {
      campaignController.startCampaign(4L, "Thor", HeroClass.WARRIOR);
    };
  }
}
