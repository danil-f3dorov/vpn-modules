package com.progun.dunta_sdk.proxy.core.channel.channelevent;

import com.progun.dunta_sdk.proxy.core.channel.HostChannel;
import com.progun.dunta_sdk.proxy.core.packet.bot.Status;

/**
 * Описывает событие соединения с хостом.
 * Слушатель регистрируется в Channel, а само событие передается из
 * BotProtocolProvider в метод handleEvent()
 * При успехе/неудаче подключения вызывается коллбек с
 * соответствующим действием (реализация коллбека
 * находится в классе BotProtocolProvider)
 */
public interface ChannelConnectEvent {
    void onFailed(int id, Status status);

    void onSuccess(int id, HostChannel channel);
}
