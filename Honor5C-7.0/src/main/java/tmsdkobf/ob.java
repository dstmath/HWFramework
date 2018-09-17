package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ob extends fs {
    static ArrayList<oc> DV;
    public ArrayList<oc> DS;
    public String DT;
    public String DU;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ob.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ob.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ob.<clinit>():void");
    }

    public ob() {
        this.DS = null;
        this.DT = "";
        this.DU = "";
    }

    public fs newInit() {
        return new ob();
    }

    public void readFrom(fq fqVar) {
        this.DS = (ArrayList) fqVar.b(DV, 0, true);
        this.DT = fqVar.a(1, true);
        this.DU = fqVar.a(2, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.DS, 0);
        frVar.a(this.DT, 1);
        frVar.a(this.DU, 2);
    }
}
