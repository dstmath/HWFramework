package tmsdkobf;

/* compiled from: Unknown */
public class ot {
    public static <T extends fs> T a(byte[] bArr, T t, boolean z) {
        if (bArr == null || t == null) {
            return null;
        }
        if (z) {
            t = t.newInit();
        }
        t.recyle();
        t.readFrom(t(bArr));
        return t;
    }

    public static byte[] d(fs fsVar) {
        fr frVar = new fr();
        frVar.ae("UTF-8");
        fsVar.writeTo(frVar);
        return frVar.toByteArray();
    }

    public static bn gf() {
        return new bn();
    }

    public static br s(byte[] bArr) {
        fs a = a(bArr, new br(), false);
        return a != null ? (br) a : null;
    }

    private static fq t(byte[] bArr) {
        fq fqVar = new fq(bArr);
        fqVar.ae("UTF-8");
        return fqVar;
    }
}
