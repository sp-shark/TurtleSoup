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

        // 解析整个API响应
        JsonObject fullResponse = new Gson().fromJson(response.body().string(), JsonObject.class);

        //System.out.println(fullResponse);

        // 获取choices数组
        JsonArray choices = fullResponse.getAsJsonArray("choices");
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
            return new Gson().fromJson(content, JsonObject.class).toString();
        }
    }
}