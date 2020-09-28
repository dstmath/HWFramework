package com.huawei.internal.telephony.cdma.sms;

import com.android.internal.telephony.cdma.sms.UserData;
import com.huawei.internal.telephony.SmsHeaderEx;

public class UserDataEx {
    public static final int ENCODING_7BIT_ASCII = 2;
    private UserData mUserData;

    public void setUserData(UserData userData) {
        this.mUserData = userData;
    }

    public UserData getUserData() {
        return this.mUserData;
    }

    public void setMsgEncoding(int msgEncoding) {
        UserData userData = this.mUserData;
        if (userData != null) {
            userData.msgEncoding = msgEncoding;
        }
    }

    public void setMsgEncodingSet(boolean msgEncodingSet) {
        UserData userData = this.mUserData;
        if (userData != null) {
            userData.msgEncodingSet = msgEncodingSet;
        }
    }

    public void setPayload(byte[] payload) {
        UserData userData = this.mUserData;
        if (userData != null) {
            userData.payload = payload;
        }
    }

    public void setPayloadLength(byte value) {
        UserData userData = this.mUserData;
        if (userData != null && userData.payload != null) {
            this.mUserData.payload[0] = value;
        }
    }

    public void setPayloadStr(String payloadStr) {
        UserData userData = this.mUserData;
        if (userData != null) {
            userData.payloadStr = payloadStr;
        }
    }

    public void setNumFields(int numFields) {
        UserData userData = this.mUserData;
        if (userData != null) {
            userData.numFields = numFields;
        }
    }

    public String getPayloadStr() {
        UserData userData = this.mUserData;
        if (userData != null) {
            return userData.payloadStr;
        }
        return null;
    }

    public byte[] getPayload() {
        UserData userData = this.mUserData;
        if (userData != null) {
            return userData.payload;
        }
        return null;
    }

    public static byte[] stringToAscii(String str) {
        return UserData.stringToAscii(str);
    }

    public void setPayloadForFirstByte(byte payloadByte) {
        this.mUserData.payload[0] = payloadByte;
    }

    public void setUserDataHeader(SmsHeaderEx smsHeader) {
        this.mUserData.userDataHeader = smsHeader.getSmsHeader();
    }
}
