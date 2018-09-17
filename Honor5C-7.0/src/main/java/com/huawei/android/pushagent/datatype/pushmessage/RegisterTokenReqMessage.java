package com.huawei.android.pushagent.datatype.pushmessage;

import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import defpackage.au;
import defpackage.aw;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class RegisterTokenReqMessage extends PushMessage {
    private String mDeviceId;
    private String mPackageName;

    public RegisterTokenReqMessage() {
        super(ax());
        this.mDeviceId = null;
        this.mPackageName = null;
    }

    public RegisterTokenReqMessage(String str, String str2) {
        super(ax());
        this.mDeviceId = null;
        this.mPackageName = null;
        this.mDeviceId = str;
        this.mPackageName = str2;
    }

    private static byte ax() {
        return (byte) -36;
    }

    public PushMessage c(InputStream inputStream) {
        byte[] bArr = new byte[16];
        PushMessage.a(inputStream, bArr);
        this.mDeviceId = new String(bArr, "UTF-8");
        bArr = new byte[2];
        PushMessage.a(inputStream, bArr);
        bArr = new byte[au.g(bArr)];
        PushMessage.a(inputStream, bArr);
        this.mPackageName = new String(bArr, "UTF-8");
        return this;
    }

    public byte[] encode() {
        byte[] bArr = null;
        try {
            if (TextUtils.isEmpty(this.mDeviceId)) {
                aw.e("PushLog2828", "encode error mDeviceId = " + this.mDeviceId);
            } else if (TextUtils.isEmpty(this.mPackageName)) {
                aw.e("PushLog2828", "encode error mPackageName = " + this.mPackageName);
            } else {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(j());
                byteArrayOutputStream.write(this.mDeviceId.getBytes("UTF-8"));
                byteArrayOutputStream.write(au.c(this.mPackageName.length()));
                byteArrayOutputStream.write(this.mPackageName.getBytes("UTF-8"));
                bArr = byteArrayOutputStream.toByteArray();
            }
        } catch (Exception e) {
            aw.e("PushLog2828", "encode error " + e.toString());
        }
        return bArr;
    }

    public String toString() {
        return new StringBuffer().append("RegisterTokenReqMessage[").append("deviceId:").append(this.mDeviceId).append(",packageName:").append(this.mPackageName).append("]").toString();
    }
}
