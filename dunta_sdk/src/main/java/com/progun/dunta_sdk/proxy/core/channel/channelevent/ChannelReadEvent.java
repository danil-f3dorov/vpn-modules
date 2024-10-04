package com.progun.dunta_sdk.proxy.core.channel.channelevent;

/**
 * Описывает событие чтения данных с хоста.
 * Слушатель регистрируется в Channel, а само событие передается из
 * BotProtocolProvider в метод handleEvent()
 * При успехе/неудаче подключения вызывается коллбек с
 * соответствующим действием (реализация коллбека
 * находится в классе BotProtocolProvider)
 */
public interface ChannelReadEvent {
    void onSuccess(int id, byte[] data);
}
