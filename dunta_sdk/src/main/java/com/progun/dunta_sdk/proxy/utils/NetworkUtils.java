package com.progun.dunta_sdk.proxy.utils;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class NetworkUtils {
    public static int getLocalPort(SocketChannel socketChannel) {
        return ((InetSocketAddress)socketChannel.socket().getLocalSocketAddress()).getPort();
    }
}
