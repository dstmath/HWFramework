package tmsdkobf;

/* compiled from: Unknown */
public final class rc extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    static dq lj;
    public int Lt;
    public String iI;
    public String iq;
    public dq lf;
    public String url;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.rc.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.rc.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.rc.<clinit>():void");
    }

    public rc() {
        this.Lt = 0;
        this.url = "";
        this.lf = null;
        this.iI = "";
        this.iq = "";
        cE(this.Lt);
        setUrl(this.url);
        a(this.lf);
        z(this.iI);
        s(this.iq);
    }

    public void a(dq dqVar) {
        this.lf = dqVar;
    }

    public void cE(int i) {
        this.Lt = i;
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
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        rc rcVar = (rc) obj;
        if (ft.equals(this.Lt, rcVar.Lt) && ft.equals(this.url, rcVar.url) && ft.equals(this.lf, rcVar.lf) && ft.equals(this.iI, rcVar.iI) && ft.equals(this.iq, rcVar.iq)) {
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
        cE(fqVar.a(this.Lt, 0, true));
        setUrl(fqVar.a(1, true));
        if (lj == null) {
            lj = new dq();
        }
        a((dq) fqVar.a(lj, 2, false));
        z(fqVar.a(3, false));
        s(fqVar.a(4, false));
    }

    public void s(String str) {
        this.iq = str;
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.Lt, 0);
        frVar.a(this.url, 1);
        if (this.lf != null) {
            frVar.a(this.lf, 2);
        }
        if (this.iI != null) {
            frVar.a(this.iI, 3);
        }
        if (this.iq != null) {
            frVar.a(this.iq, 4);
        }
    }

    public void z(String str) {
        this.iI = str;
    }
}
