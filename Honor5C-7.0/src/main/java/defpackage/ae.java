package defpackage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr;
import com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr.RECONNECTEVENT;
import java.net.InetSocketAddress;
import java.util.Date;

/* renamed from: ae */
public class ae extends k {
    private static ae aO;
    private Thread aM;
    public boolean aN;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: ae.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: ae.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: ae.<clinit>():void");
    }

    private ae(Context context) {
        super(context);
        this.aM = null;
        this.aN = false;
        h();
    }

    private boolean b(k kVar) {
        if (kVar == null || !kVar.isValid()) {
            aw.e("PushLog2828", "in PushSrvInfo:trsRetInfo, trsRetInfo is null or invalid:" + kVar);
            return false;
        }
        aw.d("PushLog2828", "queryTrs success!");
        if (!a(kVar)) {
            aw.d("PushLog2828", "heart beat range change.");
            PushService.a(new Intent("com.huawei.android.push.intent.HEARTBEAT_RANGE_CHANGE"));
        }
        if (kVar.t.containsKey("USE_SSL")) {
            ag.a(null, new g("USE_SSL", Integer.class, Integer.valueOf(((Integer) kVar.t.get("USE_SSL")).intValue())));
        }
        if (!f(kVar.o())) {
            aw.i("PushLog2828", "belongId changed, need to reRegisterDeviceToken");
            ao.B(this.context);
        }
        this.t.putAll(kVar.t);
        a("pushSrvValidTime", (Object) Long.valueOf((s() * 1000) + System.currentTimeMillis()));
        ag.a(this.context, new g("queryTrsTimes", Integer.class, Integer.valueOf(0)));
        aw.d("PushLog2828", "write the lastQueryTRSsucc_time to the pushConfig.xml file ");
        ag.a(this.context, new g("lastQueryTRSsucc_time", Long.class, Long.valueOf(System.currentTimeMillis())));
        this.aN = false;
        this.t.remove("PushID");
        i();
        ReconnectMgr.s(this.context).a(this.context, RECONNECTEVENT.bn, new Bundle());
        PushService.a(new Intent("com.huawei.android.push.intent.TRS_QUERY_SUCCESS").putExtra("trs_result", kVar.toString()));
        return true;
    }

    private synchronized boolean bs() {
        boolean z;
        if (bt()) {
            aw.d("PushLog2828", " trsQuery thread already running, just wait!!");
            z = false;
        } else {
            this.aM = new af(this, "PushTRSQuery");
            this.aM.start();
            ag.a(this.context, new g("lastQueryTRSTime", Long.class, Long.valueOf(System.currentTimeMillis())));
            ag.a(this.context, new g("queryTrsTimes", Long.class, Long.valueOf(ag.a(this.context, "queryTrsTimes", 0) + 1)));
            z = true;
        }
        return z;
    }

    private synchronized boolean bt() {
        boolean z;
        z = this.aM != null && this.aM.isAlive();
        return z;
    }

    public static synchronized ae l(Context context) {
        ae aeVar;
        synchronized (ae.class) {
            if (aO == null) {
                aO = new ae(context);
            }
            aeVar = aO;
        }
        return aeVar;
    }

    public static void m(Context context) {
        if (aO != null) {
            aO.a("pushSrvValidTime", (Object) Integer.valueOf(0));
            aO.aN = true;
        }
    }

    public InetSocketAddress g(boolean z) {
        boolean i = i(z);
        if (!isValid() || i) {
            aw.i("PushLog2828", "in getPushSrvAddr, have no invalid addr");
            return null;
        }
        aw.d("PushLog2828", "return valid PushSrvAddr");
        return new InetSocketAddress(p(), q());
    }

    public InetSocketAddress h(boolean z) {
        boolean i = i(z);
        if (!am() || i) {
            aw.i("PushLog2828", "in getPollingAddr, have no invalid addr");
            return null;
        }
        aw.d("PushLog2828", "return valid PollingSrvAddr");
        return new InetSocketAddress(P(), Q());
    }

    public boolean i(boolean z) {
        if (bt()) {
            aw.i("PushLog2828", "trsQuery thread is running");
            return true;
        }
        long a = ag.a(this.context, "lastQueryTRSTime", 0);
        long a2 = ag.a(this.context, "lastQueryTRSsucc_time", 0);
        aw.i("PushLog2828", "isvalid:" + isValid() + " srvValidBefore:" + (getLong("pushSrvValidTime", Long.MAX_VALUE) - System.currentTimeMillis()) + " pushSrvNeedQueryTRS:" + this.aN);
        if (isValid()) {
            if (!this.aN && getLong("pushSrvValidTime", Long.MAX_VALUE) >= System.currentTimeMillis() && System.currentTimeMillis() > a2) {
                aw.i("PushLog2828", " need not query TRS");
                return false;
            } else if (this.aN && System.currentTimeMillis() - a < r() * 1000 && System.currentTimeMillis() > a) {
                aw.i("PushLog2828", " cannot query TRS in trsValid_min, pushSrvNeedQueryTRS, info:" + toString());
                return false;
            }
        }
        if (-1 == au.G(this.context)) {
            aw.i("PushLog2828", "in queryTRSInfo no network");
            return false;
        }
        if (z) {
            aw.i("PushLog2828", "Force to Connect TRS");
        } else {
            long currentTimeMillis = System.currentTimeMillis() - a2;
            if (currentTimeMillis <= 0 || currentTimeMillis >= r() * 1000) {
                a2 = D() * 1000;
                if (ag.a(this.context, "queryTrsTimes", 0) > G()) {
                    a2 = E() * 1000;
                }
                currentTimeMillis = System.currentTimeMillis() - a;
                if (currentTimeMillis > 0 && currentTimeMillis < a2) {
                    aw.i("PushLog2828", "can't connect TRS Service when the connectting time more later " + (a2 / 1000) + "sec than  last contectting time,lastQueryTRSTime =" + new Date(a));
                    return false;
                }
            }
            aw.i("PushLog2828", "can not contect TRS Service when  the connect more than " + r() + " sec last contected success time," + "lastQueryTRSsucc_time = " + new Date(a2));
            return false;
        }
        if (ag.a(this.context, "cloudpush_isNoDelayConnect", false) || ai.p(this.context)) {
            return bs();
        }
        aw.i("PushLog2828", "ConnectControlMgr.canQueryTRS is false");
        return false;
    }
}
