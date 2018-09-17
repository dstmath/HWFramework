package com.huawei.android.pushagent.datatype.pushmessage;

import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import defpackage.au;
import defpackage.aw;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class NewDeviceRegisterReqMessage extends PushMessage {
    private int mAgentVersion;
    private long mCurrentConnectTime;
    private long mCurrentTime;
    private String mDeviceId;
    private long mLastDisconnectTime;
    private int mNetEventCount;
    private NetEventInfo[] mNetEventInfoList;
    private byte mNetworkType;

    public NewDeviceRegisterReqMessage() {
        super(ax());
        this.mDeviceId = null;
        this.mNetworkType = (byte) -1;
    }

    public NewDeviceRegisterReqMessage(String str, byte b, int i, long j, long j2, long j3, int i2, NetEventInfo[] netEventInfoArr) {
        super(ax());
        this.mDeviceId = null;
        this.mNetworkType = (byte) -1;
        this.mDeviceId = str;
        this.mNetworkType = b;
        this.mAgentVersion = i;
        this.mLastDisconnectTime = j;
        this.mCurrentConnectTime = j2;
        this.mCurrentTime = j3;
        this.mNetEventCount = i2;
        if (netEventInfoArr != null && netEventInfoArr.length > 0) {
            this.mNetEventInfoList = new NetEventInfo[netEventInfoArr.length];
            System.arraycopy(netEventInfoArr, 0, this.mNetEventInfoList, 0, netEventInfoArr.length);
        }
    }

    private static byte ax() {
        return (byte) -34;
    }

    public PushMessage c(InputStream inputStream) {
        byte[] bArr = new byte[16];
        PushMessage.a(inputStream, bArr);
        this.mDeviceId = new String(bArr, "UTF-8");
        bArr = new byte[1];
        PushMessage.a(inputStream, bArr);
        this.mNetworkType = bArr[0];
        bArr = new byte[2];
        PushMessage.a(inputStream, bArr);
        this.mAgentVersion = au.g(bArr);
        bArr = new byte[8];
        PushMessage.a(inputStream, bArr);
        this.mLastDisconnectTime = au.h(bArr);
        bArr = new byte[8];
        PushMessage.a(inputStream, bArr);
        this.mCurrentConnectTime = au.h(bArr);
        bArr = new byte[8];
        PushMessage.a(inputStream, bArr);
        this.mCurrentTime = au.h(bArr);
        bArr = new byte[1];
        PushMessage.a(inputStream, bArr);
        this.mNetEventCount = au.g(bArr);
        int i = this.mNetEventCount & 127;
        if (i > 0) {
            this.mNetEventInfoList = new NetEventInfo[i];
            for (NetEventInfo netEventInfo : this.mNetEventInfoList) {
                byte[] bArr2 = new byte[8];
                PushMessage.a(inputStream, bArr2);
                netEventInfo.f(au.h(bArr2));
                bArr2 = new byte[1];
                PushMessage.a(inputStream, bArr2);
                netEventInfo.b(bArr2[0]);
                bArr2 = new byte[1];
                PushMessage.a(inputStream, bArr2);
                netEventInfo.c(bArr2[0]);
            }
        }
        return this;
    }

    public byte[] encode() {
        byte[] bArr = null;
        if (TextUtils.isEmpty(this.mDeviceId)) {
            aw.e("PushLog2828", "encode error, reason mDeviceId = " + this.mDeviceId);
        } else {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(j());
                byteArrayOutputStream.write(this.mDeviceId.getBytes("UTF-8"));
                byteArrayOutputStream.write(this.mNetworkType);
                byteArrayOutputStream.write(au.c(this.mAgentVersion));
                byteArrayOutputStream.write(au.m(this.mLastDisconnectTime));
                byteArrayOutputStream.write(au.m(this.mCurrentConnectTime));
                byteArrayOutputStream.write(au.m(this.mCurrentTime));
                byteArrayOutputStream.write((byte) this.mNetEventCount);
                if (this.mNetEventInfoList != null && this.mNetEventInfoList.length > 0) {
                    for (NetEventInfo netEventInfo : this.mNetEventInfoList) {
                        byteArrayOutputStream.write(au.m(netEventInfo.aG()));
                        byteArrayOutputStream.write(netEventInfo.aH());
                        byteArrayOutputStream.write(netEventInfo.aI());
                    }
                }
                bArr = byteArrayOutputStream.toByteArray();
            } catch (Exception e) {
                aw.e("PushLog2828", "encode error " + e.toString());
            }
        }
        return bArr;
    }

    public String toString() {
        StringBuffer append = new StringBuffer(getClass().getSimpleName()).append(" cmdId:").append(aB()).append(" mDeviceId:").append(this.mDeviceId).append(" mNetworkType:").append(this.mNetworkType).append(" mAgentVersion:").append(this.mAgentVersion).append(" mLastDisconnectTime:").append(au.a(this.mLastDisconnectTime, "yyyy-MM-dd HH:mm:ss SSS")).append(" mCurrentConnectTime:").append(au.a(this.mCurrentConnectTime, "yyyy-MM-dd HH:mm:ss SSS")).append(" mCurrentTime:").append(au.a(this.mCurrentTime, "yyyy-MM-dd HH:mm:ss SSS")).append(" mNetEventAccount:").append(this.mNetEventCount);
        if (this.mNetEventInfoList != null && this.mNetEventInfoList.length > 0) {
            for (Object append2 : this.mNetEventInfoList) {
                append.append(append2);
            }
        }
        return append.toString();
    }
}
