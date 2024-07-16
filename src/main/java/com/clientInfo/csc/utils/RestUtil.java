package com.clientInfo.csc.utils;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @version V2.3
 * @ClassName:RestUtil.java
 * @author: wgcloud
 * @date: 2019年11月16日
 * @Description: RestUtil.java
 * @Copyright: 2017-2024 www.wgstart.com. All rights reserved.
 */
@Slf4j
@Component
public class RestUtil {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CommonConfig commonConfig;

    public String post(String url, JSONObject jsonObject) {
        if (null != jsonObject) {
            jsonObject.put("wgToken", MD5Utils.GetMD5Code(commonConfig.getWgToken()));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<String> httpEntity = new HttpEntity<>(JSONUtil.parse(jsonObject).toString(), headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        return responseEntity.getBody();
    }

    public JSONObject post(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<String> httpEntity = new HttpEntity<>("", headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        return JSONUtil.parseObj(responseEntity.getBody());
    }

    public String postUrlParam(String url,Map<String,String> urlParam) {
        log.info("参数预览:\r\n{}", JSON.toJSONString(urlParam, JSONWriter.Feature.PrettyFormat));
        String paramsStr = null;
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("field", JSONUtil.toJsonStr(urlParam));
        paramMap.put("userID", "engine");
        paramMap.put("sign", "engine");
        paramMap.put("method", "20000-IF17");
        paramsStr = HttpUtil.toParams(paramMap);
        String post = null;
        try {
            post = HttpUtil.post(url, paramsStr, 5000);
        } catch (Exception e) {
            log.info("请求门禁服务[{}]上报服务器状态_参数：{}，异常：{}", url, JSONUtil.toJsonStr(paramMap),e.getMessage(),e);
            throw new RuntimeException(e);
        }
        log.info("请求门禁服务[{}]上报服务器状态_参数：{}，返回结果：{}", url, paramsStr, post);
        Object invokeCls = JSONUtil.parseObj(post).get("invokeCls");
        if (!"70".equals(invokeCls)) {
            throw new RuntimeException("上报节点服务器信息异常");
        }
        return invokeCls.toString();
    }
    public JSONObject get(String url) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        return JSONUtil.parseObj(responseEntity.getBody());
    }

}
