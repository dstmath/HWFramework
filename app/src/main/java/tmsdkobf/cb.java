package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class cb extends fs {
    static ArrayList<bz> eA;
    static ArrayList<ce> eB;
    public int ep;
    public int eq;
    public int er;
    public int es;
    public ArrayList<bz> et;
    public int eu;
    public ArrayList<ce> ev;
    public String ew;
    public int ex;
    public int ey;
    public String ez;
    public String sender;
    public String sms;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.cb.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.cb.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.cb.<clinit>():void");
    }

    public cb() {
        this.sender = "";
        this.sms = "";
        this.ep = 0;
        this.eq = 0;
        this.er = 0;
        this.es = 0;
        this.et = null;
        this.eu = 0;
        this.ev = null;
        this.ew = "";
        this.ex = 0;
        this.ey = 0;
        this.ez = "ETS_NONE";
    }

    public fs newInit() {
        return new cb();
    }

    public void readFrom(fq fqVar) {
        this.sender = fqVar.a(0, true);
        this.sms = fqVar.a(1, true);
        this.ep = fqVar.a(this.ep, 2, true);
        this.eq = fqVar.a(this.eq, 3, true);
        this.er = fqVar.a(this.er, 4, true);
        this.es = fqVar.a(this.es, 5, false);
        this.et = (ArrayList) fqVar.b(eA, 6, false);
        this.eu = fqVar.a(this.eu, 7, false);
        this.ev = (ArrayList) fqVar.b(eB, 8, false);
        this.ew = fqVar.a(9, false);
        this.ex = fqVar.a(this.ex, 10, false);
        this.ey = fqVar.a(this.ey, 11, false);
        this.ez = fqVar.a(12, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.sender, 0);
        frVar.a(this.sms, 1);
        frVar.write(this.ep, 2);
        frVar.write(this.eq, 3);
        frVar.write(this.er, 4);
        if (this.es != 0) {
            frVar.write(this.es, 5);
        }
        if (this.et != null) {
            frVar.a(this.et, 6);
        }
        if (this.eu != 0) {
            frVar.write(this.eu, 7);
        }
        if (this.ev != null) {
            frVar.a(this.ev, 8);
        }
        if (this.ew != null) {
            frVar.a(this.ew, 9);
        }
        if (this.ex != 0) {
            frVar.write(this.ex, 10);
        }
        if (this.ey != 0) {
            frVar.write(this.ey, 11);
        }
        if (this.ez != null) {
            frVar.a(this.ez, 12);
        }
    }
}
