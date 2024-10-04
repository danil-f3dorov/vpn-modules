package com.progun.dunta_sdk.proxy.encryption;

import com.progun.dunta_sdk.proxy.utils.BitUtils;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;

public abstract class Cryptor {
    protected int index;
    protected byte[] key;

    Cryptor(int seed) {
        key = BitUtils.intToBytes(seed);
    }

    Cryptor() {
        key = BitUtils.intToBytes(ProtocolConstants.CRYPT_SEED);
    }

    public void setKey(int key) {
        this.key = BitUtils.intToBytes(key);
    }

    public void process(byte[] data) {
        for (int i = 0; i < data.length; ++i) {
            data[i] ^= key[index++];
            if (index == Integer.BYTES) {
                index = 0;
                rand();
            }
        }
    }
    public void process(byte[] data, int size, int pos) {
        for (int i = pos; i < size; ++i) {
            data[i] ^= key[index++];
            if (index == Integer.BYTES) {
                index = 0;
                rand();
            }
        }
    }

    protected void rand() {
        key = BitUtils.intToBytes(
                (214013 * BitUtils.bytesToIntLeOrder(key) + 2531011) & 0x7fffffff
        );
    }
}