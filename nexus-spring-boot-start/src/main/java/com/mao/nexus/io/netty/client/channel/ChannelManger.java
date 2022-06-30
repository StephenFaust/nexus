package com.mao.nexus.io.netty.client.channel;

import com.mao.nexus.io.netty.client.callback.CallBack;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;


import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ：huangjinmao
 * @date ：2022/6/29 14:40
 */
public class ChannelManger {

    public static final AttributeKey attributeKey = AttributeKey.valueOf("handler");

    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHES = new HashMap<>(4);

    public static final Map<Long, CallBack> CALLBACK_CACHES = new ConcurrentHashMap<>(16);

    public static final ThreadLocal<Long> UNIQUE_IDENTIFICATION_CACHES = new ThreadLocal<>();


//    public static void addChannel(InetSocketAddress address, Channel channel) {
//        channelCaches.put(address, channel);
//    }
//
//    public static Channel getChannel(InetSocketAddress address) {
//        return channelCaches.get(address);
//    }
//
//    public static void deleteChannel(String address) {
//        channelCaches.remove(address);
//    }
}
