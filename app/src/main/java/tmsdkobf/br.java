package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class br extends fs {
    static ArrayList<bq> dW;
    public int dG;
    public int dH;
    public ArrayList<bq> dV;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.br.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.br.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.br.<clinit>():void");
    }

    public br() {
        this.dG = 0;
        this.dH = 0;
        this.dV = null;
    }

    public fs newInit() {
        return new br();
    }

    public void readFrom(fq fqVar) {
        this.dG = fqVar.a(this.dG, 0, false);
        this.dH = fqVar.a(this.dH, 1, false);
        this.dV = (ArrayList) fqVar.b(dW, 2, false);
    }

    public void writeTo(fr frVar) {
        if (this.dG != 0) {
            frVar.write(this.dG, 0);
        }
        if (this.dH != 0) {
            frVar.write(this.dH, 1);
        }
        if (this.dV != null) {
            frVar.a(this.dV, 2);
        }
    }
}
