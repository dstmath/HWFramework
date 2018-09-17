package com.huawei.android.pushagent.model.channel.entity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.huawei.android.pushagent.datatype.b.d;
import com.huawei.android.pushagent.datatype.tcp.base.IPushMessage;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.channel.a.a;
import com.huawei.android.pushagent.utils.d.c;
import java.net.Socket;

public abstract class b {
    private static final /* synthetic */ int[] bd = null;
    protected Context at;
    public d au;
    public a av;
    public c aw;
    private boolean ax = false;
    protected boolean ay;
    private final Object az = new Object();
    private WakeLock ba = null;
    private PowerManager bb;
    public a bc;

    private static /* synthetic */ int[] gm() {
        if (bd != null) {
            return bd;
        }
        int[] iArr = new int[ConnectMode$CONNECT_METHOD.values().length];
        try {
            iArr[ConnectMode$CONNECT_METHOD.CONNECT_METHOD_DIRECT_DefaultPort.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ConnectMode$CONNECT_METHOD.CONNECT_METHOD_DIRECT_TrsPort.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ConnectMode$CONNECT_METHOD.CONNECT_METHOD_Proxy_DefaultPort.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ConnectMode$CONNECT_METHOD.CONNECT_METHOD_Proxy_TrsPort.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        bd = iArr;
        return iArr;
    }

    public abstract void fo(boolean z);

    public abstract void fs(SocketReadThread$SocketEvent socketReadThread$SocketEvent, Bundle bundle);

    public b(d dVar, Context context, c cVar) {
        this.at = context;
        this.au = dVar;
        this.aw = cVar;
        this.bb = (PowerManager) context.getSystemService("power");
    }

    public boolean gc() {
        return this.av != null ? this.av.gu() : false;
    }

    protected d gg(int i, int i2) {
        switch (gm()[ConnectMode$CONNECT_METHOD.values()[gh(i, i2)].ordinal()]) {
            case 1:
                return new d(this.au.xd(), 443, false);
            case 2:
                return new d(this.au.xd(), this.au.xe(), false);
            case 3:
                return new d(this.au.xd(), this.au.xe(), true);
            case 4:
                return new d(this.au.xd(), 443, true);
            default:
                return null;
        }
    }

    protected int gh(int i, int i2) {
        return Math.abs(i + i2) % ConnectMode$CONNECT_METHOD.values().length;
    }

    protected synchronized void gj() {
        this.ba = this.bb.newWakeLock(1, "mWakeLockForThread");
        this.ba.setReferenceCounted(false);
        this.ba.acquire(1000);
    }

    /* JADX WARNING: Missing block: B:15:0x0027, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean gb(IPushMessage iPushMessage) {
        byte[] bArr = null;
        synchronized (this) {
            if (this.av == null || this.av.gw() == null) {
                c.sf("PushLog2951", "when send pushMsg, channel is null， curCls:" + getClass().getSimpleName());
            } else {
                gk();
                if (iPushMessage != null) {
                    bArr = iPushMessage.vs();
                } else {
                    c.sf("PushLog2951", "pushMsg = null, send fail");
                }
                if (bArr == null || bArr.length == 0) {
                    c.sh("PushLog2951", "when send PushMsg, encode Len is null");
                } else {
                    c.sh("PushLog2951", "process cmdid to send to pushSrv:" + com.huawei.android.pushagent.utils.d.b.sc(iPushMessage.vt()));
                    if (this.av.gx(bArr)) {
                        c.sh("PushLog2951", "send msg to remote srv success");
                        gl(iPushMessage);
                        return true;
                    }
                    c.sf("PushLog2951", "call channel.send false!!");
                }
            }
        }
    }

    public Socket ga() {
        if (this.av != null) {
            return this.av.gw();
        }
        return null;
    }

    public void gd() {
        if (this.av != null) {
            try {
                this.av.gs();
                this.av = null;
            } catch (Throwable e) {
                c.se("PushLog2951", "call channel.close() cause:" + e.toString(), e);
            }
            if (this.bc != null) {
                this.bc.interrupt();
                this.bc = null;
            }
        }
    }

    public String toString() {
        return this.au.toString() + " " + this.aw.toString();
    }

    public synchronized void gf(boolean z) {
        this.ay = z;
    }

    public void ge(boolean z) {
        synchronized (this.az) {
            this.ax = z;
        }
    }

    public boolean gi() {
        boolean z;
        synchronized (this.az) {
            z = this.ax;
        }
        return z;
    }

    private void gk() {
        this.av.gw().setSoTimeout(0);
    }

    private void gl(IPushMessage iPushMessage) {
        if (iPushMessage != null) {
            if ((byte) 64 == iPushMessage.vt() || (byte) 66 == iPushMessage.vt()) {
                com.huawei.android.pushagent.utils.tools.a.qf(this.at, new Intent("com.huawei.android.push.intent.RESPONSE_FAIL").setPackage(this.at.getPackageName()), g.aq(this.at).do());
            }
        }
    }
}
