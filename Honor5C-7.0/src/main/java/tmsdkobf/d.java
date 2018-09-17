package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class d extends fs {
    static ArrayList<String> g;
    static ArrayList<String> j;
    static ArrayList<String> k;
    public int c;
    public ArrayList<String> d;
    public ArrayList<String> e;
    public ArrayList<String> f;
    public int hash;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.d.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.d.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.d.<clinit>():void");
    }

    public d() {
        this.hash = 0;
        this.c = 0;
        this.d = null;
        this.e = null;
        this.f = null;
    }

    public fs newInit() {
        return new d();
    }

    public void readFrom(fq fqVar) {
        this.hash = fqVar.a(this.hash, 0, true);
        this.c = fqVar.a(this.c, 1, true);
        this.d = (ArrayList) fqVar.b(g, 2, true);
        this.e = (ArrayList) fqVar.b(j, 3, true);
        this.f = (ArrayList) fqVar.b(k, 4, true);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.hash, 0);
        frVar.write(this.c, 1);
        frVar.a(this.d, 2);
        frVar.a(this.e, 3);
        frVar.a(this.f, 4);
    }
}
