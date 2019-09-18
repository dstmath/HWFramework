package com.huawei.hsm.permission.minimms;

import android.util.Log;
import java.io.UnsupportedEncodingException;

class EncodedStringValue implements Cloneable {
    private static final String TAG = "EncodedStringValue";
    private int mCharacterSet;
    private byte[] mData;

    public EncodedStringValue(int charset, byte[] data) {
        if (data != null) {
            this.mCharacterSet = charset;
            this.mData = new byte[data.length];
            System.arraycopy(data, 0, this.mData, 0, data.length);
            return;
        }
        throw new NullPointerException("EncodedStringValue: Text-string is null.");
    }

    public EncodedStringValue(byte[] data) {
        this(CharacterSets.DEFAULT_CHARSET, data);
    }

    public EncodedStringValue(String data) {
        try {
            this.mData = data.getBytes(CharacterSets.DEFAULT_CHARSET_NAME);
            this.mCharacterSet = CharacterSets.DEFAULT_CHARSET;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Default encoding must be supported.");
        }
    }

    public byte[] getTextString() {
        byte[] byteArray = new byte[this.mData.length];
        System.arraycopy(this.mData, 0, byteArray, 0, this.mData.length);
        return byteArray;
    }

    public void setTextString(byte[] textString) {
        if (textString != null) {
            this.mData = new byte[textString.length];
            System.arraycopy(textString, 0, this.mData, 0, textString.length);
            return;
        }
        throw new NullPointerException("EncodedStringValue: Text-string is null.");
    }

    public Object clone() throws CloneNotSupportedException {
        super.clone();
        int len = this.mData.length;
        byte[] dstBytes = new byte[len];
        System.arraycopy(this.mData, 0, dstBytes, 0, len);
        try {
            return new EncodedStringValue(this.mCharacterSet, dstBytes);
        } catch (Exception e) {
            Log.e(TAG, "failed to clone an EncodedStringValue");
            e.printStackTrace();
            throw new CloneNotSupportedException(e.getMessage());
        }
    }
}
