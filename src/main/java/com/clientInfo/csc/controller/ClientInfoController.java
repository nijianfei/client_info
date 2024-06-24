package com.clientInfo.csc.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.clientInfo.csc.utils.ArpUtil;
import com.clientInfo.csc.utils.IpUtil;
import com.clientInfo.csc.utils.ScheduledTask;
import com.clientInfo.csc.vo.ArpVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
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

    @Value("${base.bindIp}")
    private String baseBindIp;

    @Value("${out.time:5}")
    private Integer outTime;

    @GetMapping("clientInfo/get")
    private Object getClientInfo(HttpServletRequest request) {
        String remoteAddr = IpUtil.getIpAddr(request);
        log.info("接收到客户端{}请求", remoteAddr);
        String serverLocalPublicIp = ArpUtil.getServerLocalPublicIp(serverIpCommand);
        ArpVo arpVo = ArpUtil.getClientInfo(remoteAddr, arpCommand);
        arpVo.setNodeServerIp(serverLocalPublicIp);
        log.info("返回客户端信息：{}", JSONObject.toJSONString(arpVo));
        return arpVo;
    }

    /**
     * 接收外部请求,确认自身网络状态
     * @param request
     * @return
     */
    @GetMapping("selfTest")
    private Object selfTest(HttpServletRequest request) {
        String remoteAddr = IpUtil.getIpAddr(request);
        String paramValue = request.getParameter("targetId");
        log.info("接收到客户端{}请求,参数：{}", remoteAddr,JSONObject.toJSONString(request.getParameterMap()));
        if (StringUtils.isNotBlank(paramValue)) {
            ScheduledTask.SELF_NET_STATUS.put(paramValue, LocalDateTime.now());
        }
        return JSONUtil.toJsonStr("");
    }

    @GetMapping("showStatus")
    private Object showStatus(HttpServletRequest request){
        String remoteAddr = IpUtil.getIpAddr(request);
        String paramValue = request.getParameter("targetId");
        log.info("接收到客户端{}请求,参数：{}", remoteAddr,JSONObject.toJSONString(request.getParameterMap()));
        if (StringUtils.isNotBlank(paramValue)) {
            LocalDateTime localDateTime = ScheduledTask.SELF_NET_STATUS.get(paramValue);
            if (Objects.isNull(localDateTime)) {
                return JSONUtil.toJsonStr(Map.of());
            }
            return  JSONUtil.toJsonStr(Map.of("status",Duration.between(localDateTime, LocalDateTime.now()).getSeconds() <= outTime));
        }
        return JSONUtil.toJsonStr(Map.of("status",Boolean.FALSE));
    }
}
