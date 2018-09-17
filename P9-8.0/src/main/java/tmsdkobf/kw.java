package tmsdkobf;

import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.ManagerCreatorC;

public class kw {
    private static et b(ov ovVar) {
        int i = 0;
        et etVar = new et();
        etVar.kH = bu(ovVar.getPackageName());
        etVar.name = bu(ovVar.getAppName());
        etVar.version = bu(ovVar.getVersion());
        etVar.kK = ovVar.getVersionCode();
        etVar.kJ = ovVar.hz();
        etVar.kG = "" + ovVar.getUid();
        if (ovVar.hx()) {
            i = 1;
        }
        etVar.ib = i;
        etVar.kR = (int) ovVar.getSize();
        return etVar;
    }

    private static String bu(String str) {
        return str != null ? str : "";
    }

    public static synchronized void dP() {
        synchronized (kw.class) {
            try {
                if (dQ()) {
                    em emVar = new em();
                    emVar.kr = new ArrayList();
                    Iterator it = TMServiceFactory.getSystemInfoService().f(25, 2).iterator();
                    while (it.hasNext()) {
                        ov ovVar = (ov) it.next();
                        if (ovVar != null) {
                            eu euVar = new eu();
                            euVar.kT = b(ovVar);
                            emVar.kr.add(euVar);
                        }
                    }
                    if (((ot) ManagerCreatorC.getManager(ot.class)).a(emVar) == 0) {
                        gf.S().b(System.currentTimeMillis());
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
            }
        }
    }

    static boolean dQ() {
        boolean z = true;
        boolean z2 = false;
        if (!gf.S().Z().booleanValue()) {
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis();
        long X = gf.S().X();
        if (X == 0) {
            gf.S().b(System.currentTimeMillis());
            return false;
        }
        if (!(currentTimeMillis <= X)) {
            long ah = gf.S().ah();
            if (!(ah >= 0)) {
                ah = 604800000;
            }
            if (currentTimeMillis - X >= ah) {
                z = false;
            }
            if (!z) {
                z2 = true;
            }
        }
        return z2;
    }
}
