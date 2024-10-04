package com.progun.dunta_sdk.proxy.core.channel.channelevent;

/**
 * Описывает события, которые могут произойти
 * между SDK и прокси-сервером.
 */
public interface BotEvent {
    void onFailed();

    void onSuccess();
}
