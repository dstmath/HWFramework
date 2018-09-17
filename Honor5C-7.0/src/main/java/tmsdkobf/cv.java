package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class cv extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    static cq gk;
    static en gl;
    static eo gm;
    static ArrayList<cu> gn;
    public cq gg;
    public en gh;
    public eo gi;
    public ArrayList<cu> gj;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.cv.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.cv.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.cv.<clinit>():void");
    }

    public cv() {
        this.gg = null;
        this.gh = null;
        this.gi = null;
        this.gj = null;
        a(this.gg);
        a(this.gh);
        a(this.gi);
        b(this.gj);
    }

    public void a(cq cqVar) {
        this.gg = cqVar;
    }

    public void a(en enVar) {
        this.gh = enVar;
    }

    public void a(eo eoVar) {
        this.gi = eoVar;
    }

    public void b(ArrayList<cu> arrayList) {
        this.gj = arrayList;
    }

    public Object clone() {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException e) {
            if (!fJ) {
                throw new AssertionError();
            }
        }
        return obj;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        cv cvVar = (cv) obj;
        if (ft.equals(this.gg, cvVar.gg) && ft.equals(this.gh, cvVar.gh) && ft.equals(this.gi, cvVar.gi) && ft.equals(this.gj, cvVar.gj)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void readFrom(fq fqVar) {
        if (gk == null) {
            gk = new cq();
        }
        a((cq) fqVar.a(gk, 0, true));
        if (gl == null) {
            gl = new en();
        }
        a((en) fqVar.a(gl, 1, true));
        if (gm == null) {
            gm = new eo();
        }
        a((eo) fqVar.a(gm, 2, false));
        if (gn == null) {
            gn = new ArrayList();
            gn.add(new cu());
        }
        b((ArrayList) fqVar.b(gn, 3, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.gg, 0);
        frVar.a(this.gh, 1);
        if (this.gi != null) {
            frVar.a(this.gi, 2);
        }
        if (this.gj != null) {
            frVar.a(this.gj, 3);
        }
    }
}
