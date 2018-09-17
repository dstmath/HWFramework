package tmsdkobf;

/* compiled from: Unknown */
public final class ax extends fs {
    static byte[] bR;
    public int bN;
    public int bO;
    public String bP;
    public byte[] bQ;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ax.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ax.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ax.<clinit>():void");
    }

    public ax() {
        this.bN = 0;
        this.bO = 0;
        this.bP = "";
        this.bQ = null;
    }

    public fs newInit() {
        return new ax();
    }

    public void readFrom(fq fqVar) {
        this.bN = fqVar.a(this.bN, 0, true);
        this.bO = fqVar.a(this.bO, 1, false);
        this.bP = fqVar.a(2, false);
        this.bQ = fqVar.a(bR, 3, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.bN, 0);
        if (this.bO != 0) {
            frVar.write(this.bO, 1);
        }
        if (this.bP != null) {
            frVar.a(this.bP, 2);
        }
        if (this.bQ != null) {
            frVar.a(this.bQ, 3);
        }
    }
}
