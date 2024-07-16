package com.clientInfo.csc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//@EnableCaching
@EnableScheduling
@SpringBootApplication
public class CscApplication {

    public static void main(String[] args) {
        SpringApplication.run(CscApplication.class, args);
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
