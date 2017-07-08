package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class e extends fs {
    static ArrayList<Integer> o;
    static ArrayList<c> p;
    public int hash;
    public int interval;
    public ArrayList<Integer> l;
    public ArrayList<c> m;
    public int n;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.e.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.e.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.e.<clinit>():void");
    }

    public e() {
        this.hash = 0;
        this.interval = 0;
        this.l = null;
        this.m = null;
        this.n = 0;
    }

    public fs newInit() {
        return new e();
    }

    public void readFrom(fq fqVar) {
        this.hash = fqVar.a(this.hash, 0, true);
        this.interval = fqVar.a(this.interval, 1, false);
        this.l = (ArrayList) fqVar.b(o, 2, false);
        this.m = (ArrayList) fqVar.b(p, 3, false);
        this.n = fqVar.a(this.n, 4, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.hash, 0);
        if (this.interval != 0) {
            frVar.write(this.interval, 1);
        }
        if (this.l != null) {
            frVar.a(this.l, 2);
        }
        if (this.m != null) {
            frVar.a(this.m, 3);
        }
        if (this.n != 0) {
            frVar.write(this.n, 4);
        }
    }
}
