package tmsdkobf;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class fj extends fi {
    static HashMap<String, byte[]> me;
    static HashMap<String, HashMap<String, byte[]>> mf;
    protected fl md;
    private int mg;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.fj.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.fj.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.fj.<clinit>():void");
    }

    public /* bridge */ /* synthetic */ void Z(String str) {
        super.Z(str);
    }

    public fj() {
        this.md = new fl();
        this.mg = 0;
        this.md.mj = (short) 2;
    }

    public fj(boolean z) {
        this.md = new fl();
        this.mg = 0;
        if (z) {
            n();
        } else {
            this.md.mj = (short) 2;
        }
    }

    public <T> void put(String str, T t) {
        if (str.startsWith(".")) {
            throw new IllegalArgumentException("put name can not startwith . , now is " + str);
        }
        super.put(str, t);
    }

    public void n() {
        super.n();
        this.md.mj = (short) 3;
    }

    public byte[] m() {
        if (this.md.mj != (short) 2) {
            if (this.md.mn == null) {
                this.md.mn = "";
            }
            if (this.md.mo == null) {
                this.md.mo = "";
            }
        } else if (this.md.mn == null || this.md.mn.equals("")) {
            throw new IllegalArgumentException("servantName can not is null");
        } else if (this.md.mo == null || this.md.mo.equals("")) {
            throw new IllegalArgumentException("funcName can not is null");
        }
        fr frVar = new fr(0);
        frVar.ae(this.ma);
        if (this.md.mj != (short) 2) {
            frVar.a(this.mc, 0);
        } else {
            frVar.a(this.lX, 0);
        }
        this.md.mp = ft.a(frVar.t());
        frVar = new fr(0);
        frVar.ae(this.ma);
        this.md.writeTo(frVar);
        byte[] a = ft.a(frVar.t());
        int length = a.length;
        ByteBuffer allocate = ByteBuffer.allocate(length + 4);
        allocate.putInt(length + 4).put(a).flip();
        return allocate.array();
    }

    private void o() {
        fq fqVar = new fq(this.md.mp);
        fqVar.ae(this.ma);
        if (me == null) {
            me = new HashMap();
            me.put("", new byte[0]);
        }
        this.mc = fqVar.a(me, 0, false);
    }

    private void p() {
        fq fqVar = new fq(this.md.mp);
        fqVar.ae(this.ma);
        if (mf == null) {
            mf = new HashMap();
            HashMap hashMap = new HashMap();
            hashMap.put("", new byte[0]);
            mf.put("", hashMap);
        }
        this.lX = fqVar.a(mf, 0, false);
        this.lY = new HashMap();
    }

    public void b(byte[] bArr) {
        if (bArr.length >= 4) {
            try {
                fq fqVar = new fq(bArr, 4);
                fqVar.ae(this.ma);
                this.md.readFrom(fqVar);
                if (this.md.mj != (short) 3) {
                    this.mc = null;
                    p();
                    return;
                }
                o();
                return;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalArgumentException("decode package must include size head");
    }

    public void aa(String str) {
        this.md.mn = str;
    }

    public void ab(String str) {
        this.md.mo = str;
    }

    public int q() {
        return this.md.mm;
    }

    public void ae(int i) {
        this.md.mm = i;
    }
}
