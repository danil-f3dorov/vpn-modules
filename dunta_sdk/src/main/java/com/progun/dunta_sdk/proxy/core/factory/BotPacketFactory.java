package com.progun.dunta_sdk.proxy.core.factory;

import androidx.annotation.NonNull;

import com.progun.dunta_sdk.proxy.core.packet.bot.BotClosePacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotConnectPacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotHelloPacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotPacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotPingPacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotRecvPacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotReportPacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotSentPacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotShutdownPacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotStatePacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.Status;
import com.progun.dunta_sdk.utils.LogWrap;

/**
 * Фабрика пакетов бота. Имеет методы для создания пакетов под каждую команду.
 * В методах возвращает класс пакета бота BotPacket
 */
public final class BotPacketFactory extends PacketFactory {
    final static private String TAG = BotPacketFactory.class.getSimpleName();

    public BotPacketFactory() {
    }

    /*
    BOT_HELLO:
        16int: PACKET_CMD
        16int: PROTOCOL_VER
        32int: partner_id
        16int: application_id
        64int: bot_id
        16int: network_state
        16int: SDK_RELEASE_VER
        16int: advertisement_id
        16int: reconnection_cause
        16int: ANDROID_API_VER
        16int: model_len
        str: phone_model
        16: platform_arch_len
        str: platform_arch

        16int:network_type
    */

    public BotHelloPacket helloPacket(
            String androidId,
            int partnerId,
            int applicationId,
            long botId,
            int advertisementId,
            int networkState,
            int networkType,
            int apiVer,
            String modelStr,
            String platformStr,
            int versionCode
    ) {
        return new BotHelloPacket(
                androidId,
                partnerId,
                applicationId,
                botId,
                advertisementId,
                networkState,
                networkType,
                apiVer,
                modelStr,
                platformStr,
                versionCode
        );
    }

    public BotConnectPacket connectPacket(int channelId, Status status) {
        return new BotConnectPacket(channelId, status);
    }

    public BotSentPacket sentPacket(int channelId) {
        return new BotSentPacket(channelId);
    }

    public BotRecvPacket recvPacket(int channelId, @NonNull byte[] data) {
        return new BotRecvPacket(channelId, data);
    }

    public BotReportPacket reportPacket(@NonNull String reportMsg) {
        return new BotReportPacket(reportMsg);
    }

    public BotShutdownPacket shutDownPacket(int channelId) {
        return new BotShutdownPacket(channelId);
    }

    public BotStatePacket statePacket(int type, int state) {
        return new BotStatePacket(type, state);
    }

    public BotClosePacket closePacket(int id) {
        return new BotClosePacket(id);
    }

    public BotPingPacket pingPacket() {
        return new BotPingPacket();
    }

    private byte[] recvHeader(int channelId, Status type) {
        LogWrap.v(TAG, "recvHeader() has called");
        byte[] status = getStatus(type);

        if (status[0] != 0 || status[1] != 0)
            throw new RuntimeException("Bot RECV ERROR");

        return new byte[]{
                (byte) BotPacket.PROXY_BOT_RECV,
                (byte) (BotPacket.PROXY_BOT_RECV >> 8),
                (byte) channelId,
                (byte) (channelId >> 8),
                (byte) (channelId >> 16),
                (byte) (channelId >> 24)
        };
    }


    private byte[] getStatus(Status type) {
        LogWrap.d(TAG, "getStatus() has called");
        byte[] status;
        switch (type) {
            case FAILED:
                status = new byte[]{BotPacket.PROXY_ERROR_FAILED, (BotPacket.PROXY_ERROR_FAILED >> 8)};
                break;
            case SUCCESS:
                status = new byte[]{BotPacket.PROXY_ERROR_SUCCESS, (BotPacket.PROXY_ERROR_SUCCESS >> 8)};
                break;
            case TIMEOUT:
                status = new byte[]{BotPacket.PROXY_ERROR_TIMEOUT, (BotPacket.PROXY_ERROR_TIMEOUT >> 8)};
                break;
            case UNKNOWN:
                status = new byte[]{BotPacket.PROXY_ERROR_UNKNOWN, (BotPacket.PROXY_ERROR_UNKNOWN >> 8)};
                break;
            case MAX_CHANNELS:
                status = new byte[]{BotPacket.PROXY_ERROR_MAX_CHANNELS, (BotPacket.PROXY_ERROR_MAX_CHANNELS >> 8)};
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
        return status;
    }
}
