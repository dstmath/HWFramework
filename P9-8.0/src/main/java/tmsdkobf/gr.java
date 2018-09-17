package tmsdkobf;

import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Iterator;

public class gr {
    private static int W(int i) {
        int i2 = i + 1;
        return i2 >= 0 ? i2 : 0;
    }

    public static ar a(int i, int i2, ArrayList<JceStruct> arrayList) {
        ar arVar = new ar();
        arVar.bK = i;
        arVar.bO = i2;
        if (arrayList != null) {
            arVar.bN = new ArrayList();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                JceStruct jceStruct = (JceStruct) it.next();
                if (jceStruct != null) {
                    arVar.bN.add(jceStruct.toByteArray("UTF-8"));
                }
            }
        } else {
            arVar.bN = null;
        }
        if (i2 != 0) {
            ar R = gq.aZ().R(i);
            if (R != null) {
                arVar.bL = R.bM;
                arVar.bM = W(arVar.bL);
                return arVar;
            }
        }
        arVar.bL = 0;
        arVar.bM = W(arVar.bL);
        return arVar;
    }

    /* JADX WARNING: Missing block: B:3:0x0006, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:26:0x0044, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:40:0x006f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean a(ar arVar, ar arVar2) {
        if (arVar == null && arVar2 == null) {
            return true;
        }
        if (arVar == null || arVar2 == null || arVar2.bO != arVar.bO || arVar2.bK != arVar.bK) {
            return false;
        }
        if (arVar2.bN == null && arVar.bN == null) {
            return true;
        }
        if ((arVar2.bN != arVar.bN && (arVar2.bN == null || arVar.bN == null)) || arVar2.bN.size() != arVar.bN.size()) {
            return false;
        }
        int size = arVar2.bN.size();
        for (int i = 0; i < size; i++) {
            byte[] bArr = (byte[]) arVar2.bN.get(i);
            byte[] bArr2 = (byte[]) arVar.bN.get(i);
            if (bArr != null || bArr2 != null) {
                if ((bArr != bArr2 && (bArr == null || bArr2 == null)) || bArr.length != bArr2.length) {
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
        }
        return true;
    }

    public static byte[] a(int i, int i2, JceStruct jceStruct) {
        JceStruct b = b(i, i2, jceStruct);
        return b != null ? a(b) : null;
    }

    public static final byte[] a(JceStruct jceStruct) {
        return jceStruct != null ? nh.c(null, jceStruct) : null;
    }

    public static ar b(int i, int i2, JceStruct jceStruct) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(jceStruct);
        return a(i, i2, arrayList);
    }

    public static final ar f(byte[] bArr) {
        return bArr != null ? (ar) nh.a(null, bArr, new ar()) : null;
    }

    public static final void f(String str, String str2) {
        mb.d(str, str2);
    }
}
