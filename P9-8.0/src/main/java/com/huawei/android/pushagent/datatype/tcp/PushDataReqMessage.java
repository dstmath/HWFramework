package com.huawei.android.pushagent.datatype.tcp;

import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.b.b;
import com.huawei.android.pushagent.datatype.tcp.base.PushMessage;
import com.huawei.android.pushagent.utils.a.d;
import com.huawei.android.pushagent.utils.d.c;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.json.JSONException;
import org.json.JSONObject;

public class PushDataReqMessage extends PushMessage {
    private static final long serialVersionUID = -6661677033729532404L;
    private byte control;
    private byte[] mCookie;
    private byte[] msgId;
    private JSONObject payload;
    private byte[] tokenBytes;

    public static final byte wd() {
        return (byte) 68;
    }

    public PushDataReqMessage() {
        super(wd());
    }

    public byte[] wc() {
        if (this.mCookie == null || this.mCookie.length <= 0) {
            return new byte[0];
        }
        byte[] bArr = new byte[this.mCookie.length];
        System.arraycopy(this.mCookie, 0, bArr, 0, this.mCookie.length);
        return bArr;
    }

    public byte wb() {
        return this.control;
    }

    public byte[] wa() {
        if (this.msgId == null || this.msgId.length <= 0) {
            return new byte[0];
        }
        byte[] bArr = new byte[this.msgId.length];
        System.arraycopy(this.msgId, 0, bArr, 0, this.msgId.length);
        return bArr;
    }

    public byte[] vs() {
        return new byte[0];
    }

    public boolean isValid() {
        boolean z;
        CharSequence pkgName = getPkgName();
        if (this.tokenBytes == null || this.tokenBytes.length <= 0) {
            z = false;
        } else {
            z = true;
        }
        int i;
        if (this.msgId == null || this.msgId.length <= 0) {
            i = 0;
        } else {
            i = 1;
        }
        if (z && !TextUtils.isEmpty(pkgName) && (i ^ 1) == 0 && vz().length != 0) {
            return true;
        }
        c.sf("PushLog2951", "token pkgName msgId msgBody exist null");
        return false;
    }

    public PushMessage vr(InputStream inputStream) {
        b vo = vo(inputStream);
        this.msgId = vo.ww(com.huawei.android.pushagent.utils.b.ur(vo.ww(1)[0]));
        this.tokenBytes = vo.ww(com.huawei.android.pushagent.utils.b.tw(vo.ww(2)));
        this.control = vo.ww(1)[0];
        if (!vp(this.control)) {
            return null;
        }
        c.sf("PushLog2951", "control is: " + this.control);
        if ((this.control & 32) == 32) {
            int tw = com.huawei.android.pushagent.utils.b.tw(vo.ww(2));
            c.sf("PushLog2951", "cookieLen is: " + tw);
            this.mCookie = vo.ww(tw);
        }
        this.payload = vq(this.control, vo);
        return this;
    }

    public int getUserId() {
        if (this.payload == null) {
            c.sf("PushLog2951", "payload is null");
            return 0;
        } else if (this.payload.has("userId")) {
            try {
                return this.payload.getInt("userId");
            } catch (JSONException e) {
                c.sf("PushLog2951", "fail to get userid from payload");
            }
        } else {
            c.sf("PushLog2951", "payload not has userid");
            return 0;
        }
    }

    public String getPkgName() {
        if (this.payload == null) {
            c.sf("PushLog2951", "payload is null");
            return null;
        } else if (this.payload.has("pkgName")) {
            try {
                return this.payload.getString("pkgName");
            } catch (JSONException e) {
                c.sf("PushLog2951", "fail to get pkgname from payload");
            }
        } else {
            c.sf("PushLog2951", "payload not has pkgname");
            return null;
        }
    }

    public byte[] vy() {
        if (this.tokenBytes == null || this.tokenBytes.length <= 0) {
            return new byte[0];
        }
        byte[] bArr = new byte[this.tokenBytes.length];
        System.arraycopy(this.tokenBytes, 0, bArr, 0, this.tokenBytes.length);
        return bArr;
    }

    public int we() {
        if (this.payload == null) {
            c.sf("PushLog2951", "payload is null");
            return -1;
        } else if (this.payload.has("msgType")) {
            try {
                return this.payload.getInt("msgType");
            } catch (JSONException e) {
                c.sf("PushLog2951", "fail to get msgType from payload");
            }
        } else {
            c.sf("PushLog2951", "payload not has msgType");
            return -1;
        }
    }

    public byte[] vz() {
        if (this.payload == null) {
            c.sf("PushLog2951", "payload is null");
            return new byte[0];
        }
        if (this.payload.has("msg")) {
            try {
                String string = this.payload.getString("msg");
                if (string != null) {
                    return string.getBytes("UTF-8");
                }
            } catch (JSONException e) {
                c.sf("PushLog2951", "fail to get msg from payload");
            } catch (UnsupportedEncodingException e2) {
                c.sf("PushLog2951", "fail to get msg from payload, unsupport encoding type");
            }
        } else {
            c.sf("PushLog2951", "payload not has msg");
        }
        return new byte[0];
    }

    public String toString() {
        return "payload is" + d.ns(this.payload);
    }
}
