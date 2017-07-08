package tmsdkobf;

/* compiled from: Unknown */
public final class do extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public int iD;
    public int iE;
    public int iF;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.do.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.do.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.do.<clinit>():void");
    }

    public do() {
        this.iD = 0;
        this.iE = 0;
        this.iF = 0;
        p(this.iD);
        q(this.iE);
        r(this.iF);
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

    public void display(StringBuilder stringBuilder, int i) {
        fo foVar = new fo(stringBuilder, i);
        foVar.a(this.iD, "hostId");
        foVar.a(this.iE, "pluginId");
        foVar.a(this.iF, "pluginVersion");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        do doVar = (do) obj;
        if (ft.equals(this.iD, doVar.iD) && ft.equals(this.iE, doVar.iE) && ft.equals(this.iF, doVar.iF)) {
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

    public void p(int i) {
        this.iD = i;
    }

    public void q(int i) {
        this.iE = i;
    }

    public void r(int i) {
        this.iF = i;
    }

    public void readFrom(fq fqVar) {
        p(fqVar.a(this.iD, 0, false));
        q(fqVar.a(this.iE, 1, false));
        r(fqVar.a(this.iF, 2, false));
    }

    public void writeTo(fr frVar) {
        frVar.write(this.iD, 0);
        frVar.write(this.iE, 1);
        frVar.write(this.iF, 2);
    }
}
