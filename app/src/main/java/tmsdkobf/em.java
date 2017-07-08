package tmsdkobf;

/* compiled from: Unknown */
public final class em extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public int calltime;
    public int clientlogic;
    public String phonenum;
    public int tagtype;
    public int talktime;
    public int teltype;
    public int useraction;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.em.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.em.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.em.<clinit>():void");
    }

    public em() {
        this.phonenum = "";
        this.useraction = 0;
        this.teltype = de.ih.value();
        this.talktime = 0;
        this.calltime = 0;
        this.clientlogic = 0;
        this.tagtype = 0;
        Q(this.phonenum);
        I(this.useraction);
        J(this.teltype);
        K(this.talktime);
        L(this.calltime);
        M(this.clientlogic);
        N(this.tagtype);
    }

    public void I(int i) {
        this.useraction = i;
    }

    public void J(int i) {
        this.teltype = i;
    }

    public void K(int i) {
        this.talktime = i;
    }

    public void L(int i) {
        this.calltime = i;
    }

    public void M(int i) {
        this.clientlogic = i;
    }

    public void N(int i) {
        this.tagtype = i;
    }

    public void Q(String str) {
        this.phonenum = str;
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
        em emVar = (em) obj;
        if (ft.equals(this.phonenum, emVar.phonenum) && ft.equals(this.useraction, emVar.useraction) && ft.equals(this.teltype, emVar.teltype) && ft.equals(this.talktime, emVar.talktime) && ft.equals(this.calltime, emVar.calltime) && ft.equals(this.clientlogic, emVar.clientlogic) && ft.equals(this.tagtype, emVar.tagtype)) {
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
        Q(fqVar.a(0, true));
        I(fqVar.a(this.useraction, 1, true));
        J(fqVar.a(this.teltype, 2, false));
        K(fqVar.a(this.talktime, 3, false));
        L(fqVar.a(this.calltime, 4, false));
        M(fqVar.a(this.clientlogic, 5, false));
        N(fqVar.a(this.tagtype, 6, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.phonenum, 0);
        frVar.write(this.useraction, 1);
        frVar.write(this.teltype, 2);
        frVar.write(this.talktime, 3);
        frVar.write(this.calltime, 4);
        frVar.write(this.clientlogic, 5);
        frVar.write(this.tagtype, 6);
    }
}
