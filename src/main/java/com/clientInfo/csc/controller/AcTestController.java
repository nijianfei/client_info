package com.clientInfo.csc.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.clientInfo.csc.utils.AesUtil;
import com.clientInfo.csc.vo.ResultVo;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
    @Value("#{${qrcodeStr}}")
    private Map<String, String> qrcodeStr;
    @Value("${touchSensingFlag}")
    private Boolean touchSensingFlag;
    @Value("${testIsConnect:true}")
    private Boolean testIsConnect;
    @Value("${coreUrl}")
    private String coreUrl;
    @Value("${touchSensingFloors:''}")
    private String touchSensingFloors;

    @Value("${requestTimeOut:500}")
    private Integer requestTimeOut;

    private static final Map<String, String> DOOR_NAME = initDoorInfo();

    @PostMapping("/card")
    private Object card(HttpServletRequest request, @RequestBody Map<String, String> map) {
        checkParam(map);
        String cardId = map.get("cardId");
        if (StringUtils.isBlank(cardId)) {
            log.info("card_接收到客户端请求:{},缺少必要参数,拒绝！", JSONUtil.toJsonStr(map));
            return new ResultVo("90");
        }
        if (Objects.nonNull(cardStr.get(cardId))) {
            log.info("card_接收到客户端请求:{},本地验证通过！", JSONUtil.toJsonStr(map));
            return getResultVo();
        }
        try {
            String postResult = HttpUtil.post(coreUrl + "card", JSONUtil.toJsonStr(map), requestTimeOut);
            JSONObject parseResult = JSONUtil.parseObj(postResult);
            String resultCls = parseResult.getStr("resultCls");
            log.info("card_接收到客户端请求:{}，调用中台返回结果:{}", JSONUtil.toJsonStr(map), postResult);
            return new ResultVo(resultCls);
        } catch (Exception e) {
            log.error("card_接收到客户端请求:{}，调用中台异常 --》", JSONUtil.toJsonStr(map), e);
            return new ResultVo("90");
        }
    }

    private void checkParam(Map<String, String> map) {
        if (map == null || Objects.isNull(map.get("doorName"))) {
            return;
        }
        String doorName = map.get("doorName");
        map.put("DOOR_FULL_NAME", DOOR_NAME.get(doorName));
    }

    @PostMapping("/qrcode")
    private Object qrcode(HttpServletRequest request, @RequestBody Map<String, String> map) {
        checkParam(map);
        String qrcode = map.get("qrcode");
        if (StringUtils.isBlank(qrcode)) {
            log.error("qrcode_接收到客户端请求:{},缺少必要参数,拒绝！", JSONUtil.toJsonStr(map));
            return new ResultVo("90");
        }
        if (qrcode.startsWith("GJDS")) {
            try {
                String deQr = AesUtil.decrypt(qrcode.replace("GJDS", ""));
                if (Objects.nonNull(qrcodeStr.get(deQr))) {
                    log.info("qrcode_接收到客户端请求:{},本地验证二维码[{}]通过！", JSONUtil.toJsonStr(map), deQr);
                    return getResultVo();
                } else {
                    log.error("qrcode_接收到客户端请求:{},本地未找到此二维码[{}]配置！", JSONUtil.toJsonStr(map), deQr);
                }
            } catch (Exception e) {
                log.error("qrcode_接收到客户端请求:{}，解密数据异常 --》", JSONUtil.toJsonStr(map), e);
            }
            return new ResultVo("90");
        }
        try {
            String postResult = HttpUtil.post(coreUrl + "qrcode", JSONUtil.toJsonStr(map), requestTimeOut);
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
    private Object touchSensing(HttpServletRequest request, @RequestBody Map<String, String> map) {
        checkParam(map);
        String doorName = map.get("doorName");
        if (doorName == null) {
            return new ResultVo("90");
        }
        if (touchSensingFlag) {
            log.info("touchSensing_接收到客户端请求:{}", JSONUtil.toJsonStr(map));
            if (touchSensingFloors.contains(",")) {
                String[] split = touchSensingFloors.split(",");
                for (String s : split) {
                    if (doorName.startsWith(s)) {
                        return getResultVo();
                    }
                }
            }
            return new ResultVo("90");
        } else {
            try {
                String postResult = HttpUtil.post(coreUrl + "qrcode", JSONUtil.toJsonStr(map), requestTimeOut);
                JSONObject parseResult = JSONUtil.parseObj(postResult);
                String resultCls = parseResult.getStr("resultCls");
                log.info("touchSensing_接收到客户端请求:{}，调用中台返回结果:{}", JSONUtil.toJsonStr(map), postResult);
                return new ResultVo(resultCls);
            } catch (Exception e) {
                log.error("qrcode_接收到客户端请求:{}，调用中台异常 --》", JSONUtil.toJsonStr(map), e);
                return new ResultVo("90");
            }
        }

    }

    @PostMapping("/connect")
    private Object connect(HttpServletRequest request) {
        String param = null;
        try {
            param = getParams(request);
            log.info("connect_接收到客户端[本地策略:{}]请求:{}", testIsConnect, param);
            String postResult = HttpUtil.post(coreUrl + "connect", param, requestTimeOut);
            log.info("connect_接收到客户端请求:{}，调用中台返回结果:{}", param, postResult);
            JSONObject parseResult = JSONUtil.parseObj(postResult);
            String resultCls = parseResult.getStr("resultCls");
            return new ResultVo(resultCls);
        } catch (Exception e) {
            log.info("connect_接收到客户端请求:{},异常--》", param, e);
            if (testIsConnect) {
                return getResultVo();
            } else {
                return new ResultVo("90");
            }
        }
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

    public static Map<String, String> initDoorInfo() {
        final Map<String, String> doorInfo = new HashMap<>();
        doorInfo.put("019GJDS_A06_001", "6F-601-小会议室（一）");
        doorInfo.put("019GJDS_A06_002", "6F-602-大会议室");
        doorInfo.put("019GJDS_A06_003", "6F-603-小会议室(二)");
        doorInfo.put("019GJDS_A06_004", "6F-604-戊级备用间（一）");
        doorInfo.put("019GJDS_A06_005", "6F-605-弱电间");
        doorInfo.put("019GJDS_A06_006", "6F-606-男更衣室");
        doorInfo.put("019GJDS_A06_007", "6F-607-功能房间");
        doorInfo.put("019GJDS_A06_008", "6F-608-戊类备用间（三）");
        doorInfo.put("019GJDS_A06_009", "6F-609-监控室");
        doorInfo.put("019GJDS_A06_011", "6F-611-女更衣室");
        doorInfo.put("019GJDS_A06_012", "6F-612-电梯前室");
        doorInfo.put("019GJDS_A06_013", "6F-613-1#前室");
        doorInfo.put("019GJDS_A06_014", "6F-614-电梯厅（一）北侧");
        doorInfo.put("019GJDS_A06_015", "6F-615-电梯厅（一）南侧");
        doorInfo.put("019GJDS_A06_016", "6F-616-2#前室");
        doorInfo.put("019GJDS_A06_017", "6F-617-茶水间");
        doorInfo.put("019GJDS_A07_001", "7F-701-小会议室（一）");
        doorInfo.put("019GJDS_A07_002", "7F-702-大会议室");
        doorInfo.put("019GJDS_A07_003", "7F-703-临时洽谈室（一）");
        doorInfo.put("019GJDS_A07_004", "7F-704-弱电间");
        doorInfo.put("019GJDS_A07_005", "7F-705-档案室");
        doorInfo.put("019GJDS_A07_006", "7F-706-小会议室（二）");
        doorInfo.put("019GJDS_A07_007", "7F-707-男更衣室");
        doorInfo.put("019GJDS_A07_008", "7F-708-独立办公室");
        doorInfo.put("019GJDS_A07_009", "7F-709-戊类备用间（二）");
        doorInfo.put("019GJDS_A07_011", "7F-711-女更衣室");
        doorInfo.put("019GJDS_A07_012", "7F-712-电梯前室");
        doorInfo.put("019GJDS_A07_013", "7F-713-1#前室");
        doorInfo.put("019GJDS_A07_014", "7F-714-电梯厅（一）北侧");
        doorInfo.put("019GJDS_A07_015", "7F-715-电梯厅（一）南侧");
        doorInfo.put("019GJDS_A07_016", "7F-716-2#前室");
        doorInfo.put("019GJDS_A07_017", "7F-717-茶水间");
        doorInfo.put("019GJDS_A08_001", "8F-801-会议室-北门");
        doorInfo.put("019GJDS_A08_002", "8F-802-会议室-东门");
        doorInfo.put("019GJDS_A08_003", "8F-803-钢瓶间");
        doorInfo.put("019GJDS_A08_004", "8F-804-数据机房");
        doorInfo.put("019GJDS_A08_005", "8F-805-电池间");
        doorInfo.put("019GJDS_A08_006", "8F-806-戊类备用间（一）");
        doorInfo.put("019GJDS_A08_007", "8F-807-配电间");
        doorInfo.put("019GJDS_A08_008", "8F-808-弱电间");
        doorInfo.put("019GJDS_A08_009", "8F-809-男更衣室");
        doorInfo.put("019GJDS_A08_010", "8F-810-功能房间");
        doorInfo.put("019GJDS_A08_011", "8F-811-戊类备用间（三）");
        doorInfo.put("019GJDS_A08_012", "8F-812-母婴室");
        doorInfo.put("019GJDS_A08_014", "8F-814-女更衣室");
        doorInfo.put("019GJDS_A08_015", "8F-815-电梯前室");
        doorInfo.put("019GJDS_A08_016", "8F-816-1#前室");
        doorInfo.put("019GJDS_A08_017", "8F-817-电梯厅（一）北侧");
        doorInfo.put("019GJDS_A08_018", "8F-818-电梯厅（一）南侧");
        doorInfo.put("019GJDS_A08_019", "8F-819-2#前室");
        doorInfo.put("019GJDS_A08_020", "8F-820-数据机房-西");
        doorInfo.put("019GJDS_A08_021", "8F-821-数据机房-东");
        doorInfo.put("019GJDS_A08_022", "8F-822-茶水间");
        doorInfo.put("019GJDS_A09_001", "9F-901-小会议室（一）");
        doorInfo.put("019GJDS_A09_002", "9F-902-大会议室");
        doorInfo.put("019GJDS_A09_003", "9F-903-小会议室(二)");
        doorInfo.put("019GJDS_A09_004", "9F-904-戊级备用间（一）");
        doorInfo.put("019GJDS_A09_005", "9F-905-弱电间");
        doorInfo.put("019GJDS_A09_006", "9F-906-男更衣室");
        doorInfo.put("019GJDS_A09_007", "9F-907-功能房间");
        doorInfo.put("019GJDS_A09_008", "9F-908-戊类备用间（三）");
        doorInfo.put("019GJDS_A09_009", "9F-909-母婴室");
        doorInfo.put("019GJDS_A09_011", "9F-911-女更衣室");
        doorInfo.put("019GJDS_A09_012", "9F-912-电梯前室");
        doorInfo.put("019GJDS_A09_013", "9F-913-1#前室");
        doorInfo.put("019GJDS_A09_014", "9F-914-电梯厅（一）北侧");
        doorInfo.put("019GJDS_A09_015", "9F-915-电梯厅（一）南侧");
        doorInfo.put("019GJDS_A09_016", "9F-916-2#前室");
        doorInfo.put("019GJDS_A09_017", "9F-917-茶水间");
        doorInfo.put("019GJDS_A10_001", "10F-1001-小会议室（一）");
        doorInfo.put("019GJDS_A10_002", "10F-1002-大会议室");
        doorInfo.put("019GJDS_A10_003", "10F-1003-小会议室(二)");
        doorInfo.put("019GJDS_A10_004", "10F-1004-戊级备用间（一）");
        doorInfo.put("019GJDS_A10_005", "10F-1005-弱电间");
        doorInfo.put("019GJDS_A10_006", "10F-1006-男更衣室");
        doorInfo.put("019GJDS_A10_007", "10F-1007-功能房间");
        doorInfo.put("019GJDS_A10_008", "10F-1008-戊类备用间（三）");
        doorInfo.put("019GJDS_A10_009", "10F-1009-戊类备用间（二）");
        doorInfo.put("019GJDS_A10_011", "10F-1011-女更衣室");
        doorInfo.put("019GJDS_A10_012", "10F-1012-电梯前室");
        doorInfo.put("019GJDS_A10_013", "10F-1013-1#前室");
        doorInfo.put("019GJDS_A10_014", "10F-1014-电梯厅（一）北侧");
        doorInfo.put("019GJDS_A10_015", "10F-1015-电梯厅（一）南侧");
        doorInfo.put("019GJDS_A10_016", "10F-1016-2#前室");
        doorInfo.put("019GJDS_A10_017", "10F-1017-茶水间");
        doorInfo.put("019GJDS_A11_001", "11F-1101-小会议室（一）");
        doorInfo.put("019GJDS_A11_002", "11F-1102-大会议室");
        doorInfo.put("019GJDS_A11_003", "11F-1103-小会议室(二)");
        doorInfo.put("019GJDS_A11_004", "11F-1104-戊级备用间（一）");
        doorInfo.put("019GJDS_A11_005", "11F-1105-弱电间");
        doorInfo.put("019GJDS_A11_006", "11F-1106-男更衣室");
        doorInfo.put("019GJDS_A11_007", "11F-1107-功能房间");
        doorInfo.put("019GJDS_A11_008", "11F-1108-戊类备用间（三）");
        doorInfo.put("019GJDS_A11_009", "11F-1109-戊类备用间（二）");
        doorInfo.put("019GJDS_A11_011", "11F-1111-女更衣室");
        doorInfo.put("019GJDS_A11_012", "11F-1112-电梯前室");
        doorInfo.put("019GJDS_A11_013", "11F-1113-1#前室");
        doorInfo.put("019GJDS_A11_014", "11F-1114-电梯厅（一）北侧");
        doorInfo.put("019GJDS_A11_015", "11F-1115-电梯厅（一）南侧");
        doorInfo.put("019GJDS_A11_016", "11F-1116-2#前室");
        doorInfo.put("019GJDS_A11_017", "11F-1117-茶水间");
        return doorInfo;
    }

    public static void main(String[] args) {
        Map<String, String> stringStringMap = initDoorInfo();
        stringStringMap.keySet().stream().sorted().toList().forEach(k -> {
            String v = stringStringMap.get(k);
            String[] split = v.split(",");
            System.out.println(split[3] + "-" + split[2] + "-" + split[1]);
        });

    }
}
