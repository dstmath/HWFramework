package com.huawei.android.pushagent.model.channel.entity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.huawei.android.pushagent.datatype.IPushMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.channel.ChannelMgr.ConnectEntityMode;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel;
import com.huawei.bd.Reporter;
import defpackage.ae;
import defpackage.au;
import defpackage.aw;
import defpackage.bq;
import defpackage.j;
import defpackage.p;
import defpackage.q;
import java.net.Socket;

public abstract class ConnectEntity {
    public j P;
    public SocketReadThread Q;
    public IPushChannel R;
    public q S;
    private PowerManager T;
    public boolean U;
    public boolean V;
    private WakeLock W;
    public Context context;

    public enum CONNECT_METHOD {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.pushagent.model.channel.entity.ConnectEntity.CONNECT_METHOD.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.pushagent.model.channel.entity.ConnectEntity.CONNECT_METHOD.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.pushagent.model.channel.entity.ConnectEntity.CONNECT_METHOD.<clinit>():void");
        }
    }

    public ConnectEntity(j jVar, Context context, q qVar, String str) {
        this.V = false;
        this.W = null;
        this.context = context;
        this.P = jVar;
        this.S = qVar;
        this.T = (PowerManager) context.getSystemService("power");
    }

    public j a(int i, int i2) {
        switch (p.X[CONNECT_METHOD.values()[b(i, i2)].ordinal()]) {
            case Reporter.ACTIVITY_CREATE /*1*/:
                return new j(this.P.D, this.P.port, false, this.P.E);
            case Reporter.ACTIVITY_RESUME /*2*/:
                return new j(this.P.D, 443, false, this.P.E);
            case Reporter.ACTIVITY_PAUSE /*3*/:
                return new j(this.P.D, 443, true, this.P.E);
            case Reporter.ACTIVITY_DESTROY /*4*/:
                return new j(this.P.D, this.P.port, true, this.P.E);
            default:
                return null;
        }
    }

    public abstract void a(SocketEvent socketEvent, Bundle bundle);

    public abstract void a(boolean z);

    public abstract void a(boolean z, boolean z2);

    public synchronized boolean a(IPushMessage iPushMessage) {
        boolean z = false;
        synchronized (this) {
            if (this.R == null || this.R.getSocket() == null) {
                aw.e("PushLog2828", "when send pushMsg, channel is null\uff0c curCls:" + getClass().getSimpleName());
            } else {
                if (ChannelMgr.aU() == ba()) {
                    this.R.getSocket().setSoTimeout(0);
                } else {
                    this.R.getSocket().setSoTimeout((int) (this.S.e(false) + ae.l(this.context).ad()));
                }
                byte[] bArr = null;
                if (iPushMessage != null) {
                    bArr = iPushMessage.encode();
                } else {
                    aw.e("PushLog2828", "pushMsg = null, send fail");
                }
                if (bArr == null || bArr.length == 0) {
                    aw.i("PushLog2828", "when send PushMsg, encode Len is null");
                } else {
                    aw.i("PushLog2828", "read to Send:" + au.e(iPushMessage.j()));
                    if (this.R.a(bArr)) {
                        aw.i("PushLog2828", "send msg to remote srv success");
                        if (-34 == iPushMessage.j() || -36 == iPushMessage.j() || -92 == iPushMessage.j()) {
                            bq.b(this.context, new Intent("com.huawei.android.push.intent.RESPONSE_FAIL").setPackage(this.context.getPackageName()), ae.l(this.context).aw());
                        }
                        z = true;
                    } else {
                        aw.e("PushLog2828", "call channel.send false!!");
                    }
                }
            }
        }
        return z;
    }

    public synchronized void aZ() {
        this.W = this.T.newWakeLock(1, "mWakeLockForThread");
        this.W.setReferenceCounted(false);
        this.W.acquire(1000);
    }

    public int b(int i, int i2) {
        return Math.abs(i + i2) % CONNECT_METHOD.values().length;
    }

    public synchronized void b(boolean z) {
        this.U = z;
    }

    public abstract ConnectEntityMode ba();

    public synchronized void c(boolean z) {
        this.V = z;
    }

    public void close() {
        if (this.R != null) {
            try {
                this.R.close();
                this.R = null;
            } catch (Throwable e) {
                aw.d("PushLog2828", "call channel.close() cause:" + e.toString(), e);
            }
            if (this.Q != null) {
                this.Q.interrupt();
                this.Q = null;
            }
        }
    }

    public Socket getSocket() {
        return this.R != null ? this.R.getSocket() : null;
    }

    public boolean hasConnection() {
        return this.R != null && this.R.hasConnection();
    }

    public String toString() {
        return this.P.toString() + " " + this.S.toString();
    }
}
