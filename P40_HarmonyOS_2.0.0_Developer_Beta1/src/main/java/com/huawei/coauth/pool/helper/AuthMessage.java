package com.huawei.coauth.pool.helper;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;

public class AuthMessage {
    private static final String FILE_DESCRIPTOR = "fileDescriptor";
    private static final String MSG_BIG_DATASIZE = "bigDataSize";
    private static final String MSG_DATA = "data";
    private static final int MSG_DEFAULT_SIZE = 0;
    private static final int MSG_MAX_SIZE = 65535;
    private static final String MSG_TLV = "tlv";
    private static final String SIGN_DATA = "sign";
    private static final String TAG = "RES_POOL|AuthMessage";
    private Bundle msgData = new Bundle();

    private AuthMessage(Bundle bundle) {
        if (bundle != null) {
            this.msgData = new Bundle(bundle);
        }
    }

    public static AuthMessage fromByteArray(byte[] msg, byte[] sign) {
        Bundle bundle = new Bundle();
        if (msg != null) {
            bundle.putByteArray("data", msg);
        }
        if (sign != null) {
            bundle.putByteArray("sign", sign);
        }
        return new AuthMessage(bundle);
    }

    public static AuthMessage fromByteArray(byte[] msg) {
        Bundle bundle = new Bundle();
        if (msg != null) {
            bundle.putByteArray("data", msg);
        }
        return new AuthMessage(bundle);
    }

    public static AuthMessage fromParcelFileDescriptor(ParcelFileDescriptor fileDescriptor, int length, byte[] sign) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(FILE_DESCRIPTOR, fileDescriptor);
        bundle.putInt(MSG_BIG_DATASIZE, length);
        if (sign != null) {
            bundle.putByteArray("sign", sign);
        }
        return new AuthMessage(bundle);
    }

    public static AuthMessage fromBundle(Bundle bundle) {
        return new AuthMessage(bundle);
    }

    public Bundle toBundle() {
        return new Bundle(this.msgData);
    }

    public byte[] getMessageByteArray() {
        if (!this.msgData.containsKey("data")) {
            return new byte[0];
        }
        try {
            return this.msgData.getByteArray("data");
        } catch (ArrayIndexOutOfBoundsException e) {
            return new byte[0];
        }
    }

    public byte[] getMessageSignature() {
        if (!this.msgData.containsKey("sign")) {
            return new byte[0];
        }
        try {
            return this.msgData.getByteArray("sign");
        } catch (ArrayIndexOutOfBoundsException e) {
            return new byte[0];
        }
    }

    public ParcelFileDescriptor getParcelFileDescriptor() {
        if (!this.msgData.containsKey(FILE_DESCRIPTOR)) {
            return new ParcelFileDescriptor(null);
        }
        return (ParcelFileDescriptor) this.msgData.getParcelable(FILE_DESCRIPTOR);
    }

    public int getDataLength() {
        if (!this.msgData.containsKey(MSG_BIG_DATASIZE)) {
            return 0;
        }
        return this.msgData.getInt(MSG_BIG_DATASIZE);
    }
}
