package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ej extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    static ArrayList<ef> kT;
    static ek kU;
    public ArrayList<ef> kR;
    public ek kS;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ej.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ej.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ej.<clinit>():void");
    }

    public ej() {
        this.kR = null;
        this.kS = null;
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

    public void display(StringBuilder stringBuilder, int i) {
        fo foVar = new fo(stringBuilder, i);
        foVar.a(this.kR, "vctSofts");
        foVar.a(this.kS, "softListInfo");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        ej ejVar = (ej) obj;
        if (ft.equals(this.kR, ejVar.kR) && ft.equals(this.kS, ejVar.kS)) {
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
        if (kT == null) {
            kT = new ArrayList();
            kT.add(new ef());
        }
        this.kR = (ArrayList) fqVar.b(kT, 0, true);
        if (kU == null) {
            kU = new ek();
        }
        this.kS = (ek) fqVar.a(kU, 1, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.kR, 0);
        if (this.kS != null) {
            frVar.a(this.kS, 1);
        }
    }
}
