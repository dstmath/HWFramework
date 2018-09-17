package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ed extends fs implements Cloneable {
    static ArrayList<dv> eA;
    static ArrayList<eu> eB;
    static final /* synthetic */ boolean fJ = false;
    public int ep;
    public int eq;
    public int er;
    public int es;
    public ArrayList<dv> et;
    public int eu;
    public ArrayList<eu> ev;
    public String ew;
    public int ex;
    public int product;
    public String sender;
    public String sms;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ed.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ed.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ed.<clinit>():void");
    }

    public ed() {
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
        this.product = 0;
        O(this.sender);
        P(this.sms);
        C(this.ep);
        D(this.eq);
        E(this.er);
        F(this.es);
        g(this.et);
        G(this.eu);
        h(this.ev);
        setComment(this.ew);
        H(this.ex);
        e(this.product);
    }

    public void C(int i) {
        this.ep = i;
    }

    public void D(int i) {
        this.eq = i;
    }

    public void E(int i) {
        this.er = i;
    }

    public void F(int i) {
        this.es = i;
    }

    public void G(int i) {
        this.eu = i;
    }

    public void H(int i) {
        this.ex = i;
    }

    public void O(String str) {
        this.sender = str;
    }

    public void P(String str) {
        this.sms = str;
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
        ed edVar = (ed) obj;
        if (ft.equals(this.sender, edVar.sender) && ft.equals(this.sms, edVar.sms) && ft.equals(this.ep, edVar.ep) && ft.equals(this.eq, edVar.eq) && ft.equals(this.er, edVar.er) && ft.equals(this.es, edVar.es) && ft.equals(this.et, edVar.et) && ft.equals(this.eu, edVar.eu) && ft.equals(this.ev, edVar.ev) && ft.equals(this.ew, edVar.ew) && ft.equals(this.ex, edVar.ex) && ft.equals(this.product, edVar.product)) {
            z = true;
        }
        return z;
    }

    public void g(ArrayList<dv> arrayList) {
        this.et = arrayList;
    }

    public void h(ArrayList<eu> arrayList) {
        this.ev = arrayList;
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
        O(fqVar.a(0, true));
        P(fqVar.a(1, true));
        C(fqVar.a(this.ep, 2, true));
        D(fqVar.a(this.eq, 3, true));
        E(fqVar.a(this.er, 4, true));
        F(fqVar.a(this.es, 5, false));
        if (eA == null) {
            eA = new ArrayList();
            eA.add(new dv());
        }
        g((ArrayList) fqVar.b(eA, 6, false));
        G(fqVar.a(this.eu, 7, false));
        if (eB == null) {
            eB = new ArrayList();
            eB.add(new eu());
        }
        h((ArrayList) fqVar.b(eB, 8, false));
        setComment(fqVar.a(9, false));
        H(fqVar.a(this.ex, 10, false));
        e(fqVar.a(this.product, 11, false));
    }

    public void setComment(String str) {
        this.ew = str;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.sender, 0);
        frVar.a(this.sms, 1);
        frVar.write(this.ep, 2);
        frVar.write(this.eq, 3);
        frVar.write(this.er, 4);
        frVar.write(this.es, 5);
        if (this.et != null) {
            frVar.a(this.et, 6);
        }
        frVar.write(this.eu, 7);
        if (this.ev != null) {
            frVar.a(this.ev, 8);
        }
        if (this.ew != null) {
            frVar.a(this.ew, 9);
        }
        frVar.write(this.ex, 10);
        frVar.write(this.product, 11);
    }
}
