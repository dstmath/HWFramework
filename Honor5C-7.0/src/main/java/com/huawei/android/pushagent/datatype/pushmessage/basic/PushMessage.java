package com.huawei.android.pushagent.datatype.pushmessage.basic;

import com.huawei.android.pushagent.datatype.IPushMessage;
import defpackage.au;
import java.io.IOException;
import java.io.InputStream;

public abstract class PushMessage implements IPushMessage {
    public byte mCmdId;

    public PushMessage() {
        this.mCmdId = (byte) -1;
    }

    public PushMessage(byte b) {
        this.mCmdId = (byte) -1;
        a(b);
    }

    public static void a(InputStream inputStream, byte[] bArr) {
        int i = 0;
        while (i < bArr.length) {
            int read = inputStream.read(bArr, i, bArr.length - i);
            if (-1 == read) {
                throw new IOException("read -1 reached");
            }
            i += read;
        }
    }

    public void a(byte b) {
        this.mCmdId = b;
    }

    public String aB() {
        return au.f(new byte[]{this.mCmdId});
    }

    public abstract PushMessage c(InputStream inputStream);

    public byte j() {
        return this.mCmdId;
    }
}
