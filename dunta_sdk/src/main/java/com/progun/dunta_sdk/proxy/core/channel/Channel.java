package com.progun.dunta_sdk.proxy.core.channel;


import com.progun.dunta_sdk.proxy.exception.ReadSocketException;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * Этот интерфейс описывает канал и его поведение.
 */
public interface Channel {
    class Status {
        final static int EMPTY = 0x000;
        final static int PENDING = 0x00001;
        final static int CONNECTED = 0x00002;
        final static int SENDING = 0x00004;
        final static int READING = 0x00008;
    }

    void read() throws IOException, ReadSocketException;

    void write() throws IOException;

    void close(short closeCause) throws IOException;

    void shutDown() throws IOException;

    void connect(Selector selector) throws IOException;

    ChannelStatus getStatus();

    boolean isConnecting();

    boolean isReading();

    void checkTimeouts(long currentTime);
}

enum ChannelStatus {
    CLOSED, CONNECTED, CONNECTION_PENDING, READING, WRITING, EMPTY
}

