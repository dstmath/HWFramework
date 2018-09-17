package tmsdkobf;

import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;

/* compiled from: Unknown */
public final class ea extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public int advice;
    public String iH;
    public int id;
    public String jk;
    public int jl;
    public int level;
    public String name;
    public int type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ea.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ea.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ea.<clinit>():void");
    }

    public ea() {
        this.id = 0;
        this.name = "";
        this.jk = "";
        this.level = 0;
        this.advice = 0;
        this.iH = "";
        this.jl = 0;
        this.type = 0;
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
        foVar.a(this.id, "id");
        foVar.a(this.name, CheckVersionField.CHECK_VERSION_NAME);
        foVar.a(this.jk, "shortdesc");
        foVar.a(this.level, "level");
        foVar.a(this.advice, "advice");
        foVar.a(this.iH, "desc");
        foVar.a(this.jl, "scan");
        foVar.a(this.type, "type");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        ea eaVar = (ea) obj;
        if (ft.equals(this.id, eaVar.id) && ft.equals(this.name, eaVar.name) && ft.equals(this.jk, eaVar.jk) && ft.equals(this.level, eaVar.level) && ft.equals(this.advice, eaVar.advice) && ft.equals(this.iH, eaVar.iH) && ft.equals(this.jl, eaVar.jl) && ft.equals(this.type, eaVar.type)) {
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
        this.id = fqVar.a(this.id, 0, true);
        this.name = fqVar.a(1, true);
        this.jk = fqVar.a(2, true);
        this.level = fqVar.a(this.level, 3, true);
        this.advice = fqVar.a(this.advice, 4, true);
        this.iH = fqVar.a(5, true);
        this.jl = fqVar.a(this.jl, 6, true);
        this.type = fqVar.a(this.type, 7, true);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.id, 0);
        frVar.a(this.name, 1);
        frVar.a(this.jk, 2);
        frVar.write(this.level, 3);
        frVar.write(this.advice, 4);
        frVar.a(this.iH, 5);
        frVar.write(this.jl, 6);
        frVar.write(this.type, 7);
    }
}
