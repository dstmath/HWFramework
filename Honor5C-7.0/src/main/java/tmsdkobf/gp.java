package tmsdkobf;

import java.util.List;
import tmsdk.common.TMServiceFactory;

/* compiled from: Unknown */
public class gp {
    private boolean pk;
    private qd pl;

    public gp() {
        this.pk = false;
        this.pl = TMServiceFactory.getSystemInfoService();
    }

    public boolean aL(String str) {
        return str != null ? this.pl.aC(str) : false;
    }

    public String aM(String str) {
        py b = this.pl.b(str, 1);
        return b == null ? null : b.getAppName();
    }

    public boolean aS() {
        return true;
    }

    public String c(List<String> list) {
        if (list == null) {
            return null;
        }
        for (String str : list) {
            if (aL(str)) {
                return str;
            }
        }
        return null;
    }

    public int d(List<String> list) {
        if (list == null) {
            return -1;
        }
        int i = 0;
        long j = -1;
        int i2 = -1;
        for (String aR : list) {
            long j2;
            int i3;
            long aR2 = gs.aW().aR(aR);
            if ((aR2 <= j ? 1 : null) == null) {
                j2 = aR2;
                i3 = i;
            } else {
                long j3 = j;
                i3 = i2;
                j2 = j3;
            }
            i++;
            int i4 = i3;
            j = j2;
            i2 = i4;
        }
        return i2;
    }

    public void init() {
        this.pk = true;
    }
}
