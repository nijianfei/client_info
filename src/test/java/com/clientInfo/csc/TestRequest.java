package com.clientInfo.csc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestRequest {

    public static void main(String[] args) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api-sit.isignet.cn:8082/ids/v1/outer/mobile/checkMobile"))
                .POST(HttpRequest.BodyPublishers.ofString("{\n  \"version\": \"1.0\",\n  \"deviceId\": \"DEV_C351C9D6FABE4B7F8FB32589464112C4\",\n  \"appId\": \"APP_BE97C85A615D4ADE90046367B0E4154C\",\n  \"userTransId\": \"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX132\",\n  \"signAlgo\": \"HmacSHA256\",\n  \"signature\": \"pyrrhQY1rfu9e8seVvyVDo0gCkFZcVCv1zPASYY4etM=\",\n  \"mobile\": \"13901234567\",\n  \"idNumber\": \"110101199003078857\",\n  \"name\": \"张三\"\n}"))
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept-Encoding", "utf8")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("_Response: " + response);
        System.out.println("_Response_body: " + response.body());
    }
}
