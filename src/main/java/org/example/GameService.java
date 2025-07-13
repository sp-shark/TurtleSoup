package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
        System.out.println(prompt);
        System.out.println();
        //String response = qianfanService.callQianfan(prompt);
        String response = "{\"mystery\":\"男人被发现吊死在自家客厅吊扇上，窗户紧闭且无任何支撑物。他的宠物鹦鹉始终重复着‘生日快乐’四个字。\",\"truth\":\"男人是退休魔术师，生日当天策划了‘完美自杀’表演。他利用吊扇隐藏的升降机关将自己吊起，并设置零点自动启动装置。鹦鹉被训练在生日当天循环播放‘生日快乐’录音掩盖机关声，但装置故障导致他真正窒息。封闭房间是魔术设计的一部分。\",\"hints\":[\"死者生前最后一场表演主题是‘时间陷阱’\",\"鹦鹉每天只重复三个单词，唯独生日当天例外\",\"吊扇叶片内侧有细小绳索摩擦痕迹\"]}";
        System.out.println(response);
        JsonObject json = new Gson().fromJson(response, JsonObject.class);

        GameSession session = new GameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setMystery(json.get("mystery").getAsString());
        session.setTruth(json.get("truth").getAsString());

        JsonArray hints = json.getAsJsonArray("hints");
        for (JsonElement hint : hints) {
            session.getAvailableHints().add(hint.getAsString());
        }

        System.out.println(session.getSessionId());
        System.out.println(session.getMystery());
        System.out.println(session.getTruth());
        System.out.println(session.getAvailableHints());

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
                        "\\n" +
                        "输出JSON格式：{\\n" +
                        "\\\"answer\\\": \\\"是/否/与此无关\\\"\\n" +
                        "}";
        System.out.println();
        System.out.println(prompt);
        String response = qianfanService.callQianfan(prompt);
        //String response = "1";
        JsonObject json = new Gson().fromJson(response, JsonObject.class);

        return json.get("answer").getAsString();
    }

    public GuessResult guessTruth(String sessionId, String guess) throws IOException {
        GameSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        String prompt =
                "你是一个海龟汤游戏主持人。当前谜题：" + session.getMystery() + "\\n" +
                        "隐藏真相：" + session.getTruth() + "\\n" +
                        "玩家猜测：" + guess + "\\n" +
                        "判断玩家猜测是否接近真相，不需要完全正确，玩家能答对大致内容即可认为回答正确：\\n" +
                        "\\n" +
                        "反馈内容生成规则：\\n" +
                        "- 如果完全正确：祝贺并解释\\n" +
                        "- 如果部分正确：指出接近的部分\\n" +
                        "- 如果不正确：给予方向性提示" +
                        "- 不超过1句话" +
                        "\\n" +
                        "输出JSON格式：{\\n" +
                        "\\\"correct\\\":true/false,\\n" +
                        "\\\"feedback\\\":\\\"反馈内容\\\"";

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
//@AllArgsConstructor
    public static class GuessResult {
        private boolean correct;
        private String feedback;

        public GuessResult(boolean correct, String feedback) {
        }
    }
}
/*
class Sample {

    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().readTimeout(300, TimeUnit.SECONDS).build();

    public static void main(String[] args) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "{\"model\":\"ernie-x1-turbo-32k\",\"messages\":[{\"role\":\"user\",\"content\":\"#你是一个问题推荐助手，需要根据<题目>、<解析>以及老师和学生的<答疑内容>过程上下文，推测学生的下一个问题会是什么\\n\\n" +
                        "1. 需要仔细分析<题目>、<解析>以及老师和学生的<答疑内容>，理解学生的当前思路，推测学生的理解盲区\\n" +
                        "2. 一次输出三条学生可能问的推测问题，每个推测问题字数控制在10个字以内\\n" +
                        "3. 推测问题需要符合学生的思路，需要符合当前题目背景\\n\\n" +
                        "<题目>：\\n" +
                        "<解析>：\\n\\n" +
                        "<答疑内容>：\\n\\n\\n" +
                        "##输出推测问题：\\n" +
                        "1. `XXX`\\n" +
                        "2. `XXX`\\n" +
                        "3. `XXX`\"}]}");
        Request request = new Request.Builder()
                .url("https://qianfan.baidubce.com/v2/chat/completions")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("appid", "app-e4dJzUBW")
                .addHeader("Authorization", "Bearer bce-v3/ALTAK-0tp9Am97bKvp8IDfCuQI9/bf2eab7e5b0a0a7f09659e04b201157b37757324")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        System.out.println(response.body().string());

    }


}
*/