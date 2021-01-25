package com.huawei.coauth.pool.helper;

import android.util.Log;
import com.huawei.coauth.pool.helper.AuthAttributes;
import com.huawei.coauth.pool.types.AuthAttributeType;
import com.huawei.coauth.tlv.TlvWrapper;
import com.huawei.hwpartsecurity.BuildConfig;
import java.util.HashMap;
import java.util.Map;

public class AuthAttributesSetter {
    private static final int DEFAULT_SIZE = 5;
    private static final String SEPARATOR = ":";
    private static final String TAG = "AuthAttributesSetter";
    AuthAttributes.Signer signer;
    private byte[] tlvBuf;
    private HashMap<Integer, byte[]> tlvMap = new HashMap<>(5);

    AuthAttributesSetter(byte[] data) {
        this.tlvBuf = data;
    }

    AuthAttributesSetter(AuthAttributes.Signer signer2) {
        if (signer2 != null) {
            this.signer = signer2;
        }
    }

    public void setBooleanValue(AuthAttributeType attrType, boolean isTrue) {
        setBooleanValue(attrType.getValue(), isTrue);
    }

    public void setBooleanValue(int attrType, boolean isTrue) {
        this.tlvMap.put(Integer.valueOf(attrType), TypeTrans.intToBytes(isTrue ? 1 : 0));
    }

    public void setIntValue(AuthAttributeType attrType, int value) {
        setIntValue(attrType.getValue(), value);
    }

    public void setIntValue(int attrType, int value) {
        this.tlvMap.put(Integer.valueOf(attrType), TypeTrans.intToBytes(value));
    }

    public void setLongValue(AuthAttributeType attrType, long value) {
        setLongValue(attrType.getValue(), value);
    }

    public void setLongValue(int attrType, long value) {
        this.tlvMap.put(Integer.valueOf(attrType), TypeTrans.longToBytes(value));
    }

    public void setStringValue(AuthAttributeType attrType, String value) {
        setStringValue(attrType.getValue(), value);
    }

    public void setStringValue(int attrType, String value) {
        this.tlvMap.put(Integer.valueOf(attrType), TypeTrans.stringToBytes(value));
    }

    public void setByteArrayValue(AuthAttributeType attrType, byte[] value) {
        setByteArrayValue(attrType.getValue(), value);
    }

    public void setByteArrayValue(int attrType, byte[] value) {
        this.tlvMap.put(Integer.valueOf(attrType), value);
    }

    public void setIntArrayValue(AuthAttributeType attrType, int[] value) {
        setIntArrayValue(attrType.getValue(), value);
    }

    public void setIntArrayValue(int attrType, int[] value) {
        this.tlvMap.put(Integer.valueOf(attrType), TypeTrans.intArrayToBytes(value));
    }

    public void setStringArrayValue(AuthAttributeType attrType, String[] value) {
        setStringArrayValue(attrType.getValue(), value);
    }

    public void setStringArrayValue(int attrType, String[] value) {
        this.tlvMap.put(Integer.valueOf(attrType), TypeTrans.stringArrayToBytes(value));
    }

    public byte[] getPayload() {
        if (this.tlvBuf == null) {
            encode();
        }
        return (byte[]) this.tlvBuf.clone();
    }

    public void encode() {
        if (this.tlvMap.containsKey(Integer.valueOf(AuthAttributeType.AUTH_EXPECT_ATTRS.getValue()))) {
            encodeAuthReq();
            return;
        }
        Log.i(TAG, "no expectAttrs, try to encode as AuthRes");
        encodeAuthRes();
    }

    /* access modifiers changed from: package-private */
    public void encodeAuthReq() {
        int expectAttrs = AuthAttributeType.AUTH_EXPECT_ATTRS.getValue();
        int cmdAttrs = AuthAttributeType.AUTH_CMD_ATTRS.getValue();
        TlvWrapper cmdTlv = new TlvWrapper();
        for (Map.Entry<Integer, byte[]> entry : this.tlvMap.entrySet()) {
            if (entry.getKey().intValue() != expectAttrs) {
                cmdTlv.appendBytes(entry.getKey().intValue(), entry.getValue());
            }
        }
        this.tlvMap.put(Integer.valueOf(cmdAttrs), cmdTlv.serialize());
        TlvWrapper firstTlv = new TlvWrapper();
        firstTlv.appendBytes(expectAttrs, this.tlvMap.get(Integer.valueOf(expectAttrs)));
        firstTlv.appendBytes(cmdAttrs, this.tlvMap.get(Integer.valueOf(cmdAttrs)));
        this.tlvBuf = firstTlv.serialize();
    }

    /* access modifiers changed from: package-private */
    public void encodeAuthRes() {
        int resultAttrs = AuthAttributeType.AUTH_RESULT_ATTRS.getValue();
        int sign = AuthAttributeType.AUTH_SIGNATURE.getValue();
        TlvWrapper resTlv = new TlvWrapper();
        for (Map.Entry<Integer, byte[]> entry : this.tlvMap.entrySet()) {
            if (entry.getKey().intValue() != sign) {
                resTlv.appendBytes(entry.getKey().intValue(), entry.getValue());
            }
        }
        this.tlvMap.put(Integer.valueOf(resultAttrs), resTlv.serialize());
        TlvWrapper resultTlv = new TlvWrapper();
        resultTlv.appendBytes(resultAttrs, this.tlvMap.get(Integer.valueOf(resultAttrs)));
        this.tlvBuf = resultTlv.serialize();
        AuthAttributes.Signer signer2 = this.signer;
        if (signer2 != null) {
            byte[] signData = signer2.signature(this.tlvMap.get(Integer.valueOf(resultAttrs)));
            TlvWrapper firstTlv = new TlvWrapper();
            firstTlv.appendBytes(resultAttrs, this.tlvMap.get(Integer.valueOf(resultAttrs)));
            firstTlv.appendBytes(sign, signData);
            this.tlvBuf = firstTlv.serialize();
        }
    }

    public String toString() {
        HashMap<Integer, byte[]> hashMap = this.tlvMap;
        if (hashMap == null) {
            return BuildConfig.FLAVOR;
        }
        return hashMap.toString();
    }
}
