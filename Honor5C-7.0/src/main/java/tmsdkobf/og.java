package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class og extends fs {
    static oh Eg;
    static ArrayList<of> Eh;
    static ArrayList<of> Ei;
    public oh Ed;
    public ArrayList<of> Ee;
    public ArrayList<of> Ef;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.og.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.og.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.og.<clinit>():void");
    }

    public og() {
        this.Ed = null;
        this.Ee = null;
        this.Ef = null;
    }

    public fs newInit() {
        return new og();
    }

    public void readFrom(fq fqVar) {
        this.Ed = (oh) fqVar.a(Eg, 0, true);
        this.Ee = (ArrayList) fqVar.b(Eh, 1, true);
        this.Ef = (ArrayList) fqVar.b(Ei, 2, true);
    }

    public String toString() {
        return "SCCloudResp [scResult=" + this.Ed + ", vecBlacks=" + this.Ee + ", vecWhites=" + this.Ef + "]";
    }

    public void writeTo(fr frVar) {
        frVar.a(this.Ed, 0);
        frVar.a(this.Ee, 1);
        frVar.a(this.Ef, 2);
    }
}
