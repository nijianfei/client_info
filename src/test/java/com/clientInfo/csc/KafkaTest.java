package com.clientInfo.csc;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaTest {

    public static void main(String[] args) {
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

        // 2. 创建生产者实例
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
                            System.out.println("eventType:" + valueJsonObj.get("eventType"));
                            System.out.println("createTime:" + DateFormatUtils.format(new Date(Long.parseLong(valueJsonObj.get("createTime").toString())), "yyyyMMdd HH:mm:ss"));
                            JSONObject eventData = JSONObject.parse(String.valueOf(valueJsonObj.get("eventData")));
                            System.out.println("devId:" + eventData.get("devId"));
                            System.out.println("connectStatus:" + eventData.get("connectStatus"));
                            System.out.println("updateTime:" + DateFormatUtils.format(new Date(Long.parseLong(eventData.get("updateTime").toString())), "yyyyMMdd HH:mm:ss"));
                            break;
                        case "device_property_report":
                            System.out.println("\r\n设备属性上报：属性值--->");
                            System.out.println("devId:" + valueJsonObj.get("devId"));
                            System.out.println("productId:" + valueJsonObj.get("productId"));
                            JSONArray propertiesJsonArr = (JSONArray) (valueJsonObj.get("properties"));
                            for (Object o : propertiesJsonArr) {
                                JSONObject propertiesJsonObj = (JSONObject) o;
                                System.out.println("lastReportTime:" + DateFormatUtils.format(new Date(Long.parseLong(propertiesJsonObj.get("lastReportTime").toString())),"yyyyMMdd HH:mm:ss"));
                                System.out.println("value:" + propertiesJsonObj.get("value"));
                                System.out.println("valueObject:" + propertiesJsonObj.get("valueObject"));
                                System.out.println("propertyCode:" + propertiesJsonObj.get("propertyCode"));
                            }

                            break;
                        default:
                            System.out.println("未知事件类型：" + record.topic());
                    }
                    // 4. 手动提交偏移量（确保消息处理完成）
                    consumer.commitSync();
                }
            }
        }
    }

}
