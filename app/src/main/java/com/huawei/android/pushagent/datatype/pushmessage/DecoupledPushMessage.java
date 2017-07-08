package com.huawei.android.pushagent.datatype.pushmessage;

import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import defpackage.au;
import defpackage.aw;
import defpackage.bi;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.json.JSONObject;

public class DecoupledPushMessage extends PushMessage {
    private JSONObject mParam;

    public DecoupledPushMessage() {
        this.mParam = new JSONObject();
    }

    public DecoupledPushMessage(byte b) {
        super(b);
        this.mParam = new JSONObject();
    }

    public void a(JSONObject jSONObject) {
        this.mParam = jSONObject;
    }

    public JSONObject aF() {
        return this.mParam;
    }

    public PushMessage c(InputStream inputStream) {
        byte[] bArr = new byte[2];
        PushMessage.a(inputStream, bArr);
        int g = au.g(bArr);
        aw.d("PushLog2828", "push message len=" + g);
        bArr = new byte[g];
        PushMessage.a(inputStream, bArr);
        String str = new String(bArr, "UTF-8");
        aw.d("PushLog2828", "push message data :" + bi.w(str));
        this.mParam = new JSONObject(str);
        return this;
    }

    public byte[] encode() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(j());
            if (this.mParam.length() == 0) {
                byteArrayOutputStream.write(au.c(0));
            } else {
                byte[] bytes = this.mParam.toString().getBytes("UTF-8");
                byteArrayOutputStream.write(au.c(bytes.length));
                byteArrayOutputStream.write(bytes);
                aw.d("PushLog2828", " begin to send:" + bi.b(this.mParam));
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            aw.d("PushLog2828", "encode error," + e.toString());
            return null;
        }
    }

    public String toString() {
        return this.mParam.toString();
    }
}
