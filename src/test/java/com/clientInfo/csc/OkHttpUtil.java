package com.clientInfo.csc;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OkHttpUtil {
    private static final int connectTimeout = 5000, readTimeout = 3000 * 10;

    public static void main(String[] args) throws IOException {
        System.out.println(get("https://www.baidu.com"));
    }

    // GET 请求
    public static String get(String url) throws IOException {
        return get(url, connectTimeout, readTimeout);
    }

    public static String get(String url, int connectTimeout, int readTimeout) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(connectTimeout, TimeUnit.MINUTES)
                .readTimeout(readTimeout,TimeUnit.MINUTES)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    // POST 请求（JSON）
    public static String postJson(String url, String json) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(connectTimeout, TimeUnit.MINUTES)
                .readTimeout(readTimeout,TimeUnit.MINUTES)
                .build();
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
