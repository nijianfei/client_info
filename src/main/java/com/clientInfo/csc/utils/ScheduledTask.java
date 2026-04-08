package com.clientInfo.csc.utils;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.beust.jcommander.internal.Maps;
import com.clientInfo.csc.entity.*;
import com.clientInfo.csc.vo.WebLogVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    private static Logger logger = LoggerFactory.getLogger(ScheduledTask.class);
    public static List<AppInfo> appInfoList = Collections.synchronizedList(new ArrayList<AppInfo>());

    @Autowired
    private RestUtil restUtil;
    @Autowired
    private CommonConfig commonConfig;


    //门禁WEB log 路径
    @Value("${ac.web.log.path}")
    private String acWebLogPath;

    //门禁WEB 重启脚本
    @Value("${script.check.web.full.name}")
    private String scriptCheckWebFullName;

    //节点类型 主或者备 MASTER BACKUP
    @Value("${node.type}")
    private String nodeType;
    //终端设备ID
    @Value("${device.id}")
    private String deviceId;

    //终端设备ID
    @Value("${module.id}")
    private String moduleId;
    @Value("${tk.net.ip:0}")
    private String tkNetIp;
    @Value("${tk.net.port:0}")
    private Integer tkNetPort;
    //网络超时设定
    @Value("${out.time:30}")
    private Integer outTime;
    @Value("${ac.service.url}")
    private String acServiceUrl;

    //集群内另一节点内网IP
    @Value("${check.inner.ips}")
    private String checkInnerIps;
    //集群内另一节点外网IP
    @Value("${check.out.ips}")
    private String checkOutIps;
    @Value("${check.network.time.out:3000}")
    private Integer checkNetworkTimeOut;
    @Value("${check.max.read.line:20}")
    private Integer checkMaxReadLine;

    @Value("${restart.web_site.command}")
    private String restartWebSiteCommand;

    @Value("${restart.app_pool.command}")
    private String restartAppPoolCommand;

    @Value("${iis.reset.command}")
    private String iisResetCommand = "iisreset";

    private SystemInfo systemInfo = null;

    public static final String R_INNER = "R-inner";
    public static final String R_TK = "R-tk";
    public static final String R_OUT = "R-out";
    public static final Map<String, LocalDateTime> SELF_NET_STATUS = new HashMap<>();
    private static Map<String, Map<String, NlbClusterNodeInfo>> nodesStatusMap = new ConcurrentHashMap<>();
    /**
     * 线程池
     */
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 20, 10, TimeUnit.MINUTES, new LinkedBlockingDeque<>(10));

    static {
        SELF_NET_STATUS.put(R_INNER, LocalDateTime.now());
        SELF_NET_STATUS.put(R_TK, LocalDateTime.now());
        SELF_NET_STATUS.put(R_OUT, LocalDateTime.now());
    }

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

    private static int errTotalCount = 0;
    private static final int TRY_COUNT = 3;

    @Scheduled(initialDelay = 30 * 1000L, fixedRate = 30 * 1100L)
    public void checkAcWebStatus() {

        try {
            logger.debug("checkAcWebStatus执行线程: {}", Thread.currentThread().getName());
            for (int i = 1; i <= TRY_COUNT; i++) {
                List<Boolean> webStatus = checkAcWeb();
                if (webStatus.isEmpty()) {
                    logger.debug("checkAcWebStatus-第【{}】次执行:未找到日志文件，break", i);
                }
                if (webStatus.get(0)) {
                    logger.debug("checkAcWebStatus-第【{}】次执行: 无异常，break", i);
                    errTotalCount = 0;
                    break;
                } else {
                    /*本地异常与上次异常相同*/
                    if (webStatus.get(1)) {
                        logger.debug("checkAcWebStatus-开始第【{}】次执行:存在【相同】异常，continue-脚本执行!", i);
                        sleep(10 * 1000);
                        continue;
                    }
                    logger.debug("checkAcWebStatus-开始第【{}】次执行:errTotalCount:{}", i, ++errTotalCount);
                    if (errTotalCount >= TRY_COUNT * 2) {
                        CmdUtil.execCmd(iisResetCommand);
                        logger.debug("checkAcWebStatus-异常次数超过【{}】次，break", TRY_COUNT * 5);
                        errTotalCount = 0;
                        break;
                    }
                    logger.debug("checkAcWebStatus存在异常，开始第【{}】次执行脚本【START】: {}", i, restartAppPoolCommand);
                    //不正常 重启web ,然后再次验证
                    CmdUtil.execPowerShellCmd(restartAppPoolCommand);
                    CmdUtil.execPowerShellCmd(restartWebSiteCommand);
                    logger.debug("checkAcWebStatus存在异常，开始第【{}】次执行脚本【END】: {}", i, restartAppPoolCommand);
                    sleep(10 * 1000);
                }
            }
        } catch (Exception e) {
            logger.debug("checkAcWebStatus_cache_err:{}", e.getMessage(), e);
        }
    }

    private void sleep(int millis) {
        try {
            logger.debug("sleep:{}millis", millis);
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    @Scheduled(initialDelay = 2 * 1000L, fixedRate = 15 * 1000)
    public void checkOneselfStatus() {
        try {
            logger.debug("checkOneselfStatus执行线程: {},executor 工作线程总数:{},活跃线程数:{},空闲线程数:{},任务队列积压:{}", Thread.currentThread().getName(), executor.getPoolSize(), executor.getActiveCount(), (executor.getPoolSize() - executor.getActiveCount()), executor.getQueue().size());
            String[] innerIps = checkInnerIps.split(",");
            Arrays.asList(innerIps).forEach(ip -> executor.execute(() -> {
                long start = System.currentTimeMillis();
                try {
                    if (isReachable(ip)) {
                        ScheduledTask.SELF_NET_STATUS.put(R_INNER, LocalDateTime.now());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    logger.debug("isReachable1耗时: " + (System.currentTimeMillis() - start));
                }
            }));

            String[] outIps = checkOutIps.split(",");
            Arrays.asList(outIps).forEach(ip -> executor.execute(() -> {
                long start = System.currentTimeMillis();
                try {
                    if (isReachable(ip)) {
                        ScheduledTask.SELF_NET_STATUS.put(R_OUT, LocalDateTime.now());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    logger.debug("isReachable2耗时: " + (System.currentTimeMillis() - start));
                }
            }));
            executor.execute(() -> {
                long start = System.currentTimeMillis();
                try {
                    nodesStatusMap = NlbUtil.getNodesStatus();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    logger.debug("getNodesStatus耗时: " + (System.currentTimeMillis() - start));
                }
            });
        } catch (Exception e) {
            logger.error("checkOneselfStatus_catch_err:{}", e.getMessage(), e);
        }

    }

    private boolean netStatus = false;

    /**
     * 30秒后执行，每隔1分钟执行, 单位：ms。
     * 获取监控进程
     */
    @Scheduled(initialDelay = 5 * 1000L, fixedRate = 60 * 1000)
    public void reportNetInfo() {
        try {
            logger.debug("reportNetInfo执行线程: {}", Thread.currentThread().getName());
            LocalDateTime now = LocalDateTime.now();
            ServerStatusEntity serverStatus = new ServerStatusEntity();
            LocalDateTime rInnerDate = SELF_NET_STATUS.get(R_INNER);
            LocalDateTime rOutDate = SELF_NET_STATUS.get(R_OUT);
            LocalDateTime rTaiKang = SELF_NET_STATUS.get(R_TK);
            long rInnerSecondsDiff = Duration.between(rInnerDate, now).getSeconds();
            logger.info("R内网-当前时间差：" + rInnerSecondsDiff + "秒");
            long rOutSecondsDiff = Duration.between(rOutDate, now).getSeconds();
            logger.info("R外网-当前时间差：" + rOutSecondsDiff + "秒");
            long rTkSecondsDiff = Duration.between(rTaiKang, now).getSeconds();
            logger.info("R泰康网络-当前时间差：" + rTkSecondsDiff + "秒");
            boolean rFlagInner = rInnerSecondsDiff <= outTime;
            boolean rFlagOut = rOutSecondsDiff <= outTime;
            boolean rFlagTk = rTkSecondsDiff <= outTime;
            logger.info("节点类型:{},网络状态:外网:{},内网:{},泰康网:{}", nodeType, rFlagOut, rFlagInner, rFlagTk);
            serverStatus.setOutNetStatus(rFlagOut ? "0" : "-1");
            serverStatus.setInnerNetStatus(rFlagInner ? "0" : "-1");
            if (!(Objects.equals("0", tkNetIp) || Objects.equals("0", tkNetPort))) {
                serverStatus.setTkNetStatus(rFlagTk ? "0" : "-1");
            }
            serverStatus.setNodeType(nodeType);
            Map<String, String> strMap = Maps.newHashMap("moduleId", moduleId, "clientId", deviceId, "content", JSON.toJSONString(serverStatus));
            restUtil.postUrlParam(acServiceUrl, strMap);
        } catch (Exception e) {
            logger.error("reportNetInfo_catch_err:{}", e.getMessage(), e);
        }

    }


    private String getHostName() {
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error("getHostName_err:{}", e.getMessage(), e);
        }
        logger.debug("getHostName:{}", hostName);
        return hostName;
    }

    private boolean isExpectClusterStatus() {
        Map<String, NlbClusterNodeInfo> nodeMap = nodesStatusMap.get(getHostName());
        if (nodeMap == null || nodeMap.size() < 2) {
            logger.debug("isExpectClusterStatus_nodeMap:{}", JSONUtil.toJsonStr(nodeMap));
            return false;
        }
        List<NlbClusterNodeInfo> nodeList = nodeMap.values().stream().filter(o -> !o.getInterfaceName().contains("泰康")).toList();
        for (int i = 0; i < nodeList.size() - 1; i++) {
            NlbClusterNodeInfo n = nodeList.get(i);
            NlbClusterNodeInfo n1 = nodeList.get(i + 1);
            if (!n.getState().equals(n1.getState())) {
                logger.debug("isExpectClusterStatus_equals_n{}【{}】:n{}【{}】", i, n.getState(), i + 1, n1.getState());
                return false;
            }
        }
        return true;
    }

    private boolean isMaster(String interfaceName) {
        boolean isMaster = false;
        Map<String, NlbClusterNodeInfo> nodeMap = nodesStatusMap.get(getHostName());
        NlbClusterNodeInfo nlbClusterNodeInfo = nodeMap.get(interfaceName);
        Optional<String> first = nodeMap.values().stream().map(NlbClusterNodeInfo::getHostId).sorted().findFirst();
        if (first.isPresent() && Objects.nonNull(nlbClusterNodeInfo)) {
            isMaster = first.get().equals(nlbClusterNodeInfo.getHostId());
        }
        logger.debug("isMaster:{}", isMaster);
        return isMaster;
    }

    // 上次文件状态（用于快速判断）
    private static String lastFileState = "";
    // 上次最后N行MD5（用于精准判断）
    private static String lastLinesMD5 = "";
    private static boolean lastStatus = true;
    private final static int FIELD_COUNT = 15;

    private List<Boolean> checkAcWeb() {
        try {
            String fileName = getLastTimeFileName(acWebLogPath);
            if (StringUtils.isBlank(fileName)) {
                return Collections.emptyList();
            }
            WebLogVo webLogVo = readIISLogLastLines(Paths.get(acWebLogPath, fileName).toString(), checkMaxReadLine);
            // 判断文件是否变化
            boolean fileChanged = !webLogVo.fileState.equals(lastFileState);
            boolean contentChanged = !webLogVo.linesMD5.equals(lastLinesMD5);
            int errCount = 0;
            boolean status = true;
            boolean isSameErr = false;
            List<String> lastLineContents = webLogVo.getLastLineContent();
            if (fileChanged && contentChanged) {
                logger.info("===== IIS Log [{}] 已更新 =====", fileName);
                logger.info("在日志文件[{}]中,指定读取最后{}行,共读取到[{}]行日志内容", fileName, checkMaxReadLine, lastLineContents.size());
                for (String line : lastLineContents) {
                    String[] split = line.split(",");
                    if (split.length == FIELD_COUNT) {
                        if (!Objects.equals(split[10].trim(), "200")) {
                            errCount++;
                            status = false;
                        } else {
                            status = true;
                        }
                    }
                }
                // 更新状态
                lastFileState = webLogVo.fileState;
                lastLinesMD5 = webLogVo.linesMD5;
                logger.info("在日志文件[{}]中指定读取最后[{}]行日志内容,实际读取到[{}]行,发现[{}]行异常请求,最后一条请求状态[{}]", fileName, checkMaxReadLine, lastLineContents.size(), errCount, status);
                lastStatus = status;
            } else {
                logger.info("IIS Log [{}] 未变化，上一次检查状态:{}，等待下一次检查...", fileName, lastStatus);
                if (!lastStatus) {
                    isSameErr = true;
                }
            }
            String lastLineContent = CollectionUtils.isEmpty(lastLineContents) ? "" : lastLineContents.get(lastLineContents.size() - 1);
            logger.debug("在日志文件[{}]中,最后一行,请求内容:[{}]", fileName, lastLineContent);
            return List.of(lastStatus, isSameErr);
        } catch (Exception e) {
            logger.error("读取weblog异常:{}", e.getMessage(), e);
        }
        return List.of(true, true);
    }

    private String getLastTimeFileName(String dir) {
        logger.info("在路径:[{}]下查找最新创建的日志文件", dir);
        File directory = new File(dir);
        if (!(directory.exists() && directory.isDirectory())) {
            throw new RuntimeException("文件不存在,或不是目录:" + dir);
        }
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File latestFile = null;
        long latestTime = Long.MIN_VALUE;

        for (File file : files) {
            if (file.isFile()) {
                try {
                    Path filePath = Paths.get(file.getAbsolutePath());
                    BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
                    long fileTime = attr.creationTime().toMillis();
                    if (fileTime > latestTime) {
                        latestTime = fileTime;
                        latestFile = file;
                    }
                } catch (IOException e) {
                    // 处理文件读取异常，例如权限问题
                    logger.error("文件读取异常:{}", e.getMessage(), e);
                }
            }
        }
        String fileName = latestFile.getName();
        logger.info("在路径:[{}]下查找最新到创建的日志文件:[{}]", dir, fileName);
        return fileName;
    }

    private boolean isReachable(String ipAddress) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            if (address.isReachable(checkNetworkTimeOut)) {
                logger.info("设备可达:{}", ipAddress);
                return true;
            } else {
                logger.error("设备不可达:{}", ipAddress);
                return false;
            }
        } catch (Exception e) {
            logger.error("测试[{}]网络异常:{}", ipAddress, e.getMessage(), e);
            return false;
        }
    }

    private boolean testTelnet(String ip, int port) {
        String uri = ip + ":" + port;
        try {
            TelnetClient telnet = new TelnetClient();
            telnet.setConnectTimeout(checkNetworkTimeOut);
            telnet.connect(ip, port);
            logger.info("{} : 设备存活，连接成功！", uri);
            telnet.disconnect();
            return true;
        } catch (Exception e) {
            logger.error("telnet设备[{}]可能不存活或无法连接：{}", uri, e.getMessage());
            return false;
        }
    }

    /**
     * 读取IIS日志最后N行（专用优化版）
     */
    public static WebLogVo readIISLogLastLines(String logPath, int needLines) throws Exception {
        File logFile = new File(logPath);
        LinkedList<String> lastLines = new LinkedList<>();

        // 1. 生成文件状态（0耗时，IIS日志有效）
        String fileState = logFile.length() + "_" + logFile.lastModified();

        try (RandomAccessFile raf = new RandomAccessFile(logFile, "r")) {
            long fileLength = raf.length();
            if (fileLength == 0) {
                return new WebLogVo(lastLines, fileState, MD5Utils.GetMD5Code(""));
            }

            long pointer = fileLength - 1;
            ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
            boolean lastIsCR = false;

            // 从后往前读取（IIS日志大文件也秒读）
            while (pointer >= 0 && lastLines.size() < needLines) {
                raf.seek(pointer);
                int b = raf.read();

                if (b == '\n') {
                    // 遇到 \n，如果前一个不是 \r，则是一行的结束
                    if (!lastIsCR && lineBuffer.size() > 0) {
                        addLine(lineBuffer, lastLines);
                    }
                    lastIsCR = false;
                } else if (b == '\r') {
                    // 遇到 \r，标记并添加行（处理纯 \r 或 \r\n）
                    if (lineBuffer.size() > 0) {
                        addLine(lineBuffer, lastLines);
                    }
                    lastIsCR = true;
                } else {
                    lineBuffer.write(b);
                    lastIsCR = false;
                }
                pointer--;
            }

            // 处理最后一行（文件开头没有换行符的情况）
            if (lineBuffer.size() > 0 && lastLines.size() < needLines) {
                addLine(lineBuffer, lastLines);
            }
        }

        // 2. 生成最后N行MD5（精准判断内容变化）
        String content = String.join("\n", lastLines);
        String linesMD5 = MD5Utils.GetMD5Code(content);

        return new WebLogVo(lastLines, fileState, linesMD5);
    }

    /**
     * 解析一行日志（UTF-8编码，IIS日志默认编码）
     */
    private static void addLine(ByteArrayOutputStream lineBuffer, LinkedList<String> lastLines) {
        if (lineBuffer.size() == 0) return;
        try {
            // IIS日志默认编码：UTF-8 / ASCII，都支持
            lastLines.addFirst(new String(reverseBytes(lineBuffer.toByteArray()), StandardCharsets.UTF_8));
        } finally {
            lineBuffer.reset();
        }
    }
    /**
     * 反转字节数组（倒序读取必须用）
     */
    private static byte[] reverseBytes(byte[] bytes) {
        int left = 0;
        int right = bytes.length - 1;
        while (left < right) {
            byte temp = bytes[left];
            bytes[left] = bytes[right];
            bytes[right] = temp;
            left++;
            right--;
        }
        return bytes;
    }

    private static boolean flag = true;

    //    @Scheduled(initialDelay = 5 * 1000L, fixedRate = 60 * 1000)
    public void turnOnOffTheLights() throws UnsupportedEncodingException {
        onOrOff(flag ? "开灯" : "关灯", false, List.of(Map.of("6cad302fda2197011fe3f6", List.of("switch_1")), Map.of("6cad302f998b83230a0405", List.of("switch_1", "switch_2", "switch_3"))));
        flag = !flag;
    }

    //    @Scheduled(cron = "0 0 12 * * ?")  // 关灯
    public void turnOffTheLights() throws UnsupportedEncodingException {
        onOrOff("关灯", false, List.of(Map.of("6cad302fda2197011fe3f6", List.of("switch_1")), Map.of("6cad302f998b83230a0405", List.of("switch_1", "switch_2", "switch_3"))));
    }

    //    @Scheduled(cron = "0 03 13 * * ?")  // 开灯
    public void turnOnTheLights() {
        onOrOff("开灯", true, List.of(Map.of("6cad302fda2197011fe3f6", List.of("switch_1")), Map.of("6cad302f998b83230a0405", List.of("switch_1", "switch_2", "switch_3"))));
    }

    public static String getToken() {
        String url = "http://192.168.0.90:8888/v1.0/openapi/web/login";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(JSONUtil.toJsonStr(Map.of("password", "admin", "username", "admin"))));
            post.setHeader("Content-Type", "application/json");
            CloseableHttpResponse execute = httpClient.execute(post);
            int statusCode = execute.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String response = EntityUtils.toString(execute.getEntity());
                JSONObject jsonObject = JSONUtil.parseObj(response);
                JSONObject data = (JSONObject) (jsonObject.get("data"));
                Object token = data.get("token");
                return token.toString();
            }
        } catch (Exception exception) {
            System.out.println(exception);
        }
        return null;
    }

    public void onOrOff(String switchTag, boolean flag, List<Map<String, List<String>>> idInfos) {
        String url = "http://192.168.0.90:8888/v1.0/openapi/devices/%s/set-properties";
        for (Map<String, List<String>> idMap : idInfos) {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                String id = idMap.keySet().stream().findFirst().get();
                Map<String, Boolean> paramMap = new HashMap<>();
                for (String s : idMap.get(id)) {
                    paramMap.put(s, flag);
                }
                HttpPost httpPost = new HttpPost(String.format(url, id));
                // 设置 JSON 请求体
                httpPost.setEntity(new StringEntity(JSONUtil.toJsonStr(paramMap)));
                httpPost.setHeader("Content-Type", "application/json"); // 必须指定 JSON 类型 [2,5](@ref)
                httpPost.setHeader("Authorization", getToken()); // 必须指定 JSON 类型 [2,5](@ref)
                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    System.out.println(switchTag + "_Status: " + response.getStatusLine().getStatusCode());
                    System.out.println(switchTag + "_Response: " + EntityUtils.toString(response.getEntity()));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
