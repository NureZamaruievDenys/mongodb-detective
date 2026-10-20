package com.project.mongodb_detective.controllers;

import com.project.mongodb_detective.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {
    @Autowired
    private GameService gameService;

    @PostMapping("/game/new")
    public ResponseEntity<String> newGame() {
        gameService.generateNewCase();
        return ResponseEntity.ok("OK");
    }
}
