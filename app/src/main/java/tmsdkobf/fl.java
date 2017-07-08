package tmsdkobf;

import java.util.HashMap;
import java.util.Map;

public final class fl extends fs {
    static final /* synthetic */ boolean fJ = false;
    static byte[] mt;
    static Map<String, String> mu;
    public short mj;
    public byte mk;
    public int ml;
    public int mm;
    public String mn;
    public String mo;
    public byte[] mp;
    public int mq;
    public Map<String, String> mr;
    public Map<String, String> ms;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.fl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.fl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.fl.<clinit>():void");
    }

    public fl() {
        this.mj = (short) 0;
        this.mk = (byte) 0;
        this.ml = 0;
        this.mm = 0;
        this.mn = null;
        this.mo = null;
        this.mq = 0;
    }

    public boolean equals(Object obj) {
        fl flVar = (fl) obj;
        return ft.equals(1, flVar.mj) && ft.equals(1, flVar.mk) && ft.equals(1, flVar.ml) && ft.equals(1, flVar.mm) && ft.equals(Integer.valueOf(1), flVar.mn) && ft.equals(Integer.valueOf(1), flVar.mo) && ft.equals(Integer.valueOf(1), flVar.mp) && ft.equals(1, flVar.mq) && ft.equals(Integer.valueOf(1), flVar.mr) && ft.equals(Integer.valueOf(1), flVar.ms);
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

    public void writeTo(fr frVar) {
        frVar.a(this.mj, 1);
        frVar.b(this.mk, 2);
        frVar.write(this.ml, 3);
        frVar.write(this.mm, 4);
        frVar.a(this.mn, 5);
        frVar.a(this.mo, 6);
        frVar.a(this.mp, 7);
        frVar.write(this.mq, 8);
        frVar.a(this.mr, 9);
        frVar.a(this.ms, 10);
    }

    public void readFrom(fq fqVar) {
        try {
            this.mj = (short) fqVar.a(this.mj, 1, true);
            this.mk = (byte) fqVar.a(this.mk, 2, true);
            this.ml = fqVar.a(this.ml, 3, true);
            this.mm = fqVar.a(this.mm, 4, true);
            this.mn = fqVar.a(5, true);
            this.mo = fqVar.a(6, true);
            if (mt == null) {
                mt = new byte[1];
            }
            this.mp = fqVar.a(mt, 7, true);
            this.mq = fqVar.a(this.mq, 8, true);
            if (mu == null) {
                mu = new HashMap();
                mu.put("", "");
            }
            this.mr = (Map) fqVar.b(mu, 9, true);
            if (mu == null) {
                mu = new HashMap();
                mu.put("", "");
            }
            this.ms = (Map) fqVar.b(mu, 10, true);
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("RequestPacket decode error " + fk.c(this.mp));
            throw new RuntimeException(e);
        }
    }
}
