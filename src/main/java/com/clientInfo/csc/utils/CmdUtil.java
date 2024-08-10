package com.clientInfo.csc.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Slf4j
public class CmdUtil {//Get-NlbClusterDriverInfo

    public static String execCmd(String command) {
        try {
            log.info("开始执行->[{}]命令", command);
            Process exec = Runtime.getRuntime().exec(command);
            InputStream inputStream = exec.getInputStream();
            InputStream errorStream = exec.getErrorStream();
            byte[] readAllBytes = inputStream.readAllBytes();
            String accMsg = new String(readAllBytes, Charset.forName("GBK"));
            log.info("返回结果：\r\n{}", accMsg);
            byte[] readAllBytes2 = errorStream.readAllBytes();
            String errMsg = new String(readAllBytes2, Charset.forName("GBK"));
            if (StringUtils.isNotBlank(errMsg)) {
                log.info("返回异常信息：{}", errMsg);
            }
            return accMsg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String execPowerShellCmd(String command) {
        String cmd = "powershell.exe -Command \"" + command + "\"";
        return execCmd(cmd);
    }

    public static boolean execBatFile(String fileFullName) {
        String cmd = "cmd /c  " + fileFullName;
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Success!");
                return true;
            } else {
                // 错误处理
                System.out.println("Something went wrong");
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
