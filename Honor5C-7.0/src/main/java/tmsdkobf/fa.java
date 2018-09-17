package tmsdkobf;

/* compiled from: Unknown */
public final class fa extends fs {
    static final /* synthetic */ boolean fJ = false;
    public String lD;
    public long lE;
    public int lF;
    public String lG;
    public int lH;
    public int lI;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.fa.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.fa.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.fa.<clinit>():void");
    }

    public fa() {
        this.lD = "";
        this.lE = 0;
        this.lF = 0;
        this.lG = "";
        this.lH = 0;
        this.lI = 0;
        W(this.lD);
        a(this.lE);
        Y(this.lF);
        X(this.lG);
        Z(this.lH);
        aa(this.lI);
    }

    public void W(String str) {
        this.lD = str;
    }

    public void X(String str) {
        this.lG = str;
    }

    public void Y(int i) {
        this.lF = i;
    }

    public void Z(int i) {
        this.lH = i;
    }

    public void a(long j) {
        this.lE = j;
    }

    public void aa(int i) {
        this.lI = i;
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
        fa faVar = (fa) obj;
        return ft.equals(this.lD, faVar.lD) && ft.a(this.lE, faVar.lE) && ft.equals(this.lF, faVar.lF) && ft.equals(this.lG, faVar.lG) && ft.equals(this.lH, faVar.lH) && ft.equals(this.lI, faVar.lI);
    }

    public void readFrom(fq fqVar) {
        W(fqVar.a(0, true));
        a(fqVar.a(this.lE, 1, true));
        Y(fqVar.a(this.lF, 2, true));
        X(fqVar.a(3, true));
        Z(fqVar.a(this.lH, 4, true));
        aa(fqVar.a(this.lI, 5, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.lD, 0);
        frVar.b(this.lE, 1);
        frVar.write(this.lF, 2);
        frVar.a(this.lG, 3);
        frVar.write(this.lH, 4);
        frVar.write(this.lI, 5);
    }
}
