package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class au extends fs {
    static ArrayList<bh> bE;
    static az bJ;
    static bf bK;
    public int bC;
    public ArrayList<bh> bD;
    public az bF;
    public int bG;
    public bf bH;
    public int bI;
    public String imsi;
    public String sms;
    public int time;
    public int type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.au.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.au.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.au.<clinit>():void");
    }

    public au() {
        this.sms = "";
        this.time = 0;
        this.bF = null;
        this.type = 0;
        this.bD = null;
        this.bG = 3;
        this.bH = null;
        this.bI = 1;
        this.imsi = "";
        this.bC = 0;
    }

    public fs newInit() {
        return new au();
    }

    public void readFrom(fq fqVar) {
        this.sms = fqVar.a(0, true);
        this.time = fqVar.a(this.time, 1, true);
        this.bF = (az) fqVar.a(bJ, 2, true);
        this.type = fqVar.a(this.type, 3, true);
        this.bD = (ArrayList) fqVar.b(bE, 4, false);
        this.bG = fqVar.a(this.bG, 5, false);
        this.bH = (bf) fqVar.a(bK, 6, false);
        this.bI = fqVar.a(this.bI, 7, false);
        this.imsi = fqVar.a(8, false);
        this.bC = fqVar.a(this.bC, 9, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.sms, 0);
        frVar.write(this.time, 1);
        frVar.a(this.bF, 2);
        frVar.write(this.type, 3);
        if (this.bD != null) {
            frVar.a(this.bD, 4);
        }
        if (3 != this.bG) {
            frVar.write(this.bG, 5);
        }
        if (this.bH != null) {
            frVar.a(this.bH, 6);
        }
        if (1 != this.bI) {
            frVar.write(this.bI, 7);
        }
        if (this.imsi != null) {
            frVar.a(this.imsi, 8);
        }
        if (this.bC != 0) {
            frVar.write(this.bC, 9);
        }
    }
}
