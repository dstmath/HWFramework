package tmsdkobf;

/* compiled from: Unknown */
public final class ew extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public int lk;
    public int timestamp;
    public int version;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ew.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ew.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ew.<clinit>():void");
    }

    public ew() {
        this.timestamp = 0;
        this.version = 0;
        this.lk = 2;
        W(this.timestamp);
        setVersion(this.version);
        X(this.lk);
    }

    public void W(int i) {
        this.timestamp = i;
    }

    public void X(int i) {
        this.lk = i;
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
        ew ewVar = (ew) obj;
        if (ft.equals(this.timestamp, ewVar.timestamp) && ft.equals(this.version, ewVar.version) && ft.equals(this.lk, ewVar.lk)) {
            z = true;
        }
        return z;
    }

    public int h() {
        return this.timestamp;
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
        W(fqVar.a(this.timestamp, 0, true));
        setVersion(fqVar.a(this.version, 1, true));
        X(fqVar.a(this.lk, 2, false));
    }

    public void setVersion(int i) {
        this.version = i;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.timestamp, 0);
        frVar.write(this.version, 1);
        frVar.write(this.lk, 2);
    }
}
