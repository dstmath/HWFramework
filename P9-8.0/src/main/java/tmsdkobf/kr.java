package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.qq.taf.jce.JceStruct;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Random;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.f;
import tmsdk.common.utils.i;
import tmsdk.common.utils.l;
import tmsdk.common.utils.s;

public class kr {
    static a xA = null;
    static volatile boolean xB = false;
    static volatile long xC = -1;
    static Object xy = new Object();
    static Object xz = new Object();

    static class a extends if implements tmsdkobf.oo.a {
        public static boolean xF = false;

        a() {
        }

        public void dC() {
            if (im.bG()) {
                im.bP();
                if (kr.br("FirstCheck") || (gf.S().ai().booleanValue() && i.K(TMSDKContext.getApplicaionContext()))) {
                    kr.dB();
                }
                kr.dy();
            }
            ll.aM(2);
            s.bW(1);
        }

        public void dD() {
        }

        public void doOnRecv(Context context, Intent intent) {
            if (intent != null) {
                int i = -1;
                String action = intent.getAction();
                f.d("cccccc", "check");
                if ("android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action)) {
                    i = 4;
                } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                    i = 3;
                } else if ("tmsdk.common.ccrreport".equals(action)) {
                    i = 1;
                }
                if (i != -1) {
                    if (im.bG()) {
                        kr.dz();
                    }
                    ll.aM(i);
                    s.bW(1);
                }
            }
        }

        public synchronized void k(Context context) {
            if (!xF) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.USER_PRESENT");
                intentFilter.setPriority(Integer.MAX_VALUE);
                context.registerReceiver(this, intentFilter);
                oo A = oo.A(context);
                if (A != null) {
                    A.a((tmsdkobf.oo.a) this);
                }
                xF = true;
            }
        }
    }

    public static void aD(int i) {
        gf.S().I(i);
    }

    static boolean br(String str) {
        long currentTimeMillis = System.currentTimeMillis();
        long W = gf.S().W();
        if ((currentTimeMillis <= W ? 1 : null) == null) {
            if ((currentTimeMillis - W < 86400000 ? 1 : null) != null) {
                Calendar instance = Calendar.getInstance();
                instance.set(11, 0);
                instance.set(12, 0);
                instance.set(13, 0);
                long timeInMillis = instance.getTimeInMillis() + (((long) dA()) * 1000);
                if ((timeInMillis <= currentTimeMillis ? 1 : null) == null) {
                    timeInMillis -= 86400000;
                }
                if ((W > timeInMillis ? 1 : null) != null) {
                    return false;
                }
            }
        }
        if ((Math.abs(currentTimeMillis - W) < 86400000 ? 1 : null) != null) {
            return false;
        }
        return true;
    }

    static int dA() {
        int V = gf.S().V();
        if (V > 0) {
            return V;
        }
        V = p(1, 20);
        aD(V);
        return V;
    }

    private static void dB() {
        synchronized (xz) {
            if ((System.currentTimeMillis() - xC >= 600000 ? 1 : null) == null) {
                return;
            }
            xC = System.currentTimeMillis();
            ((ki) fj.D(4)).addTask(new Runnable() {
                public void run() {
                    if (!ll.n(TMSDKContext.getApplicaionContext())) {
                        kt.aE(1320026);
                    } else if (ll.o(TMSDKContext.getApplicaionContext())) {
                        kt.aE(1320024);
                    } else {
                        kt.aE(1320025);
                    }
                    if (gf.S().aj().booleanValue()) {
                        li.ey().C(-1);
                    }
                    kr.p(false);
                    kw.dP();
                    ku.dO();
                    kx.dR().dS();
                }
            }, "xxx");
        }
    }

    private static void dy() {
        if (!xB) {
            xB = true;
            ((ki) fj.D(4)).addTask(new Runnable() {
                public void run() {
                    try {
                        la.el();
                    } catch (Throwable th) {
                    }
                    kr.xB = false;
                }
            }, "bd");
        }
    }

    /* JADX WARNING: Missing block: B:6:0x0010, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void dz() {
        synchronized (xy) {
            dy();
            if (br("FirstCheck") && i.iE()) {
                dB();
                gf.S().f(Boolean.valueOf(true));
            }
        }
    }

    public static void init() {
        li.ey();
        if (xA == null) {
            xA = new a();
            xA.k(TMSDKContext.getApplicaionContext());
        }
        s.bW(2);
    }

    static int p(int i, int i2) {
        int i3 = i2 - i;
        if (i3 < 0) {
            return -1;
        }
        long j = 0;
        try {
            j = Long.parseLong(l.L(TMSDKContext.getApplicaionContext()));
        } catch (Throwable th) {
        }
        Random random = new Random();
        random.setSeed(((System.currentTimeMillis() + ((long) System.identityHashCode(random))) + ((long) System.identityHashCode(gf.S()))) + j);
        return (((((int) (random.nextDouble() * ((double) i3))) + i) * 3600) + (((int) (random.nextDouble() * 60.0d)) * 60)) + ((int) (random.nextDouble() * 60.0d));
    }

    public static synchronized void p(boolean z) {
        synchronized (kr.class) {
            try {
                q(z);
            } catch (Throwable th) {
            }
        }
    }

    private static void q(final boolean z) {
        final kt dE = kt.dE();
        if (gf.S().Y().booleanValue()) {
            Iterator it;
            b bVar;
            int length;
            int i = 0;
            JceStruct aVar = new a();
            aVar.a = new ArrayList();
            int i2 = 0;
            ArrayList dL = dE.dL();
            if (dL != null) {
                it = dL.iterator();
                while (it.hasNext()) {
                    bVar = (b) it.next();
                    length = bVar.toByteArray().length;
                    if (length > 1024000) {
                        kt.aH(bVar.c);
                    }
                    if (i + length >= 1024000) {
                        it.remove();
                    } else {
                        aVar.a.add(bVar);
                        i += length;
                        i2++;
                    }
                }
            }
            dL = dE.dN();
            if (dL != null) {
                it = dL.iterator();
                while (it.hasNext()) {
                    bVar = (b) it.next();
                    length = bVar.toByteArray().length;
                    if (length > 1024000) {
                        kt.aI(bVar.c);
                    }
                    if (i + length >= 1024000) {
                        it.remove();
                    } else {
                        aVar.a.add(bVar);
                        i += length;
                        i2++;
                    }
                }
            }
            dL = dE.dM();
            if (dL != null) {
                it = dL.iterator();
                while (it.hasNext()) {
                    bVar = (b) it.next();
                    length = bVar.toByteArray().length;
                    if (length > 1024000) {
                        kt.aF(bVar.c);
                    }
                    if (i + length >= 1024000) {
                        it.remove();
                    } else {
                        aVar.a.add(bVar);
                        i += length;
                        i2++;
                    }
                }
            }
            WeakReference a = im.bK().a(3651, aVar, null, 2, new jy() {
                public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                    if (i3 == 0) {
                        dE.dI();
                        dE.dK();
                        dE.dJ();
                        if (!z) {
                            gf.S().a(System.currentTimeMillis());
                        }
                    }
                }
            });
            return;
        }
        dE.dI();
        dE.dK();
        dE.dJ();
    }
}
