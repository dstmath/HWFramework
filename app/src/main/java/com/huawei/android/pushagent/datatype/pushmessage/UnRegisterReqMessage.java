package com.huawei.android.pushagent.datatype.pushmessage;

import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import defpackage.aw;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class UnRegisterReqMessage extends PushMessage {
    private String mToken;

    public UnRegisterReqMessage() {
        super(ax());
        this.mToken = null;
    }

    public UnRegisterReqMessage(String str) {
        super(ax());
        this.mToken = null;
        h(str);
    }

    private static byte ax() {
        return (byte) -42;
    }

    public String aR() {
        return this.mToken;
    }

    public PushMessage c(InputStream inputStream) {
        byte[] bArr = new byte[32];
        PushMessage.a(inputStream, bArr);
        this.mToken = new String(bArr, "UTF-8");
        return this;
    }

    public byte[] encode() {
        byte[] bArr = null;
        try {
            if (TextUtils.isEmpty(this.mToken)) {
                aw.e("PushLog2828", "encode error reason mToken is empty");
            } else {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(j());
                byteArrayOutputStream.write(aR().getBytes("UTF-8"));
                bArr = byteArrayOutputStream.toByteArray();
            }
        } catch (Exception e) {
            aw.e("PushLog2828", "encode error " + e.toString());
        }
        return bArr;
    }

    public void h(String str) {
        this.mToken = str;
    }

    public String toString() {
        return "UnRegisterReqMessage[token:" + this.mToken + "]";
    }
}
