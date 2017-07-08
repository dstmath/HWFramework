package tmsdkobf;

/* compiled from: Unknown */
public final class bc extends fs {
    static byte[] cb;
    static byte[] cc;
    public byte[] bY;
    public byte[] bZ;
    public float ca;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.bc.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.bc.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.bc.<clinit>():void");
    }

    public bc() {
        this.bY = null;
        this.bZ = null;
        this.ca = -1.0f;
    }

    public fs newInit() {
        return new bc();
    }

    public void readFrom(fq fqVar) {
        this.bY = fqVar.a(cb, 0, false);
        this.bZ = fqVar.a(cc, 1, false);
        this.ca = fqVar.a(this.ca, 2, false);
    }

    public void writeTo(fr frVar) {
        if (this.bY != null) {
            frVar.a(this.bY, 0);
        }
        if (this.bZ != null) {
            frVar.a(this.bZ, 1);
        }
        if (this.ca != -1.0f) {
            frVar.a(this.ca, 2);
        }
    }
}
