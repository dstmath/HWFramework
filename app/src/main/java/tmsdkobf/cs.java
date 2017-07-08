package tmsdkobf;

/* compiled from: Unknown */
public final class cs extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public long fP;
    public String fQ;
    public int state;
    public float weight;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.cs.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.cs.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.cs.<clinit>():void");
    }

    public cs() {
        this.fP = 0;
        this.weight = 0.0f;
        this.fQ = "";
        this.state = 0;
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
        cs csVar = (cs) obj;
        if (ft.a(this.fP, csVar.fP) && ft.equals(this.weight, csVar.weight) && ft.equals(this.fQ, csVar.fQ) && ft.equals(this.state, csVar.state)) {
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
        this.fP = fqVar.a(this.fP, 0, true);
        this.weight = fqVar.a(this.weight, 1, true);
        this.fQ = fqVar.a(2, true);
        this.state = fqVar.a(this.state, 3, false);
    }

    public void writeTo(fr frVar) {
        frVar.b(this.fP, 0);
        frVar.a(this.weight, 1);
        frVar.a(this.fQ, 2);
        frVar.write(this.state, 3);
    }
}
