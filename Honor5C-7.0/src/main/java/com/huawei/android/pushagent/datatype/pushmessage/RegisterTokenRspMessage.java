package com.huawei.android.pushagent.datatype.pushmessage;

import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import defpackage.au;
import defpackage.aw;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class RegisterTokenRspMessage extends PushMessage {
    private String mPackageName;
    private byte mResult;
    private String mToken;

    public RegisterTokenRspMessage() {
        super(ax());
        this.mResult = (byte) 1;
        this.mToken = null;
        this.mPackageName = null;
    }

    public RegisterTokenRspMessage(byte b, String str, String str2) {
        super(ax());
        this.mResult = (byte) 1;
        this.mToken = null;
        this.mPackageName = null;
        this.mResult = b;
        this.mToken = str;
        this.mPackageName = str2;
    }

    private static byte ax() {
        return (byte) -35;
    }

    public byte aJ() {
        return this.mResult;
    }

    public String aR() {
        return this.mToken;
    }

    public PushMessage c(InputStream inputStream) {
        byte[] bArr = new byte[1];
        PushMessage.a(inputStream, bArr);
        this.mResult = bArr[0];
        if (bArr[0] != null) {
            this.mPackageName = null;
            this.mToken = null;
        }
        bArr = new byte[32];
        PushMessage.a(inputStream, bArr);
        this.mToken = new String(bArr, "UTF-8");
        bArr = new byte[2];
        PushMessage.a(inputStream, bArr);
        bArr = new byte[au.g(bArr)];
        PushMessage.a(inputStream, bArr);
        this.mPackageName = new String(bArr, "UTF-8");
        return this;
    }

    public byte[] encode() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(j());
            byteArrayOutputStream.write(0);
            byteArrayOutputStream.write(this.mToken.getBytes("UTF-8"));
            byteArrayOutputStream.write(au.c(this.mPackageName.length()));
            byteArrayOutputStream.write(this.mPackageName.getBytes("UTF-8"));
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            aw.e("PushLog2828", "encode error,e " + e.toString());
            return null;
        }
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String toString() {
        return new StringBuffer().append("RegisterTokenRspMessage[").append("result:").append(au.e(this.mResult)).append(",token:").append(this.mToken).append(",packageName:").append(this.mPackageName).append("]").toString();
    }
}
