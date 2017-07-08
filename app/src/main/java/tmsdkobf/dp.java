package tmsdkobf;

/* compiled from: Unknown */
public final class dp extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public String iG;
    public String iH;
    public String iI;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.dp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.dp.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.dp.<clinit>():void");
    }

    public dp() {
        this.iG = "";
        this.iH = "";
        this.iI = "";
        x(this.iG);
        y(this.iH);
        z(this.iI);
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
        dp dpVar = (dp) obj;
        if (ft.equals(this.iG, dpVar.iG) && ft.equals(this.iH, dpVar.iH) && ft.equals(this.iI, dpVar.iI)) {
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
        x(fqVar.a(0, true));
        y(fqVar.a(1, false));
        z(fqVar.a(2, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.iG, 0);
        if (this.iH != null) {
            frVar.a(this.iH, 1);
        }
        if (this.iI != null) {
            frVar.a(this.iI, 2);
        }
    }

    public void x(String str) {
        this.iG = str;
    }

    public void y(String str) {
        this.iH = str;
    }

    public void z(String str) {
        this.iI = str;
    }
}
