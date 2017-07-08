package tmsdkobf;

/* compiled from: Unknown */
public final class dz extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public String cC;
    public String cE;
    public String cF;
    public String cG;
    public String imsi;
    public String ja;
    public int ji;
    public int jj;
    public int u;
    public String version;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.dz.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.dz.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.dz.<clinit>():void");
    }

    public dz() {
        this.cC = "";
        this.cE = "";
        this.cF = "";
        this.ja = "";
        this.cG = "";
        this.imsi = "";
        this.version = "";
        this.u = 0;
        this.ji = 0;
        this.jj = 0;
        e(this.cC);
        D(this.cE);
        setPhone(this.cF);
        E(this.ja);
        C(this.cG);
        f(this.imsi);
        r(this.version);
        y(this.u);
        z(this.ji);
        A(this.jj);
    }

    public void A(int i) {
        this.jj = i;
    }

    public void C(String str) {
        this.cG = str;
    }

    public void D(String str) {
        this.cE = str;
    }

    public void E(String str) {
        this.ja = str;
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

    public void e(String str) {
        this.cC = str;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        dz dzVar = (dz) obj;
        if (ft.equals(this.cC, dzVar.cC) && ft.equals(this.cE, dzVar.cE) && ft.equals(this.cF, dzVar.cF) && ft.equals(this.ja, dzVar.ja) && ft.equals(this.cG, dzVar.cG) && ft.equals(this.imsi, dzVar.imsi) && ft.equals(this.version, dzVar.version) && ft.equals(this.u, dzVar.u) && ft.equals(this.ji, dzVar.ji) && ft.equals(this.jj, dzVar.jj)) {
            z = true;
        }
        return z;
    }

    public void f(String str) {
        this.imsi = str;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void r(String str) {
        this.version = str;
    }

    public void readFrom(fq fqVar) {
        e(fqVar.a(0, true));
        D(fqVar.a(1, false));
        setPhone(fqVar.a(2, false));
        E(fqVar.a(3, false));
        C(fqVar.a(4, false));
        f(fqVar.a(5, false));
        r(fqVar.a(6, false));
        y(fqVar.a(this.u, 7, false));
        z(fqVar.a(this.ji, 8, false));
        A(fqVar.a(this.jj, 9, false));
    }

    public void setPhone(String str) {
        this.cF = str;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.cC, 0);
        if (this.cE != null) {
            frVar.a(this.cE, 1);
        }
        if (this.cF != null) {
            frVar.a(this.cF, 2);
        }
        if (this.ja != null) {
            frVar.a(this.ja, 3);
        }
        if (this.cG != null) {
            frVar.a(this.cG, 4);
        }
        if (this.imsi != null) {
            frVar.a(this.imsi, 5);
        }
        if (this.version != null) {
            frVar.a(this.version, 6);
        }
        frVar.write(this.u, 7);
        frVar.write(this.ji, 8);
        frVar.write(this.jj, 9);
    }

    public void y(int i) {
        this.u = i;
    }

    public void z(int i) {
        this.ji = i;
    }
}
