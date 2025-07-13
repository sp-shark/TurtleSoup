package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

        String prompt =
                "# 角色设定\\n" +
                        "你是一名海龟汤谜题主持人，擅长创作和解答逻辑严密、反转巧妙的谜题。谜题需符合以下核心特征：\\n" +
                        "1. **表面矛盾**：谜面描述看似不合常理的事件\\n" +
                        "2. **隐藏逻辑**：通过关键线索可推导出合理真相\\n" +
                        "3. **心理反转**：答案往往颠覆直觉认知\\n" +
                        "\\n" +
                        "# 游戏规则\\n" +
                        "## 玩家须知\\n" +
                        "1. 每次只能提「是/否」问题\\n" +
                        "2. 禁止直接询问答案\\n" +
                        "3. 最多提问15次后给出最终推理\\n" +
                        "\\n" +
                        "## 你作为汤主的职责\\n" +
                        "- 初始提供「谜面」和「附加提示」\\n" +
                        "- 根据玩家提问用「是/否/无关」回答\\n" +
                        "- 15问未解或玩家要求时公布「完整解析」\\n" +
                        "\\n" +
                        "# 新谜题生成要求\\n" +
                        "现在请创作一个全新海龟汤谜题，严格遵循：\\n" +
                        "1. 谜面不超过3句话，制造强烈矛盾感\\n" +
                        "2. 包含3条提示\\n" +
                        "3. 严格遵守输出的JSON格式\\n" +
                        "\\n" +
                        "输出JSON格式：{\\n" +
                        "\\\"mystery\\\": \\\"情境描述\\\",\\n" +
                        "\\\"truth\\\": \\\"隐藏真相\\\",\\n" +
                        "\\\"hints\\\": [\\\"提示1\\\", \\\"提示2\\\", \\\"提示3\\\"]\\n" +
                        "}";
        String response = qianfanService.callQianfan(prompt);
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

        String prompt =
                "你是一个海龟汤游戏主持人。\\n" +
                        "当前谜题：" + session.getMystery().replace("\"", "\\\"") + "\\n" +
                        "隐藏真相：" + session.getTruth().replace("\"", "\\\"") + "\\n" +
                        "玩家提问：" + question.replace("\"", "\\\"") + "\\n" +
                        "\\n" +
                        "要求：\\n" +
                        "1. 根据玩家的问题选择恰当的回答\\n" +
                        "2. 只能回答\\\"是\\\"、\\\"否\\\"或\\\"与此无关\\\"\\n" +
                        "3. 不能透露真相\\n" +
                        "4. 思考过程不要太长，快速给出答案\\n" +
                        "\\n" +
                        "输出JSON格式：{\\n" +
                        "\\\"answer\\\": \\\"是/否/与此无关\\\"\\n" +
                        "}";
        String response = qianfanService.callQianfan(prompt);
        JsonObject json = new Gson().fromJson(response, JsonObject.class);

        return json.get("answer").getAsString();
    }

    public GuessResult guessTruth(String sessionId, String guess) throws IOException {
        GameSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        String prompt =
                "你是一个海龟汤游戏主持人。当前谜题：" + session.getMystery().replace("\"", "\\\"") + "\\n" +
                        "隐藏真相：" + session.getTruth().replace("\"", "\\\"") + "\\n" +
                        "玩家猜测：" + guess.replace("\"", "\\\"") + "\\n" +
                        "判断玩家猜测是否接近真相，不需要完全正确，玩家能答对大致内容即可认为回答正确：\\n" +
                        "\\n" +
                        "反馈内容生成规则：\\n" +
                        "- 如果完全正确：祝贺并解释\\n" +
                        "- 如果部分正确：指出接近的部分\\n" +
                        "- 如果不正确：给予方向性提示\\n" +
                        "- 不超过1句话\\n" +
                        "\\n" +
                        "输出JSON格式：{\\n" +
                        "\\\"correct\\\":true/false,\\n" +
                        "\\\"feedback\\\":\\\"反馈内容\\\"\\n" +
                        "}";

        String response = qianfanService.callQianfan(prompt);
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
    public static class GuessResult {
        private boolean correct;
        private String feedback;

        public GuessResult(boolean correct, String feedback) {
            this.correct = correct;
            this.feedback = feedback;
        }
    }
}