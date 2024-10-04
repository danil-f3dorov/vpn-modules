package com.progun.dunta_sdk.proxy.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/*

private static byte[] intToLittleEndian(long numero) {
	byte[] b = new byte[4];
	b[0] = (byte) (numero & 0xFF);
	b[1] = (byte) ((numero >> 8) & 0xFF);
	b[2] = (byte) ((numero >> 16) & 0xFF);
	b[3] = (byte) ((numero >> 24) & 0xFF);
	return b;
}

* */

public abstract class BitUtils {

    public static int getUnsignedShort(short value) {
        return value & 0xFFFF;
    }

    public static short getUnsignedShortBEOrder(short value) {
        byte[] arr = new byte[] {
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF)
        };
        return bytesToShortBeOrder(arr);
    }

    public static long getUnsignedInt(int value) {
        return value & 0xFFFFFFFFL;
    }

    public static short getUnsignedByte(byte value) {
        return (short) (value & 0xFF);
    }

    public static int bytesToIntLeOrder(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static short bytesToShortBeOrder(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    public static byte[] intToBytes(int value) {
        return new byte[] {
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF)
        };
    }

    public static void fill(byte[] bytes, int srcPos, int value) {
        while (value > 0) {
            bytes[srcPos++] = (byte) (value & 0xFF);
            value >>= 8;
        }
    }
}
