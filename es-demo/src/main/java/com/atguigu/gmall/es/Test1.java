package com.atguigu.gmall.es;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;

/**
 * 创建索引-映射
 */
public class Test1 {
    /**
     * 创建索引
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        TransportClient client = new PreBuiltTransportClient(Settings.EMPTY);
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 3000));
        client.admin().indices().prepareCreate("java0509").get();
        client.close();
    }

}
