package android.media;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.content.Context;
import android.util.Log;
import java.util.Iterator;
import java.util.List;

public class HwMediaMonitorImpl implements IHwMediaMonitor {
    public static final int STATUS_ERROR = 1;
    public static final int STATUS_OK = 0;
    public static final int STATUS_SERVER_DIED = 100;
    private static final String TAG = "HwMediaMonitorImpl";
    private static IHwMediaMonitor mHwMediaMonitor = null;
    private ErrorCallback mErrorCallback;

    public interface ErrorCallback {
        void onError(int i);
    }

    private native int forceLogSendNative(int i);

    private native int writeBigDataNative(int i, String str);

    private native int writeBigDataNative(int i, String str, int i2, int i3);

    private native int writeBigDataNative(int i, String str, String str2, int i2);

    private native int writeKpisNative(String str);

    private native int writeLogMsgNative(int i, int i2, int i3, String str);

    private native int writeLogMsgNative(int i, int i2, String str);

    private native int writeMediaBigDataNative(int i, int i2, String str);

    public native int checkAudioFlinger();

    public native int systemReady();

    static {
        System.loadLibrary("mediamonitor_jni");
    }

    private HwMediaMonitorImpl() {
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
        return writeLogMsgNative(priority, type, msg);
    }

    public int writeLogMsg(int eventId, int eventLevel, int subType, String reason) {
        return writeLogMsgNative(eventId, eventLevel, subType, reason);
    }

    public int writeKpis(String kpis) {
        return writeKpisNative(kpis);
    }

    public int writeBigData(int eventId, String subType) {
        return writeBigDataNative(eventId, subType);
    }

    public int writeBigData(int eventId, String subType, int ext1, int ext2) {
        return writeBigDataNative(eventId, subType, ext1, ext2);
    }

    public int writeBigData(int eventId, String subType, String sext1, int ext2) {
        return writeBigDataNative(eventId, subType, sext1, ext2);
    }

    public int writeMediaBigData(int pid, int type, String msg) {
        return writeMediaBigDataNative(pid, type, msg);
    }

    public void writeMediaBigDataByReportInf(int pid, int type, String msg) {
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
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        if (activityManager == null) {
            Log.i(TAG, "getPackageNameByPid  activityManager == null");
            return null;
        }
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            Log.i(TAG, "getPackageNameByPid  appProcesses == null");
            return null;
        }
        String packageName = null;
        Iterator<ActivityManager.RunningAppProcessInfo> it = appProcesses.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ActivityManager.RunningAppProcessInfo appProcess = it.next();
            if (appProcess.pid == pid) {
                packageName = appProcess.processName;
                break;
            }
        }
        return packageName;
    }

    public int forceLogSend(int level) {
        return forceLogSendNative(level);
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
