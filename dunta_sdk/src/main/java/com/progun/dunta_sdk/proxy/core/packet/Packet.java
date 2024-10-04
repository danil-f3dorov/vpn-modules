package com.progun.dunta_sdk.proxy.core.packet;

/**
 * Абстракный класс для описания сущности пакета данных.
 * Так же хранит служебную информацию пакета и протокола(
 * id партнера, значения маркера, константы таймаута и пр.)
 */
//@SuppressWarnings("SpellCheckingInspection")
public abstract class Packet {
    protected static final int VERSION = 0x01;

    public StringBuilder debugInfo;
}