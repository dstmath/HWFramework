package com.android.internal.telephony;

import android.os.SystemProperties;
import android.util.Log;
import java.util.Arrays;

public class HwWspTypeDecoderEx extends DefaultHwWspTypeDecoderEx {
    public static final String CONTENT_TYPE_B_CONNECT_WBXML = "application/vnd.wap.connectivity-wbxml";
    private static final int CONTENT_TYPE_B_CONNECT_WBXML_KEY = 54;
    private static final String LOG_TAG = "WspTypeDecoder";
    private static final byte OMA_CP_HDR_PARAM_MAC = 18;
    private static final byte OMA_CP_HDR_PARAM_SEC = 17;
    private IWspTypeDecoderInner mWpTypeDeCoder;
    private byte[] macData;
    private int secType;

    public HwWspTypeDecoderEx(IWspTypeDecoderInner wspTypeDecoder) {
        this.mWpTypeDeCoder = wspTypeDecoder;
    }

    private boolean decodeConnectwb(int startIndex, long dataTypeLength) {
        if (!this.mWpTypeDeCoder.decodeIntegerValue(startIndex) || this.mWpTypeDeCoder.getValue32() != 54) {
            Log.e(LOG_TAG, "decodeConnectwb: decodeIntegerValue fail!!");
            return false;
        }
        int index = 0 + this.mWpTypeDeCoder.getDecodedDataLength();
        byte[] wspData = this.mWpTypeDeCoder.getWspData();
        while (((long) index) < dataTypeLength) {
            int i = wspData[startIndex + index] & Byte.MAX_VALUE;
            if (i == 17) {
                int index2 = index + 1;
                this.secType = wspData[startIndex + index2];
                this.secType &= 127;
                index = index2 + 1;
            } else if (i != 18) {
                Log.d(LOG_TAG, "decodeConnectwb:: error into default");
                return false;
            } else {
                int index3 = index + 1;
                this.macData = new byte[((int) (dataTypeLength - ((long) index3)))];
                System.arraycopy(wspData, startIndex + index3, this.macData, 0, ((int) dataTypeLength) - index3);
                index = (int) dataTypeLength;
                this.mWpTypeDeCoder.setDecodedDataLength(index);
            }
        }
        Log.d(LOG_TAG, "decodeConnectwb finish!!!");
        return true;
    }

    public boolean decodeForConnectwb(int startIndex, int mediaPrefixLength) {
        long mediaFieldLength = this.mWpTypeDeCoder.getValue32();
        if (!SystemProperties.get("ro.config.hw_omacp", "0").equals("1") || !decodeConnectwb(startIndex + mediaPrefixLength, mediaFieldLength)) {
            return false;
        }
        this.mWpTypeDeCoder.setDecodedDataLength(this.mWpTypeDeCoder.getDecodedDataLength() + mediaPrefixLength);
        this.mWpTypeDeCoder.setStringValue(CONTENT_TYPE_B_CONNECT_WBXML);
        return true;
    }

    public byte[] getMacByte() {
        byte[] bArr = this.macData;
        if (bArr != null) {
            return Arrays.copyOf(bArr, bArr.length);
        }
        return null;
    }

    public int getSec() {
        return this.secType;
    }
}
