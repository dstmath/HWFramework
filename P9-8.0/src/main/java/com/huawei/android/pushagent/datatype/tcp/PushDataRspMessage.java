package com.huawei.android.pushagent.datatype.tcp;

import com.huawei.android.pushagent.datatype.tcp.base.PushMessage;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.c;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PushDataRspMessage extends PushMessage {
    private static final long serialVersionUID = -8613876352739508266L;
    private byte control;
    private byte[] cookie;
    private byte[] msgId;
    private byte result;
    private byte[] tokenBytes;

    private static byte wp() {
        return (byte) 69;
    }

    public PushDataRspMessage() {
        super(wp());
    }

    public PushDataRspMessage(byte[] bArr, byte b, byte b2, byte[] bArr2, byte[] bArr3) {
        this();
        if (bArr == null) {
            this.msgId = new byte[0];
        } else {
            this.msgId = new byte[bArr.length];
            System.arraycopy(bArr, 0, this.msgId, 0, bArr.length);
        }
        this.result = b;
        this.control = (byte) (b2 & 48);
        wo(bArr2);
        if (bArr3 == null) {
            bArr3 = new byte[0];
        }
        this.tokenBytes = bArr3;
    }

    private void wo(byte[] bArr) {
        if (bArr == null) {
            bArr = new byte[0];
        }
        if ((this.control & 32) == 32) {
            this.cookie = new byte[bArr.length];
            System.arraycopy(bArr, 0, this.cookie, 0, bArr.length);
        }
    }

    public PushMessage vr(InputStream inputStream) {
        return null;
    }

    public void wl(byte b) {
        this.result = b;
    }

    public byte wn() {
        return this.result;
    }

    public byte[] vs() {
        if (this.msgId == null) {
            c.sf("PushLog2951", "encode error, mMsgId is null ");
            return new byte[0];
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(vt());
            int length = ((((this.msgId.length + 4) + 2) + this.tokenBytes.length) + 1) + 1;
            if ((this.control & 32) == 32) {
                length += this.cookie.length + 2;
            }
            byteArrayOutputStream.write(b.uq(length));
            byteArrayOutputStream.write(this.msgId.length);
            byteArrayOutputStream.write(this.msgId);
            byteArrayOutputStream.write(b.uq(this.tokenBytes.length));
            byteArrayOutputStream.write(this.tokenBytes);
            byteArrayOutputStream.write(this.result);
            byteArrayOutputStream.write(this.control);
            if ((this.control & 32) == 32) {
                byteArrayOutputStream.write(b.uq(this.cookie.length));
                byteArrayOutputStream.write(this.cookie);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            c.sf("PushLog2951", "encode error " + e.toString());
            return new byte[0];
        }
    }

    public String toString() {
        return new StringBuffer(getClass().getSimpleName()).append(",cmdId:").append(vu()).append(",msgId:").append(com.huawei.android.pushagent.utils.d.b.sb(this.msgId)).append(",flag:").append(this.result).toString();
    }

    public byte[] wm() {
        return this.msgId;
    }
}
