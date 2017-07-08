package tmsdkobf;

/* compiled from: Unknown */
public final class al extends fs {
    static byte[] bs;
    public int bm;
    public long bn;
    public String bo;
    public byte[] bp;
    public boolean bq;
    public short br;
    public int i;
    public int valueType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.al.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.al.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.al.<clinit>():void");
    }

    public al() {
        this.valueType = 0;
        this.bm = 0;
        this.i = 0;
        this.bn = 0;
        this.bo = "";
        this.bp = null;
        this.bq = false;
        this.br = (short) 0;
    }

    public fs newInit() {
        return new al();
    }

    public void readFrom(fq fqVar) {
        this.valueType = fqVar.a(this.valueType, 0, false);
        this.bm = fqVar.a(this.bm, 1, false);
        this.i = fqVar.a(this.i, 2, false);
        this.bn = fqVar.a(this.bn, 3, false);
        this.bo = fqVar.a(4, false);
        this.bp = fqVar.a(bs, 5, false);
        this.bq = fqVar.a(this.bq, 6, false);
        this.br = (short) fqVar.a(this.br, 7, false);
    }

    public void writeTo(fr frVar) {
        if (this.valueType != 0) {
            frVar.write(this.valueType, 0);
        }
        if (this.bm != 0) {
            frVar.write(this.bm, 1);
        }
        if (this.i != 0) {
            frVar.write(this.i, 2);
        }
        if (this.bn != 0) {
            frVar.b(this.bn, 3);
        }
        if (this.bo != null) {
            frVar.a(this.bo, 4);
        }
        if (this.bp != null) {
            frVar.a(this.bp, 5);
        }
        if (this.bq) {
            frVar.a(this.bq, 6);
        }
        if (this.br != (short) 0) {
            frVar.a(this.br, 7);
        }
    }
}
