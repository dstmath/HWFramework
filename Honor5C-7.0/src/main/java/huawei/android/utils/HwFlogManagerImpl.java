package huawei.android.utils;

import android.common.HwFlogManager;
import android.content.Context;
import com.huawei.bd.Reporter;
import huawei.android.pfw.HwPFWStartupPackageList;
import huawei.android.view.HwMotionEvent;
import huawei.com.android.internal.widget.HwFragmentContainer;
import org.json.JSONObject;

public class HwFlogManagerImpl implements HwFlogManager {
    private static final boolean LOCAL_LOGV = false;
    private static final String TAG = "HwFlogManagerImpl";
    private static HwFlogManager mHwFlogManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.utils.HwFlogManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.utils.HwFlogManagerImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.utils.HwFlogManagerImpl.<clinit>():void");
    }

    private HwFlogManagerImpl() {
    }

    public static HwFlogManager getDefault() {
        if (mHwFlogManager == null) {
            mHwFlogManager = new HwFlogManagerImpl();
        }
        return mHwFlogManager;
    }

    public int slogv(String tag, String msg) {
        return HwCoreServicesLog.v(tag, msg);
    }

    public int slogd(String tag, String msg) {
        return HwCoreServicesLog.d(tag, msg);
    }

    public boolean handleLogRequest(String[] args) {
        return HwCoreServicesLog.handleLogRequest(args);
    }

    public int slog(int priority, int tag, String msg) {
        switch (priority) {
            case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
                return HwCoreServicesLog.v(tag, msg);
            case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                return HwCoreServicesLog.d(tag, msg);
            case HwPFWStartupPackageList.STARTUP_LIST_TYPE_MUST_CONTROL_APPS /*4*/:
                return HwCoreServicesLog.i(tag, msg);
            case HwMotionEvent.TOOL_TYPE_FINGER_TIP /*5*/:
                return HwCoreServicesLog.w(tag, msg);
            case HwMotionEvent.TOOL_TYPE_FINGER_NAIL /*6*/:
                return HwCoreServicesLog.e(tag, msg);
            default:
                return -1;
        }
    }

    public int slog(int priority, int tag, String msg, Throwable tr) {
        switch (priority) {
            case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
                return HwCoreServicesLog.v(tag, msg, tr);
            case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                return HwCoreServicesLog.d(tag, msg, tr);
            case HwPFWStartupPackageList.STARTUP_LIST_TYPE_MUST_CONTROL_APPS /*4*/:
                return HwCoreServicesLog.i(tag, msg, tr);
            case HwMotionEvent.TOOL_TYPE_FINGER_TIP /*5*/:
                return HwCoreServicesLog.w(tag, msg, tr);
            case HwMotionEvent.TOOL_TYPE_FINGER_NAIL /*6*/:
                return HwCoreServicesLog.e(tag, msg, tr);
            default:
                return -1;
        }
    }

    public boolean bdReport(Context context, int eventID) {
        Reporter.c(context, eventID);
        return true;
    }

    public boolean bdReport(Context context, int eventID, String eventMsg) {
        Reporter.e(context, eventID, eventMsg);
        return true;
    }

    public boolean bdReport(Context context, int eventID, JSONObject eventMsg) {
        Reporter.j(context, eventID, eventMsg);
        return true;
    }

    public boolean bdReport(Context context, int eventID, JSONObject eventMsg, int priority) {
        Reporter.j(context, eventID, eventMsg, priority);
        return true;
    }
}
