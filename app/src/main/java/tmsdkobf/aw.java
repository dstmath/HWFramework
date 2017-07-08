package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class aw extends fs {
    static ArrayList<bh> bE;
    static az bJ;
    public int bC;
    public ArrayList<bh> bD;
    public az bF;
    public int bI;
    public String imsi;
    public String sms;
    public int time;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.aw.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.aw.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.aw.<clinit>():void");
    }

    public aw() {
        this.sms = "";
        this.time = 0;
        this.bF = null;
        this.bI = 0;
        this.bD = null;
        this.imsi = "";
        this.bC = 0;
    }

    public fs newInit() {
        return new aw();
    }

    public void readFrom(fq fqVar) {
        this.sms = fqVar.a(0, true);
        this.time = fqVar.a(this.time, 1, true);
        this.bF = (az) fqVar.a(bJ, 2, true);
        this.bI = fqVar.a(this.bI, 3, true);
        this.bD = (ArrayList) fqVar.b(bE, 4, false);
        this.imsi = fqVar.a(5, false);
        this.bC = fqVar.a(this.bC, 6, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.sms, 0);
        frVar.write(this.time, 1);
        frVar.a(this.bF, 2);
        frVar.write(this.bI, 3);
        if (this.bD != null) {
            frVar.a(this.bD, 4);
        }
        if (this.imsi != null) {
            frVar.a(this.imsi, 5);
        }
        if (this.bC != 0) {
            frVar.write(this.bC, 6);
        }
    }
}
