package tmsdkobf;

/* compiled from: Unknown */
public final class eq extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public int pos;
    public int size;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.eq.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.eq.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.eq.<clinit>():void");
    }

    public eq() {
        this.pos = 0;
        this.size = 0;
        Q(this.pos);
        setSize(this.size);
    }

    public void Q(int i) {
        this.pos = i;
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
        eq eqVar = (eq) obj;
        if (ft.equals(this.pos, eqVar.pos) && ft.equals(this.size, eqVar.size)) {
            z = true;
        }
        return z;
    }

    public int getPos() {
        return this.pos;
    }

    public int getSize() {
        return this.size;
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
        Q(fqVar.a(this.pos, 0, true));
        setSize(fqVar.a(this.size, 1, true));
    }

    public void setSize(int i) {
        this.size = i;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.pos, 0);
        frVar.write(this.size, 1);
    }
}
