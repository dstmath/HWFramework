package tmsdkobf;

/* compiled from: Unknown */
public final class z extends fs {
    static byte[] aq;
    public int am;
    public byte[] an;
    public int ao;
    public int ap;
    public int timestamp;
    public int version;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.z.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.z.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.z.<clinit>():void");
    }

    public z() {
        this.am = 0;
        this.an = null;
        this.timestamp = 0;
        this.ao = 0;
        this.ap = 0;
        this.version = 0;
    }

    public fs newInit() {
        return new z();
    }

    public void readFrom(fq fqVar) {
        this.am = fqVar.a(this.am, 0, true);
        this.an = fqVar.a(aq, 1, true);
        this.timestamp = fqVar.a(this.timestamp, 2, true);
        this.ao = fqVar.a(this.ao, 3, false);
        this.ap = fqVar.a(this.ap, 4, false);
        this.version = fqVar.a(this.version, 5, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.am, 0);
        frVar.a(this.an, 1);
        frVar.write(this.timestamp, 2);
        if (this.ao != 0) {
            frVar.write(this.ao, 3);
        }
        if (this.ap != 0) {
            frVar.write(this.ap, 4);
        }
        if (this.version != 0) {
            frVar.write(this.version, 5);
        }
    }
}
