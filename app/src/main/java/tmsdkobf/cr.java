package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class cr extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    static ArrayList<ei> fO;
    public int fL;
    public String fM;
    public ArrayList<ei> fN;
    public String id;
    public int product;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.cr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.cr.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.cr.<clinit>():void");
    }

    public cr() {
        this.id = "";
        this.product = dc.ha.value();
        this.fL = 0;
        this.fM = "";
        this.fN = null;
        b(this.id);
        e(this.product);
        f(this.fL);
        c(this.fM);
        a(this.fN);
    }

    public void a(ArrayList<ei> arrayList) {
        this.fN = arrayList;
    }

    public void b(String str) {
        this.id = str;
    }

    public void c(String str) {
        this.fM = str;
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

    public void e(int i) {
        this.product = i;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        cr crVar = (cr) obj;
        if (ft.equals(this.id, crVar.id) && ft.equals(this.product, crVar.product) && ft.equals(this.fL, crVar.fL) && ft.equals(this.fM, crVar.fM) && ft.equals(this.fN, crVar.fN)) {
            z = true;
        }
        return z;
    }

    public void f(int i) {
        this.fL = i;
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
        b(fqVar.a(0, true));
        e(fqVar.a(this.product, 1, false));
        f(fqVar.a(this.fL, 2, false));
        c(fqVar.a(3, false));
        if (fO == null) {
            fO = new ArrayList();
            fO.add(new ei());
        }
        a((ArrayList) fqVar.b(fO, 4, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.id, 0);
        frVar.write(this.product, 1);
        frVar.write(this.fL, 2);
        if (this.fM != null) {
            frVar.a(this.fM, 3);
        }
        if (this.fN != null) {
            frVar.a(this.fN, 4);
        }
    }
}
