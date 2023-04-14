package com.clientInfo.csc.controller;

import com.alibaba.fastjson2.JSONObject;
import com.clientInfo.csc.utils.ArpUtil;
import com.clientInfo.csc.utils.IpUtil;
import com.clientInfo.csc.vo.ArpVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@Slf4j
@CrossOrigin(origins = "*")
@PropertySource(value = "classpath:application.properties", encoding = "utf-8")
@RestController()
public class ClientInfoController {
    @Value("${arpCommand}")
    private String arpCommand;
    @Value("${serverIpCommand}")
    private String serverIpCommand;

    @GetMapping("clientInfo/get")
    private Object getClientInfo(HttpServletRequest request) {
        String remoteAddr = IpUtil.getIpAddr(request);
        log.info("接收到客户端{}请求",remoteAddr);
        String serverLocalPublicIp = ArpUtil.getServerLocalPublicIp(serverIpCommand);
        ArpVo arpVo = ArpUtil.getClientInfo(remoteAddr, arpCommand);
        arpVo.setNodeServerIp(serverLocalPublicIp);
        log.info("返回客户端信息：{}", JSONObject.toJSONString(arpVo));
        return arpVo;
    }
}
