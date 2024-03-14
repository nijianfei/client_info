package com.clientInfo.csc.utils;

import com.alibaba.fastjson2.JSONObject;
import com.clientInfo.csc.vo.ArpVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ArpUtil {
    private static Map<String, ArpVo> infoMap =  Map.of();
    private static Logger log = LoggerFactory.getLogger(ArpUtil.class);
    private static String arpCommand;

    public static void setArpCommand(String arpCommand) {
        ArpUtil.arpCommand = arpCommand;
    }

    public static String getMacAddress() {
        StringBuilder sb = new StringBuilder();
        NetworkInterface netint = null;
        try {
            netint = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = netint.getHardwareAddress();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], ""));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public static StringBuilder IPtoHex(String IP) {
        String[] address = IP.replace(".", ":").split(":");
        StringBuilder str = new StringBuilder();
        for (String s : address) {
            s = Integer.toHexString(Integer.parseInt(s));
            s = (s.length() < 2) ? "0" + s : s;
            str.append(s);
        }
        return str;
    }

    public static ArpVo getClientInfo(String remoteAddr,String arpCommand) {
        ArpVo arpVo = infoMap.get(remoteAddr);
        if (Objects.isNull(arpVo)) {
            synchronized (Object.class){
                initClientInfoMap(arpCommand);
                arpVo = infoMap.get(remoteAddr);
                if (Objects.isNull(arpVo)){
                    arpVo = Objects.nonNull(arpVo)?arpVo:ArpVo.builder().build();
                }
            }
        }
        return arpVo;
    }

    public static String getServerLocalPublicIp(String serverIpCommand) {
        String serverIpLine = execCmd(serverIpCommand);
        List<String> serverIds = new ArrayList<>();
        //   IPv4 地址 . . . . . . . . . . . . : 10.65.208.165
        String[] lines = serverIpLine.split("\r\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (StringUtils.isBlank(line)) {
                continue;
            }
            String[] lineSplit = line.replaceAll("(\\ )\\1+", ",").split(":");
            if (lineSplit.length == 2) {
                serverIds.add(lineSplit[1].trim());
            }
        }
        return serverIds.stream().collect(Collectors.joining(","));
    }
    public static Map<String, ArpVo> initClientInfoMap(String arpCommand) {
        ConcurrentHashMap<String, ArpVo> infoNewMap = new ConcurrentHashMap();
        String readText = execCmd(arpCommand);
        String[] lines = readText.split("\r\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (StringUtils.isBlank(line)) {
                continue;
            }
            String trimStr = line.trim();
            String[] split = trimStr.replaceAll("(\\ )\\1+", ",").split(",");
            if (split.length == 3) {
                ArpVo build = ArpVo.builder().clientIp(split[0]).clientMac(split[1]).build();
                infoNewMap.put(build.getClientIp(), build);
            }
        }
        infoMap = Collections.unmodifiableMap(infoNewMap);
        log.info("initClientInfoMap:-->{}", JSONObject.toJSONString(infoMap));
        return infoMap;
    }

    private static String execCmd(String command) {
        try {
            log.info("开始执行->[{}]命令", command);
            Process exec = Runtime.getRuntime().exec(command);
            InputStream inputStream = exec.getInputStream();
            InputStream errorStream = exec.getErrorStream();
            byte[] readAllBytes = inputStream.readAllBytes();
            String accMsg = new String(readAllBytes, Charset.forName("GBK"));
            log.info("返回结果：\r\n{}", accMsg);
            byte[] readAllBytes2 = errorStream.readAllBytes();
            String errMsg = new String(readAllBytes2, Charset.forName("GBK"));
            if (StringUtils.isNotBlank(errMsg)) {
                log.info("返回异常信息：{}", errMsg);
            }
            return accMsg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
