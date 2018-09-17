package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.utils.f;
import tmsdk.common.utils.h;

/* compiled from: Unknown */
public class ly {
    static a Ak;
    static volatile boolean Al;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.ly.4 */
    static class AnonymousClass4 extends tmsdkobf.lm.a {
        final /* synthetic */ ma Am;
        final /* synthetic */ long An;

        AnonymousClass4(ma maVar, long j) {
            this.Am = maVar;
            this.An = j;
        }

        public void a(int i, qs qsVar) {
            if (i == 0) {
                this.Am.ey();
                this.Am.eC();
                this.Am.eA();
                fw.w().b(this.An);
            }
        }
    }

    /* compiled from: Unknown */
    static class a extends jj implements tmsdkobf.ps.a {
        public static boolean Ao;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ly.a.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ly.a.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ly.a.<clinit>():void");
        }

        a() {
        }

        public void cn() {
        }

        public void co() {
            if (jq.cq()) {
                ly.eo();
            }
            mi.bA(2);
        }

        public void doOnRecv(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                int i = ("android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action)) ? 4 : !"android.intent.action.USER_PRESENT".equals(action) ? !"tmsdk.common.ccrreport".equals(action) ? -1 : 1 : 3;
                if (i != -1) {
                    if (jq.cq()) {
                        ly.ep();
                    }
                    mi.bA(i);
                }
            }
        }

        public synchronized void h(Context context) {
            if (!Ao) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.USER_PRESENT");
                intentFilter.setPriority(UrlCheckType.UNKNOWN);
                context.registerReceiver(this, intentFilter);
                ps t = ps.t(context);
                if (t != null) {
                    t.b(this);
                }
                Ao = true;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ly.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ly.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ly.<clinit>():void");
    }

    public static void bw(int i) {
        fw.w().ah(i);
    }

    static boolean co(String str) {
        long currentTimeMillis = System.currentTimeMillis();
        long A = fw.w().A();
        if (currentTimeMillis <= A) {
            if (!(Math.abs(currentTimeMillis - A) < 86400000)) {
                return true;
            }
        }
        if (!(currentTimeMillis - A < 86400000)) {
            return true;
        }
        Calendar instance = Calendar.getInstance();
        instance.set(11, 0);
        instance.set(12, 0);
        instance.set(13, 0);
        long es = (((long) es()) * 1000) + instance.getTimeInMillis();
        if (!(es <= currentTimeMillis)) {
            es -= 86400000;
        }
        if (!(A > es)) {
            return true;
        }
        return false;
    }

    public static void en() {
        if (Ak == null) {
            Ak = new a();
            Ak.h(TMSDKContext.getApplicaionContext());
        }
        na.s("ccrService", "initAtBg()");
    }

    public static void eo() {
        if (!Al) {
            if (!co("FirstCheck")) {
                if (f.B(TMSDKContext.getApplicaionContext())) {
                    ((lq) fe.ad(4)).a(new Runnable() {
                        public void run() {
                            if (!ly.Al) {
                                md.eH().eI();
                            }
                        }
                    }, "withcheck");
                }
            } else if (f.iu()) {
                ((lq) fe.ad(4)).a(new Runnable() {
                    public void run() {
                        if (!ly.Al) {
                            ly.Al = true;
                            ly.er();
                            me.eK().ac(-1);
                            mc.eF();
                            md.eH().eI();
                            mb.eE();
                            ly.Al = false;
                        }
                    }
                }, "withcheck");
            }
        }
    }

    public static void ep() {
        if (!Al && co("FirstCheck") && f.iu()) {
            ((lq) fe.ad(4)).a(new Runnable() {
                public void run() {
                    if (!ly.Al) {
                        ly.Al = true;
                        ly.er();
                        me.eK().ac(-1);
                        mc.eF();
                        md.eH().eI();
                        mb.eE();
                        ly.Al = false;
                    }
                }
            }, "WithoutCheck");
        }
    }

    public static void eq() {
        if (jq.cq()) {
            ma.bx(29987);
        } else {
            ma.bx(29988);
        }
    }

    private static synchronized void er() {
        synchronized (ly.class) {
            try {
                ma et = ma.et();
                if (fw.w().C().booleanValue()) {
                    if (!mi.k(TMSDKContext.getApplicaionContext())) {
                        ma.bx(1320026);
                    } else if (mi.l(TMSDKContext.getApplicaionContext())) {
                        ma.bx(1320024);
                    } else {
                        ma.bx(1320025);
                    }
                    ArrayList arrayList = new ArrayList();
                    Collection ex = et.ex();
                    if (ex != null) {
                        arrayList.addAll(ex);
                    }
                    ex = et.eB();
                    if (ex != null) {
                        arrayList.addAll(ex);
                    }
                    long currentTimeMillis = System.currentTimeMillis();
                    ex = et.ez();
                    if (ex != null) {
                        arrayList.addAll(ex);
                    }
                    if (arrayList.size() > 0) {
                        jq.cu().a(v(arrayList), new AnonymousClass4(et, currentTimeMillis));
                        return;
                    }
                    return;
                }
                et.ey();
                et.eC();
                et.eA();
            } catch (Throwable th) {
            }
        }
    }

    static int es() {
        int z = fw.w().z();
        if (z > 0) {
            return z;
        }
        z = m(1, 20);
        bw(z);
        return z;
    }

    static int m(int i, int i2) {
        int i3 = i2 - i;
        if (i3 < 0) {
            return -1;
        }
        long j = 0;
        try {
            j = Long.parseLong(h.C(TMSDKContext.getApplicaionContext()));
        } catch (Throwable th) {
        }
        Random random = new Random();
        random.setSeed(j + ((System.currentTimeMillis() + ((long) System.identityHashCode(random))) + ((long) System.identityHashCode(fw.w()))));
        return (((((int) (random.nextDouble() * ((double) i3))) + i) * 3600) + (((int) (random.nextDouble() * 60.0d)) * 60)) + ((int) (random.nextDouble() * 60.0d));
    }

    static qs v(ArrayList<dx> arrayList) {
        qo ic = ((qt) ManagerCreatorC.getManager(qt.class)).ic();
        qs qsVar = new qs(17, new qq("report", "reportSoftUsageInfo"));
        HashMap hashMap = new HashMap(2);
        hashMap.put("suikey", ic.hP());
        hashMap.put("vecsui", arrayList);
        qsVar.Ky = hashMap;
        return qsVar;
    }
}
