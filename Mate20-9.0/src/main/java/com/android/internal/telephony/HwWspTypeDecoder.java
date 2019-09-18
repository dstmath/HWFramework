package com.android.internal.telephony;

import android.os.SystemProperties;
import android.util.Log;
import java.util.Arrays;

public class HwWspTypeDecoder extends WspTypeDecoder {
    public static final String CONTENT_TYPE_B_CONNECT_WBXML = "application/vnd.wap.connectivity-wbxml";
    public static final int CONTENT_TYPE_B_CONNECT_WBXML_KEY = 54;
    private static final String LOG_TAG = "WspTypeDecoder";
    private static final byte OMA_CP_HDR_PARAM_MAC = 18;
    private static final byte OMA_CP_HDR_PARAM_SEC = 17;
    byte[] macData;
    int secType;

    public HwWspTypeDecoder(byte[] pdu) {
        super(pdu);
    }

    public boolean decodeConnectwb(int startIndex, long dataTypeLength) {
        if (!decodeIntegerValue(startIndex) || this.mUnsigned32bit != 54) {
            Log.e(LOG_TAG, "decodeConnectwb: decodeIntegerValue fail!!");
            return false;
        }
        int index = 0 + this.mDataLength;
        while (((long) index) < dataTypeLength) {
            switch (this.mWspData[startIndex + index] & Byte.MAX_VALUE) {
                case 17:
                    int index2 = index + 1;
                    this.secType = this.mWspData[startIndex + index2];
                    this.secType &= 127;
                    index = index2 + 1;
                    break;
                case 18:
                    int start = index + 1;
                    this.macData = new byte[((int) (dataTypeLength - ((long) start)))];
                    System.arraycopy(this.mWspData, startIndex + start, this.macData, 0, ((int) dataTypeLength) - start);
                    index = (int) dataTypeLength;
                    this.mDataLength = index;
                    break;
                default:
                    Log.d(LOG_TAG, "decodeConnectwb:: error into default");
                    return false;
            }
        }
        Log.d(LOG_TAG, "decodeConnectwb finish!!!");
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean decodeForConnectwb(int startIndex, int mediaPrefixLength) {
        long mediaFieldLength = getValue32();
        if (!SystemProperties.get("ro.config.hw_omacp", "0").equals("1") || !decodeConnectwb(startIndex + mediaPrefixLength, mediaFieldLength)) {
            return false;
        }
        this.mDataLength += mediaPrefixLength;
        this.mStringValue = CONTENT_TYPE_B_CONNECT_WBXML;
        return true;
    }

    public byte[] getMacByte() {
        if (this.macData != null) {
            return Arrays.copyOf(this.macData, this.macData.length);
        }
        return null;
    }

    public int getSec() {
        return this.secType;
    }
}
