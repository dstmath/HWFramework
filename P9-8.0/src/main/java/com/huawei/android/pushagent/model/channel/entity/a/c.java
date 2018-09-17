package com.huawei.android.pushagent.model.channel.entity.a;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.b.d;
import com.huawei.android.pushagent.datatype.exception.PushException;
import com.huawei.android.pushagent.datatype.tcp.base.IPushMessage;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.a.i;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread$SocketEvent;
import com.huawei.android.pushagent.model.channel.entity.b;
import com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr$RECONNECTEVENT;
import com.huawei.android.pushagent.utils.bastet.a;
import java.net.InetSocketAddress;

public class c extends b {
    private static final /* synthetic */ int[] ak = null;
    private boolean aj = false;

    private static /* synthetic */ int[] fv() {
        if (ak != null) {
            return ak;
        }
        int[] iArr = new int[SocketReadThread$SocketEvent.values().length];
        try {
            iArr[SocketReadThread$SocketEvent.SocketEvent_CLOSE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SocketReadThread$SocketEvent.SocketEvent_CONNECTED.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SocketReadThread$SocketEvent.SocketEvent_CONNECTING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SocketReadThread$SocketEvent.SocketEvent_MSG_RECEIVED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        ak = iArr;
        return iArr;
    }

    public c(d dVar, Context context) {
        super(dVar, context, new a(context));
        fq();
    }

    public final boolean fq() {
        if (this.au == null) {
            this.au = new d("", -1, false);
        }
        return true;
    }

    public boolean fn() {
        return this.aj;
    }

    /* JADX WARNING: Missing block: B:48:0x013a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void fo(boolean z) {
        try {
            if (a.ra(this.at).rb()) {
                com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "enter connect, bastetProxy is started");
                if (gc()) {
                    com.huawei.android.pushagent.a.a.xv(37);
                    com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "enter connect, has Connection, do not reconnect");
                    return;
                } else if (!com.huawei.android.pushagent.utils.b.ul(this.at)) {
                    com.huawei.android.pushagent.a.a.xv(38);
                    com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "no network, so cannot connect");
                    return;
                } else if (this.ay) {
                    com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "isSkipControl, resetBastet");
                    a.ra(this.at).rc();
                } else {
                    boolean gi = gi();
                    com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "enter connect, hasResetBastetAlarm " + gi);
                    if (gi) {
                        com.huawei.android.pushagent.a.a.xv(39);
                        return;
                    }
                    com.huawei.android.pushagent.a.a.xv(40);
                    ge(true);
                    com.huawei.android.pushagent.utils.tools.a.qf(this.at, new Intent("com.huawei.android.push.intent.RESET_BASTET").setPackage(this.at.getPackageName()), g.aq(this.at).dn());
                    com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "bastetProxyStarted, setDelayAlarm");
                    return;
                }
            }
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "enter PushConnectMode:connect(isForceToConnPushSrv:" + z + ")");
            if (!g.aq(this.at).isValid()) {
                com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "puserverip is not valid");
            } else if (!com.huawei.android.pushagent.utils.b.ul(this.at)) {
                com.huawei.android.pushagent.a.a.xv(41);
                com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "no network, so cannot connect");
            } else if (gc()) {
                com.huawei.android.pushagent.a.a.xv(42);
                if (z) {
                    com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "hasConnect, but isForceToConnPushSrv:" + z + ", so send heartBeat");
                    this.aw.fj();
                } else {
                    com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "aready connect, need not connect more");
                }
            } else {
                long lq = com.huawei.android.pushagent.model.flowcontrol.a.lk(this.at).lq(this.at);
                if (lq <= 0) {
                    com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "no limit to connect pushsvr");
                } else if (this.ay) {
                    com.huawei.android.pushagent.a.a.xv(43);
                    com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "no limit to connect pushsvr, skipControl");
                    gf(false);
                } else {
                    com.huawei.android.pushagent.a.a.xv(46);
                    com.huawei.android.pushagent.model.channel.a.hl(this.at).hr(lq);
                    return;
                }
                fp(z);
            }
        } catch (Throwable e) {
            throw new PushException(e);
        }
    }

    /* JADX WARNING: Missing block: B:17:0x009d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void fp(boolean z) {
        if (this.bc == null || (this.bc.isAlive() ^ 1) != 0) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "begin to create new socket, so close socket");
            gj();
            gd();
            if (com.huawei.android.pushagent.model.flowcontrol.c.mp(this.at, 1)) {
                this.aj = false;
                int er = i.ea(this.at).er();
                InetSocketAddress ke = com.huawei.android.pushagent.model.d.c.jz(this.at).ke(z);
                if (ke != null) {
                    com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "get pushSrvAddr:" + ke);
                    this.au.xg(ke.getAddress().getHostAddress());
                    this.au.xh(ke.getPort());
                    this.au = gg(er, i.ea(this.at).es());
                    this.bc = new b(this);
                    this.bc.start();
                } else {
                    com.huawei.android.pushagent.a.a.xv(49);
                    com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "no valid pushSrvAddr, just wait!!");
                    return;
                }
            }
            com.huawei.android.pushagent.a.a.xv(48);
            com.huawei.android.pushagent.utils.d.c.sj("PushLog2951", "can't connect push server because of flow control.");
            return;
        }
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "It is in connecting...");
        com.huawei.android.pushagent.a.a.xv(47);
    }

    public void fs(SocketReadThread$SocketEvent socketReadThread$SocketEvent, Bundle bundle) {
        int es = i.ea(this.at).es();
        int er = i.ea(this.at).er();
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "enter PushConnectMode. notifyEvent is " + socketReadThread$SocketEvent + ", " + " tryConnectPushSevTimes:" + es + " lastConnctIdx:" + er);
        switch (fv()[socketReadThread$SocketEvent.ordinal()]) {
            case 1:
                ft(bundle, es, er);
                return;
            case 2:
                fu();
                return;
            case 3:
                PushService.yx(new Intent("com.huawei.android.push.intent.CONNECTING"));
                return;
            case 4:
                fr(bundle, es, er);
                return;
            default:
                return;
        }
    }

    private void fu() {
        com.huawei.android.pushagent.model.flowcontrol.a.lk(this.at).lm(this.at, ReconnectMgr$RECONNECTEVENT.SOCKET_CONNECTED, new Bundle());
        PushService.yx(new Intent("com.huawei.android.push.intent.CONNECTED"));
    }

    private void ft(Bundle bundle, int i, int i2) {
        com.huawei.android.pushagent.utils.tools.a.qg(this.at, "com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT");
        com.huawei.android.pushagent.model.flowcontrol.a.lk(this.at).lm(this.at, ReconnectMgr$RECONNECTEVENT.SOCKET_CLOSE, bundle);
        if (!this.aj) {
            int i3 = i + 1;
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "channel is not Regist, tryConnectPushSevTimes add to " + i3);
            i.ea(this.at).eu(i3);
            i.ea(this.at).et(i2);
        }
    }

    private void fr(Bundle bundle, int i, int i2) {
        com.huawei.android.pushagent.utils.tools.a.qg(this.at, "com.huawei.android.push.intent.RESPONSE_FAIL");
        IPushMessage iPushMessage = (IPushMessage) bundle.getSerializable("push_msg");
        if (iPushMessage == null) {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "push_msg is null");
            return;
        }
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "process cmdid to receive from pushSrv:" + com.huawei.android.pushagent.utils.d.b.sc(iPushMessage.vt()));
        switch (iPushMessage.vt()) {
            case (byte) -37:
                com.huawei.android.pushagent.utils.tools.a.qg(this.at, "com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT");
                this.aw.fb(false);
                this.aw.gq();
                break;
            case (byte) 65:
                this.aj = true;
                i.ea(this.at).et(gh(i2, i));
                i.ea(this.at).eu(0);
                com.huawei.android.pushagent.model.flowcontrol.a.lk(this.at).lm(this.at, ReconnectMgr$RECONNECTEVENT.SOCKET_REG_SUCCESS, new Bundle());
                break;
            default:
                this.aw.gq();
                break;
        }
        Intent intent = new Intent("com.huawei.android.push.intent.MSG_RECEIVED");
        intent.putExtra("push_msg", iPushMessage);
        PushService.yx(intent);
    }
}
