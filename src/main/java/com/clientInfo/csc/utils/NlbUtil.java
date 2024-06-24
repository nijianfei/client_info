package com.clientInfo.csc.utils;

import java.util.*;

public class NlbUtil {
    private static Map<String,String> ipToInterface = new HashMap<>();

    public static void startNode(String clusterIPAddress){
        String interfaceName = ipToInterface.get(clusterIPAddress);
        if (Objects.nonNull(interfaceName) && interfaceName.trim().equals("")) {
            CmdUtil.execPowerShellCmd("Start-NlbClusterNode");
            return;
        }
        CmdUtil.execPowerShellCmd("Start-NlbClusterNode -InterfaceName " + interfaceName);
    }
    public static void stopNode(String clusterIPAddress){
        String interfaceName = ipToInterface.get(clusterIPAddress);
        if (Objects.isNull(interfaceName) || interfaceName.trim().equals("")) {
            CmdUtil.execPowerShellCmd("Stop-NlbClusterNode");
            return;
        }
        CmdUtil.execPowerShellCmd("Stop-NlbClusterNode -InterfaceName "+interfaceName);
    }
    public static String getNodeStatus(String clusterIPAddress){//Ethernet1
        String interfaceName = ipToInterface.get(clusterIPAddress);
        String readText = CmdUtil.execPowerShellCmd("Get-NlbClusterDriverInfo -InterfaceName " + interfaceName);
        String[] lines = readText.split("\r\n");
        for (String line : lines) {
            if (!"".equals(line)) {
                String[] kv = line.split(":");
                if (kv.length == 2) {
                    String key = kv[0].trim();
                    if (key.equals("CurrentHostState")) {
                        return kv[1].trim();//Started:启动,Stopped:停止,Suspended:挂起
                    }
                }
            }
        }
        return null;
    }

    public static int queryNodeCount(String clusterIPAddress){
        String readText = CmdUtil.execPowerShellCmd("nlb query " + clusterIPAddress);
        String[] lines = readText.split("\r\n");
        List<String> nodeIds = new ArrayList<>();
        for (String s : lines[4].split(",")) {
            nodeIds.add(s.trim());
        }
        return nodeIds.size();
    }

    public static Map<String,String> queryInterfaceName(){
        String readText = CmdUtil.execPowerShellCmd("Get-NlbClusterNodeNetworkInterface");
        String[] lines_new = readText.split("\r\n");
        String lastValue = null;
        for (String s : lines_new) {
            if (!"".equals(s)) {
                String[] l_split = s.split(":");
                String key = l_split[0].trim();
                if (Objects.equals(key,"InterfaceName")) {
                    lastValue = l_split[1].trim();
                }
                if (Objects.equals(key,"ClusterPrimaryIP")) {
                    ipToInterface.put(l_split[1].trim(), lastValue);
                }
            }

        }
        return ipToInterface;

    }
}
