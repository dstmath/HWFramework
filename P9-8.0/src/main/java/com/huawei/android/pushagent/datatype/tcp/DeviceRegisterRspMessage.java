package com.huawei.android.pushagent.datatype.tcp;

import com.huawei.android.pushagent.datatype.b.b;
import com.huawei.android.pushagent.datatype.tcp.base.PushMessage;
import java.io.InputStream;
import org.json.JSONObject;

public class DeviceRegisterRspMessage extends PushMessage {
    private static final long serialVersionUID = -6902385088066252595L;
    private JSONObject payload;
    private byte result = (byte) 1;

    private static byte wg() {
        return (byte) 65;
    }

    public DeviceRegisterRspMessage() {
        super(wg());
    }

    public byte getResult() {
        return this.result;
    }

    public byte[] vs() {
        return new byte[0];
    }

    public PushMessage vr(InputStream inputStream) {
        b vo = vo(inputStream);
        this.result = vo.ww(1)[0];
        byte b = vo.ww(1)[0];
        if (!vp(b)) {
            return null;
        }
        this.payload = vq(b, vo);
        return this;
    }

    public String toString() {
        return new StringBuffer(getClass().getSimpleName()).append(" cmdId:").append(vu()).append(" result:").append(this.result).toString();
    }
}
