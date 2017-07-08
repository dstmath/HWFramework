package tmsdkobf;

/* compiled from: Unknown */
public final class eo extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public String C;
    public int D;
    public int kX;
    public int kY;
    public String title;
    public int type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.eo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.eo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.eo.<clinit>():void");
    }

    public eo() {
        this.title = "";
        this.C = "";
        this.type = 0;
        this.D = 0;
        this.kX = 0;
        this.kY = 0;
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
        eo eoVar = (eo) obj;
        if (ft.equals(this.title, eoVar.title) && ft.equals(this.C, eoVar.C) && ft.equals(this.type, eoVar.type) && ft.equals(this.D, eoVar.D) && ft.equals(this.kX, eoVar.kX) && ft.equals(this.kY, eoVar.kY)) {
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
        this.title = fqVar.a(0, true);
        this.C = fqVar.a(1, true);
        this.type = fqVar.a(this.type, 2, true);
        this.D = fqVar.a(this.D, 3, true);
        this.kX = fqVar.a(this.kX, 4, false);
        this.kY = fqVar.a(this.kY, 5, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.title, 0);
        frVar.a(this.C, 1);
        frVar.write(this.type, 2);
        frVar.write(this.D, 3);
        frVar.write(this.kX, 4);
        frVar.write(this.kY, 5);
    }
}
