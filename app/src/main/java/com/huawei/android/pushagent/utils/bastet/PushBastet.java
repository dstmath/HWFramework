package com.huawei.android.pushagent.utils.bastet;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import com.huawei.android.bastet.HwBastet;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.pushmessage.NewHeartBeatReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.NewHeartBeatRspMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.channel.ChannelMgr.ConnectEntityMode;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;
import defpackage.aa;
import defpackage.ae;
import defpackage.au;
import defpackage.aw;
import defpackage.bc;
import defpackage.bq;
import java.lang.reflect.Method;
import java.net.Socket;

public class PushBastet {
    private static PushBastet bQ;
    private HwBastet bR;
    private BasteProxyStatus bS;
    private Context mContext;
    private Handler mHandler;

    enum BasteProxyStatus {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.pushagent.utils.bastet.PushBastet.BasteProxyStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.pushagent.utils.bastet.PushBastet.BasteProxyStatus.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.pushagent.utils.bastet.PushBastet.BasteProxyStatus.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.pushagent.utils.bastet.PushBastet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.pushagent.utils.bastet.PushBastet.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.pushagent.utils.bastet.PushBastet.<clinit>():void");
    }

    private PushBastet(Context context) {
        this.bR = null;
        this.mHandler = null;
        this.bS = BasteProxyStatus.bV;
        this.mContext = context;
    }

    private boolean a(int i, long j) {
        aw.d("PushLog2828", "initPushHeartBeatDataContent");
        try {
            NewHeartBeatReqMessage newHeartBeatReqMessage = new NewHeartBeatReqMessage();
            newHeartBeatReqMessage.d((byte) ((int) Math.ceil((1.0d * ((double) j)) / 60000.0d)));
            byte[] b = aa.b(newHeartBeatReqMessage.encode());
            aw.d("PushLog2828", "heartbeat fixedSendContent is : " + au.f(b));
            byte[] b2 = aa.b(new NewHeartBeatRspMessage().encode());
            aw.d("PushLog2828", "heartbeat fixedReplyContent is : " + au.f(b2));
            this.bR.setAolHeartbeat(i, b, b2);
            return true;
        } catch (Throwable e) {
            aw.d("PushLog2828", "initPushHeartBeatDataContent error :" + e.toString(), e);
            return false;
        }
    }

    public static synchronized PushBastet ac(Context context) {
        PushBastet pushBastet;
        synchronized (PushBastet.class) {
            if (bQ != null) {
                pushBastet = bQ;
            } else {
                bQ = new PushBastet(context);
                pushBastet = bQ;
            }
        }
        return pushBastet;
    }

    private boolean bT() {
        aw.d("PushLog2828", "initPushBastet");
        if (!bV() || !bU()) {
            return false;
        }
        if (ConnectEntityMode.N == ChannelMgr.aU()) {
            aw.i("PushLog2828", "initPushBastet, getCurConnectMode is Polling");
            return false;
        } else if (!bW()) {
            return false;
        } else {
            Socket socket = ChannelMgr.aW().getSocket();
            if (socket == null || !cb()) {
                return false;
            }
            try {
                this.bR = new HwBastet("PUSH_BASTET", socket, this.mHandler, this.mContext);
                if (this.bR.isBastetAvailable()) {
                    this.bR.reconnectSwitch(true);
                    return true;
                }
                aw.i("PushLog2828", "isBastetAvailable false, can't use bastet.");
                ca();
                return false;
            } catch (Throwable e) {
                aw.c("PushLog2828", "init bastet error", e);
                ca();
                return false;
            }
        }
    }

    private boolean bU() {
        boolean as = ae.l(this.mContext).as();
        aw.d("PushLog2828", "isPushServerAllowBastet: " + as);
        return as;
    }

    private boolean bV() {
        try {
            Class.forName("com.huawei.android.bastet.HwBastet");
            return true;
        } catch (ClassNotFoundException e) {
            aw.e("PushLog2828", "bastet not exist");
            return false;
        }
    }

    private boolean bW() {
        if (ChannelType.aH.equals(ChannelMgr.g(this.mContext).aY())) {
            return true;
        }
        aw.d("PushLog2828", "only ChannelType_Secure support bastet");
        return false;
    }

    private synchronized void bX() {
        try {
            if (!(this.mHandler == null || this.mHandler.getLooper() == null)) {
                this.mHandler.getLooper().quitSafely();
            }
            this.mHandler = null;
        } catch (Throwable e) {
            aw.d("PushLog2828", "PushBastetListener release error", e);
        }
    }

    private void bY() {
        aw.i("PushLog2828", "reConnectPush");
        ca();
        ChannelMgr.g(this.mContext).aV().close();
        PushService.a(new Intent("com.huawei.action.CONNECT_PUSHSRV").setPackage(this.mContext.getPackageName()));
    }

    private synchronized boolean cb() {
        boolean z = false;
        synchronized (this) {
            aw.d("PushLog2828", "initMsgHandler");
            try {
                if (!(this.mHandler == null || this.mHandler.getLooper() == null)) {
                    this.mHandler.getLooper().quitSafely();
                }
                HandlerThread handlerThread = new HandlerThread("bastetRspHandlerThread");
                handlerThread.start();
                int i = 0;
                while (!handlerThread.isAlive()) {
                    int i2 = i + 1;
                    try {
                        wait(10);
                        if (i2 % 100 == 0) {
                            aw.e("PushLog2828", "wait bastetRspHandlerThread start take time: " + (i2 * 10) + " ms");
                        }
                        if (i2 > 500) {
                            aw.e("PushLog2828", "reached the max retry times:500");
                            break;
                        }
                        i = i2;
                    } catch (Throwable e) {
                        aw.d("PushLog2828", "InterruptedException error", e);
                        i = i2;
                    }
                }
                if (handlerThread.getLooper() == null) {
                    aw.e("PushLog2828", "looper is null when initMsgHandler");
                } else {
                    this.mHandler = new bc(this, handlerThread.getLooper());
                    z = true;
                }
            } catch (Throwable e2) {
                aw.d("PushLog2828", "initMsgHandler error:" + e2.getMessage(), e2);
                ca();
            }
        }
        return z;
    }

    private void cd() {
        aw.d("PushLog2828", "enter clearBastetProxy!");
        if (this.bR == null) {
            aw.i("PushLog2828", "enter clearBastetProxy, mHwBastet is null");
            return;
        }
        try {
            Method declaredMethod = this.bR.getClass().getDeclaredMethod("clearBastetProxy", new Class[0]);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(this.bR, new Object[0]);
            aw.d("PushLog2828", "clearBastetProxy success!");
        } catch (Throwable e) {
            aw.d("PushLog2828", e.toString(), e);
        } catch (Throwable e2) {
            aw.d("PushLog2828", e2.toString(), e2);
        } catch (Throwable e22) {
            aw.d("PushLog2828", e22.toString(), e22);
        } catch (Throwable e222) {
            aw.d("PushLog2828", e222.toString(), e222);
        } catch (Throwable e2222) {
            aw.c("PushLog2828", e2222.toString(), e2222);
        }
    }

    public void a(BasteProxyStatus basteProxyStatus) {
        this.bS = basteProxyStatus;
    }

    public synchronized boolean bS() {
        boolean z = true;
        synchronized (this) {
            try {
                if (bZ()) {
                    aw.i("PushLog2828", "bastet has started, need not restart");
                } else if (bT()) {
                    if (a(3, 900000)) {
                        aw.i("PushLog2828", "startPushBastetProxy success");
                        a(BasteProxyStatus.bU);
                    }
                    aw.i("PushLog2828", "startPushBastetProxy failed");
                    z = false;
                } else {
                    aw.i("PushLog2828", "init push bastet failed!");
                    z = false;
                }
            } catch (Throwable e) {
                aw.d("PushLog2828", "startPushBastetProxy failed:" + e.getMessage(), e);
            }
        }
        return z;
    }

    public boolean bZ() {
        return this.bR != null && BasteProxyStatus.bU == this.bS;
    }

    public void ca() {
        aw.i("PushLog2828", "resetBastet");
        cd();
        this.bR = null;
        a(BasteProxyStatus.bV);
        bX();
        ChannelMgr.aW().c(false);
        bq.w(this.mContext, "com.huawei.android.push.intent.RESET_BASTET");
    }

    public long cc() {
        return 900000;
    }
}
