package android.media;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityThread;
import android.content.Context;
import android.os.FreezeScreenScene;
import android.util.Log;
import com.huawei.android.statistical.StatisticalUtils;
import java.util.List;

public class HwMediaMonitorImpl implements IHwMediaMonitor {
    private static final String TAG = "HwMediaMonitorImpl";
    private static IHwMediaMonitor mHwMediaMonitor;
    public final int STATUS_ERROR;
    public final int STATUS_OK;
    public final int STATUS_SERVER_DIED;
    private ErrorCallback mErrorCallback;

    public interface ErrorCallback {
        void onError(int i);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.HwMediaMonitorImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.HwMediaMonitorImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.HwMediaMonitorImpl.<clinit>():void");
    }

    private native int forceLogSend_native(int i);

    private native int writeLogMsg_native(int i, int i2, String str);

    private native int writeMediaBigData_native(int i, int i2, String str);

    public native int checkAudioFlinger();

    public native int systemReady();

    private HwMediaMonitorImpl() {
        this.STATUS_OK = 0;
        this.STATUS_ERROR = 1;
        this.STATUS_SERVER_DIED = 100;
    }

    public static IHwMediaMonitor getDefault() {
        IHwMediaMonitor iHwMediaMonitor;
        synchronized (HwMediaMonitorImpl.class) {
            if (mHwMediaMonitor == null) {
                mHwMediaMonitor = new HwMediaMonitorImpl();
            }
            iHwMediaMonitor = mHwMediaMonitor;
        }
        return iHwMediaMonitor;
    }

    public int writeLogMsg(int priority, int type, String msg) {
        return writeLogMsg_native(priority, type, msg);
    }

    public int writeMediaBigData(int pid, int type, String msg) {
        return writeMediaBigData_native(pid, type, msg);
    }

    public void writeMediaBigDataByReportInf(int pid, int type, String msg) {
        Context context = ActivityThread.currentApplication();
        if (context != null) {
            StatisticalUtils.reporte(context, 60, String.format("{pid:%s, app:%s, type:%s, msg:%s}", new Object[]{Integer.valueOf(pid), getPackageNameByPid(pid), Integer.valueOf(type), msg}));
        }
    }

    private String getPackageNameByPid(int pid) {
        if (pid <= 0) {
            Log.i(TAG, "getPackageNameByPid  pid<=0");
            return null;
        }
        Context context = ActivityThread.currentApplication();
        if (context == null) {
            return null;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(FreezeScreenScene.ACTIVITY_PARAM);
        if (activityManager == null) {
            Log.i(TAG, "getPackageNameByPid  activityManager == null");
            return null;
        }
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            Log.i(TAG, "getPackageNameByPid  appProcesses == null");
            return null;
        }
        String packageName = null;
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.pid == pid) {
                packageName = appProcess.processName;
                break;
            }
        }
        return packageName;
    }

    public int forceLogSend(int level) {
        return forceLogSend_native(level);
    }

    private boolean checkMediaLogPermission() {
        return true;
    }

    public void setErrorCallback(ErrorCallback cb) {
        synchronized (HwMediaMonitorImpl.class) {
            this.mErrorCallback = cb;
            if (cb != null) {
                cb.onError(checkAudioFlinger());
            }
        }
    }

    private void errorCallbackFromNative(int error) {
        ErrorCallback errorCallback = null;
        synchronized (HwMediaMonitorImpl.class) {
            if (this.mErrorCallback != null) {
                errorCallback = this.mErrorCallback;
            }
        }
        if (errorCallback != null) {
            errorCallback.onError(error);
        }
    }
}
