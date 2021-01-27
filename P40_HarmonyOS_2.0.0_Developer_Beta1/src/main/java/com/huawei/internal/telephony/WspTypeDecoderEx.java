package com.huawei.internal.telephony;

import com.android.internal.telephony.WspTypeDecoder;

public class WspTypeDecoderEx {
    WspTypeDecoder mWspTypeDecoder;

    public void setWspTypeDecoder(WspTypeDecoder wspTypeDecoder) {
        this.mWspTypeDecoder = wspTypeDecoder;
    }

    public byte[] getMacByte() {
        WspTypeDecoder wspTypeDecoder = this.mWspTypeDecoder;
        if (wspTypeDecoder != null) {
            return wspTypeDecoder.getMacByte();
        }
        return null;
    }

    public int getSec() {
        WspTypeDecoder wspTypeDecoder = this.mWspTypeDecoder;
        if (wspTypeDecoder != null) {
            return wspTypeDecoder.getSec();
        }
        return -1;
    }
}
