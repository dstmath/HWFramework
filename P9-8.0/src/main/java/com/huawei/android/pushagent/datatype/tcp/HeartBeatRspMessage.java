package com.huawei.android.pushagent.datatype.tcp;

import com.huawei.android.pushagent.datatype.tcp.base.PushMessage;
import com.huawei.android.pushagent.utils.d.b;
import java.io.InputStream;

public class HeartBeatRspMessage extends PushMessage {
    private static final long serialVersionUID = 210693033513730317L;

    public static byte wf() {
        return (byte) -37;
    }

    public HeartBeatRspMessage() {
        super(wf());
    }

    public PushMessage vr(InputStream inputStream) {
        return this;
    }

    public byte[] vs() {
        return new byte[]{this.mCmdId};
    }

    public String toString() {
        return new StringBuffer("HeartBeatRspMessage[").append("cmdId:").append(b.sc(this.mCmdId)).append("]").toString();
    }
}
