package tmsdkobf;

/* compiled from: Unknown */
public final class ai extends fs {
    static byte[] bb;
    public int aZ;
    public byte[] ba;
    public int status;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ai.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ai.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ai.<clinit>():void");
    }

    public ai() {
        this.aZ = 0;
        this.ba = null;
        this.status = 0;
    }

    public fs newInit() {
        return new ai();
    }

    public void readFrom(fq fqVar) {
        this.aZ = fqVar.a(this.aZ, 0, true);
        this.ba = fqVar.a(bb, 1, false);
        this.status = fqVar.a(this.status, 2, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.aZ, 0);
        if (this.ba != null) {
            frVar.a(this.ba, 1);
        }
        if (this.status != 0) {
            frVar.write(this.status, 2);
        }
    }
}
