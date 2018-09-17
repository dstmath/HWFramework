package com.huawei.android.pushagent.datatype.pushmessage;

import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import defpackage.au;
import java.io.InputStream;

public class HeartBeatRspMessage extends PushMessage {
    public HeartBeatRspMessage() {
        super(ax());
    }

    private static byte ax() {
        return (byte) -47;
    }

    public PushMessage c(InputStream inputStream) {
        return this;
    }

    public byte[] encode() {
        return new byte[]{this.mCmdId};
    }

    public String toString() {
        return new StringBuffer("HeartBeatRspMessage[").append("cmdId:").append(au.e(this.mCmdId)).append("]").toString();
    }
}
