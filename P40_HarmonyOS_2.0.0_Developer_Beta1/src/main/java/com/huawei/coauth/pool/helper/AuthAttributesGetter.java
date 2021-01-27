package com.huawei.coauth.pool.helper;

import android.util.Log;
import com.huawei.coauth.pool.types.AuthAttributeType;
import com.huawei.coauth.tlv.TlvBase;
import com.huawei.coauth.tlv.TlvTransformException;
import com.huawei.coauth.tlv.TlvWrapper;
import com.huawei.hwpartsecurity.BuildConfig;
import java.util.HashMap;

public class AuthAttributesGetter {
    private static final int DEFAULT_SIZE = 5;
    private static final int RESULT_ERROR = -1;
    private static final String SEPARATOR = ":";
    private static final String TAG = "AuthAttributesGetter";
    private int[] exceptedAttrsData;
    private byte[] tlvBuf;
    private HashMap<Integer, byte[]> tlvMap = new HashMap<>(5);

    AuthAttributesGetter(byte[] data) {
        Log.i(TAG, "init AuthAttributesGetter");
        if (data == null) {
            Log.e(TAG, "init AuthAttributesGetter fail, data is null");
            return;
        }
        this.tlvBuf = (byte[]) data.clone();
        parse();
    }

    public int[] getExpectAttrTypes() {
        int[] iArr = this.exceptedAttrsData;
        return iArr == null ? new int[0] : (int[]) iArr.clone();
    }

    public byte[] getPayload() {
        byte[] bArr = this.tlvBuf;
        if (bArr != null) {
            return (byte[]) bArr.clone();
        }
        Log.e(TAG, "getPayload fail, tlvBuf is null");
        return new byte[0];
    }

    public boolean getBooleanValue(AuthAttributeType attrType) {
        return getBooleanValue(attrType.getValue());
    }

    public boolean getBooleanValue(int attrType) {
        byte[] data = this.tlvMap.get(Integer.valueOf(attrType));
        if (data != null) {
            return TypeTrans.bytesToBoolean(data);
        }
        Log.e(TAG, "getBooleanValue error, data is null");
        return true;
    }

    public int getIntValue(AuthAttributeType attrType) {
        return getIntValue(attrType.getValue());
    }

    public int getIntValue(int attrType) {
        byte[] data = this.tlvMap.get(Integer.valueOf(attrType));
        if (data != null) {
            return TypeTrans.bytesToInt(data);
        }
        Log.e(TAG, "getIntValue error, data is null");
        return -1;
    }

    public long getLongValue(AuthAttributeType attrType) {
        return getLongValue(attrType.getValue());
    }

    public long getLongValue(int attrType) {
        byte[] data = this.tlvMap.get(Integer.valueOf(attrType));
        if (data != null) {
            return TypeTrans.bytesToLong(data);
        }
        Log.e(TAG, "getLongValue error, data is null");
        return -1;
    }

    public String getStringValue(AuthAttributeType attrType) {
        return getStringValue(attrType.getValue());
    }

    public String getStringValue(int attrType) {
        byte[] data = this.tlvMap.get(Integer.valueOf(attrType));
        if (data != null) {
            return TypeTrans.bytesToString(data);
        }
        Log.e(TAG, "getStringValue error, data is null");
        return BuildConfig.FLAVOR;
    }

    public byte[] getByteArrayValue(AuthAttributeType attrType) {
        return getByteArrayValue(attrType.getValue());
    }

    public byte[] getByteArrayValue(int attrType) {
        byte[] data = this.tlvMap.get(Integer.valueOf(attrType));
        if (data != null) {
            return data;
        }
        Log.e(TAG, "getByteArrayValue error, data is null");
        return new byte[0];
    }

    public int[] getIntArrayValue(AuthAttributeType attrType) {
        return getIntArrayValue(attrType.getValue());
    }

    public int[] getIntArrayValue(int attrType) {
        byte[] data = this.tlvMap.get(Integer.valueOf(attrType));
        if (data != null) {
            return TypeTrans.bytesToIntArray(data);
        }
        Log.e(TAG, "getIntArrayValue error, data is null");
        return new int[0];
    }

    public String[] getStringArrayValue(AuthAttributeType attrType) {
        return getStringArrayValue(attrType.getValue());
    }

    public String[] getStringArrayValue(int attrType) {
        byte[] data = this.tlvMap.get(Integer.valueOf(attrType));
        if (data != null) {
            return TypeTrans.bytesToStringArray(data);
        }
        Log.e(TAG, "getStringArrayValue error, data is null");
        return new String[0];
    }

    private void parse() {
        this.exceptedAttrsData = new int[0];
        decode();
    }

    private void decode() {
        try {
            int expectAttrs = AuthAttributeType.AUTH_EXPECT_ATTRS.getValue();
            int cmdAttrs = AuthAttributeType.AUTH_CMD_ATTRS.getValue();
            int resultAttrs = AuthAttributeType.AUTH_RESULT_ATTRS.getValue();
            if (this.tlvBuf == null) {
                Log.e(TAG, "decode TLV data error, data is null");
                return;
            }
            for (TlvBase tlv : TlvWrapper.deserialize(this.tlvBuf).getTlvList()) {
                this.tlvMap.put(Integer.valueOf(tlv.getType()), tlv.getValue());
            }
            if (this.tlvMap.containsKey(Integer.valueOf(expectAttrs))) {
                this.exceptedAttrsData = TypeTrans.bytesToIntArray(this.tlvMap.get(Integer.valueOf(expectAttrs)));
                for (TlvBase tlv2 : TlvWrapper.deserialize(this.tlvMap.get(Integer.valueOf(cmdAttrs))).getTlvList()) {
                    this.tlvMap.put(Integer.valueOf(tlv2.getType()), tlv2.getValue());
                }
            } else if (this.tlvMap.containsKey(Integer.valueOf(resultAttrs))) {
                for (TlvBase tlv3 : TlvWrapper.deserialize(this.tlvMap.get(Integer.valueOf(resultAttrs))).getTlvList()) {
                    this.tlvMap.put(Integer.valueOf(tlv3.getType()), tlv3.getValue());
                }
            } else {
                Log.e(TAG, "decode TLV data error, neighter REQUSET TLV nor RESUTL TLV");
            }
        } catch (TlvTransformException ex) {
            Log.e(TAG, "decode Auth message error = " + ex.getErrorMsg());
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
