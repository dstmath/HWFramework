package tmsdkobf;

/* compiled from: Unknown */
public final class dn extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public int cJ;
    public int iC;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.dn.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.dn.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.dn.<clinit>():void");
    }

    public dn() {
        this.iC = 0;
        this.cJ = dd.hL.value();
        n(this.iC);
        o(this.cJ);
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
        dn dnVar = (dn) obj;
        if (ft.equals(this.iC, dnVar.iC) && ft.equals(this.cJ, dnVar.cJ)) {
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

    public void n(int i) {
        this.iC = i;
    }

    public void o(int i) {
        this.cJ = i;
    }

    public void readFrom(fq fqVar) {
        n(fqVar.a(this.iC, 0, true));
        o(fqVar.a(this.cJ, 1, false));
    }

    public void writeTo(fr frVar) {
        frVar.write(this.iC, 0);
        frVar.write(this.cJ, 1);
    }
}
