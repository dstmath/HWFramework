package tmsdkobf;

/* compiled from: Unknown */
public final class af extends fs {
    static byte[] aR;
    static byte[] aS;
    static byte[] aq;
    public boolean aN;
    public byte[] aO;
    public byte[] aP;
    public int aQ;
    public int am;
    public byte[] an;
    public int fileSize;
    public int timestamp;
    public String url;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.af.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.af.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.af.<clinit>():void");
    }

    public af() {
        this.am = 0;
        this.an = null;
        this.timestamp = 0;
        this.url = "";
        this.aN = false;
        this.aO = null;
        this.aP = null;
        this.aQ = 0;
        this.fileSize = 0;
    }

    public fs newInit() {
        return new af();
    }

    public void readFrom(fq fqVar) {
        this.am = fqVar.a(this.am, 0, true);
        this.an = fqVar.a(aq, 1, true);
        this.timestamp = fqVar.a(this.timestamp, 2, true);
        this.url = fqVar.a(3, false);
        this.aN = fqVar.a(this.aN, 4, false);
        this.aO = fqVar.a(aR, 5, false);
        this.aP = fqVar.a(aS, 6, false);
        this.aQ = fqVar.a(this.aQ, 7, false);
        this.fileSize = fqVar.a(this.fileSize, 8, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.am, 0);
        frVar.a(this.an, 1);
        frVar.write(this.timestamp, 2);
        if (this.url != null) {
            frVar.a(this.url, 3);
        }
        if (this.aN) {
            frVar.a(this.aN, 4);
        }
        if (this.aO != null) {
            frVar.a(this.aO, 5);
        }
        if (this.aP != null) {
            frVar.a(this.aP, 6);
        }
        if (this.aQ != 0) {
            frVar.write(this.aQ, 7);
        }
        if (this.fileSize != 0) {
            frVar.write(this.fileSize, 8);
        }
    }
}
