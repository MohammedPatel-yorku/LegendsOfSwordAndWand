package com.university.project.legendsofswordandwand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LegendsOfSwordAndWandApplication {

  public static void main(String[] args) {
    SpringApplication.run(LegendsOfSwordAndWandApplication.class, args);
  }

  /*
  @Bean
  @Profile("dev")
  CommandLineRunner demo(CampaignController campaignController) {
    return args -> {
      campaignController.startCampaign(4L, "Thor", HeroClass.WARRIOR);
    };
  }
  */
}
