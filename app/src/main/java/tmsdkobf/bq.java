package tmsdkobf;

/* compiled from: Unknown */
public final class bq extends fs {
    static byte[] dM;
    static bp dU;
    public int aZ;
    public int dG;
    public int dH;
    public int dJ;
    public int dK;
    public bp dT;
    public byte[] data;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.bq.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.bq.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.bq.<clinit>():void");
    }

    public bq() {
        this.aZ = 0;
        this.dG = 0;
        this.dH = 0;
        this.dJ = 0;
        this.dK = 0;
        this.data = null;
        this.dT = null;
    }

    public fs newInit() {
        return new bq();
    }

    public void readFrom(fq fqVar) {
        this.aZ = fqVar.a(this.aZ, 0, true);
        this.dG = fqVar.a(this.dG, 1, false);
        this.dH = fqVar.a(this.dH, 2, false);
        this.dJ = fqVar.a(this.dJ, 3, false);
        this.dK = fqVar.a(this.dK, 4, false);
        this.data = fqVar.a(dM, 5, false);
        this.dT = (bp) fqVar.a(dU, 6, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.aZ, 0);
        if (this.dG != 0) {
            frVar.write(this.dG, 1);
        }
        if (this.dH != 0) {
            frVar.write(this.dH, 2);
        }
        frVar.write(this.dJ, 3);
        if (this.dK != 0) {
            frVar.write(this.dK, 4);
        }
        if (this.data != null) {
            frVar.a(this.data, 5);
        }
        if (this.dT != null) {
            frVar.a(this.dT, 6);
        }
    }
}
