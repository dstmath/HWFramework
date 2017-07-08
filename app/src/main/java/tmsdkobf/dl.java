package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class dl extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    static ArrayList<String> iz;
    public String ix;
    public ArrayList<String> iy;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.dl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.dl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.dl.<clinit>():void");
    }

    public dl() {
        this.ix = "";
        this.iy = null;
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
        foVar.a(this.ix, "typeName");
        foVar.a(this.iy, "keySet");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        dl dlVar = (dl) obj;
        if (ft.equals(this.ix, dlVar.ix) && ft.equals(this.iy, dlVar.iy)) {
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
        this.ix = fqVar.a(0, true);
        if (iz == null) {
            iz = new ArrayList();
            iz.add("");
        }
        this.iy = (ArrayList) fqVar.b(iz, 1, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.ix, 0);
        if (this.iy != null) {
            frVar.a(this.iy, 1);
        }
    }
}
