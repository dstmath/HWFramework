package tmsdkobf;

/* compiled from: Unknown */
public final class et extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public String iH;
    public int lc;
    public int ld;
    public int mainHarmId;
    public int seq;
    public String url;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.et.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.et.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.et.<clinit>():void");
    }

    public et() {
        this.url = "";
        this.mainHarmId = 0;
        this.lc = 0;
        this.seq = 0;
        this.iH = "";
        this.ld = 0;
        setUrl(this.url);
        S(this.mainHarmId);
        T(this.lc);
        R(this.seq);
        y(this.iH);
        U(this.ld);
    }

    public void R(int i) {
        this.seq = i;
    }

    public void S(int i) {
        this.mainHarmId = i;
    }

    public void T(int i) {
        this.lc = i;
    }

    public void U(int i) {
        this.ld = i;
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

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        et etVar = (et) obj;
        if (ft.equals(this.url, etVar.url) && ft.equals(this.mainHarmId, etVar.mainHarmId) && ft.equals(this.lc, etVar.lc) && ft.equals(this.seq, etVar.seq) && ft.equals(this.iH, etVar.iH) && ft.equals(this.ld, etVar.ld)) {
            z = true;
        }
        return z;
    }

    public String getUrl() {
        return this.url;
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
        setUrl(fqVar.a(0, true));
        S(fqVar.a(this.mainHarmId, 1, true));
        T(fqVar.a(this.lc, 2, false));
        R(fqVar.a(this.seq, 3, false));
        y(fqVar.a(4, false));
        U(fqVar.a(this.ld, 5, false));
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.url, 0);
        frVar.write(this.mainHarmId, 1);
        frVar.write(this.lc, 2);
        frVar.write(this.seq, 3);
        if (this.iH != null) {
            frVar.a(this.iH, 4);
        }
        frVar.write(this.ld, 5);
    }

    public void y(String str) {
        this.iH = str;
    }
}
