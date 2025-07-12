package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/game")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public ResponseEntity<GameSession> startGame() {
        try {
            GameSession session = gameService.startNewGame();
            return ResponseEntity.ok(session);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/ask")
    public ResponseEntity<String> askQuestion(
            @RequestParam String sessionId,
            @RequestParam String question) {
        try {
            String answer = gameService.askQuestion(sessionId, question);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("发生错误: " + e.getMessage());
        }
    }

    @PostMapping("/guess")
    public ResponseEntity<GameService.GuessResult> makeGuess(
            @RequestParam String sessionId,
            @RequestParam String guess) {
        try {
            GameService.GuessResult result = gameService.guessTruth(sessionId, guess);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/hint")
    public ResponseEntity<String> getHint(@RequestParam String sessionId) {
        try {
            String hint = gameService.getHint(sessionId);
            return ResponseEntity.ok(hint);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("发生错误: " + e.getMessage());
        }
    }
}