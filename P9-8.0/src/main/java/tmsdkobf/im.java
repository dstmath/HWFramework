package tmsdkobf;

import com.qq.taf.jce.JceStruct;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import tmsdk.common.IDualPhoneInfoFetcher;
import tmsdk.common.TMSDKContext;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.f;

public class im {
    private static List<WeakReference<a>> rC = new LinkedList();
    private static in rD;
    public static volatile qc rE = null;
    private static volatile boolean rF = false;
    private static boolean rG = true;
    public static IDualPhoneInfoFetcher rH = null;

    public interface a {
    }

    public static synchronized void a(a aVar) {
        synchronized (im.class) {
            c(rC);
            rC.add(new WeakReference(aVar));
        }
    }

    public static boolean bG() {
        return rG || gf.S().ac().booleanValue();
    }

    public static boolean bH() {
        bI();
        return rF;
    }

    public static void bI() {
        if (!rF) {
            rF = ma.f(TMSDKContext.getApplicaionContext(), "Tmsdk-2.0.10-mfr");
            if (rF) {
                try {
                    TMSDKContext.registerNatives(0, TccCryptor.class);
                } catch (Throwable th) {
                    th.printStackTrace();
                    rF = false;
                }
            }
            f.h("demo", "mIsSdkLibraryLoaded =" + rF);
        }
    }

    public static in bJ() {
        if (rD == null) {
            Class cls = im.class;
            synchronized (im.class) {
                if (rD == null) {
                    rD = new in(bL(), "com.tmsdk.common");
                }
            }
        }
        return rD;
    }

    public static ob bK() {
        return (ob) fj.D(5);
    }

    private static long bL() {
        int i = 1 != bN() ? 2 != bN() ? 3 : 2 : 1;
        return ig.getIdent(i, 4294967296L);
    }

    public static qc bM() {
        return rE;
    }

    public static int bN() {
        return 0;
    }

    public static IDualPhoneInfoFetcher bO() {
        return rH;
    }

    public static void bP() {
        f.h("ImsiChecker", "[API]onImsiChanged");
        if (new ge().R()) {
            jn.cx().onImsiChanged();
            bK().gm();
        }
    }

    public static String bQ() {
        return ir.bU().bQ();
    }

    private static <T> void c(List<WeakReference<T>> list) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            if (((WeakReference) it.next()).get() == null) {
                it.remove();
            }
        }
    }

    public static void requestDelUserData() {
        if ("874556".equals(bQ())) {
            ob bK = bK();
            JceStruct czVar = new cz();
            ArrayList arrayList = new ArrayList();
            arrayList.add(new Integer(1));
            czVar.gh = arrayList;
            bK.a(4048, czVar, new da(), 0, new jy() {
                public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                    if (i3 != 0) {
                        f.h("TMSDKContextInternal", "cmdId return :" + i2 + " code : " + i3);
                    } else if (jceStruct != null) {
                        String str = "TMSDKContextInternal";
                        f.h(str, "cmdId sucessed :" + i2 + " result: " + ((da) jceStruct).result);
                    }
                }
            });
        }
        kt.saveActionData(1320080);
    }

    public static void setAutoConnectionSwitch(boolean z) {
        rG = z;
    }

    public static void setDualPhoneInfoFetcher(IDualPhoneInfoFetcher iDualPhoneInfoFetcher) {
        f.h("TMSDKContextInternal", "setDualPhoneInfoFetcher:[" + iDualPhoneInfoFetcher + "]");
        f.h("TrafficCorrection", "setDualPhoneInfoFetcher:[" + iDualPhoneInfoFetcher + "]");
        rH = iDualPhoneInfoFetcher;
    }
}
