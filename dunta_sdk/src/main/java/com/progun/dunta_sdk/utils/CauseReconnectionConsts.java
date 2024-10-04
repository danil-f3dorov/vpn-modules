package com.progun.dunta_sdk.utils;


/*
* Константы для отслеживания причины переподключения СДК к серверу. Не является точной метрикой, но иногда может дать понимание причины отключения.
* Работает просто как константы, которые при разных крашах, сменах типов сети, таймаутов, ошибок в протоколе и т.д.
* сохраняется в памяти и отслыается на сервер.
* */

public class CauseReconnectionConsts {
    public final static String TAG = CauseReconnectionConsts.class.getSimpleName();

    /**
     * Значение для инициализации константы - все в порядке
     */
    public final static short EMPTY_VALUE = 0;

    /**
     * Внезапный обрыв соединения, сервер упал, перезагружается
     */
    public final static short UNEXPECTED_CONNECTION_RESET = 1;

    /**
     * Отключение от серевера по таймауту
     */
    public final static short TIMEOUT = 2;

    /**
     * Что то упало в приложении
     */
    public final static short APPLICATION_CRASH = 3;

    /**
     * Потряно соединение с сетью. Либо проблемы с сетью, либо смена типа сети
     */
    public final static short NETWORK_WAS_LOST = 4;

    /**
     * Была ошибка при попытке чтения данных с сокета сервера
     */
    public final static short READ_SERVER_DATA_FAILED = 5;

    /**
     * Ошибка при попытке записи данных в сокет сервера
     */
    public final static short WRITE_SERVER_DATA_FAILED = 6;

    /**
     * Ошибка при попытке чтения данных с хоста
     */
    public final static short READ_HOST_DATA_FAILED = 7;

    /**
     * Ошибка при попытке записи данных на хост
     */
    public final static short WRITE_HOST_DATA_FAILED = 8;

    /**
     * Ошибка при чтении/обработке SRV пакета
     */
    public final static short HANDLE_SRV_PACKET_FAILED = 9;

    /**
     * Ошибка при чтении/обработке BOT пакета
     */
    public final static short HANDLE_BOT_PACKET_FAILED = 10;

    /**
     * Отсутствует маркер в SRV-пакете
     */
    public final static short MARKER_SRV_NOT_FOUND = 11;

    /**
     * Фоновый сервис был мягко уничтожен (система вызвала onDestroy)
     */
    public final static short SERVICE_SOFT_DESTROYED = 12;

    /**
     * Ошибка в момент закрытия каналов/сокетов
     */
    public final static short CLASSES_CLOSE_FAILED = 13;

    /**
     * Ошибка, связанная с пакетом SRV_SENT
     */
    public final static short HANDLE_SRV_SENT_FAILED = 14;

    /**
     * Ошибка, связанная с пакетом SRV_RECV
     */
    public final static short HANDLE_SRV_RECV_FAILED = 15;

    /**
     * Ошибка с чтением пакета с сервера, что то не то пришло или как-то неверно считалось с сокета
     * */
    public final static short SRV_INVALID_PACKET = 16;

    public final static short SRV_SOCKET_END_OF_STREAM = 17;
    public final static short SRV_SOCKET_CONNECTION_RESET = 18;

    public final static short END_OF_WORK = 19;

    public final static short SELECTOR_OPEN_FAILED = 20;


    /**
     * Отсутствует маркер в BOT-пакете
     */
    public final static short MARKER_BOT_NOT_FOUND = 21;

    /**
     * Отключеие из за ошибки в переключении каналов селектора
     */
    public final static short SELECTOR_SWITCHING_FAILED = 22;

    /**
     * Ошибка в момент открытия каналов/сокетов
     */
    public final static short CLASSES_OPEN_FAILED = 23;

    /**
     * Ошибка, связанная с пакетом BOT_SENT
     */
    public final static short HANDLE_BOT_SENT_FAILED = 24;

    /**
     * Ошибка, связанная с пакетом BOT_RECV
     */
    public final static short HANDLE_BOT_RECV_FAILED = 25;


/*    public static String causeToString(short cause) {
        switch (cause) {
            case EMPTY_VALUE -> {return "EMPTY_VALUE";}
            case UNEXPECTED_CONNECTION_RESET -> {return "UNEXPECTED_CONNECTION_RESET";}
            case TIMEOUT -> {return "TIMEOUT";}
            case APPLICATION_CRASH -> {return "APPLICATION_CRASH";}
            case NETWORK_WAS_LOST -> {return "NETWORK_WAS_LOST";}
            case READ_SERVER_DATA_FAILED -> {return "READ_SERVER_DATA_FAILED";}
            case WRITE_SERVER_DATA_FAILED -> {return "WRITE_SERVER_DATA_FAILED";}
            case READ_HOST_DATA_FAILED -> {return "READ_HOST_DATA_FAILED";}
            case WRITE_HOST_DATA_FAILED -> {return "WRITE_HOST_DATA_FAILED";}
            case HANDLE_SRV_PACKET_FAILED -> {return "HANDLE_SRV_PACKET_FAILED";}
            case HANDLE_BOT_PACKET_FAILED -> {return "HANDLE_BOT_PACKET_FAILED";}
            case MARKER_BOT_NOT_FOUND -> {return "MARKER_BOT_NOT_FOUND";}
            case MARKER_SRV_NOT_FOUND -> {return "MARKER_SRV_NOT_FOUND";}
            case SERVICE_SOFT_DESTROYED -> {return "SERVICE_SOFT_DESTROYED";}
            case SELECTOR_SWITCHING_FAILED -> {return "SELECTOR_SWITCHING_FAILED";}
            case CLASSES_CLOSE_FAILED -> {return "CLASSES_CLOSE_FAILED";}
            case CLASSES_OPEN_FAILED -> {return "CLASSES_OPEN_FAILED";}
            case HANDLE_BOT_SENT_FAILED -> {return "HANDLE_BOT_SENT_FAILED";}
            case HANDLE_BOT_RECV_FAILED -> {return "HANDLE_BOT_RECV_FAILED";}
            case HANDLE_SRV_SENT_FAILED -> {return "HANDLE_SRV_SENT_FAILED";}
            case HANDLE_SRV_RECV_FAILED -> {return "HANDLE_SRV_RECV_FAILED";}
            case SRV_INVALID_PACKET -> {return "SRV_INVALID_PACKET";}
            case SRV_SOCKET_END_OF_STREAM -> {return "SRV_SOCKET_END_OF_STREAM";}
            default -> throw new IllegalStateException("Unexpected value: " + cause);
        }
    }*/

    public static String causeToString(short cause) {
        switch (cause) {
            case EMPTY_VALUE: return "EMPTY_VALUE";
            case UNEXPECTED_CONNECTION_RESET: return "UNEXPECTED_CONNECTION_RESET";
            case TIMEOUT: return "TIMEOUT";
            case APPLICATION_CRASH: return "APPLICATION_CRASH";
            case NETWORK_WAS_LOST: return "NETWORK_WAS_LOST";
            case READ_SERVER_DATA_FAILED: return "READ_SERVER_DATA_FAILED";
            case WRITE_SERVER_DATA_FAILED: return "WRITE_SERVER_DATA_FAILED";
            case READ_HOST_DATA_FAILED: return "READ_HOST_DATA_FAILED";
            case WRITE_HOST_DATA_FAILED: return "WRITE_HOST_DATA_FAILED";
            case HANDLE_SRV_PACKET_FAILED: return "HANDLE_SRV_PACKET_FAILED";
            case HANDLE_BOT_PACKET_FAILED: return "HANDLE_BOT_PACKET_FAILED";
            case MARKER_BOT_NOT_FOUND: return "MARKER_BOT_NOT_FOUND";
            case MARKER_SRV_NOT_FOUND: return "MARKER_SRV_NOT_FOUND";
            case SERVICE_SOFT_DESTROYED: return "SERVICE_SOFT_DESTROYED";
            case SELECTOR_SWITCHING_FAILED: return "SELECTOR_SWITCHING_FAILED";
            case CLASSES_CLOSE_FAILED: return "CLASSES_CLOSE_FAILED";
            case CLASSES_OPEN_FAILED: return "CLASSES_OPEN_FAILED";
            case HANDLE_BOT_SENT_FAILED: return "HANDLE_BOT_SENT_FAILED";
            case HANDLE_BOT_RECV_FAILED: return "HANDLE_BOT_RECV_FAILED";
            case HANDLE_SRV_SENT_FAILED: return "HANDLE_SRV_SENT_FAILED";
            case HANDLE_SRV_RECV_FAILED: return "HANDLE_SRV_RECV_FAILED";
            case SRV_INVALID_PACKET: return "SRV_INVALID_PACKET";
            case SRV_SOCKET_END_OF_STREAM: return "SRV_SOCKET_END_OF_STREAM";
            default: throw new IllegalStateException("Unexpected value: " + cause);
        }
    }
}
