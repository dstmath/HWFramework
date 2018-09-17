package com.huawei.android.pushagent.datatype.tcp.base;

import com.huawei.android.pushagent.utils.a;
import com.huawei.android.pushagent.utils.d.b;
import com.huawei.android.pushagent.utils.d.c;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class PushMessage implements IPushMessage {
    protected byte mCmdId = (byte) -1;

    public abstract String toString();

    public abstract PushMessage vr(InputStream inputStream);

    public abstract byte[] vs();

    public String vu() {
        return b.sb(new byte[]{this.mCmdId});
    }

    public PushMessage(byte b) {
        vw(b);
    }

    public byte vt() {
        return this.mCmdId;
    }

    public void vw(byte b) {
        this.mCmdId = b;
    }

    public com.huawei.android.pushagent.datatype.b.b vo(InputStream inputStream) {
        byte[] bArr = new byte[2];
        vv(inputStream, bArr);
        int tw = com.huawei.android.pushagent.utils.b.tw(bArr);
        c.sg("PushLog2951", "msg total lenth is: " + tw);
        bArr = new byte[((tw - 1) - 2)];
        vv(inputStream, bArr);
        return new com.huawei.android.pushagent.datatype.b.b(bArr);
    }

    public JSONObject vq(byte b, com.huawei.android.pushagent.datatype.b.b bVar) {
        if ((b & 2) == 2) {
            try {
                byte[] ww = bVar.ww(com.huawei.android.pushagent.utils.b.tw(bVar.ww(2)));
                if (ww == null || ww.length == 0) {
                    return null;
                }
                if ((b & 1) == 1) {
                    ww = a.ti(ww);
                }
                return new JSONObject(new String(ww, "UTF-8"));
            } catch (JSONException e) {
                c.sf("PushLog2951", "fail to parser payload");
            } catch (UnsupportedEncodingException e2) {
                c.sf("PushLog2951", "unsupported encoding type");
            } catch (ArrayIndexOutOfBoundsException e3) {
                c.sf("PushLog2951", "fail to parser payload, array out of bounds");
            }
        }
        return null;
    }

    public boolean vp(byte b) {
        boolean z = false;
        if ((b & 12) == 0) {
            z = true;
        }
        if (!z) {
            c.sf("PushLog2951", "tlv is not support in current version");
        }
        return z;
    }

    public static void vv(InputStream inputStream, byte[] bArr) {
        int i = 0;
        while (i < bArr.length) {
            int read = inputStream.read(bArr, i, bArr.length - i);
            if (-1 == read) {
                throw new IOException("read -1 reached");
            }
            i += read;
        }
    }
}
