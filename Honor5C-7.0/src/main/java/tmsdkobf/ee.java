package tmsdkobf;

/* compiled from: Unknown */
public final class ee extends fs implements Cloneable {
    static dg fF;
    static final /* synthetic */ boolean fJ = false;
    public int fE;
    public dg fy;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ee.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ee.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ee.<clinit>():void");
    }

    public ee() {
        this.fy = null;
        this.fE = 0;
        a(this.fy);
        c(this.fE);
    }

    public ee(dg dgVar, int i) {
        this.fy = null;
        this.fE = 0;
        a(dgVar);
        c(i);
    }

    public void a(dg dgVar) {
        this.fy = dgVar;
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

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        ee eeVar = (ee) obj;
        if (ft.equals(this.fy, eeVar.fy) && ft.equals(this.fE, eeVar.fE)) {
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
        c(fqVar.a(this.fE, 1, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.fy, 0);
        frVar.write(this.fE, 1);
    }
}
