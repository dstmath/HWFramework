package com.huawei.android.pushagent.utils.bastet;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import com.huawei.android.bastet.HwBastet;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.tcp.HeartBeatReqMessage;
import com.huawei.android.pushagent.datatype.tcp.HeartBeatRspMessage;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.c;
import java.lang.reflect.Method;
import java.net.Socket;

public class a {
    private static a fi = null;
    private PushBastet$BasteProxyStatus fe = PushBastet$BasteProxyStatus.Stoped;
    private Context ff;
    private Handler fg = null;
    private HwBastet fh = null;

    public static synchronized a ra(Context context) {
        synchronized (a.class) {
            a aVar;
            if (fi != null) {
                aVar = fi;
                return aVar;
            }
            fi = new a(context);
            aVar = fi;
            return aVar;
        }
    }

    private a(Context context) {
        this.ff = context;
    }

    public synchronized boolean qy() {
        try {
            if (rb()) {
                c.sh("PushLog2951", "bastet has started, need not restart");
                return true;
            } else if (rf()) {
                com.huawei.android.pushagent.a.a.xv(73);
                if (rg(3, 900000)) {
                    c.sh("PushLog2951", "startPushBastetProxy success");
                    rl(PushBastet$BasteProxyStatus.Started);
                    return true;
                }
                c.sh("PushLog2951", "startPushBastetProxy failed");
                return false;
            } else {
                com.huawei.android.pushagent.a.a.xv(72);
                c.sh("PushLog2951", "init push bastet failed!");
                return false;
            }
        } catch (Throwable e) {
            c.se("PushLog2951", "startPushBastetProxy failed:" + e.getMessage(), e);
        }
    }

    private boolean rf() {
        c.sg("PushLog2951", "initPushBastet");
        if (1 == b.tm(this.ff)) {
            c.sh("PushLog2951", "not support bastet in wifi");
            return false;
        } else if (!rh() || !ri()) {
            return false;
        } else {
            Socket ga = com.huawei.android.pushagent.model.channel.a.hk().ga();
            if (ga == null || !re()) {
                return false;
            }
            try {
                this.fh = new HwBastet("PUSH_BASTET", ga, this.fg, this.ff);
                if (this.fh.isBastetAvailable()) {
                    this.fh.reconnectSwitch(true);
                    return true;
                }
                c.sh("PushLog2951", "isBastetAvailable false, can't use bastet.");
                rc();
                return false;
            } catch (Throwable e) {
                c.sk("PushLog2951", "init bastet error", e);
                rc();
                return false;
            }
        }
    }

    private boolean ri() {
        boolean bj = g.aq(this.ff).bj();
        c.sg("PushLog2951", "isPushServerAllowBastet: " + bj);
        return bj;
    }

    private boolean rh() {
        try {
            Class.forName("com.huawei.android.bastet.HwBastet");
            return true;
        } catch (ClassNotFoundException e) {
            c.sf("PushLog2951", "bastet not exist");
            return false;
        }
    }

    private synchronized void rj() {
        c.sh("PushLog2951", "enter quitLooper");
        try {
            if (!(this.fg == null || this.fg.getLooper() == null)) {
                this.fg.getLooper().quitSafely();
                c.sh("PushLog2951", "bastet loop quitSafely");
            }
            this.fg = null;
        } catch (Throwable e) {
            c.se("PushLog2951", "PushBastetListener release error", e);
        }
        return;
    }

    private void rk() {
        c.sh("PushLog2951", "reConnectPush");
        rc();
        com.huawei.android.pushagent.model.channel.a.hl(this.ff).ho().gd();
        com.huawei.android.pushagent.a.a.xv(80);
        PushService.yx(new Intent("com.huawei.action.CONNECT_PUSHSRV").setPackage(this.ff.getPackageName()));
    }

    private boolean rg(int i, long j) {
        c.sg("PushLog2951", "initPushHeartBeatDataContent");
        try {
            HeartBeatReqMessage heartBeatReqMessage = new HeartBeatReqMessage();
            heartBeatReqMessage.wh((byte) ((int) Math.ceil((((double) j) * 1.0d) / 60000.0d)));
            byte[] gy = com.huawei.android.pushagent.model.channel.a.b.gy(heartBeatReqMessage.vs(), false);
            c.sg("PushLog2951", "heartbeat fixedSendContent is : " + com.huawei.android.pushagent.utils.d.b.sb(gy));
            byte[] gy2 = com.huawei.android.pushagent.model.channel.a.b.gy(new HeartBeatRspMessage().vs(), true);
            c.sg("PushLog2951", "heartbeat fixedReplyContent is : " + com.huawei.android.pushagent.utils.d.b.sb(gy2));
            int bk = g.aq(this.ff).bk();
            this.fh.setAolHeartbeat(bk, gy, gy2);
            c.sg("PushLog2951", "set bastet heartbeat interval level as: " + bk);
            return true;
        } catch (Throwable e) {
            c.se("PushLog2951", "initPushHeartBeatDataContent error :" + e.toString(), e);
            return false;
        }
    }

    public void rl(PushBastet$BasteProxyStatus pushBastet$BasteProxyStatus) {
        this.fe = pushBastet$BasteProxyStatus;
    }

    public boolean rb() {
        return this.fh != null && PushBastet$BasteProxyStatus.Started == this.fe;
    }

    public void rc() {
        c.sh("PushLog2951", "resetBastet");
        rd();
        this.fh = null;
        rl(PushBastet$BasteProxyStatus.Stoped);
        rj();
        com.huawei.android.pushagent.model.channel.a.hk().ge(false);
        c.sh("PushLog2951", "after setExistResetBastetAlarm");
        com.huawei.android.pushagent.utils.tools.a.qg(this.ff, "com.huawei.android.push.intent.RESET_BASTET");
    }

    private synchronized boolean re() {
        c.sh("PushLog2951", "initMsgHandler");
        try {
            if (!(this.fg == null || this.fg.getLooper() == null)) {
                this.fg.getLooper().quitSafely();
            }
            HandlerThread handlerThread = new HandlerThread("bastetRspHandlerThread");
            handlerThread.start();
            int i = 0;
            while (!handlerThread.isAlive()) {
                int i2 = i + 1;
                try {
                    wait(10);
                    if (i2 % 100 == 0) {
                        c.sf("PushLog2951", "wait bastetRspHandlerThread start take time: " + (i2 * 10) + " ms");
                    }
                    if (i2 > 500) {
                        c.sf("PushLog2951", "reached the max retry times:500");
                        return false;
                    }
                    i = i2;
                } catch (Throwable e) {
                    c.se("PushLog2951", "InterruptedException error", e);
                }
            }
            if (handlerThread.getLooper() == null) {
                c.sf("PushLog2951", "looper is null when initMsgHandler");
                return false;
            }
            this.fg = new b(this, handlerThread.getLooper());
            return true;
        } catch (Throwable e2) {
            c.se("PushLog2951", "initMsgHandler error:" + e2.getMessage(), e2);
            rc();
            return false;
        }
    }

    public long qz() {
        return 900000;
    }

    private void rd() {
        c.sh("PushLog2951", "enter clearBastetProxy!");
        if (this.fh == null) {
            c.sh("PushLog2951", "enter clearBastetProxy, mHwBastet is null");
            return;
        }
        try {
            Method declaredMethod = this.fh.getClass().getDeclaredMethod("clearBastetProxy", new Class[0]);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(this.fh, new Object[0]);
            c.sh("PushLog2951", "clearBastetProxy success!");
        } catch (Throwable e) {
            c.se("PushLog2951", e.toString(), e);
        } catch (Throwable e2) {
            c.se("PushLog2951", e2.toString(), e2);
        } catch (Throwable e22) {
            c.se("PushLog2951", e22.toString(), e22);
        } catch (Throwable e222) {
            c.se("PushLog2951", e222.toString(), e222);
        } catch (Throwable e2222) {
            c.sk("PushLog2951", e2222.toString(), e2222);
        }
    }
}
