package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QianfanService {
    private static final String URL = "https://qianfan.baidubce.com/v2/chat/completions";
    private static final Pattern JSON_PATTERN = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().readTimeout(300, TimeUnit.SECONDS).build();

    @Value("$(qianfan.app-id}")
    private String appId;

    @Value("${qianfan.api-key}")
    private String apiKey;

    /*
        public String getAccessToken() throws IOException {
            String url = ACCESS_TOKEN_URL + "?grant_type=client_credentials" +
                    "&client_id=" + apiKey +
                    "&client_secret=" + secretKey;

            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            HttpResponse response = client.execute(request);
            String json = EntityUtils.toString(response.getEntity());

            JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
            //return jsonObject.get("access_token").getAsString();

        }
    */
    public String callQianfan(String prompt) throws IOException {

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "{\"model\":\"ernie-x1-turbo-32k\",\"messages\":[{\"role\":\"user\",\"content\":\"" + prompt + "\"}]}");
        Request request = new Request.Builder()
                .url(URL)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("appid", "")
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        //System.out.println('\n' + response.body().string());

        //String response = "{\"id\":\"as-t2ddzsinqa\",\"object\":\"chat.completion\",\"created\":1752381365,\"model\":\"ernie-x1-turbo-32k\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"```json\\n{\\n  \\\"mystery\\\": \\\"婚礼当天，新郎在交换戒指时突然倒地身亡。法医鉴定死因是溺水，但现场没有一滴水。\\\",\\n  \\\"truth\\\": \\\"新郎对百合花粉严重过敏，婚戒内圈被涂抹了百合花粉提取物。交换戒指时花粉接触皮肤引发喉头水肿，最终导致窒息性溺水假象。\\\",\\n  \\\"hints\\\": [\\n    \\\"致命因素与植物有关\\\",\\n    \\\"死亡过程存在15分钟潜伏期\\\",\\n    \\\"仪式中有个被忽视的触觉传递动作\\\"\\n  ]\\n}\\n```\",\"reasoning_content\":\"我现在需要帮用户生成一个新的海龟汤谜题。首先，我得仔细理解用户的要求。用户希望谜题符合表面矛盾、隐藏逻辑和心理反转这三个核心特征。谜面不能超过三句话，要有三个提示，并且要按照严格的JSON格式输出。\\n\\n首先，我得确定谜面的矛盾点。海龟汤的关键在于制造看似不合常理的情境，让玩家产生疑问。比如，经典的“一个人在房间里，门锁着，没有窗户，但他是怎么死的？”这种谜面。我需要找一个类似的矛盾场景。\\n\\n接下来考虑隐藏逻辑。真相必须合理，但需要关键线索才能推导出来。比如，可能涉及时间、环境因素或者物品的特殊用途。同时，心理反转意味着答案会出乎意料，但回头看又合乎逻辑。\\n\\n然后，附加提示需要引导玩家，但又不直接揭示答案。每个提示应该逐步缩小范围，帮助玩家接近真相。比如，提示可能涉及时间、物品的存在与否，或者人物的关系。\\n\\n现在，我需要构思具体的谜题。假设场景是婚礼，但新郎在交换戒指时死亡。表面矛盾是为什么在幸福时刻死亡，而真相可能涉及隐藏的动机或物品。比如，戒指有毒，但需要线索提示。或者，新郎有某种疾病，但需要更巧妙的反转。\\n\\n另一个思路是自然现象，比如溺水但周围没有水。可能涉及花粉过敏导致窒息，但看起来像溺水。这样谜面可以是“男人在沙漠中溺亡，周围没有水源。” 提示可能涉及植物、呼吸问题、时间因素。\\n\\n再检查是否符合要求：谜面三句话，制造矛盾。比如：“婚礼当天，新郎在交换戒指时突然死亡。现场没有凶器，所有人都面带微笑。” 这样表面矛盾是为什么在幸福时刻死亡，且没有凶器。隐藏逻辑可能是戒指本身有毒，或者某种仪式导致。反转可能是新郎其实已经死了很久，比如灵异因素，但用户可能希望更现实。或者，戒指是冰做的，融化后露出毒针，但需要更合理的线索。\\n\\n或者，新郎有严重过敏，戒指上有花粉，导致过敏性休克，但现场有花，所以提示可能涉及花粉、医疗条件、时间延迟。但需要更简洁。\\n\\n再调整，比如谜面：“新郎在婚礼上交换戒指时突然倒地身亡。法医鉴定死因是溺水，但现场没有水。” 这样矛盾点在于溺水却无水，隐藏逻辑可能是过敏反应导致肺部积水，比如对花粉过敏，而婚礼现场有大量鲜花。反转是看似溺水实为过敏引发的肺水肿。\\n\\n提示可能包括：1. 新郎有已知的健康问题；2. 现场存在某种常见过敏原；3. 死亡时间与仪式环节有关联。\\n\\n这样谜面三句话，符合要求。接下来检查JSON格式是否正确，提示是否三个，并且隐藏真相合理且有反转。\\n\\n最终确定谜面和提示，确保逻辑严密，答案出乎意料但合理。\"},\"finish_reason\":\"stop\",\"flag\":0}],\"usage\":{\"prompt_tokens\":260,\"completion_tokens\":706,\"total_tokens\":966,\"completion_tokens_details\":{\"reasoning_tokens\":585}}}";
        // 解析整个API响应
        JsonObject fullResponse = new Gson().fromJson(response.body().string(), JsonObject.class);
        System.out.println();
        System.out.println(fullResponse);
        System.out.println();

        // 获取choices数组
        JsonArray choices = fullResponse.getAsJsonArray("choices");
        System.out.println(choices);
        if (choices == null || choices.size() == 0) {
            throw new RuntimeException("No choices in API response");
        }

        // 获取第一个choice的内容
        JsonObject firstChoice = choices.get(0).getAsJsonObject();
        String content = firstChoice.getAsJsonObject("message").get("content").getAsString();

        // 从content中提取JSON字符串
        Matcher matcher = JSON_PATTERN.matcher(content);
        if (matcher.find()) {
            String jsonContent = matcher.group(1);
            return new Gson().fromJson(jsonContent, JsonObject.class).toString();
        } else {
            // 如果没有代码块标记，尝试直接解析整个内容
            return new Gson().fromJson(content, JsonObject.class).toString();
        }

        //return response.body().string();
    }
}
/*

class Sample {

    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().readTimeout(300, TimeUnit.SECONDS).build();

    public static void main(String []args) throws IOException{
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"model\":\"ernie-3.5-8k\",\"messages\":[{\"role\":\"user\",\"content\":\"您好\"},{\"role\":\"assistant\",\"content\":[{\"type\":\"text\",\"text\":\"您好！我是文心一言，很高兴能为您提供帮助。无论是知识问答、文本创作，还是逻辑推理、代码编写等任务，我都可以协助您完成。请问有什么我可以帮您的吗？\"}]},{\"role\":\"assistant\",\"content\":[{\"type\":\"text\",\"text\":\"您好呀！看起来您可能还没完全提出具体问题或需求呢～ 无论是想讨论某个话题、需要创意灵感、解决技术问题，还是单纯想聊聊天，我都在这儿随时待命！😊 您有什么特别想聊的内容吗？\"}]},{\"role\":\"assistant\",\"content\":\"很高兴与您交流。其实，我并没有什么具体的问题或需求，只是简单地想向您问候一声。\\n\\n在现代社会，人与人之间的交流变得越来越少，很多时候我们都在忙于各自的事情，忽略了与他人的互动。因此，我觉得有时候向他人问候一声，表达一下友好和关怀，也是一件很有意义的事情。\\n\\n您觉得呢？\\n\\n另外，我还想问您一下，您作为一个智能助手，在与人类交流的过程中，有没有遇到过什么有趣或者难忘的事情呢？\"}],\"web_search\":{\"enable\":false,\"enable_citation\":false,\"enable_trace\":false},\"plugin_options\":{}}");
        Request request = new Request.Builder()
                .url("https://qianfan.baidubce.com/v2/chat/completions")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("appid", "")
                .addHeader("Authorization", "Bearer bce-v3/ALTAK-0tp9Am97bKvp8IDfCuQI9/bf2eab7e5b0a0a7f09659e04b201157b37757324")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        System.out.println(response.body().string());

    }


}*/