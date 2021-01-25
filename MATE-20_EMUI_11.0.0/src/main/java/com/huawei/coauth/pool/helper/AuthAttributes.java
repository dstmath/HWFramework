package com.huawei.coauth.pool.helper;

import android.os.Bundle;
import android.util.Log;
import com.huawei.coauth.pool.types.AuthAttributeType;
import com.huawei.hwpartsecurity.BuildConfig;

public final class AuthAttributes {
    private static final int RESULT_ERROR = -1;
    private static final String TAG = "AuthAttributes";
    private static final String TAG_DATA = "msgData";
    private Bundle attrData = new Bundle();
    private AuthAttributesGetter getter;
    private AuthAttributesSetter setter;

    public interface Signer {
        byte[] signature(byte[] bArr);
    }

    private AuthAttributes(Bundle bundle) {
        if (bundle != null) {
            this.attrData = bundle.deepCopy();
        }
    }

    private AuthAttributes(AuthAttributesGetter getter2) {
        this.getter = getter2;
    }

    private AuthAttributes(AuthAttributesSetter setter2) {
        this.setter = setter2;
    }

    public static AuthAttributes fromTlvBuffer(byte[] tlvMsg) {
        return new AuthAttributes(new AuthAttributesGetter(tlvMsg));
    }

    public static AuthAttributes setTlvBuffer(byte[] tlvMsg) {
        return new AuthAttributes(new AuthAttributesSetter(tlvMsg));
    }

    public static AuthAttributes create(Signer signer) {
        return new AuthAttributes(new AuthAttributesSetter(signer));
    }

    public static AuthAttributes fromBundle(Bundle bundle) {
        if (bundle == null) {
            return new AuthAttributes(new Bundle());
        }
        return new AuthAttributes(bundle);
    }

    public Bundle toBundle() {
        Bundle bundle = this.attrData;
        if (bundle == null) {
            return new Bundle();
        }
        return bundle.deepCopy();
    }

    public byte[] getPayload() {
        AuthAttributesGetter authAttributesGetter = this.getter;
        if (authAttributesGetter != null) {
            return authAttributesGetter.getPayload();
        }
        Log.e(TAG, "getPayload error, getter is null");
        return new byte[0];
    }

    public static AuthAttributes getTlvfromBundle(Bundle bundle) {
        if (bundle == null) {
            return new AuthAttributes(new AuthAttributesGetter(new byte[0]));
        }
        try {
            return new AuthAttributes(new AuthAttributesGetter(bundle.getByteArray(TAG_DATA)));
        } catch (ArrayIndexOutOfBoundsException e) {
            return new AuthAttributes(new AuthAttributesGetter(new byte[0]));
        }
    }

    public Bundle setTlvtoBundle() {
        Bundle bundle = new Bundle();
        AuthAttributesSetter authAttributesSetter = this.setter;
        if (authAttributesSetter != null) {
            bundle.putByteArray(TAG_DATA, authAttributesSetter.getPayload());
            return bundle;
        }
        Log.e(TAG, "toBundle error: neither getter nor setter");
        return bundle;
    }

    public int[] getExpectAttrTypes() {
        AuthAttributesGetter authAttributesGetter = this.getter;
        if (authAttributesGetter != null) {
            return authAttributesGetter.getExpectAttrTypes();
        }
        Log.e(TAG, "getExpectAttrTypes error, getter is null");
        return new int[0];
    }

    public boolean getBooleanValue(AuthAttributeType attrType) {
        return getBooleanValue(attrType.getValue());
    }

    public boolean getBooleanValue(int attrType) {
        AuthAttributesGetter authAttributesGetter = this.getter;
        if (authAttributesGetter != null) {
            return authAttributesGetter.getBooleanValue(attrType);
        }
        Log.e(TAG, "getBooleanValue error, getter is null");
        return false;
    }

    public int getIntValue(AuthAttributeType attrType) {
        return getIntValue(attrType.getValue());
    }

    public int getIntValue(int attrType) {
        AuthAttributesGetter authAttributesGetter = this.getter;
        if (authAttributesGetter != null) {
            return authAttributesGetter.getIntValue(attrType);
        }
        Log.e(TAG, "getIntValue error, getter is null");
        return -1;
    }

    public long getLongValue(AuthAttributeType attrType) {
        return getLongValue(attrType.getValue());
    }

    public long getLongValue(int attrType) {
        AuthAttributesGetter authAttributesGetter = this.getter;
        if (authAttributesGetter != null) {
            return authAttributesGetter.getLongValue(attrType);
        }
        Log.e(TAG, "getLongValue error, getter is null");
        return -1;
    }

    public String getStringValue(AuthAttributeType attrType) {
        return getStringValue(attrType.getValue());
    }

    public String getStringValue(int attrType) {
        AuthAttributesGetter authAttributesGetter = this.getter;
        if (authAttributesGetter != null) {
            return authAttributesGetter.getStringValue(attrType);
        }
        Log.e(TAG, "getStringValue error, getter is null");
        return BuildConfig.FLAVOR;
    }

    public byte[] getByteArrayValue(AuthAttributeType attrType) {
        return getByteArrayValue(attrType.getValue());
    }

    public byte[] getByteArrayValue(int attrType) {
        AuthAttributesGetter authAttributesGetter = this.getter;
        if (authAttributesGetter != null) {
            return authAttributesGetter.getByteArrayValue(attrType);
        }
        Log.e(TAG, "getByteArrayValue error, getter is null");
        return new byte[0];
    }

    public int[] getIntArrayValue(AuthAttributeType attrType) {
        return getIntArrayValue(attrType.getValue());
    }

    public int[] getIntArrayValue(int attrType) {
        AuthAttributesGetter authAttributesGetter = this.getter;
        if (authAttributesGetter != null) {
            return authAttributesGetter.getIntArrayValue(attrType);
        }
        Log.e(TAG, "getIntArrayValue error, getter is null");
        return new int[0];
    }

    public String[] getStringArrayValue(AuthAttributeType attrType) {
        return getStringArrayValue(attrType.getValue());
    }

    public String[] getStringArrayValue(int attrType) {
        AuthAttributesGetter authAttributesGetter = this.getter;
        if (authAttributesGetter != null) {
            return authAttributesGetter.getStringArrayValue(attrType);
        }
        Log.e(TAG, "getStringArrayValue error, getter is null");
        return new String[0];
    }

    public void setBooleanValue(AuthAttributeType attrType, boolean isTrue) {
        setBooleanValue(attrType.getValue(), isTrue);
    }

    public void setBooleanValue(int attrType, boolean isTrue) {
        AuthAttributesSetter authAttributesSetter = this.setter;
        if (authAttributesSetter != null) {
            authAttributesSetter.setBooleanValue(attrType, isTrue);
        }
    }

    public void setIntValue(AuthAttributeType attrType, int value) {
        setIntValue(attrType.getValue(), value);
    }

    public void setIntValue(int attrType, int value) {
        AuthAttributesSetter authAttributesSetter = this.setter;
        if (authAttributesSetter != null) {
            authAttributesSetter.setIntValue(attrType, value);
        }
    }

    public void setLongValue(AuthAttributeType attrType, long value) {
        setLongValue(attrType.getValue(), value);
    }

    public void setLongValue(int attrType, long value) {
        AuthAttributesSetter authAttributesSetter = this.setter;
        if (authAttributesSetter != null) {
            authAttributesSetter.setLongValue(attrType, value);
        }
    }

    public void setStringValue(AuthAttributeType attrType, String value) {
        setStringValue(attrType.getValue(), value);
    }

    public void setStringValue(int attrType, String value) {
        AuthAttributesSetter authAttributesSetter = this.setter;
        if (authAttributesSetter != null) {
            authAttributesSetter.setStringValue(attrType, value);
        }
    }

    public void setByteArrayValue(AuthAttributeType attrType, byte[] value) {
        setByteArrayValue(attrType.getValue(), value);
    }

    public void setByteArrayValue(int attrType, byte[] value) {
        AuthAttributesSetter authAttributesSetter = this.setter;
        if (authAttributesSetter != null) {
            authAttributesSetter.setByteArrayValue(attrType, value);
        }
    }

    public void setIntArrayValue(AuthAttributeType attrType, int[] value) {
        setIntArrayValue(attrType.getValue(), value);
    }

    public void setIntArrayValue(int attrType, int[] value) {
        AuthAttributesSetter authAttributesSetter = this.setter;
        if (authAttributesSetter != null) {
            authAttributesSetter.setIntArrayValue(attrType, value);
        }
    }

    public void setStringArrayValue(AuthAttributeType attrType, String[] value) {
        setStringArrayValue(attrType.getValue(), value);
    }

    public void setStringArrayValue(int attrType, String[] value) {
        AuthAttributesSetter authAttributesSetter = this.setter;
        if (authAttributesSetter != null) {
            authAttributesSetter.setStringArrayValue(attrType, value);
        }
    }

    public String toString() {
        AuthAttributesGetter authAttributesGetter = this.getter;
        if (authAttributesGetter != null) {
            return authAttributesGetter.toString();
        }
        AuthAttributesSetter authAttributesSetter = this.setter;
        if (authAttributesSetter != null) {
            return authAttributesSetter.toString();
        }
        return BuildConfig.FLAVOR;
    }
}
