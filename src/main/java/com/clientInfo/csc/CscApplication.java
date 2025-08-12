package com.clientInfo.csc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//@EnableCaching
@Slf4j
@EnableScheduling
@SpringBootApplication
public class CscApplication {

    public static void main(String[] args) {
        SpringApplication.run(CscApplication.class, args);
        log.info("应用已成功启动");
//        ArpUtil.initClientInfoMap("cmd /c arp -a | findstr 10.65.208 |findstr  /V \\u63A5\\u53E3");
//        //临时测试用
//        List<String> ipList = FileUtil.readLines(new File(new File(System.getProperty("user.dir")), "ips.txt"), "utf8");
//        ipList.forEach(ip-> SurveillanceItem.pingSync(ip));
//        Runtime.getRuntime().addShutdownHook(new Thread(()->{
//            Set<String> keySet = SurveillanceItem.processMap.keySet();
//            for (String key : keySet) {
//                Process process = SurveillanceItem.processMap.get(key);
//                if (process != null) {
//                    process.destroy();
//                    System.out.println("ShutdownHook---------------> "+key+" isAlive:" + process.isAlive());
//                }
//            }
//            System.out.println("ShutdownHook--------------->End");
//        }));
//        NlbUtil.queryInterfaceName();
    }

}
