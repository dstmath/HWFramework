package tmsdkobf;

/* compiled from: Unknown */
public final class dq extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public int iJ;
    public int iK;
    public int iL;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.dq.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.dq.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.dq.<clinit>():void");
    }

    public dq() {
        this.iJ = 0;
        this.iK = 0;
        this.iL = 0;
        s(this.iJ);
        t(this.iK);
        u(this.iL);
    }

    public dq(int i, int i2, int i3) {
        this.iJ = 0;
        this.iK = 0;
        this.iL = 0;
        s(i);
        t(i2);
        u(i3);
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
        dq dqVar = (dq) obj;
        if (ft.equals(this.iJ, dqVar.iJ) && ft.equals(this.iK, dqVar.iK) && ft.equals(this.iL, dqVar.iL)) {
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
        s(fqVar.a(this.iJ, 1, true));
        t(fqVar.a(this.iK, 2, true));
        u(fqVar.a(this.iL, 3, true));
    }

    public void s(int i) {
        this.iJ = i;
    }

    public void t(int i) {
        this.iK = i;
    }

    public void u(int i) {
        this.iL = i;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.iJ, 1);
        frVar.write(this.iK, 2);
        frVar.write(this.iL, 3);
    }
}
