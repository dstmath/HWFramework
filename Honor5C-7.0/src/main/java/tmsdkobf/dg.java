package tmsdkobf;

/* compiled from: Unknown */
public final class dg extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public int fileSize;
    public String ip;
    public String iq;
    public String softName;
    public String version;
    public int versionCode;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.dg.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.dg.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.dg.<clinit>():void");
    }

    public dg() {
        this.ip = "";
        this.softName = "";
        this.version = "";
        this.versionCode = 0;
        this.iq = "";
        this.fileSize = 0;
        p(this.ip);
        q(this.softName);
        r(this.version);
        l(this.versionCode);
        s(this.iq);
        m(this.fileSize);
    }

    public dg(String str, String str2, String str3, int i, String str4, int i2) {
        this.ip = "";
        this.softName = "";
        this.version = "";
        this.versionCode = 0;
        this.iq = "";
        this.fileSize = 0;
        p(str);
        q(str2);
        r(str3);
        l(i);
        s(str4);
        m(i2);
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
        dg dgVar = (dg) obj;
        if (ft.equals(this.ip, dgVar.ip) && ft.equals(this.softName, dgVar.softName) && ft.equals(this.version, dgVar.version) && ft.equals(this.versionCode, dgVar.versionCode) && ft.equals(this.iq, dgVar.iq) && ft.equals(this.fileSize, dgVar.fileSize)) {
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

    public void l(int i) {
        this.versionCode = i;
    }

    public void m(int i) {
        this.fileSize = i;
    }

    public void p(String str) {
        this.ip = str;
    }

    public void q(String str) {
        this.softName = str;
    }

    public void r(String str) {
        this.version = str;
    }

    public void readFrom(fq fqVar) {
        p(fqVar.a(0, true));
        q(fqVar.a(1, true));
        r(fqVar.a(2, true));
        l(fqVar.a(this.versionCode, 3, false));
        s(fqVar.a(4, false));
        m(fqVar.a(this.fileSize, 5, false));
    }

    public void s(String str) {
        this.iq = str;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.ip, 0);
        frVar.a(this.softName, 1);
        frVar.a(this.version, 2);
        frVar.write(this.versionCode, 3);
        if (this.iq != null) {
            frVar.a(this.iq, 4);
        }
        frVar.write(this.fileSize, 5);
    }
}
