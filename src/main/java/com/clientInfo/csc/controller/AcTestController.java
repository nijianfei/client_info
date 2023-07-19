package com.clientInfo.csc.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.clientInfo.csc.vo.ResultVo;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Slf4j
@CrossOrigin(origins = "*")
@PropertySource(value = "classpath:application.properties", encoding = "utf-8")
@DependsOn("filePropertiesSource")
@RequestMapping("/verify")
@RestController()
public class AcTestController {
    @Value("${arpCommand}")
    private String arpCommand;
    @Value("${serverIpCommand}")
    private String serverIpCommand;
    @Value("#{${cardStr}}")
    private Map<String, String> cardStr;
    @Value("#{${qrcodeStrqrcodeStr}}")
    private Map<String, String> qrcodeStr;
    @Value("${touchSensingFlag}")
    private Boolean touchSensingFlag;

    @Value("${coreUrl}")
    private String coreUrl;

    @PostMapping("/card")
    private Object card(HttpServletRequest request, @RequestBody Map<String, String> map) {
        if (Objects.nonNull(map.get("cardId")) && Objects.nonNull(cardStr.get(map.get("cardId")))) {
            log.info("card_接收到客户端请求:{},本地验证通过！", JSONUtil.toJsonStr(map));
            return getResultVo();
        }
        try {
            String postResult = HttpUtil.post(coreUrl + "card", JSONUtil.toJsonStr(map));
            JSONObject parseResult = JSONUtil.parseObj(postResult);
            String resultCls = parseResult.getStr("resultCls");
            log.info("card_接收到客户端请求:{}，调用中台返回结果:{}", JSONUtil.toJsonStr(map), postResult);
            return new ResultVo(resultCls);
        } catch (Exception e) {
            log.error("card_接收到客户端请求:{}，调用中台异常 --》", JSONUtil.toJsonStr(map), e);
            return new ResultVo("90");
        }
    }

    @PostMapping("/qrcode")
    private Object qrcode(HttpServletRequest request, @RequestBody Map<String, String> map) {
        if (Objects.nonNull(map.get("qrcode")) && Objects.nonNull(qrcodeStr.get(map.get("qrcode")))) {
            log.info("qrcode_接收到客户端请求:{},本地验证通过！", JSONUtil.toJsonStr(map));
            return getResultVo();
        }
        try {
            String postResult = HttpUtil.post(coreUrl + "qrcode", JSONUtil.toJsonStr(map));
            JSONObject parseResult = JSONUtil.parseObj(postResult);
            String resultCls = parseResult.getStr("resultCls");
            log.info("qrcode_接收到客户端请求:{}，调用中台返回结果:{}", JSONUtil.toJsonStr(map), postResult);
            return new ResultVo(resultCls);
        } catch (Exception e) {
            log.error("qrcode_接收到客户端请求:{}，调用中台异常 --》", JSONUtil.toJsonStr(map), e);
            return new ResultVo("90");
        }
    }

    @PostMapping("/touchSensing")
    private Object touchSensing(HttpServletRequest request) {
        return new ResultVo("70");
    }

    @PostMapping("/connect")
    private Object connect(HttpServletRequest request) {
        String param = null;
        try {
            param = getParams(request);
            log.info("connect_接收到客户端请求:{}", param);
            String postResult = HttpUtil.post(coreUrl + "connect", param);
            log.info("connect_接收到客户端请求:{}，调用中台返回结果:{}", param, postResult);
        } catch (Exception e) {
            log.info("connect_接收到客户端请求:{},异常--》", param, e);
        }
        return getResultVo();
    }

    private String getParams(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try {
            ServletInputStream inputStream = request.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bfReader = new BufferedReader(reader);
            String line;
            while ((line = bfReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String str = sb.toString();
        return str.replace(",}", "}");
    }

    private Object getResultVo() {
        return new ResultVo("70");
    }
}
