package tmsdkobf;

/* compiled from: Unknown */
public final class cy extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public String ew;
    public int score;
    public String title;
    public String user;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.cy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.cy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.cy.<clinit>():void");
    }

    public cy() {
        this.title = "";
        this.ew = "";
        this.user = "";
        this.score = 0;
        setTitle(this.title);
        setComment(this.ew);
        d(this.user);
        i(this.score);
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

    public void d(String str) {
        this.user = str;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        cy cyVar = (cy) obj;
        if (ft.equals(this.title, cyVar.title) && ft.equals(this.ew, cyVar.ew) && ft.equals(this.user, cyVar.user) && ft.equals(this.score, cyVar.score)) {
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

    public void i(int i) {
        this.score = i;
    }

    public void readFrom(fq fqVar) {
        setTitle(fqVar.a(0, true));
        setComment(fqVar.a(1, true));
        d(fqVar.a(2, true));
        i(fqVar.a(this.score, 3, true));
    }

    public void setComment(String str) {
        this.ew = str;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.title, 0);
        frVar.a(this.ew, 1);
        frVar.a(this.user, 2);
        frVar.write(this.score, 3);
    }
}
