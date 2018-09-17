package tmsdkobf;

import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class hu {
    public static ak a(int i, int i2, ArrayList<fs> arrayList) {
        ak akVar = new ak();
        akVar.bf = i;
        akVar.bj = i2;
        if (arrayList != null) {
            akVar.bi = new ArrayList();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                fs fsVar = (fs) it.next();
                if (fsVar != null) {
                    akVar.bi.add(fsVar.toByteArray("UTF-8"));
                }
            }
        } else {
            akVar.bi = null;
        }
        if (i2 != 0) {
            ak aK = ht.bD().aK(i);
            if (aK != null) {
                akVar.bg = aK.bh;
                akVar.bh = aP(akVar.bg);
                return akVar;
            }
        }
        akVar.bg = 0;
        akVar.bh = aP(akVar.bg);
        return akVar;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean a(ak akVar, ak akVar2) {
        if (akVar == null && akVar2 == null) {
            return true;
        }
        if (akVar == null || akVar2 == null || akVar2.bj != akVar.bj || akVar2.bf != akVar.bf) {
            return false;
        }
        if (akVar2.bi == null && akVar.bi == null) {
            return true;
        }
        if (akVar2.bi != akVar.bi) {
            if (akVar2.bi == null || akVar.bi == null) {
                return false;
            }
        }
        if (akVar2.bi.size() != akVar.bi.size()) {
            return false;
        }
        int size = akVar2.bi.size();
        for (int i = 0; i < size; i++) {
            byte[] bArr = (byte[]) akVar2.bi.get(i);
            byte[] bArr2 = (byte[]) akVar.bi.get(i);
            if (bArr == null) {
                if (bArr2 == null) {
                    continue;
                }
            }
            if (bArr != bArr2) {
                if (bArr == null || bArr2 == null) {
                    return false;
                }
            }
            if (bArr.length != bArr2.length) {
                return false;
            }
            int length = bArr.length;
            for (int i2 = 0; i2 < length; i2++) {
                if (bArr[i2] != bArr2[i2]) {
                    return false;
                }
            }
            continue;
        }
        return true;
    }

    public static byte[] a(int i, int i2, fs fsVar) {
        fs b = b(i, i2, fsVar);
        return b != null ? a(b) : null;
    }

    public static final byte[] a(fs fsVar) {
        return fsVar != null ? ok.b(null, fsVar) : null;
    }

    private static int aP(int i) {
        int i2 = i + 1;
        return i2 >= 0 ? i2 : 0;
    }

    public static ak b(int i, int i2, fs fsVar) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(fsVar);
        return a(i, i2, arrayList);
    }

    public static final ak f(byte[] bArr) {
        return bArr != null ? (ak) ok.a(null, bArr, new ak()) : null;
    }

    public static final void h(String str, String str2) {
        d.h(str, str2);
    }
}
