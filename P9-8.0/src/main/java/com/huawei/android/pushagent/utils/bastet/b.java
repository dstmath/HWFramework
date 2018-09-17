package com.huawei.android.pushagent.utils.bastet;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.huawei.android.pushagent.a.a;
import com.huawei.android.pushagent.utils.d.c;

final class b extends Handler {
    final /* synthetic */ a fm;

    b(a aVar, Looper looper) {
        this.fm = aVar;
        super(looper);
    }

    public void handleMessage(Message message) {
        try {
            super.handleMessage(message);
            int i = message.what;
            switch (i) {
                case 2:
                    a.xx(71, String.valueOf(2));
                    c.sh("PushLog2951", "receive handler message BASTET_CONNECTION_CLOSED");
                    this.fm.rk();
                    return;
                case 4:
                    i = message.arg1;
                    a.xx(71, a.xw(String.valueOf(4), String.valueOf(i)));
                    c.sh("PushLog2951", "receive handler message BASTET_HEARTBEAT_CYCLE " + i);
                    this.fm.fh.pauseHeartbeat();
                    this.fm.rg(i, ((long) (i * 5)) * 60000);
                    return;
                case 5:
                    a.xx(71, String.valueOf(5));
                    c.sh("PushLog2951", "receive handler message BASTET_HB_NOT_AVAILABLE");
                    this.fm.rk();
                    return;
                case 7:
                    a.xx(71, String.valueOf(7));
                    c.sh("PushLog2951", "receive handler message BASTET_RECONNECTION_BEST_POINT");
                    this.fm.rk();
                    return;
                case 8:
                    a.xx(71, String.valueOf(8));
                    c.sh("PushLog2951", "receive handler message BASTET_RECONNECTION_BREAK");
                    this.fm.rk();
                    return;
                default:
                    c.sh("PushLog2951", "receive handler message default, what is " + i);
                    return;
            }
        } catch (Throwable e) {
            c.se("PushLog2951", "handle bastetMessage error:" + e.getMessage(), e);
            this.fm.rc();
        }
        c.se("PushLog2951", "handle bastetMessage error:" + e.getMessage(), e);
        this.fm.rc();
    }
}
