package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class bn extends fs {
    static f dR;
    static ArrayList<bm> dS;
    public int dG;
    public int dH;
    public int dO;
    public f dP;
    public ArrayList<bm> dQ;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.bn.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.bn.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.bn.<clinit>():void");
    }

    public bn() {
        this.dG = 0;
        this.dH = 0;
        this.dO = 1;
        this.dP = null;
        this.dQ = null;
    }

    public fs newInit() {
        return new bn();
    }

    public void readFrom(fq fqVar) {
        this.dG = fqVar.a(this.dG, 0, false);
        this.dH = fqVar.a(this.dH, 1, false);
        this.dO = fqVar.a(this.dO, 2, false);
        this.dP = (f) fqVar.a(dR, 3, false);
        this.dQ = (ArrayList) fqVar.b(dS, 4, false);
    }

    public void writeTo(fr frVar) {
        if (this.dG != 0) {
            frVar.write(this.dG, 0);
        }
        if (this.dH != 0) {
            frVar.write(this.dH, 1);
        }
        if (this.dO != 1) {
            frVar.write(this.dO, 2);
        }
        if (this.dP != null) {
            frVar.a(this.dP, 3);
        }
        if (this.dQ != null) {
            frVar.a(this.dQ, 4);
        }
    }
}
