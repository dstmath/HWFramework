package android.media;

public class HwMediaMonitorDummy implements IHwMediaMonitor {
    private static final String TAG = "HwMediaMonitorDummy";
    private static IHwMediaMonitor mHwMediaMonitor;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.HwMediaMonitorDummy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.HwMediaMonitorDummy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.HwMediaMonitorDummy.<clinit>():void");
    }

    public static IHwMediaMonitor getDefault() {
        IHwMediaMonitor iHwMediaMonitor;
        synchronized (HwMediaMonitorDummy.class) {
            if (mHwMediaMonitor == null) {
                mHwMediaMonitor = new HwMediaMonitorDummy();
            }
            iHwMediaMonitor = mHwMediaMonitor;
        }
        return iHwMediaMonitor;
    }

    public int writeLogMsg(int priority, int type, String msg) {
        return 0;
    }

    public int writeMediaBigData(int pid, int type, String msg) {
        return 0;
    }

    public void writeMediaBigDataByReportInf(int pid, int type, String msg) {
    }

    public int forceLogSend(int level) {
        return 0;
    }
}
