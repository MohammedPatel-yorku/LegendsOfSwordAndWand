package com.university.project.legendsofswordandwand;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.service.BattleService;
import com.university.project.legendsofswordandwand.service.HeroService;
import com.university.project.legendsofswordandwand.service.UserService;
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
  CommandLineRunner demo(
      UserService userService, HeroService heroService, BattleService battleService) {
    return args -> {
      User user = userService.login("demo", "pass");

      Hero mage = heroService.createHeroForUser(user.getId(), "Arcanis", "Mage");
      Hero warrior = heroService.createHeroForUser(user.getId(), "Thorn", "Warrior");

      battleService.fight(mage, warrior);

      System.out.println("Mage HP: " + mage.getHealth());
      System.out.println("Warrior HP: " + warrior.getHealth());
    };
  }
}
