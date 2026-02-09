package com.clientInfo.csc;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum DeviceEventEnum {

    DEVICE_ACTIVE("device_active","设备激活"),
    DEVICE_DELETE("device_delete","设备删除"),
    DEVICE_CONNECT_CHANGE("device_connect_change","设备连接状态变更"),
    ;

    private String code;
    private String name;

    private static Map<String, DeviceEventEnum> mappingMap = Arrays.stream(DeviceEventEnum.values()).collect(Collectors.toMap(DeviceEventEnum::getCode, Function.identity()));

    DeviceEventEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static DeviceEventEnum getEnum(String code){
        return mappingMap.get(code);
    }
}
