package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.service.InnService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inn")
public class InnController {

    private final InnService innService;

    @PostMapping("/enter")
    public String enterInn(@RequestParam Long partyId) {
        return innService.loadInnView(partyId);
    }

    @PostMapping("/buy")
    public String buyItem(@RequestParam Long itemId) {
        boolean success = innService.purchaseItem(itemId);
        return success ? "Item purchase successful." : "Item purchase failed.";
    }

    @PostMapping("/recruit")
    public String recruitHero(@RequestParam Long partyId, @RequestParam Long heroId) {
        boolean success = innService.recruitHero(partyId, heroId);
        return success ? "Hero recruited successfully." : "Hero recruitment failed.";
    }

    @PostMapping("/exit")
    public String exitInn(@RequestParam Long partyId) {
        return innService.exitInn(partyId);
    }
}
