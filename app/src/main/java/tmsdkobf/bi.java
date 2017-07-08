package tmsdkobf;

/* compiled from: Unknown */
public final class bi extends fs {
    static az cB;
    public int bC;
    public int cA;
    public String cu;
    public String cv;
    public int cw;
    public az cx;
    public int cy;
    public String cz;
    public String imsi;
    public int status;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.bi.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.bi.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.bi.<clinit>():void");
    }

    public bi() {
        this.cu = "";
        this.cv = "";
        this.cw = 0;
        this.cx = null;
        this.cy = 0;
        this.cz = "";
        this.status = 0;
        this.imsi = "";
        this.cA = 0;
        this.bC = 0;
    }

    public fs newInit() {
        return new bi();
    }

    public void readFrom(fq fqVar) {
        this.cu = fqVar.a(0, true);
        this.cv = fqVar.a(1, true);
        this.cw = fqVar.a(this.cw, 2, true);
        this.cx = (az) fqVar.a(cB, 3, true);
        this.cy = fqVar.a(this.cy, 4, true);
        this.cz = fqVar.a(5, true);
        this.status = fqVar.a(this.status, 6, false);
        this.imsi = fqVar.a(7, false);
        this.cA = fqVar.a(this.cA, 8, false);
        this.bC = fqVar.a(this.bC, 9, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.cu, 0);
        frVar.a(this.cv, 1);
        frVar.write(this.cw, 2);
        frVar.a(this.cx, 3);
        frVar.write(this.cy, 4);
        frVar.a(this.cz, 5);
        frVar.write(this.status, 6);
        if (this.imsi != null) {
            frVar.a(this.imsi, 7);
        }
        frVar.write(this.cA, 8);
        if (this.bC != 0) {
            frVar.write(this.bC, 9);
        }
    }
}
