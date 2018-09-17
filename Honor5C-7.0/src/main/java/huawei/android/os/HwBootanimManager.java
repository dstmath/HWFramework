package huawei.android.os;

public class HwBootanimManager {
    private static final String TAG = "HwBootanimManager";
    private static volatile HwBootanimManager mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.os.HwBootanimManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.os.HwBootanimManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.os.HwBootanimManager.<clinit>():void");
    }

    public static synchronized HwBootanimManager getInstance() {
        HwBootanimManager hwBootanimManager;
        synchronized (HwBootanimManager.class) {
            if (mInstance == null) {
                mInstance = new HwBootanimManager();
            }
            hwBootanimManager = mInstance;
        }
        return hwBootanimManager;
    }

    public void switchBootOrShutSound(String openOrClose) {
        HwGeneralManager.getInstance().switchBootOrShutSound(openOrClose);
    }

    public boolean isBootOrShutdownSoundCapable() {
        return HwGeneralManager.getInstance().isBootOrShutdownSoundCapable();
    }

    public int getBootAnimSoundSwitch() {
        return HwGeneralManager.getInstance().getBootAnimSoundSwitch();
    }
}
