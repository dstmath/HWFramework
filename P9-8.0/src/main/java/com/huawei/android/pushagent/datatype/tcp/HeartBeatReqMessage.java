package com.huawei.android.pushagent.datatype.tcp;

import com.huawei.android.pushagent.datatype.tcp.base.PushMessage;
import java.io.InputStream;

public class HeartBeatReqMessage extends PushMessage {
    private static final long serialVersionUID = 135034166096684602L;
    private byte nextHeartBeatInterval = (byte) 10;

    public HeartBeatReqMessage() {
        super(wi());
    }

    private static byte wi() {
        return (byte) -38;
    }

    public void wh(byte b) {
        this.nextHeartBeatInterval = b;
    }

    public byte[] vs() {
        return new byte[]{vt(), this.nextHeartBeatInterval};
    }

    public PushMessage vr(InputStream inputStream) {
        return null;
    }

    public String toString() {
        return new StringBuffer(getClass().getSimpleName()).append(" cmdId:").append(vu()).append(" NextHeartBeatInterval:").append(this.nextHeartBeatInterval).toString();
    }
}
