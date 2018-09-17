package com.huawei.android.pushagent.datatype.pollingmessage.basic;

import com.huawei.android.pushagent.datatype.IPushMessage;
import defpackage.au;
import defpackage.aw;
import defpackage.l;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class PollingMessage implements IPushMessage {
    private byte mCmdId;

    public PollingMessage() {
        this.mCmdId = (byte) -1;
    }

    public PollingMessage(byte b) {
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

    public static PollingMessage b(InputStream inputStream) {
        byte[] bArr = new byte[2];
        a(inputStream, bArr);
        short g = (short) au.g(bArr);
        byte[] bArr2 = new byte[g];
        a(inputStream, bArr2);
        InputStream byteArrayInputStream = new ByteArrayInputStream(bArr2);
        byte read = (byte) byteArrayInputStream.read();
        aw.d("PushLog2828", "cmdId: 0X" + Integer.toHexString(read) + " len:" + g);
        return l.a(Byte.valueOf(read), byteArrayInputStream);
    }

    public abstract PollingMessage a(InputStream inputStream);

    public void a(byte b) {
        this.mCmdId = b;
    }

    public String aB() {
        return au.f(new byte[]{this.mCmdId});
    }

    public byte j() {
        return this.mCmdId;
    }
}
