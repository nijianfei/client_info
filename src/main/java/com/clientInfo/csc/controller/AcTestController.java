package com.clientInfo.csc.controller;

import com.alibaba.fastjson2.JSONObject;
import com.clientInfo.csc.utils.ArpUtil;
import com.clientInfo.csc.utils.IpUtil;
import com.clientInfo.csc.vo.ArpVo;
import com.clientInfo.csc.vo.ResultVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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
    @Value("#{touchSensingFlag}")
    private Boolean touchSensingFlag;

    @PostMapping("/card")
    private Object card(HttpServletRequest request, @RequestBody Map<String, String> map) {
        String remoteAddr = IpUtil.getIpAddr(request);
        log.info("card_接收到客户端{}请求", remoteAddr);
        String serverLocalPublicIp = ArpUtil.getServerLocalPublicIp(serverIpCommand);
        ArpVo arpVo = ArpUtil.getClientInfo(remoteAddr, arpCommand);
        arpVo.setNodeServerIp(serverLocalPublicIp);
        log.info("card_返回客户端信息：{}", JSONObject.toJSONString(arpVo));
        if (CollectionUtils.isEmpty(cardStr)) {
            return getResultVo();
        }
        if (Objects.nonNull(map.get("cardId")) && Objects.nonNull(cardStr.get(map.get("cardId")))) {
            return getResultVo();
        }
        return new ResultVo("90");
    }

    @PostMapping("/qrcode")
    private Object qrcode(HttpServletRequest request, @RequestBody Map<String, String> map) {
        String remoteAddr = IpUtil.getIpAddr(request);
        log.info("qrcode_接收到客户端{}请求", remoteAddr);
        String serverLocalPublicIp = ArpUtil.getServerLocalPublicIp(serverIpCommand);
        ArpVo arpVo = ArpUtil.getClientInfo(remoteAddr, arpCommand);
        arpVo.setNodeServerIp(serverLocalPublicIp);
        log.info("qrcode_返回客户端信息：{}", JSONObject.toJSONString(arpVo));
        if (CollectionUtils.isEmpty(qrcodeStr)) {
            return getResultVo();
        }
        if (Objects.nonNull(map.get("qrcode")) && Objects.nonNull(cardStr.get(map.get("qrcode")))) {
            return getResultVo();
        }
        return new ResultVo("90");
    }

    @PostMapping("/touchSensing")
    private Object touchSensing(HttpServletRequest request) {
        String remoteAddr = IpUtil.getIpAddr(request);
        log.info("touchSensing_接收到客户端{}请求", remoteAddr);
        String serverLocalPublicIp = ArpUtil.getServerLocalPublicIp(serverIpCommand);
        ArpVo arpVo = ArpUtil.getClientInfo(remoteAddr, arpCommand);
        arpVo.setNodeServerIp(serverLocalPublicIp);
        log.info("touchSensing_返回客户端信息：{}", JSONObject.toJSONString(arpVo));
        if (touchSensingFlag) {
            return getResultVo();
        }
        return new ResultVo("90");
    }

    @PostMapping("/connect")
    private Object connect(HttpServletRequest request) {
        String remoteAddr = IpUtil.getIpAddr(request);
        log.info("connect_接收到客户端{}请求", remoteAddr);
        String serverLocalPublicIp = ArpUtil.getServerLocalPublicIp(serverIpCommand);
        ArpVo arpVo = ArpUtil.getClientInfo(remoteAddr, arpCommand);
        arpVo.setNodeServerIp(serverLocalPublicIp);
        log.info("connect_返回客户端信息：{}", JSONObject.toJSONString(arpVo));
        return getResultVo();
    }

    private Object getResultVo() {
        return new ResultVo("70");
    }
}
