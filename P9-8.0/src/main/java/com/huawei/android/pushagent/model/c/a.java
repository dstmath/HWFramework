package com.huawei.android.pushagent.model.c;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.a.i;
import com.huawei.android.pushagent.model.d.f;
import com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr$RECONNECTEVENT;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.c;

public class a implements e {
    private int cf = -1;

    public a(Context context) {
    }

    public void onReceive(Context context, Intent intent) {
        try {
            c.sg("PushLog2951", "enter ConnectReceiver:onReceive");
            String action = intent.getAction();
            String stringExtra = intent.getStringExtra("EXTRA_INTENT_TYPE");
            if ("com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT".equals(action)) {
                com.huawei.android.pushagent.model.channel.a.hl(context).hm(intent);
                return;
            }
            if (ir(context, intent, action, stringExtra)) {
                com.huawei.android.pushagent.model.channel.a.hl(context).hm(intent);
            } else if (iq(context, action, stringExtra)) {
                ip(context, action);
            } else if ("com.huawei.android.push.intent.CONNECTING".equals(action)) {
                c.sh("PushLog2951", "receive the ACTION_CONNECTING action.");
            } else if ("com.huawei.android.push.intent.CONNECTED".equals(action)) {
                c.sh("PushLog2951", "receive the ACTION_CONNECTED action.");
            } else if ("com.huawei.intent.action.PUSH_OFF".equals(action)) {
                action = intent.getStringExtra("Remote_Package_Name");
                if (action == null || (action.equals(context.getPackageName()) ^ 1) != 0) {
                    c.sg("PushLog2951", "need stop PkgName:" + action + " is not me, need not stop!");
                    return;
                }
                com.huawei.android.pushagent.model.channel.a.hl(context).hn();
                if (com.huawei.android.pushagent.utils.bastet.a.ra(context).rb()) {
                    com.huawei.android.pushagent.utils.bastet.a.ra(context).rc();
                }
                PushService.za();
            }
        } catch (Throwable e) {
            c.si("PushLog2951", e.toString(), e);
        }
    }

    private void ip(Context context, String str) {
        try {
            com.huawei.android.pushagent.a.a.xv(30);
            if (!f.ku(context)) {
                com.huawei.android.pushagent.a.a.xv(31);
                com.huawei.android.pushagent.a.a.xv(83);
                com.huawei.android.pushagent.model.channel.a.hl(context).hn();
                c.sh("PushLog2951", "no push client, stop push apk service");
                com.huawei.android.pushagent.utils.tools.a.qf(context, new Intent("com.huawei.intent.action.PUSH_OFF").setPackage(context.getPackageName()).putExtra("Remote_Package_Name", context.getPackageName()), g.aq(context).ay() * 1000);
            } else if (g.aq(context).isValid()) {
                boolean z;
                if ("com.huawei.android.push.intent.TRS_QUERY_SUCCESS".equals(str)) {
                    com.huawei.android.pushagent.a.a.xv(33);
                    com.huawei.android.pushagent.model.channel.a.hk().aw.fh();
                    com.huawei.android.pushagent.model.flowcontrol.c.mi(context);
                    com.huawei.android.pushagent.model.flowcontrol.a.lk(context).ll(context);
                }
                int tm = b.tm(context);
                if (-1 == tm || tm != this.cf) {
                    if (-1 == tm) {
                        c.sg("PushLog2951", "no network in ConnectReceiver:connect, so close socket");
                    } else {
                        c.sg("PushLog2951", "net work switch from:" + this.cf + " to " + tm);
                    }
                    try {
                        com.huawei.android.pushagent.model.channel.a.hl(context).hn();
                    } catch (Throwable e) {
                        c.se("PushLog2951", "call channel.close cause exception:" + e.toString(), e);
                    }
                }
                if (this.cf != tm) {
                    z = true;
                } else {
                    z = false;
                }
                c.sh("PushLog2951", "lastnetWorkType:" + this.cf + " " + "curNetWorkType:" + tm + ", [-1:NONE, 0:MOBILE, 1:WIFI]");
                this.cf = tm;
                if (tm == 0 && i.ea(context).eb() == 0) {
                    c.sg("PushLog2951", "It is mobile network and network policy is close of NC, so not connect push.");
                } else {
                    io(context, str, z);
                }
            } else {
                com.huawei.android.pushagent.a.a.xv(32);
                c.sh("PushLog2951", "connect srv: TRS is invalid, so need to query TRS");
                com.huawei.android.pushagent.model.d.c.jz(context).ka(false);
            }
        } catch (Exception e2) {
            c.sf("PushLog2951", "call switchChannel cause Exceptino:" + e2.toString());
        }
    }

    private boolean ir(Context context, Intent intent, String str, String str2) {
        if (("com.huawei.intent.action.PUSH".equals(str) && "com.huawei.android.push.intent.HEARTBEAT_REQ".equals(str2)) || "android.intent.action.TIME_SET".equals(str)) {
            return true;
        }
        return "android.intent.action.TIMEZONE_CHANGED".equals(str);
    }

    private boolean iq(Context context, String str, String str2) {
        if ("com.huawei.push.action.NET_CHANGED".equals(str) || "com.huawei.action.CONNECT_PUSHSRV".equals(str) || "com.huawei.action.CONNECT_PUSHSRV_PUSHSRV".equals(str) || "com.huawei.android.push.intent.TRS_QUERY_SUCCESS".equals(str)) {
            return true;
        }
        return "com.huawei.intent.action.PUSH".equals(str) ? "com.huawei.intent.action.PUSH_ON".equals(str2) : false;
    }

    private void io(Context context, String str, boolean z) {
        if (context == null || str == null) {
            c.sf("PushLog2951", "context or action is null");
            return;
        }
        if ("com.huawei.action.CONNECT_PUSHSRV_PUSHSRV".equals(str)) {
            c.sg("PushLog2951", "get " + str + " so get a pushSrv to connect");
            com.huawei.android.pushagent.model.channel.a.hk().fo(true);
        } else if (com.huawei.android.pushagent.model.channel.a.hl(context).ho().gc()) {
            com.huawei.android.pushagent.a.a.xv(36);
            c.sg("PushLog2951", "pushChannel already connect");
        } else {
            c.sg("PushLog2951", "get " + str + " so get a srv to connect");
            if (z) {
                com.huawei.android.pushagent.model.flowcontrol.a.lk(context).lm(context, ReconnectMgr$RECONNECTEVENT.NETWORK_CHANGE, new Bundle());
            }
            com.huawei.android.pushagent.model.channel.a.hl(context).ho().fo(false);
        }
    }
}
