package com.clientInfo.csc;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

/*测试 智慧照明 kafka 消费者模式 DEMO*/

public class KafkaTest {

    private static Map<String, String> proMap = new HashMap<>();
    private static Map<String, DeviceInfo> devMap = new HashMap<>();
    private static Map<String, Map<Integer,JSONObject>> propertiesMap = new HashMap<>();

    public static void main(String[] args) {

        /*产品查询*/
        String product = postRequest("产品多条件查询", "/v1.0/openapi/web/products/search",
                JSONObject.toJSONString(Map.of("filter", new Object(), "pageNo", 1, "pageSize", 50)));
        JSONObject jsonObject = JSONObject.parseObject(product);
        JSONArray prodList = (JSONArray) ((JSONObject) (jsonObject.get("data"))).get("payloads");

        for (Object obj : prodList) {
            JSONObject pro = (JSONObject) obj;
            String productId = pro.get("productId").toString();
            proMap.put(productId, pro.get("name").toString());
            String modelId = pro.get("modelId").toString();
            boolean isMore = true;
            while (isMore){

            }
            String ability = getRequest("获取产品能力","/v1.0/openapi/web/thing-model/ability",Map.of("modelId",modelId));
            JSONObject jsonObject1 = JSONObject.parseObject(ability);
            JSONArray propertiesList = (JSONArray) ((JSONObject) (jsonObject1.get("data"))).get("properties");
            for (int i = 0; i < propertiesList.size(); i++) {
                JSONObject proper = (JSONObject) (propertiesList.get(i));
                if(i == 0){
                    propertiesMap.put(productId, new HashMap<>());
                }
                propertiesMap.get(productId).put(Integer.parseInt(proper.get("abilityId").toString()), proper);
            }

        }
        System.out.println("产品类型:\r\n" + JSONObject.toJSONString(proMap, JSONWriter.Feature.PrettyFormat));

        /*设备查询*/
        String device = postRequest("产品多条件查询", "/v1.0/openapi/web/devices/search",
                JSONObject.toJSONString(Map.of("filter", new Object(), "pageNo", 1, "pageSize", 50)));
        JSONObject jsonObject1 = JSONObject.parseObject(device);
        JSONObject data = (JSONObject) (jsonObject1.get("data"));
        boolean hasMore  = (Boolean)data.get("hasMore");
        JSONArray devList = (JSONArray) data.get("payloads");

        for (Object obj : devList) {
            JSONObject pro = (JSONObject) obj;
            DeviceInfo deviceInfo = JSONObject.parseObject(pro.toJSONString(), DeviceInfo.class);
            devMap.put(pro.get("devId").toString(), deviceInfo);
        }
        System.out.println(JSONObject.toJSONString(devMap, JSONWriter.Feature.PrettyFormat));


//        new Thread(()->producerTest()).start();
        new Thread(() -> consumerTest()).start();
    }

    public static void producerTest() {
        // 1. 加载配置
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.90:29092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.LINGER_MS_CONFIG, 20); // 批量发送延迟
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 批量大小

        // 2. 创建生产者实例+
        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            // 3. 发送消息（异步）
            ProducerRecord<String, String> record =
                    new ProducerRecord<>("test-topic", "key1", "Hello Kafka!");

            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    System.out.printf("消息发送成功！主题=%s, 分区=%d, 偏移量=%d\n",
                            metadata.topic(), metadata.partition(), metadata.offset());
                } else {
                    System.err.println("发送失败: " + exception.getMessage());
                }
            });

            // 4. 同步发送（可选）
            producer.send(new ProducerRecord<>("test-topic", "key2", "Sync Message")).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void consumerTest() {
        // 1. 加载配置
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.90:29092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // 手动提交偏移量

        // 2. 创建消费者实例
        try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of("device_event", "device_property_report"));

            // 3. 持续轮询消息
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("\r\n收到消息: 主题=%s, 分区=%d, 偏移量=%d, Key=%s, Value=%s\n",
                            record.topic(), record.partition(), record.offset(),
                            record.key(), record.value());
                    JSONObject valueJsonObj = JSONObject.parse(record.value());
                    switch (record.topic()) {
                        case "device_event":
                            System.out.println("\r\n设备事件上报：");
                            DeviceEventEnum eventType = DeviceEventEnum.getEnum(valueJsonObj.get("eventType").toString());
                            System.out.println("eventType:" + valueJsonObj.get("eventType") + " - " + eventType.getName());
                            System.out.println("createTime:" + DateFormatUtils.format(new Date(Long.parseLong(valueJsonObj.get("createTime").toString())), "yyyyMMdd HH:mm:ss"));
                            if (DeviceEventEnum.DEVICE_CONNECT_CHANGE.equals(eventType)) {
                                JSONObject eventData = JSONObject.parse(String.valueOf(valueJsonObj.get("eventData")));
                                System.out.println("devId:" + eventData.get("devId") + " - " + devMap.get(eventData.get("devId").toString()).getName());
                                System.out.println("connectStatus:" + eventData.get("connectStatus") + " - " + (Objects.equals(eventData.get("connectStatus"), 1) ? "在线" : "离线"));
                                System.out.println("updateTime:" + DateFormatUtils.format(new Date(Long.parseLong(eventData.get("updateTime").toString())), "yyyyMMdd HH:mm:ss"));
                            }
                            break;
                        case "device_property_report":
                            System.out.println("\r\n设备属性上报：属性值--->");
                            System.out.println("devId:" + valueJsonObj.get("devId") + " - " + devMap.get(valueJsonObj.get("devId").toString()).getName());
                            String productId = valueJsonObj.get("productId").toString();
                            System.out.println("productId:" + productId + " - " + proMap.get(productId));
                            JSONArray propertiesJsonArr = (JSONArray) (valueJsonObj.get("properties"));
                            for (Object o : propertiesJsonArr) {
                                JSONObject propertiesJsonObj = (JSONObject) o;
                                JSONObject jsonObject = propertiesMap.get(productId).get(propertiesJsonObj.get("propertyId"));
                                System.out.println("lastReportTime:" + DateFormatUtils.format(new Date(), "yyyyMMdd HH:mm:ss"));
                                System.out.println("code:" + jsonObject.get("code"));
                                System.out.println("name:" + jsonObject.get("name"));
                                if (Objects.equals(jsonObject.get("outputParamsTypes"),"bool")) {
                                    System.out.println("value:" + propertiesJsonObj.get("value") + " --- " );
                                }else{
                                    System.out.println("value:" + propertiesJsonObj.get("value"));
                                }
                                System.out.println("propertyCode:" + propertiesJsonObj.get("propertyCode"));
                            }

                            break;
                        default:
                            System.out.println("未知事件类型：" + record.topic());
                            /*位置类型不做处理....  */
                    }
                    // 4. 手动提交偏移量（确保消息处理完成）
                    consumer.commitSync();
                }
            }
        }
    }


    public static String postRequest(String detailTag, String url, String jsonParams) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String host = "http://192.168.0.90:8888";
            HttpPost httpPost = new HttpPost(String.format(host + url));
            httpPost.setEntity(new StringEntity(jsonParams));
            httpPost.setHeader("Content-Type", "application/json"); // 必须指定 JSON 类型 [2,5](@ref)
            httpPost.setHeader("Authorization", getToken()); // 必须指定 JSON 类型 [2,5](@ref)
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                System.out.println(detailTag + "_Status: " + response.getStatusLine().getStatusCode());
                String responseStr = EntityUtils.toString(response.getEntity());
                System.out.println(detailTag + "_Response: " + responseStr);
                return responseStr;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getRequest(String detailTag, String url, Map<String, Object> params) {
        String host = "http://192.168.0.90:8888";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            StringBuilder paramsBuff = new StringBuilder();
            params.forEach((k, v) -> paramsBuff.append(k).append("=").append(v));
            HttpGet httpGet = new HttpGet(String.format("%s%s?%s", host, url, paramsBuff));
            httpGet.setHeader("Authorization", getToken()); // 必须指定 JSON 类型 [2,5](@ref)
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String result = EntityUtils.toString(response.getEntity());
                System.out.println(detailTag + "_Status: " + response.getStatusLine().getStatusCode());
                System.out.println(detailTag + "_Response: " + result);
                return result;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getToken() {
        String url = "http://192.168.0.90:8888/v1.0/openapi/web/login";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(JSONUtil.toJsonStr(Map.of("password", "admin", "username", "admin"))));
            post.setHeader("Content-Type", "application/json");
            CloseableHttpResponse execute = httpClient.execute(post);
            int statusCode = execute.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String response = EntityUtils.toString(execute.getEntity());
                cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(response);
                cn.hutool.json.JSONObject data = (cn.hutool.json.JSONObject) (jsonObject.get("data"));
                Object token = data.get("token");
                return token.toString();
            }
        } catch (Exception exception) {
            System.out.println(exception);
        }
        return null;
    }
}
