package com.progun.dunta_sdk.proxy.core.packet.bot;

import com.progun.dunta_sdk.proxy.core.DeviceNetworkState;
import com.progun.dunta_sdk.proxy.core.ProxyClient;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;
import com.progun.dunta_sdk.utils.CauseReconnectionConsts;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BotHelloPacket extends BotPacket {

    private final int partnerId;
    private final int applicationId;
    private final long botId;
    private final int advertisementId;
    private final int networkState;
    private final int networkType;
    private final int apiLevel;
    private final String modelStr;
    private final String platformStr;
    private final int versionCode;
    private final String androidId;
    private final int reconnectionCause;

    public BotHelloPacket(
            String androidId,
            int partnerId,
            int applicationId,
            long botId,
            int advertisementId,
            int networkState,
            int networkType,
            int apiLevel,
            String modelStr,
            String platformStr,
            int versionCode
    ) {
        super(BotCommandType.HELLO, ProtocolConstants.PROXY_CMD_MARKER);
        this.androidId = androidId;
        this.partnerId = partnerId;
        this.applicationId = applicationId;
        this.botId = botId;
        this.advertisementId = advertisementId;
        this.networkState = networkState;
        this.networkType = networkType;
        this.apiLevel = apiLevel;
        this.modelStr = modelStr;
        this.platformStr = platformStr;
        this.versionCode = versionCode;
        this.reconnectionCause = ProxyClient.RECONNECTION_CAUSE_FLAG;
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
        int16: platform_arch_len
        str: platform_arch
        16int:network_type
        16int: mark
    */

//    16int: versionCode
    @Override
    public byte[] toByteArray() {
        byte[] helloData;
        ArrayList<Byte> helloByteArrayList = new ArrayList<>();

        helloByteArrayList.add((byte) BotPacket.PROXY_BOT_HELLO);
        helloByteArrayList.add((byte) (BotPacket.PROXY_BOT_HELLO >> 8));
        helloByteArrayList.add((byte) ProtocolConstants.SRV_PROTO_VERSION);
        helloByteArrayList.add((byte) (ProtocolConstants.SRV_PROTO_VERSION >> 8));

        helloByteArrayList.add((byte) partnerId);
        helloByteArrayList.add((byte) (partnerId >> 8));
        helloByteArrayList.add((byte) (partnerId >> 16));
        helloByteArrayList.add((byte) (partnerId >> 24));

        helloByteArrayList.add((byte) applicationId);
        helloByteArrayList.add((byte) (applicationId >> 8));

        helloByteArrayList.add((byte) botId);
        helloByteArrayList.add((byte) (botId >> 8));
        helloByteArrayList.add((byte) (botId >> 16));
        helloByteArrayList.add((byte) (botId >> 24));
        helloByteArrayList.add((byte) (botId >> 32));
        helloByteArrayList.add((byte) (botId >> 40));
        helloByteArrayList.add((byte) (botId >> 48));
        helloByteArrayList.add((byte) (botId >> 56));

        helloByteArrayList.add((byte) networkState);
        helloByteArrayList.add((byte) (networkState >> 8));

        helloByteArrayList.add((byte) ProtocolConstants.SDK_RELEASE_VERSION_TEST_APP);
        helloByteArrayList.add((byte) (ProtocolConstants.SDK_RELEASE_VERSION_TEST_APP >> 8));

        helloByteArrayList.add((byte) advertisementId);
        helloByteArrayList.add((byte) (advertisementId >> 8));

        helloByteArrayList.add((byte) reconnectionCause);
        helloByteArrayList.add((byte) (reconnectionCause >> 8));

        helloByteArrayList.add((byte) apiLevel);
        helloByteArrayList.add((byte) (apiLevel >> 8));

        byte[] modelBytes = modelStr.getBytes(StandardCharsets.UTF_8);
        helloByteArrayList.add((byte) modelBytes.length);
        helloByteArrayList.add((byte) (modelBytes.length >> 8));
        for (byte modelByte : modelBytes)
            helloByteArrayList.add(modelByte);

        // platform (string)
        byte[] platformBytes = platformStr.getBytes(StandardCharsets.UTF_8);
        helloByteArrayList.add((byte) platformBytes.length);
        helloByteArrayList.add((byte) (platformBytes.length >> 8));
        for (byte platformByte : platformBytes)
            helloByteArrayList.add(platformByte);

        // network type (uint16)
        helloByteArrayList.add((byte) networkType);
        helloByteArrayList.add((byte) (networkType >> 8));

        // androidId (string)
        byte[] androidIdBytes = androidId.getBytes(StandardCharsets.UTF_8);
        helloByteArrayList.add((byte) androidIdBytes.length);
        helloByteArrayList.add((byte) (androidIdBytes.length >> 8));
        for(byte b : androidIdBytes) {
            helloByteArrayList.add(b);
        }


        helloByteArrayList.add((byte) mark);
        helloByteArrayList.add((byte) (mark >> 8));

        helloData = new byte[helloByteArrayList.size()];
        for (int i = 0; i < helloData.length; i++)
            helloData[i] = helloByteArrayList.get(i);

        ProxyClient.setCause(CauseReconnectionConsts.EMPTY_VALUE, true);

        return helloData;
    }

    @Override
    public String convertToString(boolean fullString) {
        if (!fullString) return super.convertToString();

        var helloData = new StringBuilder(super.convertToString());

        helloData.append("\n\tprotoVersion=").append(ProtocolConstants.SRV_PROTO_VERSION)
                .append("\n\tpartnerId=").append(partnerId)
                .append("\n\tappId=").append(applicationId)
                .append("\n\tbotId=").append(botId);

        if (networkState == 0) helloData.append("\n\tnetState=WIFI=").append(networkState);
        else if (networkState == 1) helloData.append("\n\tnetState=CELLULAR=").append(networkState);

        helloData.append("\n\trelease=").append(ProtocolConstants.SDK_RELEASE_VERSION_TEST_APP)
                .append("\n\tadvId=").append(advertisementId)
                .append("\n\treconCause=")
                .append(CauseReconnectionConsts.causeToString((short) reconnectionCause))
                .append("\n\tapi=").append(apiLevel)
                .append("\n\tmodel=").append(modelStr)
                .append("\n\tarch=").append(platformStr)
                .append("\n\tnetworkType=");

        /*switch (networkType) {
            case 0 -> helloData.append(DeviceNetworkState.TYPE.NONE.name());
            case 1 -> helloData.append(DeviceNetworkState.TYPE.TYPE_2G.name());
            case 2 -> helloData.append(DeviceNetworkState.TYPE.TYPE_3G.name());
            case 3 -> helloData.append(DeviceNetworkState.TYPE.TYPE_4G.name());
            case 4 -> helloData.append(DeviceNetworkState.TYPE.TYPE_5G.name());
            default -> throw new IllegalStateException("Network type parse exception");
        }*/

        switch (networkType) {
            case 0:
                helloData.append(DeviceNetworkState.TYPE.NONE.name());
                break;
            case 1:
                helloData.append(DeviceNetworkState.TYPE.TYPE_2G.name());
                break;
            case 2:
                helloData.append(DeviceNetworkState.TYPE.TYPE_3G.name());
                break;
            case 3:
                helloData.append(DeviceNetworkState.TYPE.TYPE_4G.name());
                break;
            case 4:
                helloData.append(DeviceNetworkState.TYPE.TYPE_5G.name());
                break;
            default:
                throw new IllegalStateException("Network type parse exception");
        }

        helloData.append(System.lineSeparator())
                .append("\tmark=").append(mark)
                .append(System.lineSeparator());

        return helloData.toString();
    }


}
