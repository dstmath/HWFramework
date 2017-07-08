package tmsdkobf;

/* compiled from: Unknown */
public final class dj extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public String is;
    public String it;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.dj.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.dj.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.dj.<clinit>():void");
    }

    public dj() {
        this.is = "";
        this.it = "";
        u(this.is);
        v(this.it);
    }

    public dj(String str, String str2) {
        this.is = "";
        this.it = "";
        u(str);
        v(str2);
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
        dj djVar = (dj) obj;
        if (ft.equals(this.is, djVar.is) && ft.equals(this.it, djVar.it)) {
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
        u(fqVar.a(0, true));
        v(fqVar.a(1, false));
    }

    public void u(String str) {
        this.is = str;
    }

    public void v(String str) {
        this.it = str;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.is, 0);
        if (this.it != null) {
            frVar.a(this.it, 1);
        }
    }
}
