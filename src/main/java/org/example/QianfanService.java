package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class QianfanService {
    private static final String ACCESS_TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String COMPLETION_URL = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro";

    @Value("${qianfan.api-key}")
    private String apiKey;

    @Value("${qianfan.secret-key}")
    private String secretKey;

    public String getAccessToken() throws IOException {
        String url = ACCESS_TOKEN_URL + "?grant_type=client_credentials" +
                "&client_id=" + apiKey +
                "&client_secret=" + secretKey;

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        HttpResponse response = client.execute(request);
        String json = EntityUtils.toString(response.getEntity());

        JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
        return jsonObject.get("access_token").getAsString();
    }

    public String callQianfan(String prompt, String accessToken) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(COMPLETION_URL + "?access_token=" + accessToken);

        // 构造请求体
        JsonObject requestBody = new JsonObject();
        JsonArray messages = new JsonArray();

        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);

        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.7);

        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(requestBody.toString()));

        HttpResponse response = client.execute(post);
        String jsonResponse = EntityUtils.toString(response.getEntity());

        JsonObject json = new Gson().fromJson(jsonResponse, JsonObject.class);
        return json.getAsJsonObject("result")
                .getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .get("content").getAsString();
    }
}