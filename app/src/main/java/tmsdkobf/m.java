package tmsdkobf;

/* compiled from: Unknown */
public final class m extends fs {
    static byte[] R;
    static u S;
    static v T;
    public int H;
    public byte[] N;
    public int O;
    public u P;
    public v Q;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.m.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.m.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.m.<clinit>():void");
    }

    public m() {
        this.H = 0;
        this.N = null;
        this.O = 0;
        this.P = null;
        this.Q = null;
    }

    public fs newInit() {
        return new m();
    }

    public void readFrom(fq fqVar) {
        this.H = fqVar.a(this.H, 0, false);
        this.N = fqVar.a(R, 1, false);
        this.O = fqVar.a(this.O, 2, false);
        this.P = (u) fqVar.a(S, 3, false);
        this.Q = (v) fqVar.a(T, 4, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.H, 0);
        if (this.N != null) {
            frVar.a(this.N, 1);
        }
        if (this.O != 0) {
            frVar.write(this.O, 2);
        }
        if (this.P != null) {
            frVar.a(this.P, 3);
        }
        if (this.Q != null) {
            frVar.a(this.Q, 4);
        }
    }
}
