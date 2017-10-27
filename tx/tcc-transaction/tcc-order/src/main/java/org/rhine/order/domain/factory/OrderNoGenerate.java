package org.rhine.order.domain.factory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OrderNoGenerate {

    private static final SnowflakeIdWorker workInstance = new SnowflakeIdWorker(getWorkerId(), 0);

    /**
     * 生成订单号.
     * @return
     */
    public static long getNo() {
       return workInstance.nextId();
    }

    /**
     * 获取机器workId.
     * @return
     */
    public static long getWorkerId() {
        return getLastIP() & 0xff;
    }

    private static byte getLastIP(){
        byte lastIp = 0;
        try{
            InetAddress ip = InetAddress.getLocalHost();
            byte[] ipByte = ip.getAddress();
            lastIp = ipByte[ipByte.length - 1];
        } catch (UnknownHostException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        return lastIp;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            System.out.println(getNo());
        }
    }
}
