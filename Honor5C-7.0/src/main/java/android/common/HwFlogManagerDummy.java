package android.common;

import android.content.Context;
import org.json.JSONObject;

public class HwFlogManagerDummy implements HwFlogManager {
    private static HwFlogManager mHwFlogManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.common.HwFlogManagerDummy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.common.HwFlogManagerDummy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.common.HwFlogManagerDummy.<clinit>():void");
    }

    private HwFlogManagerDummy() {
    }

    public static HwFlogManager getDefault() {
        if (mHwFlogManager == null) {
            mHwFlogManager = new HwFlogManagerDummy();
        }
        return mHwFlogManager;
    }

    public int slog(int priority, int tag, String msg) {
        return 0;
    }

    public int slog(int priority, int tag, String msg, Throwable tr) {
        return 0;
    }

    public int slogv(String tag, String msg) {
        return 0;
    }

    public int slogd(String tag, String msg) {
        return 0;
    }

    public boolean handleLogRequest(String[] args) {
        return false;
    }

    public boolean bdReport(Context context, int eventID) {
        return false;
    }

    public boolean bdReport(Context context, int eventID, String eventMsg) {
        return false;
    }

    public boolean bdReport(Context context, int eventID, JSONObject eventMsg) {
        return false;
    }

    public boolean bdReport(Context context, int eventID, JSONObject eventMsg, int priority) {
        return false;
    }
}
