package com.huawei.android.pushagent.datatype.tcp.base;

import com.huawei.android.pushagent.datatype.tcp.DecoupledPushMessage;
import com.huawei.android.pushagent.datatype.tcp.DeviceRegisterReqMessage;
import com.huawei.android.pushagent.datatype.tcp.DeviceRegisterRspMessage;
import com.huawei.android.pushagent.datatype.tcp.HeartBeatReqMessage;
import com.huawei.android.pushagent.datatype.tcp.HeartBeatRspMessage;
import com.huawei.android.pushagent.datatype.tcp.PushDataReqMessage;
import com.huawei.android.pushagent.datatype.tcp.PushDataRspMessage;
import com.huawei.android.pushagent.utils.d.b;
import com.huawei.android.pushagent.utils.d.c;
import java.io.InputStream;
import java.util.HashMap;

public class a {
    private static HashMap<Byte, Class> gu = new HashMap();

    static {
        gu.put(Byte.valueOf((byte) -38), HeartBeatReqMessage.class);
        gu.put(Byte.valueOf((byte) -37), HeartBeatRspMessage.class);
        gu.put(Byte.valueOf((byte) 64), DeviceRegisterReqMessage.class);
        gu.put(Byte.valueOf((byte) 65), DeviceRegisterRspMessage.class);
        gu.put(Byte.valueOf((byte) 68), PushDataReqMessage.class);
        gu.put(Byte.valueOf((byte) 69), PushDataRspMessage.class);
        gu.put(Byte.valueOf((byte) 66), DecoupledPushMessage.class);
        gu.put(Byte.valueOf((byte) 67), DecoupledPushMessage.class);
    }

    public static PushMessage vx(byte b, InputStream inputStream) {
        if (gu.containsKey(Byte.valueOf(b))) {
            PushMessage pushMessage = (PushMessage) ((Class) gu.get(Byte.valueOf(b))).newInstance();
            if (pushMessage.vt() == (byte) -1) {
                pushMessage.vw(b);
            }
            PushMessage vr = pushMessage.vr(inputStream);
            if (vr != null) {
                c.sg("PushLog2951", "after decode msg:" + b.sc(vr.vt()));
            } else {
                c.sf("PushLog2951", "call " + pushMessage.getClass().getSimpleName() + " decode failed!");
            }
            return vr;
        }
        c.sf("PushLog2951", "cmdId:" + b + " is not exist, all:" + gu.keySet());
        throw new InstantiationException("cmdId:" + b + " is not register");
    }
}
