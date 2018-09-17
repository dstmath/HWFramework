package tmsdkobf;

import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.ManagerCreatorC;

/* compiled from: Unknown */
public class mc {
    private static ei c(py pyVar) {
        int i = 0;
        ei eiVar = new ei();
        eiVar.kJ = cr(pyVar.getPackageName());
        eiVar.name = cr(pyVar.getAppName());
        eiVar.version = cr(pyVar.getVersion());
        eiVar.kL = pyVar.hB();
        eiVar.iq = pyVar.hD();
        eiVar.iI = "" + pyVar.getUid();
        if (pyVar.hA()) {
            i = 1;
        }
        eiVar.fL = i;
        eiVar.jS = (int) pyVar.getSize();
        return eiVar;
    }

    private static String cr(String str) {
        return str != null ? str : "";
    }

    public static synchronized void eF() {
        synchronized (mc.class) {
            try {
                if (eG()) {
                    dr drVar = new dr();
                    drVar.iM = new ArrayList();
                    Iterator it = TMServiceFactory.getSystemInfoService().c(25, 2).iterator();
                    while (it.hasNext()) {
                        py pyVar = (py) it.next();
                        if (pyVar != null) {
                            el elVar = new el();
                            elVar.jH = c(pyVar);
                            drVar.iM.add(elVar);
                        }
                    }
                    if (((qt) ManagerCreatorC.getManager(qt.class)).a(drVar) == 0) {
                        fw.w().c(System.currentTimeMillis());
                    }
                    return;
                }
            } catch (Throwable th) {
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static boolean eG() {
        boolean z = true;
        if (!fw.w().D().booleanValue()) {
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis();
        long B = fw.w().B();
        if (!(currentTimeMillis <= B)) {
        }
        z = false;
        return z;
    }
}
