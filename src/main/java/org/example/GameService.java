package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GameService {
    private final QianfanService qianfanService;
    private final GameSessionRepository sessionRepository;

    public GameService(QianfanService qianfanService,
                       GameSessionRepository sessionRepository) {
        this.qianfanService = qianfanService;
        this.sessionRepository = sessionRepository;
    }

    public GameSession startNewGame() throws IOException {
        String accessToken = qianfanService.getAccessToken();

        String prompt = """
            生成一个海龟汤谜题，包含：
            1. 情境描述(100字以内)
            2. 隐藏真相(30字以内)
            3. 3个关键词提示
            
            输出JSON格式：{
                "mystery": "情境描述",
                "truth": "隐藏真相",
                "hints": ["提示1", "提示2", "提示3"]
            }
            """;

        String response = qianfanService.callQianfan(prompt, accessToken);
        JsonObject json = new Gson().fromJson(response, JsonObject.class);

        GameSession session = new GameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setMystery(json.get("mystery").getAsString());
        session.setTruth(json.get("truth").getAsString());

        JsonArray hints = json.getAsJsonArray("hints");
        for (JsonElement hint : hints) {
            session.getAvailableHints().add(hint.getAsString());
        }

        session.setCreatedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    public String askQuestion(String sessionId, String question) throws IOException {
        GameSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.getQuestionHistory().add(question);
        sessionRepository.save(session);

        String accessToken = qianfanService.getAccessToken();

        String prompt = String.format("""
            你是一个海龟汤游戏主持人。当前谜题：%s
            隐藏真相：%s
            玩家提问：%s
            
            要求：
            1. 只能回答"是"、"否"或"与此无关"
            2. 不能透露真相
            3. 回答不超过5个字
            """, session.getMystery(), session.getTruth(), question);

        return qianfanService.callQianfan(prompt, accessToken);
    }

    public GuessResult guessTruth(String sessionId, String guess) throws IOException {
        GameSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        String accessToken = qianfanService.getAccessToken();

        String prompt = String.format("""
            判断玩家猜测是否接近真相：
            谜题：%s
            真相：%s
            玩家猜测：%s
            
            输出JSON格式：{
                "correct": true/false,
                "feedback": "反馈内容"
            }
            
            反馈规则：
            - 如果完全正确：祝贺并解释
            - 如果部分正确：指出接近的部分
            - 如果不正确：给予方向性提示
            """, session.getMystery(), session.getTruth(), guess);

        String response = qianfanService.callQianfan(prompt, accessToken);
        JsonObject json = new Gson().fromJson(response, JsonObject.class);

        boolean correct = json.get("correct").getAsBoolean();
        String feedback = json.get("feedback").getAsString();

        if (correct) {
            session.setSolved(true);
            sessionRepository.save(session);
        }

        return new GuessResult(correct, feedback);
    }

    public String getHint(String sessionId) {
        GameSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getAvailableHints().isEmpty()) {
            return "没有更多提示了";
        }

        String hint = session.getAvailableHints().remove(0);
        session.getUsedHints().add(hint);
        sessionRepository.save(session);

        return hint;
    }

    @Data
    @AllArgsConstructor
    public static class GuessResult {
        private boolean correct;
        private String feedback;

        public GuessResult(boolean correct, String feedback) {
        }
    }
}