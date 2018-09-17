package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class bw extends fs {
    static ArrayList<cb> eg;
    public ArrayList<cb> ee;
    public int ef;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.bw.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.bw.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.bw.<clinit>():void");
    }

    public bw() {
        this.ee = null;
        this.ef = 0;
    }

    public fs newInit() {
        return new bw();
    }

    public void readFrom(fq fqVar) {
        this.ee = (ArrayList) fqVar.b(eg, 0, true);
        this.ef = fqVar.a(this.ef, 1, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.ee, 0);
        if (this.ef != 0) {
            frVar.write(this.ef, 1);
        }
    }
}
