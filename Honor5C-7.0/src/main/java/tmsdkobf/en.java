package tmsdkobf;

/* compiled from: Unknown */
public final class en extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public int A;
    public int B;
    public int time;
    public boolean z;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.en.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.en.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.en.<clinit>():void");
    }

    public en() {
        this.time = 0;
        this.z = true;
        this.A = 0;
        this.B = 0;
        v(this.time);
        a(this.z);
        O(this.A);
        P(this.B);
    }

    public void O(int i) {
        this.A = i;
    }

    public void P(int i) {
        this.B = i;
    }

    public void a(boolean z) {
        this.z = z;
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
        en enVar = (en) obj;
        if (ft.equals(this.time, enVar.time) && ft.a(this.z, enVar.z) && ft.equals(this.A, enVar.A) && ft.equals(this.B, enVar.B)) {
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
        v(fqVar.a(this.time, 0, true));
        a(fqVar.a(this.z, 1, true));
        O(fqVar.a(this.A, 2, false));
        P(fqVar.a(this.B, 3, false));
    }

    public void v(int i) {
        this.time = i;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.time, 0);
        frVar.a(this.z, 1);
        frVar.write(this.A, 2);
        frVar.write(this.B, 3);
    }
}
