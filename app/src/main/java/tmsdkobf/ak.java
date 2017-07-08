package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ak extends fs {
    static ArrayList<byte[]> bl;
    public int bf;
    public int bg;
    public int bh;
    public ArrayList<byte[]> bi;
    public int bj;
    public boolean bk;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ak.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ak.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ak.<clinit>():void");
    }

    public ak() {
        this.bf = 0;
        this.bg = 0;
        this.bh = 0;
        this.bi = null;
        this.bj = 0;
        this.bk = false;
    }

    public fs newInit() {
        return new ak();
    }

    public void readFrom(fq fqVar) {
        this.bf = fqVar.a(this.bf, 0, true);
        this.bg = fqVar.a(this.bg, 1, true);
        this.bh = fqVar.a(this.bh, 2, true);
        this.bi = (ArrayList) fqVar.b(bl, 3, true);
        this.bj = fqVar.a(this.bj, 4, false);
        this.bk = fqVar.a(this.bk, 5, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.bf, 0);
        frVar.write(this.bg, 1);
        frVar.write(this.bh, 2);
        frVar.a(this.bi, 3);
        if (this.bj != 0) {
            frVar.write(this.bj, 4);
        }
        if (this.bk) {
            frVar.a(this.bk, 5);
        }
    }
}
