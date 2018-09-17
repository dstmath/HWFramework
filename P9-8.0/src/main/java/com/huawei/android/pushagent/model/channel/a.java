package com.huawei.android.pushagent.model.channel;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.channel.entity.b;
import com.huawei.android.pushagent.utils.d.c;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class a {
    private static volatile a by = null;
    private static final Object bz = new Object();
    private Context bx;
    private b ca;

    public static a hl(Context context) {
        if (by == null) {
            synchronized (bz) {
                if (by == null) {
                    if (context != null) {
                        by = new a(context);
                    } else {
                        by = new a(PushService.yy().yz());
                    }
                    by.hv();
                }
            }
        }
        return by;
    }

    private a(Context context) {
        this.bx = context;
    }

    private boolean hv() {
        c.sg("PushLog2951", "begin to init ChannelMgr");
        this.ca = new com.huawei.android.pushagent.model.channel.entity.a.c(null, this.bx);
        return true;
    }

    public static com.huawei.android.pushagent.model.channel.entity.c hp(Context context) {
        return hl(context).ho().aw;
    }

    public List<String> hs() {
        List<String> linkedList = new LinkedList();
        if (this.ca.aw.fc() != null) {
            linkedList.add(this.ca.aw.fc());
        }
        return linkedList;
    }

    public void hn() {
        hq(this.bx);
        if (this.ca != null) {
            this.ca.gd();
        }
    }

    public void hr(long j) {
        c.sh("PushLog2951", "next connect pushsvr will be after " + j);
        Intent intent = new Intent("com.huawei.action.CONNECT_PUSHSRV");
        intent.setPackage(this.bx.getPackageName());
        com.huawei.android.pushagent.utils.tools.a.qf(this.bx, intent, j);
    }

    public b ho() {
        return this.ca;
    }

    public static b hk() {
        return hl(PushService.yy().yz()).ca;
    }

    public void hm(Intent intent) {
        String action = intent.getAction();
        String stringExtra = intent.getStringExtra("EXTRA_INTENT_TYPE");
        if ("com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT".equals(action)) {
            hu();
        } else if ("com.huawei.intent.action.PUSH".equals(action) && "com.huawei.android.push.intent.HEARTBEAT_REQ".equals(stringExtra)) {
            ht(intent);
        } else if ("android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action)) {
            hw(action);
        }
    }

    private static void hq(Context context) {
        c.sg("PushLog2951", "enter ConnectMgr:cancelDelayAlarm");
        com.huawei.android.pushagent.utils.tools.a.qg(context, "com.huawei.action.CONNECT_PUSHSRV");
        com.huawei.android.pushagent.utils.tools.a.qg(context, "com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT");
        com.huawei.android.pushagent.utils.tools.a.qh(context, new Intent("com.huawei.intent.action.PUSH").putExtra("EXTRA_INTENT_TYPE", "com.huawei.android.push.intent.HEARTBEAT_REQ").setPackage(context.getPackageName()));
    }

    private void hu() {
        com.huawei.android.pushagent.a.a.xx(70, String.valueOf(hp(this.bx).ff(false)));
        com.huawei.android.pushagent.a.a.xv(81);
        c.sh("PushLog2951", "time out for wait heartbeat so reconnect");
        hp(this.bx).fb(true);
        Socket ga = ho().ga();
        boolean di = g.aq(this.bx).di();
        if (ga != null && di) {
            try {
                c.sg("PushLog2951", "setSoLinger 0 when close socket after heartbeat timeout");
                ga.setSoLinger(true, 0);
            } catch (Throwable e) {
                c.se("PushLog2951", e.toString(), e);
            }
        }
        ho().gd();
    }

    private void ht(Intent intent) {
        c.sh("PushLog2951", "heartbeatArrive");
        if (!(intent == null || -1 == com.huawei.android.pushagent.utils.b.tm(this.bx))) {
            b ho = ho();
            if (ho.gc()) {
                c.sh("PushLog2951", "heartbeatArrive, send heart beat");
                ho.aw.gn(intent.getBooleanExtra("isHeartbeatReq", true));
                ho.aw.fj();
            } else {
                PushService.yx(new Intent("com.huawei.action.CONNECT_PUSHSRV").setPackage(this.bx.getPackageName()));
            }
        }
    }

    private void hw(String str) {
        if (str != null) {
            if (ho().gc()) {
                hp(this.bx).gn(false);
                hp(this.bx).fj();
            } else if (-1 != com.huawei.android.pushagent.utils.b.tm(this.bx)) {
                c.sg("PushLog2951", "received " + str + ", but not Connect, go to connect!");
                PushService.yx(new Intent("com.huawei.action.CONNECT_PUSHSRV").setPackage(this.bx.getPackageName()));
            } else {
                c.sh("PushLog2951", "no net work, when recevice :" + str + ", do nothing");
            }
        }
    }
}
