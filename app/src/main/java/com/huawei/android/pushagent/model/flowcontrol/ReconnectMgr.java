package com.huawei.android.pushagent.model.flowcontrol;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.PushException.ErrorType;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.utils.bastet.PushBastet;
import com.huawei.bd.Reporter;
import defpackage.ae;
import defpackage.ag;
import defpackage.aj;
import defpackage.ak;
import defpackage.aw;
import defpackage.bt;
import defpackage.g;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class ReconnectMgr {
    private static int bb;
    private static long bc;
    private static long bd;
    private static long be;
    private static ReconnectMgr bf;
    private int bg;
    private ArrayList bh;

    public enum RECONNECTEVENT {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr.RECONNECTEVENT.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr.RECONNECTEVENT.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr.RECONNECTEVENT.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr.<clinit>():void");
    }

    private ReconnectMgr() {
        this.bg = 0;
        this.bh = new ArrayList();
    }

    private void a(Context context, boolean z) {
        ak akVar;
        aw.d("PushLog2828", "save connection info " + z);
        long currentTimeMillis = System.currentTimeMillis();
        Collection arrayList = new ArrayList();
        Iterator it = this.bh.iterator();
        while (it.hasNext()) {
            akVar = (ak) it.next();
            if (currentTimeMillis < akVar.bE() || currentTimeMillis - akVar.bE() > bc) {
                arrayList.add(akVar);
            }
        }
        if (!arrayList.isEmpty()) {
            aw.d("PushLog2828", "some connection info is expired:" + arrayList.size());
            this.bh.removeAll(arrayList);
        }
        akVar = new ak();
        akVar.j(z);
        akVar.j(System.currentTimeMillis());
        if (this.bh.size() < bb) {
            this.bh.add(akVar);
        } else {
            this.bh.remove(0);
            this.bh.add(akVar);
        }
        String str = "|";
        StringBuffer stringBuffer = new StringBuffer();
        Iterator it2 = this.bh.iterator();
        while (it2.hasNext()) {
            stringBuffer.append(((ak) it2.next()).toString());
            stringBuffer.append(str);
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        new bt(context, "PushConnectControl").f("connectPushSvrInfos", stringBuffer.toString());
    }

    private void b(Context context, boolean z) {
        aw.d("PushLog2828", "set bad network mode " + z);
        ag.a(context, new g("isBadNetworkMode", Boolean.class, Boolean.valueOf(z)));
    }

    private boolean bB() {
        if (this.bh.size() < bb) {
            aw.d("PushLog2828", "total connect times is less than " + bb);
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis();
        Iterator it = this.bh.iterator();
        int i = 0;
        while (it.hasNext()) {
            ak akVar = (ak) it.next();
            int i2 = (currentTimeMillis <= akVar.bE() || currentTimeMillis - akVar.bE() >= bc) ? i : i + 1;
            i = i2;
        }
        aw.d("PushLog2828", "connect times in last " + bc + " is " + i + ", limits is " + bb);
        return i >= bb;
    }

    private void bC() {
        this.bg = 0;
    }

    private void bD() {
        this.bg++;
    }

    public static synchronized ReconnectMgr s(Context context) {
        ReconnectMgr reconnectMgr;
        synchronized (ReconnectMgr.class) {
            if (bf == null) {
                bf = new ReconnectMgr();
            }
            if (bf.bh.isEmpty()) {
                bf.t(context);
            }
            reconnectMgr = bf;
        }
        return reconnectMgr;
    }

    private void t(Context context) {
        int i = 0;
        bb = ae.l(context).ag();
        bc = ae.l(context).af();
        bd = ae.l(context).aq();
        be = ae.l(context).ap();
        String string = new bt(context, "PushConnectControl").getString("connectPushSvrInfos");
        if (!TextUtils.isEmpty(string)) {
            aw.d("PushLog2828", "connectPushSvrInfos is " + string);
            for (String str : string.split("\\|")) {
                ak akVar = new ak();
                if (akVar.load(str)) {
                    this.bh.add(akVar);
                }
            }
        }
        Collections.sort(this.bh);
        if (this.bh.size() > bb) {
            Collection arrayList = new ArrayList();
            int size = this.bh.size() - bb;
            while (i < size) {
                arrayList.add(this.bh.get(i));
                i++;
            }
            this.bh.removeAll(arrayList);
        }
    }

    private void u(Context context) {
        if (!y(context)) {
            aw.d("PushLog2828", "It is not bad network mode, do nothing");
        } else if (this.bh.isEmpty()) {
            b(context, false);
        } else {
            ak akVar = (ak) this.bh.get(this.bh.size() - 1);
            if (akVar.bF()) {
                aw.d("PushLog2828", "last connection is success");
                long currentTimeMillis = System.currentTimeMillis();
                long bE = akVar.bE();
                if (currentTimeMillis - bE > bd || currentTimeMillis < bE) {
                    aw.d("PushLog2828", bd + " has passed since last connect");
                    b(context, false);
                    return;
                }
                aw.d("PushLog2828", "connection keep too short , still in bad network mode");
                return;
            }
            aw.d("PushLog2828", "last connection result is false , still in bad network mode");
        }
    }

    private long w(Context context) {
        if (this.bh.isEmpty()) {
            aw.d("PushLog2828", "first connection, return 0");
            return 0;
        }
        long x;
        long B;
        if (!ag.a(context, "cloudpush_isNoDelayConnect", false)) {
            if (((long) this.bg) != ae.l(context).F()) {
                switch (this.bg) {
                    case 0:
                        x = 1000 * ae.l(context).x();
                        break;
                    case Reporter.ACTIVITY_CREATE /*1*/:
                        x = 1000 * ae.l(context).y();
                        break;
                    case Reporter.ACTIVITY_RESUME /*2*/:
                        x = 1000 * ae.l(context).z();
                        break;
                    case Reporter.ACTIVITY_PAUSE /*3*/:
                        x = 1000 * ae.l(context).A();
                        break;
                    default:
                        B = 1000 * ae.l(context).B();
                        ae.l(context).aN = true;
                        x = B;
                        break;
                }
            }
            ae.l(context).aN = true;
            x = 1000 * ae.l(context).B();
        } else {
            x = 1000;
        }
        long currentTimeMillis = System.currentTimeMillis();
        B = ((ak) this.bh.get(this.bh.size() - 1)).bj;
        if (currentTimeMillis < B) {
            aw.d("PushLog2828", "now is less than last connect time");
            B = 0;
        } else {
            B = Math.max((B + x) - currentTimeMillis, 0);
        }
        aw.i("PushLog2828", "after getConnectPushSrvInterval:" + B + " ms, connectTimes:" + this.bg);
        return B;
    }

    private long x(Context context) {
        if (bB()) {
            b(context, true);
        }
        boolean y = y(context);
        aw.d("PushLog2828", "bad network mode is " + y);
        if (!y || this.bh.isEmpty()) {
            return 0;
        }
        long currentTimeMillis = System.currentTimeMillis();
        long b = ((ak) this.bh.get(this.bh.size() - 1)).bj;
        if (currentTimeMillis < b) {
            aw.d("PushLog2828", "now is less than last connect time");
            b = 0;
        } else {
            b = Math.max((b + be) - currentTimeMillis, 0);
        }
        aw.d("PushLog2828", "It is in bad network mode, connect limit interval is " + b);
        return b;
    }

    private boolean y(Context context) {
        return ag.a(context, "isBadNetworkMode", false);
    }

    public void a(Context context, RECONNECTEVENT reconnectevent, Bundle bundle) {
        aw.d("PushLog2828", "receive reconnectevent:" + reconnectevent);
        switch (aj.bi[reconnectevent.ordinal()]) {
            case Reporter.ACTIVITY_CREATE /*1*/:
                bC();
            case Reporter.ACTIVITY_RESUME /*2*/:
                bC();
            case Reporter.ACTIVITY_PAUSE /*3*/:
                ErrorType errorType = ErrorType.u;
                u(context);
                if (bundle.containsKey("errorType")) {
                    errorType = (ErrorType) bundle.getSerializable("errorType");
                    if (ErrorType.w == errorType) {
                        a(context, false);
                    } else {
                        aw.d("PushLog2828", "socket close not caused by connect error, do not need save connection info");
                    }
                } else {
                    aw.d("PushLog2828", "socket close not caused by pushException");
                }
                bD();
                boolean bZ = PushBastet.ac(context).bZ();
                if (!bZ || (bZ && ErrorType.x != r0)) {
                    aw.i("PushLog2828", "socket closed, set alarm to reconnect srv");
                    ChannelMgr.g(context).g(v(context));
                }
            case Reporter.ACTIVITY_DESTROY /*4*/:
                bC();
                a(context, true);
            default:
        }
    }

    public long v(Context context) {
        return Math.max(w(context), x(context));
    }
}
