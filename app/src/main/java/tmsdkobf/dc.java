package tmsdkobf;

import java.io.Serializable;

/* compiled from: Unknown */
public final class dc implements Serializable {
    static final /* synthetic */ boolean fJ = false;
    private static dc[] gZ;
    public static final dc hA = null;
    public static final dc hB = null;
    public static final dc hC = null;
    public static final dc hD = null;
    public static final dc hE = null;
    public static final dc hF = null;
    public static final dc hG = null;
    public static final dc hH = null;
    public static final dc hI = null;
    public static final dc hJ = null;
    public static final dc ha = null;
    public static final dc hb = null;
    public static final dc hc = null;
    public static final dc hd = null;
    public static final dc he = null;
    public static final dc hf = null;
    public static final dc hg = null;
    public static final dc hh = null;
    public static final dc hi = null;
    public static final dc hj = null;
    public static final dc hk = null;
    public static final dc hl = null;
    public static final dc hm = null;
    public static final dc hn = null;
    public static final dc ho = null;
    public static final dc hp = null;
    public static final dc hq = null;
    public static final dc hr = null;
    public static final dc hs = null;
    public static final dc ht = null;
    public static final dc hu = null;
    public static final dc hv = null;
    public static final dc hw = null;
    public static final dc hx = null;
    public static final dc hy = null;
    public static final dc hz = null;
    private String gA;
    private int gz;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.dc.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.dc.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.dc.<clinit>():void");
    }

    private dc(int i, int i2, String str) {
        this.gA = new String();
        this.gA = str;
        this.gz = i2;
        gZ[i] = this;
    }

    public static dc k(int i) {
        for (int i2 = 0; i2 < gZ.length; i2++) {
            if (gZ[i2].value() == i) {
                return gZ[i2];
            }
        }
        if (fJ) {
            return null;
        }
        throw new AssertionError();
    }

    public static dc n(String str) {
        for (int i = 0; i < gZ.length; i++) {
            if (gZ[i].toString().equals(str)) {
                return gZ[i];
            }
        }
        if (fJ) {
            return null;
        }
        throw new AssertionError();
    }

    public String toString() {
        return this.gA;
    }

    public int value() {
        return this.gz;
    }
}
