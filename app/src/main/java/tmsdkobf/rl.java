package tmsdkobf;

import java.util.ArrayList;
import java.util.List;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class rl {
    private boolean Oc;
    private List<a> Od;

    /* compiled from: Unknown */
    private static class a {
        private int Oe;
        private String Of;

        private a() {
            this.Oe = 0;
            this.Of = null;
        }
    }

    public rl() {
        this.Oc = true;
    }

    private boolean a(String str, a aVar) {
        switch (aVar.Oe) {
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                return str.startsWith(aVar.Of);
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                return str.contains(aVar.Of);
            case FileInfo.TYPE_BIGFILE /*3*/:
                return str.endsWith(aVar.Of);
            default:
                return false;
        }
    }

    public static rl dB(String str) {
        int i = 0;
        if (str == null || str.length() <= 1) {
            return null;
        }
        rl rlVar = new rl();
        rlVar.Oc = str.charAt(0) == '0';
        String[] split = str.substring(1).split("\\|");
        if (split == null || split.length == 0) {
            return null;
        }
        List arrayList = new ArrayList();
        int length = split.length;
        while (i < length) {
            a dC = dC(split[i]);
            if (dC != null) {
                arrayList.add(dC);
            }
            i++;
        }
        if (arrayList.size() == 0) {
            return null;
        }
        rlVar.Od = arrayList;
        return rlVar;
    }

    private static a dC(String str) {
        if (str == null || str.length() <= 3) {
            return null;
        }
        a aVar = new a();
        int i = str.charAt(0) != '*' ? 0 : 1;
        int i2 = str.charAt(str.length() + -1) != '*' ? 0 : 1;
        if (i != 0 && i2 != 0) {
            aVar.Oe = 2;
            aVar.Of = str.substring(1, str.length() - 1);
        } else if (i != 0) {
            aVar.Oe = 3;
            aVar.Of = str.substring(1);
        } else if (i2 != 0) {
            aVar.Oe = 1;
            aVar.Of = str.substring(0, str.length() - 1);
        }
        return (aVar.Oe == 0 || aVar.Of == null || aVar.Of.length() == 0) ? null : aVar;
    }

    public boolean match(String str) {
        boolean z = false;
        if (str == null || str.length() == 0) {
            return false;
        }
        boolean z2;
        for (a a : this.Od) {
            if (a(str, a)) {
                z2 = true;
                break;
            }
        }
        z2 = false;
        if (this.Oc) {
            z = z2;
        } else if (!z2) {
            z = true;
        }
        return z;
    }
}
