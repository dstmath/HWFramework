package com.huawei.android.pushagent.datatype.pushmessage;

import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import defpackage.au;
import defpackage.aw;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PushDataRspMessage extends PushMessage {
    private byte[] mMsgId;
    private byte mPkgFlag;

    public PushDataRspMessage() {
        super(ax());
    }

    public PushDataRspMessage(byte[] bArr, byte b) {
        this();
        this.mMsgId = new byte[bArr.length];
        System.arraycopy(bArr, 0, this.mMsgId, 0, bArr.length);
        this.mPkgFlag = b;
    }

    private static byte ax() {
        return (byte) -95;
    }

    public byte[] aL() {
        return this.mMsgId;
    }

    public byte aQ() {
        return this.mPkgFlag;
    }

    public PushMessage c(InputStream inputStream) {
        byte[] bArr = new byte[8];
        PushMessage.a(inputStream, bArr);
        this.mMsgId = bArr;
        bArr = new byte[1];
        PushMessage.a(inputStream, bArr);
        this.mPkgFlag = bArr[0];
        return this;
    }

    public byte[] encode() {
        byte[] bArr = null;
        if (this.mMsgId == null) {
            aw.e("PushLog2828", "encode error, mMsgId is null ");
        } else {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(j());
                byteArrayOutputStream.write(this.mMsgId);
                byteArrayOutputStream.write(this.mPkgFlag);
                bArr = byteArrayOutputStream.toByteArray();
            } catch (IOException e) {
                aw.e("PushLog2828", "encode error " + e.toString());
            }
        }
        return bArr;
    }

    public String toString() {
        return new StringBuffer(getClass().getSimpleName()).append(",cmdId:").append(aB()).append(",msgId:").append(au.f(this.mMsgId)).append(",flag:").append(this.mPkgFlag).toString();
    }
}
