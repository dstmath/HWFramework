package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ec extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    static ArrayList<cv> jr;
    public ArrayList<cv> jo;
    public int jp;
    public String jq;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ec.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ec.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ec.<clinit>():void");
    }

    public ec() {
        this.jo = null;
        this.jp = 0;
        this.jq = "";
        f(this.jo);
        B(this.jp);
        N(this.jq);
    }

    public void B(int i) {
        this.jp = i;
    }

    public void N(String str) {
        this.jq = str;
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
        ec ecVar = (ec) obj;
        if (ft.equals(this.jo, ecVar.jo) && ft.equals(this.jp, ecVar.jp) && ft.equals(this.jq, ecVar.jq)) {
            z = true;
        }
        return z;
    }

    public String f() {
        return this.jq;
    }

    public void f(ArrayList<cv> arrayList) {
        this.jo = arrayList;
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
        if (jr == null) {
            jr = new ArrayList();
            jr.add(new cv());
        }
        f((ArrayList) fqVar.b(jr, 1, true));
        B(fqVar.a(this.jp, 2, true));
        N(fqVar.a(3, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.jo, 1);
        frVar.write(this.jp, 2);
        if (this.jq != null) {
            frVar.a(this.jq, 3);
        }
    }
}
