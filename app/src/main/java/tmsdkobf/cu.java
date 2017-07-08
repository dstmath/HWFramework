package tmsdkobf;

/* compiled from: Unknown */
public final class cu extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    static byte[] gf;
    public int gc;
    public byte[] gd;
    public int ge;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.cu.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.cu.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.cu.<clinit>():void");
    }

    public cu() {
        this.gc = 0;
        this.gd = null;
        this.ge = 0;
        g(this.gc);
        a(this.gd);
        h(this.ge);
    }

    public void a(byte[] bArr) {
        this.gd = bArr;
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
        cu cuVar = (cu) obj;
        if (ft.equals(this.gc, cuVar.gc) && ft.equals(this.gd, cuVar.gd) && ft.equals(this.ge, cuVar.ge)) {
            z = true;
        }
        return z;
    }

    public void g(int i) {
        this.gc = i;
    }

    public void h(int i) {
        this.ge = i;
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
        g(fqVar.a(this.gc, 0, true));
        if (gf == null) {
            gf = new byte[1];
            gf[0] = (byte) 0;
        }
        a(fqVar.a(gf, 1, true));
        h(fqVar.a(this.ge, 2, true));
    }

    public void writeTo(fr frVar) {
        frVar.write(this.gc, 0);
        frVar.a(this.gd, 1);
        frVar.write(this.ge, 2);
    }
}
