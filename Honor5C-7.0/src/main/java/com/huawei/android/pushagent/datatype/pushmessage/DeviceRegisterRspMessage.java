package com.huawei.android.pushagent.datatype.pushmessage;

import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import java.io.InputStream;

public class DeviceRegisterRspMessage extends PushMessage {
    private byte mResult;

    public DeviceRegisterRspMessage() {
        super(ax());
        this.mResult = (byte) 1;
    }

    private static byte ax() {
        return (byte) -45;
    }

    public PushMessage c(InputStream inputStream) {
        byte[] bArr = new byte[1];
        PushMessage.a(inputStream, bArr);
        this.mResult = bArr[0];
        return this;
    }

    public byte[] encode() {
        return new byte[]{j(), this.mResult};
    }

    public String toString() {
        return new StringBuffer(getClass().getSimpleName()).append(" cmdId:").append(aB()).append(" result:").append(this.mResult).toString();
    }
}
