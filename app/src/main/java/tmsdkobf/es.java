package tmsdkobf;

/* compiled from: Unknown */
public final class es extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public String lb;
    public int seq;
    public String url;
    public int version;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.es.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.es.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.es.<clinit>():void");
    }

    public es() {
        this.url = "";
        this.lb = "";
        this.seq = 0;
        this.version = 0;
        setUrl(this.url);
        S(this.lb);
        R(this.seq);
        setVersion(this.version);
    }

    public es(String str, String str2, int i, int i2) {
        this.url = "";
        this.lb = "";
        this.seq = 0;
        this.version = 0;
        setUrl(str);
        S(str2);
        R(i);
        setVersion(i2);
    }

    public void R(int i) {
        this.seq = i;
    }

    public void S(String str) {
        this.lb = str;
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
        es esVar = (es) obj;
        if (ft.equals(this.url, esVar.url) && ft.equals(this.lb, esVar.lb) && ft.equals(this.seq, esVar.seq) && ft.equals(this.version, esVar.version)) {
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
        setUrl(fqVar.a(0, true));
        S(fqVar.a(1, false));
        R(fqVar.a(this.seq, 2, false));
        setVersion(fqVar.a(this.version, 3, false));
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public void setVersion(int i) {
        this.version = i;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.url, 0);
        if (this.lb != null) {
            frVar.a(this.lb, 1);
        }
        frVar.write(this.seq, 2);
        frVar.write(this.version, 3);
    }
}
