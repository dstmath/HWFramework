package tmsdkobf;

/* compiled from: Unknown */
public final class cw extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public String go;
    public String gp;
    public String gq;
    public String gr;
    public String gs;
    public String gt;
    public String gu;
    public String gv;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.cw.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.cw.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.cw.<clinit>():void");
    }

    public cw() {
        this.go = "";
        this.gp = "";
        this.gq = "";
        this.gr = "";
        this.gs = "";
        this.gt = "";
        this.gu = "";
        this.gv = "";
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

    public void display(StringBuilder stringBuilder, int i) {
        fo foVar = new fo(stringBuilder, i);
        foVar.a(this.go, "data1");
        foVar.a(this.gp, "data2");
        foVar.a(this.gq, "data3");
        foVar.a(this.gr, "data4");
        foVar.a(this.gs, "data5");
        foVar.a(this.gt, "data6");
        foVar.a(this.gu, "data7");
        foVar.a(this.gv, "data8");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        cw cwVar = (cw) obj;
        if (ft.equals(this.go, cwVar.go) && ft.equals(this.gp, cwVar.gp) && ft.equals(this.gq, cwVar.gq) && ft.equals(this.gr, cwVar.gr) && ft.equals(this.gs, cwVar.gs) && ft.equals(this.gt, cwVar.gt) && ft.equals(this.gu, cwVar.gu) && ft.equals(this.gv, cwVar.gv)) {
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
        this.go = fqVar.a(0, false);
        this.gp = fqVar.a(1, false);
        this.gq = fqVar.a(3, false);
        this.gr = fqVar.a(4, false);
        this.gs = fqVar.a(5, false);
        this.gt = fqVar.a(6, false);
        this.gu = fqVar.a(7, false);
        this.gv = fqVar.a(8, false);
    }

    public void writeTo(fr frVar) {
        if (this.go != null) {
            frVar.a(this.go, 0);
        }
        if (this.gp != null) {
            frVar.a(this.gp, 1);
        }
        if (this.gq != null) {
            frVar.a(this.gq, 3);
        }
        if (this.gr != null) {
            frVar.a(this.gr, 4);
        }
        if (this.gs != null) {
            frVar.a(this.gs, 5);
        }
        if (this.gt != null) {
            frVar.a(this.gt, 6);
        }
        if (this.gu != null) {
            frVar.a(this.gu, 7);
        }
        if (this.gv != null) {
            frVar.a(this.gv, 8);
        }
    }
}
