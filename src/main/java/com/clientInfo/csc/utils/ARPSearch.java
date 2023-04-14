///*
//package com.clientInfo.csc.utils;
//
//import net.sourceforge.jpcap.net.ARPPacket;
//import net.sourceforge.jpcap.net.EthernetPacket;
//
//import java.net.Inet4Address;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.util.Arrays;
//
//public class ARPSearch {
//    public static ARPPacket getTargetMAC(InetAddress targetIp) {
//        NetworkInterface[] 	devices = JpcapCaptor.getDeviceList();
//        NetworkInterface 	device 	= null;
//// 寻找适合的网络设备
//        loop:
//        for (NetworkInterface d : devices) {
//            for (NetworkInterfaceAddress addr : d.addresses) {
//                if (!(addr.address instanceof Inet4Address))	continue;
//                byte[] bip 		= targetIp.getAddress();
//                byte[] subnet 	= addr.subnet.getAddress();
//                byte[] bif 		= addr.address.getAddress();
//                for (int i = 0; i < 4; i++) {
//                    bip[i] 		= (byte) (bip[i] & subnet[i]);
//                    bif[i] 		= (byte) (bif[i] & subnet[i]);
//                }
//                if (Arrays.equals(bip, bif)) {
//                    device = d;
//                    break loop;
//                }
//            }
//        }
//        if (device == null)	{
//            ARPFace.textArea.append(targetIp+ " is not a local address");
//            throw new IllegalArgumentException(targetIp+ " is not a local address");
//        }
//        JpcapCaptor captor = null;
//// 打开一个网络数据包捕捉者
//        try {
//            captor = JpcapCaptor.openDevice(device, 2000, false, 300);
//// 只接收ARP数包
//            captor.setFilter("arp", true);
//        } catch (Exception e) {	}
//// 获得发送数据包的实例
//        JpcapSender sender 	= captor.getJpcapSenderInstance();
//        InetAddress srcip 	= null;
//        for (NetworkInterfaceAddress addr : device.addresses)
//            if (addr.address instanceof Inet4Address) {
//                srcip 		= addr.address;
//                break;
//            }
//// 进行广播数据报的MAC地址
//        byte[] broadcast = new byte[] { (byte) 255, (byte) 255, (byte) 255,(byte) 255, (byte) 255, (byte) 255 };
//// 构造REQUEST 类型的ARP的数据包
//        ARPPacket arp 			= new ARPPacket();
//        arp.hardtype 			= ARPPacket.HARDTYPE_ETHER;
//        arp.prototype 			= ARPPacket.PROTOTYPE_IP;
//        arp.operation 			= ARPPacket.ARP_REQUEST;
//        arp.hlen 				= 6;
//        arp.plen 				= 4;
//// 源MAC地址
//        arp.sender_hardaddr 	= device.mac_address;
//// 源IP地址
//        arp.sender_protoaddr 	= srcip.getAddress();
//// 目地MAC地址:广播地址全为1(二进制)
//        arp.target_hardaddr 	= broadcast;
//// 目地IP地址
//        arp.target_protoaddr 	= targetIp.getAddress();
//// 构造以太网头部
//        EthernetPacket ether 	= new EthernetPacket();
//        ether.frametype 		= EthernetPacket.ETHERTYPE_ARP;
//        ether.src_mac 			= device.mac_address;
//        ether.dst_mac 			= broadcast;
//// ARP数据包加上以网关头部
//        arp.datalink 			= ether;
//// 向局域网广播ARP请求数据报
//        sender.sendPacket(arp);
//// 接收目标主面的答应ARP数据报
//        while (true) {
//            ARPPacket p = (ARPPacket) captor.getPacket();// 接收返回包
//            if (p == null) {
//                System.out.println(targetIp + "不是本地局域网的IP号");
//                ARPFace.textArea.append(targetIp + "不是本地局域网的IP号\n");
//                return p;
//            } else if(Arrays.equals(p.target_protoaddr, srcip.getAddress())) {
//                return p;
//            }
//        }
//    }*/
