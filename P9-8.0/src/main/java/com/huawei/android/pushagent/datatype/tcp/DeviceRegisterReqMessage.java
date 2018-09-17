package com.huawei.android.pushagent.datatype.tcp;

import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.tcp.base.PushMessage;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.c;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class DeviceRegisterReqMessage extends PushMessage {
    private static final long serialVersionUID = 5945012025726493592L;
    private int agentVersion;
    private byte chanMode;
    private byte control;
    private String deviceId;
    private byte networkType;

    private static byte wq() {
        return (byte) 64;
    }

    public DeviceRegisterReqMessage() {
        super(wq());
        this.deviceId = null;
        this.networkType = (byte) -1;
        this.chanMode = (byte) 1;
        this.control = (byte) 0;
    }

    public DeviceRegisterReqMessage(String str, byte b, byte b2, int i) {
        this();
        this.deviceId = str;
        this.chanMode = b;
        this.networkType = b2;
        this.agentVersion = i;
    }

    private byte[] wr(String str) {
        if (TextUtils.isEmpty(str)) {
            return new byte[0];
        }
        byte[] bytes;
        try {
            bytes = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            c.sf("PushLog2951", "fail to get parm bytes");
            bytes = new byte[0];
        }
        return bytes;
    }

    public byte[] vs() {
        if (TextUtils.isEmpty(this.deviceId)) {
            c.sf("PushLog2951", "encode error, reason mDeviceId = " + this.deviceId);
            return new byte[0];
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(vt());
            byte[] wr = wr(this.deviceId);
            byteArrayOutputStream.write(b.uq(((((wr.length + 4) + 1) + 4) + 1) + 1));
            byteArrayOutputStream.write(wr.length);
            byteArrayOutputStream.write(wr);
            byteArrayOutputStream.write(this.networkType);
            byteArrayOutputStream.write(b.uj(this.agentVersion));
            byteArrayOutputStream.write(this.chanMode);
            byteArrayOutputStream.write(this.control);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            c.sf("PushLog2951", "encode error " + e.toString());
            return new byte[0];
        }
    }

    public PushMessage vr(InputStream inputStream) {
        return null;
    }

    public String toString() {
        return new StringBuffer(getClass().getSimpleName()).append(" cmdId:").append(vu()).append(" mDeviceId:").append(this.deviceId).append(" mNetworkType:").append(this.networkType).append(" mAgentVersion:").append(this.agentVersion).toString();
    }
}
