package tmsdkobf;

import java.util.ArrayList;
import java.util.List;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class gw {
    public String om;
    public List<gv> op;
    public List<a> pA;
    private String pB;
    public List<String> px;
    public List<String> py;
    public gv pz;

    /* compiled from: Unknown */
    public static class a {
        public byte[] pC;
        public byte[] pD;
    }

    public gv ax(int i) {
        for (gv gvVar : this.op) {
            if (gvVar.mID == i) {
                return gvVar;
            }
        }
        return null;
    }

    public String az(int i) {
        if (this.pB != null) {
            return this.pB;
        }
        if (this.py != null) {
            this.pB = (String) this.py.get(i);
        } else {
            try {
                if (this.pA != null && this.pA.size() > 0) {
                    this.pB = new String(TccCryptor.decrypt(((a) this.pA.get(i)).pD, null));
                }
            } catch (Exception e) {
                d.c("xx", e);
                return this.pB;
            }
        }
        return this.pB;
    }

    public List<String> bc() {
        if (this.px == null && this.pA != null) {
            try {
                List arrayList = new ArrayList();
                for (a aVar : this.pA) {
                    arrayList.add(new String(TccCryptor.decrypt(aVar.pC, null)));
                }
                this.px = arrayList;
            } catch (Exception e) {
                d.c("xx", e);
                return this.px;
            }
        }
        return this.px;
    }
}
