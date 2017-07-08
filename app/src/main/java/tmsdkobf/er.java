package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class er extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    static ArrayList<eq> la;
    public String id;
    public ArrayList<eq> kZ;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.er.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.er.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.er.<clinit>():void");
    }

    public er() {
        this.id = "";
        this.kZ = null;
        b(this.id);
        i(this.kZ);
    }

    public void b(String str) {
        this.id = str;
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
        er erVar = (er) obj;
        if (ft.equals(this.id, erVar.id) && ft.equals(this.kZ, erVar.kZ)) {
            z = true;
        }
        return z;
    }

    public ArrayList<eq> g() {
        return this.kZ;
    }

    public String getId() {
        return this.id;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void i(ArrayList<eq> arrayList) {
        this.kZ = arrayList;
    }

    public void readFrom(fq fqVar) {
        b(fqVar.a(0, true));
        if (la == null) {
            la = new ArrayList();
            la.add(new eq());
        }
        i((ArrayList) fqVar.b(la, 1, true));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.id, 0);
        frVar.a(this.kZ, 1);
    }
}
