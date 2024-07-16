package com.clientInfo.csc.utils;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.beust.jcommander.internal.Maps;
import com.clientInfo.csc.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @version V2.3
 * @ClassName:ScheduledTask.java
 * @author: wgcloud
 * @date: 2019年11月16日
 * @Description: ScheduledTask.java
 * @Copyright: 2017-2024 www.wgstart.com. All rights reserved.
 */
@Component
public class ScheduledTask {

    private Logger logger = LoggerFactory.getLogger(ScheduledTask.class);
    public static List<AppInfo> appInfoList = Collections.synchronizedList(new ArrayList<AppInfo>());
    @Autowired
    private RestUtil restUtil;
    @Autowired
    private CommonConfig commonConfig;

    //节点类型 主或者备
    @Value("${node.type}")
    private String nodeType;
    //终端设备ID
    @Value("${device.id}")
    private String deviceId;

    @Value("${tk.net.ip}")
    private String tkNetIp;
    //网络超时设定
    @Value("${out.time:15}")
    private Integer outTime;
    @Value("${ac.service.url}")
    private String acServiceUrl;

    //集群内另一节点内网IP
    @Value("${check.inner.ips}")
    private String checkInnerIps;
    //集群内另一节点外网IP
    @Value("${check.out.ips}")
    private String checkOutIps;
    //是否检查改变集群节点状态
    @Value("${is.check.cluster:false}")
    private Boolean isCheckCluster;


    private SystemInfo systemInfo = null;

    public static final String R_INNER = "R-inner";
    public static final String R_OUT = "R-out";
    public static final Map<String, LocalDateTime> SELF_NET_STATUS = new HashMap<>();

    static {
        SELF_NET_STATUS.put(R_INNER, LocalDateTime.now());
        SELF_NET_STATUS.put(R_OUT, LocalDateTime.now());
    }

    /**
     * 线程池
     */
    static ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 50, 10, TimeUnit.MINUTES, new LinkedBlockingDeque<>());

    /**
     * 30秒后执行，每隔5分钟执行, 单位：ms。
     * 获取监控进程
     */
//    @Scheduled(initialDelay = 60 * 1000L, fixedRate = 300 * 1000)
    public void showTask() {
        logger.info("ScheduledTask_showTask:{}", JSON.toJSONString(SurveillanceItem.recordMap, JSONWriter.Feature.PrettyFormat));
    }

    /**
     * 60秒后执行，每隔120秒执行, 单位：ms。
     */
//    @Scheduled(initialDelay = 1 * 1000L, fixedRate = 1 * 1000)
    public void minTask() {
        List<AppInfo> APP_INFO_LIST_CP = new ArrayList<AppInfo>();
        APP_INFO_LIST_CP.addAll(appInfoList);
        JSONObject jsonObject = new JSONObject();
        LogInfo logInfo = new LogInfo();
        Timestamp t = FormatUtil.getNowTime();
        logInfo.setHostname(commonConfig.getBindIp() + "：Agent错误");
        logInfo.setCreateTime(t);
        try {
            oshi.SystemInfo si = new oshi.SystemInfo();

            HardwareAbstractionLayer hal = si.getHardware();
            OperatingSystem os = si.getOperatingSystem();

            // 操作系统信息
            systemInfo = OshiUtil.os(hal.getProcessor(), os);
            systemInfo.setCreateTime(t);
            // 文件系统信息
            List<DeskState> deskStateList = OshiUtil.file(t, os.getFileSystem());
            // cpu信息
            CpuState cpuState = OshiUtil.cpu(hal.getProcessor());
            cpuState.setCreateTime(t);
            // 内存信息
            MemState memState = OshiUtil.memory(hal.getMemory());
            memState.setCreateTime(t);
            // 网络流量信息
            NetIoState netIoState = OshiUtil.net(hal);
            netIoState.setCreateTime(t);
            // 系统负载信息
            SysLoadState sysLoadState = OshiUtil.getLoadState(systemInfo, hal.getProcessor());
            if (sysLoadState != null) {
                sysLoadState.setCreateTime(t);
            }
            if (cpuState != null) {
                jsonObject.put("cpuState", cpuState);
            }
            if (memState != null) {
                jsonObject.put("memState", memState);
            }
            if (netIoState != null) {
                jsonObject.put("netIoState", netIoState);
            }
            if (sysLoadState != null) {
                jsonObject.put("sysLoadState", sysLoadState);
            }
            if (systemInfo != null) {
                if (memState != null) {
                    systemInfo.setVersionDetail(systemInfo.getVersion() + "，总内存：" + oshi.util.FormatUtil.formatBytes(hal.getMemory().getTotal()));
                    systemInfo.setMemPer(memState.getUsePer());
                } else {
                    systemInfo.setMemPer(0d);
                }
                if (cpuState != null) {
                    systemInfo.setCpuPer(cpuState.getSys());
                } else {
                    systemInfo.setCpuPer(0d);
                }
                jsonObject.put("systemInfo", systemInfo);
            }
            if (deskStateList != null) {
                jsonObject.put("deskStateList", deskStateList);
            }
            //进程信息
            if (APP_INFO_LIST_CP.size() > 0) {
                List<AppInfo> appInfoResList = new ArrayList<>();
                List<AppState> appStateResList = new ArrayList<>();
                for (AppInfo appInfo : APP_INFO_LIST_CP) {
                    appInfo.setHostname(commonConfig.getBindIp());
                    appInfo.setCreateTime(t);
                    appInfo.setState("1");
                    String pid = FormatUtil.getPidByFile(appInfo);
                    if (StringUtils.isEmpty(pid)) {
                        continue;
                    }
                    AppState appState = OshiUtil.getLoadPid(pid, os, hal.getMemory());
                    if (appState != null) {
                        appState.setCreateTime(t);
                        appState.setAppInfoId(appInfo.getId());
                        appInfo.setMemPer(appState.getMemPer());
                        appInfo.setCpuPer(appState.getCpuPer());
                        appInfoResList.add(appInfo);
                        appStateResList.add(appState);
                    }
                }

                jsonObject.put("appInfoList", appInfoResList);
                jsonObject.put("appStateList", appStateResList);
            }

            logger.debug("---------------" + jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logInfo.setInfoContent(e.toString());
        } finally {
            if (!StringUtils.isEmpty(logInfo.getInfoContent())) {
                jsonObject.put("logInfo", logInfo);
            }
            logger.info("---------------\r\n" + JSON.toJSONString(jsonObject, JSONWriter.Feature.PrettyFormat));
//            restUtil.post(commonConfig.getServerUrl() + "/wgcloud/agent/minTask", jsonObject);
        }

    }

    /**
     * 30秒后执行，每隔5分钟执行, 单位：ms。
     * 获取监控进程
     */
//    @Scheduled(initialDelay = 28 * 1000L, fixedRate = 300 * 1000)
    public void appInfoListTask() {
        JSONObject jsonObject = new JSONObject();
        LogInfo logInfo = new LogInfo();
        Timestamp t = FormatUtil.getNowTime();
        logInfo.setHostname(commonConfig.getBindIp() + "：Agent获取进程列表错误");
        logInfo.setCreateTime(t);
        try {
            JSONObject paramsJson = new JSONObject();
            paramsJson.put("hostname", commonConfig.getBindIp());
            String resultJson = restUtil.post(commonConfig.getServerUrl() + "/wgcloud/appInfo/agentList", paramsJson);
            if (resultJson != null) {
                JSONArray resultArray = JSONUtil.parseArray(resultJson);
                appInfoList.clear();
                if (resultArray.size() > 0) {
                    appInfoList = JSONUtil.toList(resultArray, AppInfo.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logInfo.setInfoContent(e.toString());
        } finally {
            if (!StringUtils.isEmpty(logInfo.getInfoContent())) {
                jsonObject.put("logInfo", logInfo);
            }
            restUtil.post(commonConfig.getServerUrl() + "/wgcloud/agent/minTask", jsonObject);
        }
    }


    @Scheduled(initialDelay = 2 * 1000L, fixedRate = 5 * 1000)
    public void checkOneselfStatus() {
        String[] innerIps = checkInnerIps.split(",");
        Arrays.asList(innerIps).forEach(ip -> executor.execute(() -> {
            if (isReachable(ip)) {
                ScheduledTask.SELF_NET_STATUS.put(R_INNER, LocalDateTime.now());
            }
        }));

        String[] outIps = checkOutIps.split(",");
        Arrays.asList(outIps).forEach(ip -> executor.execute(() -> {
            if (isReachable(ip)) {
                ScheduledTask.SELF_NET_STATUS.put(R_OUT, LocalDateTime.now());
            }
        }));
    }

    private boolean netStatus = false;

    /**
     * 30秒后执行，每隔1分钟执行, 单位：ms。
     * 获取监控进程
     */
    @Scheduled(initialDelay = 5 * 1000L, fixedRate = 60 * 1000)
    public void reportNetInfo() {
        LocalDateTime now = LocalDateTime.now();
        ServerStatusEntity serverStatus = new ServerStatusEntity();
        LocalDateTime rInnerDate = SELF_NET_STATUS.get(R_INNER);
        LocalDateTime rOutDate = SELF_NET_STATUS.get(R_OUT);
        long rInnerSecondsDiff = Duration.between(rInnerDate, now).getSeconds();
        System.out.println("R内网-当前时间差：" + rInnerSecondsDiff + "秒");
        long rOutSecondsDiff = Duration.between(rOutDate, now).getSeconds();
        System.out.println("R外网-当前时间差：" + rOutSecondsDiff + "秒");
        boolean rFlagInner = rInnerSecondsDiff <= outTime;
        boolean rFlagOut = rOutSecondsDiff <= outTime;
        logger.info("节点类型:{},是否检查集群状态:{},集主机网络状态:外网:{},内网:{}", nodeType, isCheckCluster, rFlagOut, rFlagInner);
        serverStatus.setOutNetStatus(rFlagOut ? "0" : "-1");
        serverStatus.setInnerNetStatus(rFlagInner ? "0" : "-1");
        serverStatus.setNodeType(nodeType);

        if (isCheckCluster) {
            //如果是主节点
            if (Objects.equals(nodeType, "Active")) {
                //内外网状态不一致
                if (rFlagOut != rFlagInner) {
                    //备机网络正常,则停止本机所有集群节点
                    logger.info("门禁集群状态不一致,停止本机所有集群节点");
                    NlbUtil.stopNode("");
                    netStatus = false;
                } else {
                    //集群状态一致,并且集群不可用 , 且 内外网正常
                    if (rFlagInner && rFlagOut && !netStatus) {
                        Map<String, String> interfaceNameMap = NlbUtil.queryInterfaceName();
                        for (String vip : interfaceNameMap.keySet()) {
                            //自身集群状态
                            String nodeStatus = NlbUtil.getNodeStatus(vip);
                            //本节点非启动,则启动集群节点
                            if (!Objects.equals("Started", nodeStatus)) {
                                logger.info("本机内外网正常,集群内节点为{},启动本机所有集群节点", nodeStatus);
                                NlbUtil.startNode(vip);
                            }
                        }
                        netStatus = true;
                    }
                }
            }
        }

        Map<String, String> strMap = Maps.newHashMap("moduleId", "RC_002", "clientId", deviceId, "content", JSON.toJSONString(serverStatus));
        restUtil.postUrlParam(acServiceUrl, strMap);

    }

    private boolean isReachable(String ipAddress) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            if (address.isReachable(3000)) {
                logger.info("设备可达:{}", ipAddress);
                return true;
            } else {
                logger.info("设备不可达:{}", ipAddress);
                return false;
            }
        } catch (Exception e) {
            logger.error("测试[{}]网络异常:{}", ipAddress, e.getMessage(), e);
            return false;
        }
    }
}
