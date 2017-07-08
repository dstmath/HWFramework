package tmsdkobf;

import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;

/* compiled from: Unknown */
public class kv {
    private static Object lock;
    private static kv wm;
    private lf nq;
    private Boolean wn;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.kv.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.kv.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.kv.<clinit>():void");
    }

    private kv() {
        this.wn = null;
        this.nq = ((ln) fe.ad(9)).getPreferenceService("kv_profile_sp_name");
    }

    public static kv dr() {
        if (wm == null) {
            synchronized (lock) {
                if (wm == null) {
                    wm = new kv();
                }
            }
        }
        return wm;
    }

    public void bd(int i) {
        this.nq.e("kv_profile_full_quantity_", ds() + i);
    }

    public int ds() {
        return this.nq.getInt("kv_profile_full_quantity_", CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY);
    }

    public boolean dt() {
        if (this.wn == null) {
            this.wn = Boolean.valueOf(this.nq.getBoolean("kv_profile_all_report", true));
        }
        return this.wn.booleanValue();
    }

    public void x(boolean z) {
        this.wn = Boolean.valueOf(z);
        this.nq.d("kv_profile_all_report", z);
    }
}
