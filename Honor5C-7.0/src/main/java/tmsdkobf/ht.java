package tmsdkobf;

/* compiled from: Unknown */
public class ht {
    private static Object lock;
    private static ht qZ;
    private lf nq;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ht.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ht.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ht.<clinit>():void");
    }

    private ht() {
        this.nq = ((ln) fe.ad(9)).getPreferenceService("prfle_cnfg_dao");
    }

    private String aN(int i) {
        return "profile_quantity_" + i;
    }

    private String aO(int i) {
        return "profile_last_enqueue_key_" + i;
    }

    public static ht bD() {
        if (qZ == null) {
            synchronized (lock) {
                if (qZ == null) {
                    qZ = new ht();
                }
            }
        }
        return qZ;
    }

    private ak bj(String str) {
        if (str == null || str.equals("")) {
            return null;
        }
        fq fqVar = new fq(mo.cw(str));
        fqVar.ae("UTF-8");
        return (ak) fqVar.a(new ak(), 0, false);
    }

    private String c(ak akVar) {
        if (akVar == null) {
            return "";
        }
        fr frVar = new fr();
        frVar.ae("UTF-8");
        frVar.a((fs) akVar, 0);
        return mo.bytesToHexString(frVar.toByteArray());
    }

    public void a(ak akVar) {
        if (akVar != null) {
            this.nq.m(aO(akVar.bf), c(akVar));
        }
    }

    public boolean aJ(int i) {
        return this.nq.getBoolean("prf_upl_exception_" + i, false);
    }

    public ak aK(int i) {
        String string = this.nq.getString(aO(i), null);
        return string != null ? bj(string) : null;
    }

    public int aL(int i) {
        return this.nq.getInt(aN(i), 0);
    }

    public void aM(int i) {
        this.nq.e(aN(i), 0);
    }

    public void b(int i, boolean z) {
        this.nq.d("prf_upl_exception_" + i, z);
    }

    public boolean b(ak akVar) {
        return hu.a(aK(akVar.bf), akVar);
    }

    public int bE() {
        return this.nq.getInt("profile_task_id", 0);
    }

    public void bF() {
        int bE = bE();
        if (bE < 0) {
            bE = 0;
        }
        this.nq.e("profile_task_id", bE + 1);
    }

    public void e(int i, int i2) {
        this.nq.e(aN(i), aL(i) + i2);
    }

    public void f(int i, int i2) {
        this.nq.e(aN(i), aL(i) - i2);
    }

    public void u(boolean z) {
        this.nq.d("profile_soft_list_upload_opened", z);
    }
}
