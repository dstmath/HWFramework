package tmsdkobf;

/* compiled from: Unknown */
public final class ez extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public String fU;
    public String lA;
    public boolean lB;
    public String lC;
    public int lk;
    public int timestamp;
    public String url;
    public int version;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ez.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ez.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ez.<clinit>():void");
    }

    public ez() {
        this.lA = "";
        this.lB = true;
        this.version = 0;
        this.timestamp = 0;
        this.url = "";
        this.fU = "";
        this.lC = "";
        this.lk = 2;
        T(this.lA);
        b(this.lB);
        setVersion(this.version);
        W(this.timestamp);
        setUrl(this.url);
        U(this.fU);
        V(this.lC);
        X(this.lk);
    }

    public void T(String str) {
        this.lA = str;
    }

    public void U(String str) {
        this.fU = str;
    }

    public void V(String str) {
        this.lC = str;
    }

    public void W(int i) {
        this.timestamp = i;
    }

    public void X(int i) {
        this.lk = i;
    }

    public void b(boolean z) {
        this.lB = z;
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
        ez ezVar = (ez) obj;
        if (ft.equals(this.lA, ezVar.lA) && ft.a(this.lB, ezVar.lB) && ft.equals(this.version, ezVar.version) && ft.equals(this.timestamp, ezVar.timestamp) && ft.equals(this.url, ezVar.url) && ft.equals(this.fU, ezVar.fU) && ft.equals(this.lC, ezVar.lC) && ft.equals(this.lk, ezVar.lk)) {
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

    public boolean i() {
        return this.lB;
    }

    public void readFrom(fq fqVar) {
        T(fqVar.a(0, true));
        b(fqVar.a(this.lB, 1, true));
        setVersion(fqVar.a(this.version, 2, true));
        W(fqVar.a(this.timestamp, 3, true));
        setUrl(fqVar.a(4, true));
        U(fqVar.a(5, true));
        V(fqVar.a(6, true));
        X(fqVar.a(this.lk, 7, false));
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public void setVersion(int i) {
        this.version = i;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.lA, 0);
        frVar.a(this.lB, 1);
        frVar.write(this.version, 2);
        frVar.write(this.timestamp, 3);
        frVar.a(this.url, 4);
        frVar.a(this.fU, 5);
        frVar.a(this.lC, 6);
        frVar.write(this.lk, 7);
    }
}
