package com.huawei.android.pushagent.datatype.pushmessage;

import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import java.io.InputStream;

public class NewHeartBeatReqMessage extends PushMessage {
    private byte mNextHeartBeatToServer;

    public NewHeartBeatReqMessage() {
        super(ax());
        this.mNextHeartBeatToServer = (byte) 10;
    }

    private static byte ax() {
        return (byte) -38;
    }

    public byte aK() {
        return this.mNextHeartBeatToServer;
    }

    public PushMessage c(InputStream inputStream) {
        byte[] bArr = new byte[1];
        PushMessage.a(inputStream, bArr);
        this.mNextHeartBeatToServer = bArr[0];
        return this;
    }

    public void d(byte b) {
        this.mNextHeartBeatToServer = b;
    }

    public byte[] encode() {
        return new byte[]{j(), aK()};
    }

    public String toString() {
        return new StringBuffer(getClass().getSimpleName()).append(" cmdId:").append(aB()).append(" NextHeartBeatToServer:").append(this.mNextHeartBeatToServer).toString();
    }
}
