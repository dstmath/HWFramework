package tmsdkobf;

/* compiled from: Unknown */
class la {
    private static Object lock;
    private static la wu;
    private lf nq;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.la.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.la.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.la.<clinit>():void");
    }

    private la() {
        this.nq = ((ln) fe.ad(9)).getPreferenceService("soft_list_sp_name");
    }

    public static la dA() {
        if (wu == null) {
            synchronized (lock) {
                if (wu == null) {
                    wu = new la();
                }
            }
        }
        return wu;
    }

    public void aZ(int i) {
        this.nq.e("soft_list_profile_full_quantity_", i);
    }

    public boolean dB() {
        return this.nq.getBoolean("soft_list_profile_full_upload_", false);
    }

    public int ds() {
        return this.nq.getInt("soft_list_profile_full_quantity_", 0);
    }

    public void y(boolean z) {
        this.nq.d("soft_list_profile_full_upload_", z);
    }
}
