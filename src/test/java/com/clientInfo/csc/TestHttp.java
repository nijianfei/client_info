package com.clientInfo.csc;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestHttp {

    public static void main(String[] args) {
        System.out.println("JVM 默认编码: " + Charset.defaultCharset());
        System.out.println("文件编码参数: " + System.getProperty("file.encoding"));

//   /ids/v1/outer/identity/checkIdentity
        //  /ids/v1/outer/mobile/checkMobile
        //"https://api-sit.isignet.cn:8082/ids/v1/outer/mobile/checkMobile
//        postRequest("三要素校验", "https://api-sit.isignet.cn:8082/ids/v1/outer/mobile/checkMobile"
//                , Map.of("version", "1.0", "signAlgo", "HmacSHA256", "deviceId", "DEV_C351C9D6FABE4B7F8FB32589464112C4",
//                        "appId", "APP_BE97C85A615D4ADE90046367B0E4154C", "userTransId", "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX132"),
//                Map.of("name", "张三", "idNumber", "110101199003078857", "mobile", "13901234567")
//        );

//        postRequest("三要素校验", "https://api-sit.isignet.cn:8082/ids/v1/outer/mobile/checkMobile"
//                , Map.of("version", "1.0", "signAlgo", "HmacSHA256", "deviceId", "DEV_C351C9D6FABE4B7F8FB32589464112C4",
//                        "appId", "APP_BE97C85A615D4ADE90046367B0E4154C", "userTransId", "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX132"),
//                Map.of("name", "栗文", "idNumber", "310115198110205613", "mobile", "13916436281")
//        );



        //生产测试
        postRequest("三要素校验", "https://api.isignet.cn/ids/v1/outer/mobile/checkMobile"
                , Map.of("version", "1.0", "signAlgo", "HmacSHA256", "deviceId", "DEV_6B700826E1D04594AFDC57B3615FC39C",
                        "appId", "APP_043CDAE9439E4D668A1450B52640297A", "userTransId", "20250918-1651-001"),
                Map.of("name", "倪建飞", "idNumber", "131128198408100012", "mobile", "15131885899")
        );
    }

    public static String postRequest(String detailTag, String url, Map<String, Object> urlParams, Map<String, Object> bodyParams) {
        LinkedHashMap<String, Object> allMap = new LinkedHashMap<>();
        allMap.putAll(urlParams);
        allMap.putAll(bodyParams);
        allMap.remove("signature");

        LinkedHashMap<String,Object> linkedMap = new LinkedHashMap<>();
        List<String> keysortList = allMap.keySet().stream().sorted().toList();
        String stringA = keysortList.stream().map(k -> {
            linkedMap.put(k, allMap.get(k));
            return k + "=" + allMap.get(k);
        }).collect(Collectors.joining("&"));
        System.out.println("stringA:"+stringA);
        String signature = "";
        try {
            signature = calculateHmacSha256(stringA, "54LEXn5rZrsnjltdfaKeHVjOYr7Eg7aA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        System.out.println("signature:"+signature);
        linkedMap.put("signature", signature);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {//https://api-sit.isignet.cn:8082
//            String host = "https://api-sit.isignet.cn:8082";
            HttpPost httpPost = new HttpPost(String.format(url));

            httpPost.setEntity(new StringEntity(JSONObject.toJSONString(linkedMap),"UTF-8"));
            httpPost.setHeader("Content-Type", "application/json;charset=UTF-8"); // 必须指定 JSON 类型 [2,5](@ref)
            httpPost.setHeader("Accept-Encoding", "utf8");
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                System.out.println(detailTag + "_RequestParam: " + JSONObject.toJSONString(linkedMap, JSONWriter.Feature.PrettyFormat));
                String responseStr = EntityUtils.toString(response.getEntity());
                System.out.println(detailTag + "_Response: " + JSONObject.parseObject(responseStr).toJSONString(JSONWriter.Feature.PrettyFormat));
                return responseStr;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String calculateHmacSha256(String message, String secretKey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        // 1. 创建 HMAC-SHA256 算法实例
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        // 2. 将密钥转换为字节数组
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        // 3. 创建密钥规范
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
        // 4. 初始化 Mac 实例
        hmacSha256.init(secretKeySpec);
        // 5. 计算签名
        byte[] signatureBytes = hmacSha256.doFinal(
                message.getBytes(StandardCharsets.UTF_8)
        );
        // 6. 将签名转换为十六进制字符串（或 Base64）
//        return bytesToHex(signatureBytes);
//         如果需要 Base64 格式，可以使用：
         return Base64.getEncoder().encodeToString(signatureBytes);
    }

    // 字节数组转十六进制字符串的辅助方法
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
