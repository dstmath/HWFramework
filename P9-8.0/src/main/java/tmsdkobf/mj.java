package tmsdkobf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tmsdkobf.im.a;

public final class mj implements a {
    private static volatile mj zQ;
    private static final String[] zR = new String[]{"phone", "phone1", "phone2", "phoneEX"};
    private List<ly> zS = new ArrayList();
    private List zT = new ArrayList(2);

    private mj() {
        eP();
    }

    public static synchronized mj eO() {
        mj mjVar;
        synchronized (mj.class) {
            if (zQ == null) {
                zQ = new mj();
            }
            mjVar = zQ;
        }
        return mjVar;
    }

    private boolean eP() {
        if (this.zS.size() == 0) {
            synchronized (this.zS) {
                if (this.zS.size() == 0) {
                    for (String lzVar : eQ()) {
                        this.zS.add(new lz(lzVar));
                    }
                }
            }
        }
        return this.zS.size() > 0;
    }

    public static final List<String> eQ() {
        List list = null;
        qc qcVar = im.rE;
        if (qcVar != null) {
            list = qcVar.ip();
        }
        if (list == null) {
            list = Arrays.asList(zR);
        }
        List arrayList = new ArrayList();
        for (String str : r1) {
            if (mi.checkService(str) != null) {
                arrayList.add(str);
            }
        }
        return arrayList;
    }

    public boolean endCall() {
        qc qcVar = im.rE;
        if (!eP()) {
            return false;
        }
        boolean z = false;
        if (qcVar != null && qcVar.iu()) {
            for (ly lyVar : this.zS) {
                if (lyVar.aP(0)) {
                    z = true;
                }
                if (lyVar.aP(1)) {
                    z = true;
                }
            }
        } else {
            for (ly endCall : this.zS) {
                if (endCall.endCall()) {
                    z = true;
                }
            }
        }
        return z;
    }
}
