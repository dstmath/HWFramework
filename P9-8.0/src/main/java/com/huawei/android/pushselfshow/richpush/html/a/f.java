package com.huawei.android.pushselfshow.richpush.html.a;

import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.richpush.html.a.e.a;
import com.huawei.android.pushselfshow.richpush.html.api.d;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.MessageSafeConfigJsonField;
import org.json.JSONObject;

class f implements Runnable {
    final /* synthetic */ e a;

    f(e eVar) {
        this.a = eVar;
    }

    public void run() {
        try {
            c.e("PushSelfShowLog", "getPlayingStatusRb getPlayingStatus this.state = " + this.a.e);
            if (a.MEDIA_RUNNING.ordinal() == this.a.e.ordinal()) {
                long i = this.a.i();
                float b = this.a.k();
                JSONObject jSONObject = new JSONObject();
                try {
                    jSONObject.put("current_postion", i);
                    jSONObject.put("duration", (double) b);
                    jSONObject.put(CheckVersionField.CHECK_VERSION_SERVER_URL, this.a.f);
                    this.a.j.a(this.a.a, d.a.OK, MessageSafeConfigJsonField.UPDATE_STATUS, jSONObject);
                } catch (Throwable e) {
                    c.e("PushSelfShowLog", "getPlayingStatus error", e);
                }
            }
        } catch (Throwable e2) {
            c.e("PushSelfShowLog", "getPlayingStatusRb run error", e2);
        }
        if (a.MEDIA_NONE.ordinal() != this.a.e.ordinal() && a.MEDIA_STOPPED.ordinal() != this.a.e.ordinal()) {
            this.a.b.postDelayed(this, (long) this.a.g);
        }
    }
}
