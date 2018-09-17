package defpackage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.IPushMessage;
import com.huawei.android.pushagent.datatype.PushException;
import com.huawei.android.pushagent.datatype.pushmessage.HeartBeatRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.NewHeartBeatRspMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.channel.ChannelMgr.ConnectEntityMode;
import com.huawei.android.pushagent.model.channel.entity.ConnectEntity;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent;
import com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr;
import com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr.RECONNECTEVENT;
import com.huawei.android.pushagent.utils.bastet.PushBastet;
import com.huawei.bd.Reporter;
import java.net.InetSocketAddress;

/* renamed from: w */
public class w extends ConnectEntity {
    private boolean aq;

    public w(j jVar, Context context) {
        super(jVar, context, new y(context), w.class.getSimpleName());
        this.aq = false;
        bl();
    }

    public void a(SocketEvent socketEvent, Bundle bundle) {
        int a = ag.a(this.context, "tryConnectPushSevTimes", 0);
        int a2 = ag.a(this.context, "lastConnectPushSrvMethodIdx", 0);
        aw.d("PushLog2828", "enter PushConnectEntity. notifyEvent is " + socketEvent + ", " + " tryConnectPushSevTimes:" + a + " lastConnctIdx:" + a2);
        switch (x.ao[socketEvent.ordinal()]) {
            case Reporter.ACTIVITY_CREATE /*1*/:
                PushService.a(new Intent("com.huawei.android.push.intent.CONNECTING"));
            case Reporter.ACTIVITY_RESUME /*2*/:
                this.S.bb();
                this.S.h(System.currentTimeMillis());
                ReconnectMgr.s(this.context).a(this.context, RECONNECTEVENT.bm, new Bundle());
                ag.a(this.context, new g("lastcontectsucc_time", Long.class, Long.valueOf(System.currentTimeMillis())));
                Intent intent = new Intent("com.huawei.android.push.intent.CONNECTED");
                if (bundle != null) {
                    intent.putExtras(bundle);
                }
                PushService.a(intent);
            case Reporter.ACTIVITY_PAUSE /*3*/:
                bundle.putInt("connect_mode", ba().ordinal());
                PushService.a(new Intent("com.huawei.android.push.intent.CHANNEL_CLOSED").putExtras(bundle));
                if (ChannelMgr.aU() == ba()) {
                    bq.w(this.context, "com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT");
                    ReconnectMgr.s(this.context).a(this.context, RECONNECTEVENT.bl, bundle);
                }
                if (!this.aq) {
                    int i = a + 1;
                    aw.i("PushLog2828", "channel is not Regist, tryConnectPushSevTimes add to " + i);
                    ag.a(this.context, new g("tryConnectPushSevTimes", Integer.class, Integer.valueOf(i)));
                    ag.a(this.context, new g("lastConnectPushSrvMethodIdx", Integer.class, Integer.valueOf(a2)));
                }
            case Reporter.ACTIVITY_DESTROY /*4*/:
                bq.w(this.context, "com.huawei.android.push.intent.RESPONSE_FAIL");
                IPushMessage iPushMessage = (IPushMessage) bundle.getSerializable("push_msg");
                if (iPushMessage == null) {
                    aw.i("PushLog2828", "push_msg is null");
                    return;
                }
                aw.d("PushLog2828", "received pushSrv Msg:" + au.e(iPushMessage.j()));
                if (iPushMessage.j() == -45 || iPushMessage.j() == -33) {
                    this.aq = true;
                    ag.a(this.context, new g("lastConnectPushSrvMethodIdx", Integer.class, Integer.valueOf(b(a, a2))));
                    ag.a(this.context, new g("tryConnectPushSevTimes", Integer.class, Integer.valueOf(0)));
                } else if ((iPushMessage instanceof HeartBeatRspMessage) || (iPushMessage instanceof NewHeartBeatRspMessage)) {
                    bq.w(this.context, "com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT");
                    this.S.f(false);
                }
                this.S.bb();
                Intent intent2 = new Intent("com.huawei.android.push.intent.MSG_RECEIVED");
                intent2.putExtra("push_msg", iPushMessage);
                PushService.a(intent2);
            default:
        }
    }

    public synchronized void a(boolean z) {
        a(z, false);
    }

    public synchronized void a(boolean z, boolean z2) {
        try {
            if (PushBastet.ac(this.context).bZ()) {
                aw.i("PushLog2828", "enter connect, bastetProxy is started");
                if (hasConnection()) {
                    aw.i("PushLog2828", "enter connect, has Connection, do not reconnect");
                } else if (au.G(this.context) == -1) {
                    aw.e("PushLog2828", "no network, so cannot connect");
                } else {
                    aw.i("PushLog2828", "enter connect, hasResetBastetAlarm " + this.V);
                    if (!this.V) {
                        c(true);
                        bq.b(this.context, new Intent("com.huawei.android.push.intent.RESET_BASTET").setPackage(this.context.getPackageName()), ae.l(this.context).av());
                        aw.i("PushLog2828", "bastetProxyStarted, setDelayAlarm");
                    }
                }
            } else {
                aw.d("PushLog2828", "enter PushConnectEntity:connect(isForceToConnPushSrv:" + z + ")");
                this.S.bc();
                if (ae.l(this.context).isValid()) {
                    if (au.G(this.context) == -1) {
                        aw.e("PushLog2828", "no network, so cannot connect");
                    } else {
                        if (ag.a(this.context, "cloudpush_isNoDelayConnect", false)) {
                            z = true;
                        }
                        if (!hasConnection()) {
                            int a = ag.a(this.context, "tryConnectPushSevTimes", 0);
                            long v = ReconnectMgr.s(this.context).v(this.context);
                            if (v <= 0) {
                                aw.i("PushLog2828", "no limit to connect pushsvr");
                            } else if (this.U) {
                                aw.i("PushLog2828", "no limit to connect pushsvr, skipControl");
                                b(false);
                            } else {
                                ChannelMgr.g(this.context).g(v);
                            }
                            if (this.Q == null || !this.Q.isAlive()) {
                                aw.d("PushLog2828", "begin to create new socket, so close socket");
                                aZ();
                                close();
                                aw.d("PushLog2828", "IS_NODELAY_CONNECT:" + ag.a(this.context, "cloudpush_isNoDelayConnect", false) + " hasMsg:" + z2);
                                if (ag.a(this.context, "cloudpush_isNoDelayConnect", false) || z2 || ai.a(this.context, 1)) {
                                    this.aq = false;
                                    int a2 = ag.a(this.context, "lastConnectPushSrvMethodIdx", 0);
                                    InetSocketAddress g = ae.l(this.context).g(z);
                                    if (g != null) {
                                        aw.d("PushLog2828", "get pushSrvAddr:" + g);
                                        this.P.D = g.getAddress().getHostAddress();
                                        this.P.port = g.getPort();
                                        this.P.E = ag.o(this.context);
                                        this.P = a(a2, a);
                                        this.Q = new z(this);
                                        this.Q.start();
                                    } else {
                                        aw.d("PushLog2828", "no valid pushSrvAddr, just wait!!");
                                    }
                                } else {
                                    ChannelMgr.g(this.context).a(ConnectEntityMode.N);
                                    ChannelMgr.g(this.context).a(ConnectEntityMode.N, false);
                                }
                            } else {
                                aw.i("PushLog2828", "It is in connecting...");
                            }
                        } else if (z) {
                            aw.d("PushLog2828", "hasConnect, but isForceToConnPushSrv:" + z + ", so send heartBeat");
                            this.S.bh();
                        } else {
                            aw.d("PushLog2828", "aready connect, need not connect more");
                        }
                    }
                }
            }
        } catch (Throwable e) {
            throw new PushException(e);
        }
    }

    public ConnectEntityMode ba() {
        return ConnectEntityMode.M;
    }

    public boolean bl() {
        if (this.P == null) {
            this.P = new j("", -1, false, ag.o(this.context));
        }
        return true;
    }
}
