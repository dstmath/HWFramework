package tmsdkobf;

import java.util.List;
import java.util.Map;
import tmsdk.common.TMServiceFactory;

public class rj {
    private boolean Ac = false;
    private pa Pp = TMServiceFactory.getSystemInfoService();

    public String J(List<String> list) {
        if (list == null) {
            return null;
        }
        for (String str : list) {
            if (dk(str)) {
                return str;
            }
        }
        return null;
    }

    public int K(List<String> list) {
        if (list == null) {
            return -1;
        }
        int i = 0;
        int i2 = -1;
        long j = -1;
        Map jB = qo.jz().jB();
        for (String str : list) {
            Long l = (Long) jB.get(str);
            long j2 = 0;
            if (l != null) {
                j2 = l.longValue();
            }
            if ((j2 <= j ? 1 : null) == null) {
                j = j2;
                i2 = i;
            }
            i++;
        }
        return i2;
    }

    public String cS(String str) {
        ov a = this.Pp.a(str, 1);
        return a == null ? null : a.getAppName();
    }

    public boolean dk(String str) {
        return str != null ? this.Pp.ai(str) : false;
    }

    public void init() {
        this.Ac = true;
    }
}
