package com.huawei.internal.telephony.cdma.sms;

import com.android.internal.telephony.cdma.sms.UserData;
import com.huawei.internal.telephony.SmsHeaderEx;

public class UserDataEx {
    public static final int ENCODING_7BIT_ASCII = 2;
    private UserData mUserData = new UserData();

    public static byte[] stringToAscii(String str) {
        return UserData.stringToAscii(str);
    }

    public UserData getUserData() {
        return this.mUserData;
    }

    public void setUserData(UserData userData) {
        this.mUserData = userData;
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

    public void setPayloadLength(byte value) {
        UserData userData = this.mUserData;
        if (userData != null && userData.payload != null) {
            this.mUserData.payload[0] = value;
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

    public void setPayloadStr(String payloadStr) {
        UserData userData = this.mUserData;
        if (userData != null) {
            userData.payloadStr = payloadStr;
        }
    }

    public byte[] getPayload() {
        UserData userData = this.mUserData;
        if (userData != null) {
            return userData.payload;
        }
        return new byte[0];
    }

    public void setPayload(byte[] payload) {
        UserData userData = this.mUserData;
        if (userData != null) {
            userData.payload = payload;
        }
    }

    public void setPayloadForFirstByte(byte payloadByte) {
        UserData userData = this.mUserData;
        if (userData != null) {
            userData.payload[0] = payloadByte;
        }
    }

    public void setUserDataHeader(SmsHeaderEx smsHeader) {
        UserData userData = this.mUserData;
        if (userData != null) {
            userData.userDataHeader = smsHeader.getSmsHeader();
        }
    }
}
