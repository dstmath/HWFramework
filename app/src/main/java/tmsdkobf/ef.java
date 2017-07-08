package tmsdkobf;

import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;

/* compiled from: Unknown */
public final class ef extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public String cL;
    public String iq;
    public int js;
    public int jt;
    public String ju;
    public String name;
    public String path;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ef.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ef.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ef.<clinit>():void");
    }

    public ef() {
        this.cL = "";
        this.iq = "";
        this.js = 0;
        this.path = "";
        this.name = "";
        this.jt = 0;
        this.ju = "";
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
        foVar.a(this.cL, "pkgname");
        foVar.a(this.iq, "cert");
        foVar.a(this.js, "softsize");
        foVar.a(this.path, "path");
        foVar.a(this.name, CheckVersionField.CHECK_VERSION_NAME);
        foVar.a(this.jt, "isOfficial");
        foVar.a(this.ju, "expanda");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        ef efVar = (ef) obj;
        if (ft.equals(this.cL, efVar.cL) && ft.equals(this.iq, efVar.iq) && ft.equals(this.js, efVar.js) && ft.equals(this.path, efVar.path) && ft.equals(this.name, efVar.name) && ft.equals(this.jt, efVar.jt) && ft.equals(this.ju, efVar.ju)) {
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
        this.cL = fqVar.a(0, true);
        this.iq = fqVar.a(1, true);
        this.js = fqVar.a(this.js, 3, false);
        this.path = fqVar.a(4, false);
        this.name = fqVar.a(5, false);
        this.jt = fqVar.a(this.jt, 6, false);
        this.ju = fqVar.a(7, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.cL, 0);
        frVar.a(this.iq, 1);
        frVar.write(this.js, 3);
        if (this.path != null) {
            frVar.a(this.path, 4);
        }
        if (this.name != null) {
            frVar.a(this.name, 5);
        }
        frVar.write(this.jt, 6);
        if (this.ju != null) {
            frVar.a(this.ju, 7);
        }
    }
}
