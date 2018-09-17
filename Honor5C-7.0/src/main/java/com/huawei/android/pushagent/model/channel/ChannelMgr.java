package com.huawei.android.pushagent.model.channel;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.model.channel.entity.ConnectEntity;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;
import defpackage.ae;
import defpackage.ag;
import defpackage.au;
import defpackage.aw;
import defpackage.bq;
import defpackage.g;
import defpackage.q;
import defpackage.s;
import defpackage.w;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ChannelMgr {
    private static ChannelMgr L;
    private ConnectEntityMode J;
    private ConnectEntity[] K;
    private Context context;

    public enum ConnectEntityMode {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.pushagent.model.channel.ChannelMgr.ConnectEntityMode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.pushagent.model.channel.ChannelMgr.ConnectEntityMode.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.pushagent.model.channel.ChannelMgr.ConnectEntityMode.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.pushagent.model.channel.ChannelMgr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.pushagent.model.channel.ChannelMgr.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.pushagent.model.channel.ChannelMgr.<clinit>():void");
    }

    private ChannelMgr(Context context) {
        this.J = ConnectEntityMode.N;
        this.K = new ConnectEntity[ConnectEntityMode.values().length];
        this.context = context;
    }

    public static ConnectEntityMode aU() {
        return g(null).J;
    }

    public static ConnectEntity aW() {
        return g(null).K[ConnectEntityMode.M.ordinal()];
    }

    public static ConnectEntity aX() {
        return g(null).K[ConnectEntityMode.N.ordinal()];
    }

    public static synchronized ChannelMgr g(Context context) {
        ChannelMgr channelMgr;
        synchronized (ChannelMgr.class) {
            if (L != null) {
                channelMgr = L;
            } else if (context == null) {
                aw.e("PushLog2828", "when init ChannelMgr g_channelMgr and context all null!!");
                channelMgr = null;
            } else {
                L = new ChannelMgr(context);
                L.init();
                channelMgr = L;
            }
        }
        return channelMgr;
    }

    public static q h(Context context) {
        return g(context).aV().S;
    }

    private static void i(Context context) {
        aw.d("PushLog2828", "enter ConnectMgrProcessor:cancelDelayAlarm");
        bq.w(context, "com.huawei.action.CONNECT_PUSHSRV");
        bq.w(context, "com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT");
        bq.h(context, new Intent("com.huawei.intent.action.PUSH").putExtra("EXTRA_INTENT_TYPE", "com.huawei.android.push.intent.HEARTBEAT_REQ").putExtra("heartbeat_interval", 2592000000L).setPackage(context.getPackageName()));
    }

    private boolean init() {
        aw.d("PushLog2828", "begin to init ChannelMgr");
        int a = ag.a(this.context, "curConnectEntity", ConnectEntityMode.N.ordinal());
        aw.d("PushLog2828", "in cfg curConEntity:" + a);
        if (a >= 0 && a < ConnectEntityMode.values().length) {
            this.J = ConnectEntityMode.values()[a];
        }
        if (ConnectEntityMode.N == this.J && !ae.l(this.context).am() && ae.l(this.context).al()) {
            this.J = ConnectEntityMode.M;
        }
        this.K[ConnectEntityMode.M.ordinal()] = new w(null, this.context);
        this.K[ConnectEntityMode.N.ordinal()] = new s(null, this.context);
        return true;
    }

    public void a(ConnectEntityMode connectEntityMode) {
        this.J = connectEntityMode;
        if (ConnectEntityMode.N == connectEntityMode && !ae.l(this.context).am() && ae.l(this.context).al()) {
            connectEntityMode = ConnectEntityMode.M;
        }
        ag.a(this.context, new g("curConnectEntity", Integer.class, Integer.valueOf(connectEntityMode.ordinal())));
    }

    public void a(ConnectEntityMode connectEntityMode, boolean z) {
        aw.e("PushLog2828", "enter ChannelMgr:connect(entity" + connectEntityMode + ", forceCon" + z + ")");
        if (connectEntityMode != null) {
            try {
                this.K[connectEntityMode.ordinal()].a(z);
                return;
            } catch (Throwable e) {
                aw.d("PushLog2828", e.toString(), e);
                return;
            }
        }
        aw.e("PushLog2828", "entityMode is invalid!!");
    }

    public List aS() {
        List linkedList = new LinkedList();
        for (ConnectEntity connectEntity : this.K) {
            if (connectEntity.S.bd() != null) {
                linkedList.add(connectEntity.S.bd());
            }
        }
        return linkedList;
    }

    public void aT() {
        i(this.context);
        for (ConnectEntity close : this.K) {
            close.close();
        }
    }

    public ConnectEntity aV() {
        aw.d("PushLog2828", "enter getCurConnetEntity(curConnectType:" + this.J + ", ordinal:" + this.J.ordinal() + " curConnect:" + this.K[this.J.ordinal()].getClass().getSimpleName() + ")");
        if (ConnectEntityMode.N == this.J && !ae.l(this.context).am() && ae.l(this.context).al()) {
            aw.d("PushLog2828", "polling srv is not ready, push is ok, so change it to Push");
            this.J = ConnectEntityMode.M;
        }
        return this.K[this.J.ordinal()];
    }

    public ChannelType aY() {
        ConnectEntity aV = aV();
        if (aV == null) {
            aw.e("PushLog2828", "getCurrentChannelType:currentConnectEntity is null");
            return null;
        } else if (aV.R != null) {
            return aV().R.bq();
        } else {
            aw.e("PushLog2828", "channel is null");
            return null;
        }
    }

    public void c(Intent intent) {
        String action = intent.getAction();
        String stringExtra = intent.getStringExtra("EXTRA_INTENT_TYPE");
        if ("com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT".equals(action)) {
            aw.i("PushLog2828", "time out for wait heartbeat so reconnect");
            h(this.context).f(true);
            Socket socket = aV().getSocket();
            boolean at = ae.l(this.context).at();
            if (socket != null && at) {
                try {
                    aw.d("PushLog2828", "setSoLinger 0 when close socket after heartbeat timeout");
                    socket.setSoLinger(true, 0);
                } catch (Throwable e) {
                    aw.d("PushLog2828", e.toString(), e);
                }
            }
            aV().close();
            if (-1 != au.G(this.context) && aU() == ConnectEntityMode.M) {
                try {
                    aV().a(false);
                } catch (Throwable e2) {
                    aw.d("PushLog2828", e2.toString(), e2);
                }
            }
        } else if ("com.huawei.intent.action.PUSH".equals(action) && "com.huawei.android.push.intent.HEARTBEAT_REQ".equals(stringExtra)) {
            if (-1 != au.G(this.context)) {
                ConnectEntity aV = aV();
                if (aV.hasConnection()) {
                    aV.S.d(intent.getBooleanExtra("isHeartbeatReq", true));
                    aV.S.bh();
                    return;
                }
                PushService.a(new Intent("com.huawei.action.CONNECT_PUSHSRV").setPackage(this.context.getPackageName()));
                return;
            }
            aw.e("PushLog2828", "when send heart beat, not net work");
            h(this.context).bc();
        } else if (!"android.intent.action.TIME_SET".equals(action) && !"android.intent.action.TIMEZONE_CHANGED".equals(action)) {
        } else {
            if (aV().hasConnection()) {
                h(this.context).d(false);
                h(this.context).bh();
            } else if (-1 != au.G(this.context)) {
                aw.d("PushLog2828", "received " + action + ", but not Connect, go to connect!");
                PushService.a(new Intent("com.huawei.action.CONNECT_PUSHSRV"));
            } else {
                aw.i("PushLog2828", "no net work, when recevice :" + action + ", do nothing");
            }
        }
    }

    public void g(long j) {
        aw.i("PushLog2828", "next connect pushsvr will be after " + j);
        Intent intent = new Intent("com.huawei.action.CONNECT_PUSHSRV");
        intent.setPackage(this.context.getPackageName());
        bq.b(this.context, intent, j);
    }
}
