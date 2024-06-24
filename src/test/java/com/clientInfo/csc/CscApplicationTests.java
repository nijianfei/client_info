package com.clientInfo.csc;

import cn.hutool.core.io.FileUtil;
import com.clientInfo.csc.entity.PingEntity;
import com.clientInfo.csc.utils.ArpUtil;
import com.clientInfo.csc.utils.SurveillanceItem;
import com.clientInfo.csc.vo.ArpVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.telnet.TelnetClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class CscApplicationTests {

    @Autowired
    private ThreadPoolTaskExecutor executor;

    public static void main(String[] args) {

        String testString = "NLB 群集控制实用程序 V2.6\n" +
                "主机 2 在加入到群集后进入聚合状态 1 次，\n" +
                "  上次聚合完成于大约: 2024/4/19 13:50:04\n" +
                "作为群集的一部分，主机 2 已与下列主机作为默认值聚合:\n" +
                "2";
        String[] split = testString.split("\\n");
        String s1 =split[1];
        String s2 =split[2].trim();
        String s4 =split[4];

        LinkedList<String> lList = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            lList.add("a" + i);//向尾部增加元素
        }
        for (int i = 0; i < 10; i++) {
            lList.push("p" + i);//向头部增加元素
        }
        for (int i = 0; i < 10; i++) {
            lList.offer("off" + i);//向尾部增加元素
        }

        System.out.println("size:"+lList.size());
        System.out.println("peek:"+lList.peek());//查看头部元素
        System.out.println("element:"+lList.element());//查看头部元素
        System.out.println("peekFirst:"+lList.peekFirst());//查看头部元素
        System.out.println("peekLast:"+lList.peekLast());//查看尾部元素
        System.out.println("size:"+lList.size());
        System.out.println("poll:"+lList.poll());//查看并移除头部元素
        System.out.println("pollFirst:"+lList.pollFirst());//查看并移除头部元素
        System.out.println("pollLast:"+lList.pollLast());//查看并移除尾部元素
        System.out.println("remove:"+lList.remove());//移除并返回头部元素
        System.out.println("pop:"+lList.pop());//移除并返回头部元素
        System.out.println("size:"+lList.size());
        System.out.println("lList:"+ lList.toString());
//        String str = "来自 10.65.208.90 的回复: 字节=32 时间=16ms TTL=128";
//        String str1 = "来自 10.65.208.90 的回复: 字节=32 时间<1ms TTL=128";
//        String[] s = str.split(" ");
//        String[] s1 = str1.split(" ");
//        System.out.println(str.split(" ")[4].substring(3).replace("ms","")+" - "+s1[4].substring(3).replace("ms",""));

    }
    @Test
    void testCase(){
        System.out.println();
        List<String> ipList = FileUtil.readLines(new File(new File(System.getProperty("user.dir")), "ips.txt"), "utf8");
        ipList.forEach(ip->SurveillanceItem.pingSync(ip));
        sleep(999999);
    }

    private Map<String, LinkedList<PingEntity>> recordMap = new HashMap<>();
    @Test
    void testPing() {
        Map<String, ArpVo> stringArpVoMap = ArpUtil.initClientInfoMap("cmd /c arp -a | findstr 10.65.208 |findstr  /V \\u63A5\\u53E3");
        for (ArpVo value : stringArpVoMap.values()) {
            executor.execute(() -> SurveillanceItem.ping(value.getClientIp()));
        }
        sleep(999999);
    }

    void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void testTelnet(){
        //TELNET
        String ipAddress1 = "设备的IP地址";
        int port = 23; // 例如：23
        int timeout = 5000; // 连接超时时间（毫秒）
        try {
            TelnetClient telnet = new TelnetClient();
            telnet.setConnectTimeout(timeout);
            telnet.connect(ipAddress1, port);
            System.out.println("设备存活，连接成功！");
        } catch (Exception e) {
            System.out.println("设备可能不存活或无法连接：" + e.getMessage());
        }
    }
    @Test
    void contextLoads() {
        try {
            InetAddress address = InetAddress.getByName("10.65.208.33");
            if (address.isReachable(5000)) { // 5000毫秒超时时间
                System.out.println("设备可达");
            } else {
                System.out.println("设备不可达");
            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }




        //UDP
        String ipAddress2 = "10.65.208.33";
        int port = 22; // 例如：某个UDP服务端口
        int timeout = 5000; // 超时时间（毫秒）
        byte[] sendData = "ping".getBytes(); // 要发送的数据

        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(timeout); // 设置接收超时时间

            InetAddress address = InetAddress.getByName(ipAddress2);
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, address, port);

            // 发送UDP数据包
            socket.send(packet);

            // 创建一个用于接收响应的缓冲区
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                // 尝试接收响应，如果设备在线并且响应，这里将不会抛出异常
                socket.receive(receivePacket);
                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("收到响应: " + response);
                System.out.println("设备可能在线。");
            } catch (SocketException e) {
                // 如果在超时时间内没有收到响应，将抛出SocketException
                System.out.println("没有收到响应，设备可能不在线或没有响应。");
            }

            socket.close();
        } catch (UnknownHostException e) {
            System.out.println("无法解析主机名: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O错误: " + e.getMessage());
        }
    }

    @Test
    public void testPrint() {

        try {
            // 加载图片
            BufferedImage image = ImageIO.read(new File("C:\\print\\print_recode\\0\\20240311\\CSE00015_164521.png"));

            // 创建打印作业
            PrinterJob printerJob = PrinterJob.getPrinterJob();

            // 设置打印页面
            PageFormat pageFormat = printerJob.defaultPage();
            pageFormat.setOrientation(PageFormat.PORTRAIT); // 设置纸张方向为纵向

            // 创建Printable对象，用于绘制图片到打印页面上
            Printable printable = (graphics, pageFormat1, pageIndex) -> {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat1.getImageableX(), pageFormat1.getImageableY());
                g2d.drawImage(image, 0, 0, (int) pageFormat1.getImageableWidth(), (int) pageFormat1.getImageableHeight(), null);
                return Printable.PAGE_EXISTS;
            };

            // 设置Printable对象到打印作业中
            printerJob.setPrintable(printable, pageFormat);
            printerJob.print();
        } catch (IOException | PrinterException e) {
            e.printStackTrace();
        }
    }


}
