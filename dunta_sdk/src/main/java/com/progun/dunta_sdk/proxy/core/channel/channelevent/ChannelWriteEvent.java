package com.progun.dunta_sdk.proxy.core.channel.channelevent;

/**
 * Описывает событие записи данных в канал между хостом и SDK.
 * Слушатель регистрируется в Channel, а само событие передается из
 * BotProtocolProvider в метод handleEvent()
 * При успехе/неудаче подключения вызывается коллбек с
 * соответствующим действием (реализация коллбека
 * находится в классе BotProtocolProvider)
 */
public interface ChannelWriteEvent {
    void onSuccess(int id, int size);
}
