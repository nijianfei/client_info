package com.clientInfo.csc.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.clientInfo.csc.entity.PingEntity;
import com.clientInfo.csc.entity.PingTaskEntity;
import com.clientInfo.csc.entity.ServerStatusEntity;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
//@Component
public class SurveillanceItem {
    private static String pingCmd;

    private static ThreadPoolTaskExecutor executor;

    public static ServerStatusEntity serverStatus;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor nonStaticExecutor;

    @Value("${ping.cmd}")
    private String nonStaticPingCmd;

    public static Map<String, Process> processMap = new HashMap<>(16);

    @PostConstruct
    public void init() {
        SurveillanceItem.pingCmd = this.nonStaticPingCmd;
        SurveillanceItem.executor = this.nonStaticExecutor;
    }

    protected static
    Map<String, PingTaskEntity> recordMap = new HashMap<>();

    public static void pingSync(String ipAddress) {
        executor.execute(() -> ping(ipAddress));
    }

    public static void ping(String ipAddress) {
        Process process = null;
        try {
            Date startDate = new Date();
            System.out.println(ipAddress + "-开始时间：" + DateFormatUtils.format(startDate, "HH:mm:ss"));
            process = Runtime.getRuntime().exec(pingCmd + " " + ipAddress);
            processMap.put(ipAddress, process);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));
            String line;
            PingTaskEntity pingTaskEntity = null;
            while ((line = reader.readLine()) != null) {
                pingTaskEntity = recordMap.get(ipAddress);
                if (pingTaskEntity == null) {
                    pingTaskEntity = new PingTaskEntity();
                    pingTaskEntity.setIp(ipAddress);
                    recordMap.put(ipAddress, pingTaskEntity);
                }
                if (!line.equals("") && !line.contains("来自") && !line.contains("正在 Ping")) {
                    pingTaskEntity.addEntity(new PingEntity(new Date(), line));
                    pingTaskEntity.totalCountAdd();
                }
                if (line.contains("的回复")) {
                    //获取ping返回时间
                    pingTaskEntity.changeTime(line);
                    pingTaskEntity.totalCountAdd();
                }
            }
            int exitCode = process.waitFor(); // 等待命令执行完成并获取退出码
            if (exitCode == 0) {
                System.out.println(ipAddress + "-Ping成功，网络稳定。");
            } else {
                System.out.println(ipAddress + "-Ping失败，可能存在网络问题。");
                System.out.println("PrettyFormat-" + ipAddress + ":" + JSON.toJSONString(pingTaskEntity, JSONWriter.Feature.PrettyFormat));
            }
            Date endDate = new Date();
            System.out.println(ipAddress + "-结束时间：" + DateFormatUtils.format(endDate, "HH:mm:ss") + "-历史：" + (endDate.getTime() - startDate.getTime()) / 1000 + "秒钟");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
