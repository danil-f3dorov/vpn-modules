package com.progun.dunta_sdk.proxy.core.jsonserver;


/*
* Нужен для отслеживания была ли отправлена
* расширенная информация о клиенте на JSON сервер при первом запуске.
* При следующих подключениях отправляется меньше инфы
* */
public interface JsonInitListener {
    void initialize();
}
