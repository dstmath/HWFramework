package tmsdkobf;

/* compiled from: Unknown */
public final class cp extends fs implements Cloneable {
    static dg fF;
    static eh fG;
    static ct fH;
    static er fI;
    static final /* synthetic */ boolean fJ = false;
    public int bj;
    public ct fA;
    public int fB;
    public er fC;
    public int fD;
    public int fE;
    public dg fy;
    public eh fz;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.cp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.cp.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.cp.<clinit>():void");
    }

    public cp() {
        this.fy = null;
        this.fz = null;
        this.fA = null;
        this.fB = 0;
        this.fC = null;
        this.fD = 0;
        this.fE = 0;
        this.bj = 0;
        a(this.fy);
        a(this.fz);
        a(this.fA);
        a(this.fB);
        a(this.fC);
        b(this.fD);
        c(this.fE);
        d(this.bj);
    }

    public er a() {
        return this.fC;
    }

    public void a(int i) {
        this.fB = i;
    }

    public void a(ct ctVar) {
        this.fA = ctVar;
    }

    public void a(dg dgVar) {
        this.fy = dgVar;
    }

    public void a(eh ehVar) {
        this.fz = ehVar;
    }

    public void a(er erVar) {
        this.fC = erVar;
    }

    public int b() {
        return this.fE;
    }

    public void b(int i) {
        this.fD = i;
    }

    public void c(int i) {
        this.fE = i;
    }

    public Object clone() {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException e) {
            if (!fJ) {
                throw new AssertionError();
            }
        }
        return obj;
    }

    public void d(int i) {
        this.bj = i;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        cp cpVar = (cp) obj;
        if (ft.equals(this.fy, cpVar.fy) && ft.equals(this.fz, cpVar.fz) && ft.equals(this.fA, cpVar.fA) && ft.equals(this.fB, cpVar.fB) && ft.equals(this.fC, cpVar.fC) && ft.equals(this.fD, cpVar.fD) && ft.equals(this.fE, cpVar.fE) && ft.equals(this.bj, cpVar.bj)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void readFrom(fq fqVar) {
        if (fF == null) {
            fF = new dg();
        }
        a((dg) fqVar.a(fF, 0, true));
        if (fG == null) {
            fG = new eh();
        }
        a((eh) fqVar.a(fG, 1, true));
        if (fH == null) {
            fH = new ct();
        }
        a((ct) fqVar.a(fH, 2, true));
        a(fqVar.a(this.fB, 3, true));
        if (fI == null) {
            fI = new er();
        }
        a((er) fqVar.a(fI, 4, false));
        b(fqVar.a(this.fD, 5, false));
        c(fqVar.a(this.fE, 6, false));
        d(fqVar.a(this.bj, 7, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.fy, 0);
        frVar.a(this.fz, 1);
        frVar.a(this.fA, 2);
        frVar.write(this.fB, 3);
        if (this.fC != null) {
            frVar.a(this.fC, 4);
        }
        frVar.write(this.fD, 5);
        frVar.write(this.fE, 6);
        frVar.write(this.bj, 7);
    }
}
