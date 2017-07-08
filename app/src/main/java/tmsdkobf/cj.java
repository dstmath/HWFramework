package tmsdkobf;

/* compiled from: Unknown */
public final class cj extends fs {
    static ck fl;
    static cf fm;
    public ck fj;
    public cf fk;
    public int level;
    public int linkType;
    public int riskType;
    public String url;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.cj.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.cj.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.cj.<clinit>():void");
    }

    public cj() {
        this.url = "";
        this.level = 0;
        this.linkType = 0;
        this.riskType = 0;
        this.fj = null;
        this.fk = null;
    }

    public fs newInit() {
        return new cj();
    }

    public void readFrom(fq fqVar) {
        this.url = fqVar.a(0, true);
        this.level = fqVar.a(this.level, 1, true);
        this.linkType = fqVar.a(this.linkType, 2, true);
        this.riskType = fqVar.a(this.riskType, 3, false);
        this.fj = (ck) fqVar.a(fl, 4, false);
        this.fk = (cf) fqVar.a(fm, 5, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.url, 0);
        frVar.write(this.level, 1);
        frVar.write(this.linkType, 2);
        frVar.write(this.riskType, 3);
        if (this.fj != null) {
            frVar.a(this.fj, 4);
        }
        if (this.fk != null) {
            frVar.a(this.fk, 5);
        }
    }
}
