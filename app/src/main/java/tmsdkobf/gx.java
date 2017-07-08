package tmsdkobf;

import java.util.HashSet;
import java.util.Set;

/* compiled from: Unknown */
public class gx implements Comparable<gx> {
    public String mName;
    public String mPkg;
    public long mTotalSize;
    public int pE;
    public int pF;
    public String pG;
    public boolean pH;
    public Set<String> pI;
    public Set<String> pJ;
    public boolean pK;
    public boolean pL;

    public gx() {
        this.pE = 0;
        this.pH = false;
        this.pK = false;
    }

    public int a(gx gxVar) {
        long j = this.mTotalSize - gxVar.mTotalSize;
        return ((j > 0 ? 1 : (j == 0 ? 0 : -1)) <= 0 ? 1 : 0) == 0 ? -1 : j == 0 ? 0 : 1;
    }

    public /* synthetic */ int compareTo(Object obj) {
        return a((gx) obj);
    }

    public String e(String str, String str2) {
        if (this.pI == null) {
            this.pI = new HashSet();
        }
        this.pI.add(str);
        if (this.pJ == null) {
            this.pJ = new HashSet();
        }
        if (str2 != null) {
            this.pJ.add(str + str2);
            return str + str2;
        }
        this.pJ.add(str);
        return str;
    }
}
