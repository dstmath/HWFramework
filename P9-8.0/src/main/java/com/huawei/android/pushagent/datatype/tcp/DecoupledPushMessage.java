package com.huawei.android.pushagent.datatype.tcp;

import com.huawei.android.pushagent.datatype.tcp.base.PushMessage;
import com.huawei.android.pushagent.utils.a.d;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.c;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.json.JSONObject;

public class DecoupledPushMessage extends PushMessage {
    private static final long serialVersionUID = -1186347017264161627L;
    private JSONObject payload = new JSONObject();

    public DecoupledPushMessage(byte b) {
        super(b);
    }

    public byte[] vs() {
        if (this.payload == null) {
            c.sf("PushLog2951", "encode error, payload is null");
            return new byte[0];
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(vt());
            byte[] bytes = this.payload.toString().getBytes("UTF-8");
            byteArrayOutputStream.write(b.uq(bytes.length + 6));
            byteArrayOutputStream.write(2);
            byteArrayOutputStream.write(b.uq(bytes.length));
            byteArrayOutputStream.write(bytes);
            return byteArrayOutputStream.toByteArray();
        } catch (UnsupportedEncodingException e) {
            c.sf("PushLog2951", "unsupported encoding type");
        } catch (IOException e2) {
            c.sf("PushLog2951", "io exception");
        }
        return new byte[0];
    }

    public PushMessage vr(InputStream inputStream) {
        com.huawei.android.pushagent.datatype.b.b vo = vo(inputStream);
        byte b = vo.ww(1)[0];
        if (!vp(b)) {
            return null;
        }
        this.payload = vq(b, vo);
        return this;
    }

    public String toString() {
        return "payload is" + d.ns(this.payload);
    }

    public JSONObject wk() {
        return this.payload;
    }

    public void wj(JSONObject jSONObject) {
        this.payload = jSONObject;
    }
}
