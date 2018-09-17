package com.huawei.android.pushagent.datatype.pushmessage;

import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import defpackage.aw;
import java.io.InputStream;

public class DeviceRegisterReqMessage extends PushMessage {
    private String mDeviceId;
    private byte mNetworkType;

    public DeviceRegisterReqMessage() {
        super(ax());
        this.mDeviceId = null;
        this.mNetworkType = (byte) -1;
    }

    private static byte ax() {
        return (byte) -46;
    }

    public PushMessage c(InputStream inputStream) {
        byte[] bArr = new byte[16];
        PushMessage.a(inputStream, bArr);
        this.mDeviceId = new String(bArr, "UTF-8");
        bArr = new byte[1];
        PushMessage.a(inputStream, bArr);
        this.mNetworkType = bArr[0];
        return this;
    }

    public byte[] encode() {
        byte[] bArr = null;
        if (TextUtils.isEmpty(this.mDeviceId)) {
            aw.e("PushLog2828", "encode error, reason mDeviceId = " + this.mDeviceId);
        } else {
            try {
                byte[] bytes = this.mDeviceId.getBytes("UTF-8");
                bArr = new byte[((bytes.length + 1) + 1)];
                bArr[0] = j();
                System.arraycopy(bytes, 0, bArr, 1, bytes.length);
                bArr[bArr.length - 1] = this.mNetworkType;
            } catch (Throwable e) {
                aw.a("PushLog2828", e.toString(), e);
            }
        }
        return bArr;
    }

    public String toString() {
        return new StringBuffer(getClass().getSimpleName()).append(" cmdId:").append(aB()).append(" mDeviceId:").append(this.mDeviceId).append(" mNetworkType:").append(this.mNetworkType).toString();
    }
}
