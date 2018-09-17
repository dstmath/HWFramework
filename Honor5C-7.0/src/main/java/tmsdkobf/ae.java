package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ae extends fs {
    static g aJ;
    static h aK;
    static h aL;
    static ArrayList<af> aM;
    public String aE;
    public g aF;
    public h aG;
    public h aH;
    public ArrayList<af> aI;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ae.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ae.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ae.<clinit>():void");
    }

    public ae() {
        this.aE = "";
        this.aF = null;
        this.aG = null;
        this.aH = null;
        this.aI = null;
    }

    public fs newInit() {
        return new ae();
    }

    public void readFrom(fq fqVar) {
        this.aE = fqVar.a(0, true);
        this.aF = (g) fqVar.a(aJ, 1, false);
        this.aG = (h) fqVar.a(aK, 2, false);
        this.aH = (h) fqVar.a(aL, 3, false);
        this.aI = (ArrayList) fqVar.b(aM, 4, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.aE, 0);
        if (this.aF != null) {
            frVar.a(this.aF, 1);
        }
        if (this.aG != null) {
            frVar.a(this.aG, 2);
        }
        if (this.aH != null) {
            frVar.a(this.aH, 3);
        }
        if (this.aI != null) {
            frVar.a(this.aI, 4);
        }
    }
}
