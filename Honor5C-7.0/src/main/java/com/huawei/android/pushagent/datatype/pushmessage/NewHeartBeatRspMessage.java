package com.huawei.android.pushagent.datatype.pushmessage;

import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import defpackage.au;
import java.io.InputStream;

public class NewHeartBeatRspMessage extends PushMessage {
    public NewHeartBeatRspMessage() {
        super(ax());
    }

    public static byte ax() {
        return (byte) -37;
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
