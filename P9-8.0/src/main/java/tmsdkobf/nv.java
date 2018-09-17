package tmsdkobf;

import java.util.Iterator;

public class nv {
    public static final void A(String str, String str2) {
        qg.c(65539, str + "|" + str2);
        mb.o(str, str2);
        y(str, str2);
    }

    public static final void a(String str, String str2, bw bwVar, ce ceVar) {
        mb.d(str, str2);
        qg.d(65539, str + "|" + str2);
    }

    public static final void a(String str, cf cfVar) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ServerShark seqNo|" + cfVar.ey + "|refSeqNo|" + cfVar.ez);
        if (cfVar.eQ != null && cfVar.eQ.size() > 0) {
            Iterator it = cfVar.eQ.iterator();
            while (it.hasNext()) {
                ce ceVar = (ce) it.next();
                if (ceVar.ez == 0) {
                    stringBuilder.append(" || push cmd|" + ceVar.bz + "|seqNo|" + ceVar.ey + "|refSeqNo|" + ceVar.ez + "|retCode|" + ceVar.eB + "|dataRetCode|" + ceVar.eC + "|pushId|" + ceVar.eO.ex);
                } else {
                    stringBuilder.append(" || sashimi cmd|" + ceVar.bz + "|seqNo|" + ceVar.ey + "|refSeqNo|" + ceVar.ez + "|retCode|" + ceVar.eB + "|dataRetCode|" + ceVar.eC);
                }
            }
        }
        r(str, stringBuilder.toString());
    }

    public static final void a(String str, byte[] bArr) {
        try {
            a(str, nn.r(bArr));
        } catch (Throwable th) {
            c(str, mb.getStackTraceString(th), null, null);
        }
    }

    public static final void b(String str, String str2, bw bwVar, ce ceVar) {
        mb.n(str, str2);
        qg.b(65539, str + "|" + str2);
    }

    public static final void c(String str, String str2, bw bwVar, ce ceVar) {
        mb.o(str, str2);
        qg.c(65539, str + "|" + str2);
    }

    public static final void r(String str, String str2) {
        mb.r(str, str2);
        qg.a(65539, str + "|" + str2);
    }

    private static final void y(String str, String str2) {
    }

    public static final void z(String str, String str2) {
        qg.d(65539, str + "|" + str2);
        mb.d(str, str2);
        y(str, str2);
    }
}
