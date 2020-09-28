package android.zrhung.appeye;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.rms.iaware.DataContract;
import android.util.Log;
import android.util.Slog;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;
import com.android.internal.os.BackgroundThread;
import com.huawei.android.app.ActivityManagerEx;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class AppEyeFocusWindow extends ZrHungImpl {
    public static final int CHECK_FOCUS_WINDOW_ERROR_MSG = 1;
    public static final String FOCUS_ACTIVITY_PARAM = "focusedActivityName";
    public static final String FOCUS_PACKAGE_PARAM = "focusedAppPackageName";
    public static final String FOCUS_WINDOW_PARAM = "focusedWindowName";
    public static final String HUNG_CONFIG_ENABLE = "1";
    private static final boolean IS_BETA_VERSION;
    private static final String NULL_STRING = "null";
    private static final String TAG = "ZrHung.AppEyeFocusWindow";
    private static AppEyeFocusWindow appEyeFocusWindow = null;
    private static int checkFreezeScreenDelayTime = 6000;
    private AppEyeFocusWindowHandler mAppEyeFocusWindowHandler;
    private StringBuilder mFocusWindowInfo;
    private String mFocusedActivity;
    private String mFocusedPackage;
    private String mFocusedWindow;
    private ZRHung.HungConfig mHungConfig;
    private String mHungConfigEnable;
    private int mHungConfigStatus;

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.logsystem.usertype", 0) == 3) {
            z = true;
        }
        IS_BETA_VERSION = z;
    }

    public AppEyeFocusWindow(String wpName) {
        super(wpName);
        this.mAppEyeFocusWindowHandler = null;
        this.mFocusWindowInfo = new StringBuilder();
        this.mFocusedPackage = null;
        this.mFocusedActivity = null;
        this.mFocusedWindow = null;
        this.mHungConfig = null;
        this.mHungConfigStatus = -1;
        this.mHungConfigEnable = NULL_STRING;
        this.mAppEyeFocusWindowHandler = new AppEyeFocusWindowHandler(BackgroundThread.getHandler().getLooper());
    }

    public static synchronized AppEyeFocusWindow getInstance(String wpName) {
        AppEyeFocusWindow appEyeFocusWindow2;
        synchronized (AppEyeFocusWindow.class) {
            if (appEyeFocusWindow == null) {
                appEyeFocusWindow = new AppEyeFocusWindow(wpName);
            }
            appEyeFocusWindow2 = appEyeFocusWindow;
        }
        return appEyeFocusWindow2;
    }

    private class AppEyeFocusWindowHandler extends Handler {
        AppEyeFocusWindowHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.d(AppEyeFocusWindow.TAG, "handleMessage CHECK_FOCUS_WINDOW_ERROR_MSG");
                if (msg.obj instanceof ZrHungData) {
                    AppEyeFocusWindow.this.sendEvent((ZrHungData) msg.obj);
                }
            }
        }
    }

    @Override // android.zrhung.ZrHungImpl
    public int init(ZrHungData zrHungData) {
        if (this.mHungConfig == null || this.mHungConfigStatus == 1) {
            this.mHungConfig = ZRHung.getHungConfig(ZRHung.APPEYE_NFW);
            ZRHung.HungConfig hungConfig = this.mHungConfig;
            if (hungConfig != null) {
                this.mHungConfigStatus = hungConfig.status;
                String[] values = this.mHungConfig.value.split(",");
                this.mHungConfigEnable = values[0];
                if (values.length > 1) {
                    checkFreezeScreenDelayTime = parseInt(values[1]);
                }
            }
        }
        return 0;
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseInt NumberFormatException");
            return -1;
        }
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean check(ZrHungData zrHungData) {
        if (zrHungData == null) {
            return false;
        }
        init(null);
        Object focusedWindow = zrHungData.get("focusedWindowName");
        Object focusedPackage = zrHungData.get(FOCUS_PACKAGE_PARAM);
        Object focusedActivity = zrHungData.get("focusedActivityName");
        if (!(focusedWindow instanceof String) || !(focusedPackage instanceof String) || !(focusedActivity instanceof String)) {
            return false;
        }
        this.mFocusedWindow = (String) focusedWindow;
        this.mFocusedPackage = (String) focusedPackage;
        this.mFocusedActivity = (String) focusedActivity;
        this.mAppEyeFocusWindowHandler.removeMessages(1);
        Message msg = this.mAppEyeFocusWindowHandler.obtainMessage(1);
        if (msg != null) {
            msg.obj = zrHungData;
            this.mAppEyeFocusWindowHandler.sendMessageDelayed(msg, (long) checkFreezeScreenDelayTime);
            Log.d(TAG, "FocusWindowErrorScene CheckFreezeScreen");
        }
        return true;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean cancelCheck(ZrHungData zrHungData) {
        if (zrHungData == null) {
            return false;
        }
        try {
            Object focusedWindow = zrHungData.get("focusedWindowName");
            Object focusedPackage = zrHungData.get(FOCUS_PACKAGE_PARAM);
            Object focusedActivity = zrHungData.get("focusedActivityName");
            if (!(focusedWindow instanceof String) || !(focusedPackage instanceof String) || !(focusedActivity instanceof String)) {
                return false;
            }
            this.mFocusedWindow = (String) focusedWindow;
            this.mFocusedPackage = (String) focusedPackage;
            this.mFocusedActivity = (String) focusedActivity;
            if (this.mAppEyeFocusWindowHandler != null && this.mAppEyeFocusWindowHandler.hasMessages(1)) {
                this.mAppEyeFocusWindowHandler.removeMessages(1);
                Log.d(TAG, "FocusWindowErrorScene cancelCheckFreezeScreen");
            }
            this.mFocusWindowInfo.delete(0, this.mFocusWindowInfo.length());
            return true;
        } catch (Exception e) {
            Slog.e(TAG, "cancel check exception");
            return false;
        }
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean sendEvent(ZrHungData zrHungData) {
        if (NULL_STRING.equals(this.mFocusedWindow)) {
            StringBuilder eventBuffer = new StringBuilder();
            eventBuffer.append("FocusWindowErrorScene find freezeScreen");
            eventBuffer.append(System.lineSeparator());
            eventBuffer.append("FOCUS_PACKAGE: ");
            eventBuffer.append(this.mFocusedPackage);
            eventBuffer.append(System.lineSeparator());
            eventBuffer.append("FOCUS_WINDOW: ");
            eventBuffer.append(this.mFocusedWindow);
            eventBuffer.append(System.lineSeparator());
            eventBuffer.append("FOCUS_ACTIVITY: ");
            eventBuffer.append(this.mFocusedActivity);
            eventBuffer.append(System.lineSeparator());
            eventBuffer.append("FocusWindowInfo: ");
            eventBuffer.append(this.mFocusWindowInfo.toString());
            eventBuffer.append(System.lineSeparator());
            Log.i(TAG, eventBuffer.toString());
            if (this.mHungConfig != null && this.mHungConfigStatus == 0 && "1".equals(this.mHungConfigEnable)) {
                ZrHungData data = new ZrHungData();
                data.putString(DataContract.BaseProperty.PACKAGE_NAME, this.mFocusedPackage);
                updateUploadInfo(data, this.mFocusedPackage);
                sendAppEyeEvent(ZRHung.APPEYE_NFW, data, null, eventBuffer.toString());
            }
        }
        StringBuilder eventBuffer2 = this.mFocusWindowInfo;
        eventBuffer2.delete(0, eventBuffer2.length());
        return true;
    }

    @Override // android.zrhung.ZrHungImpl
    public synchronized boolean addInfo(ZrHungData zrHungData) {
        if (zrHungData == null) {
            return false;
        }
        if (Binder.getCallingUid() >= 10000) {
            Log.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        }
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        if (this.mFocusWindowInfo == null) {
            return false;
        }
        StringBuilder sb = this.mFocusWindowInfo;
        sb.append(nowTime);
        sb.append(":");
        sb.append(zrHungData.getString("addFocusWindowInfo"));
        sb.append(System.lineSeparator());
        return true;
    }

    private void updateUploadInfo(ZrHungData data, String pkgName) {
        if (!"false".equals(SystemProperties.get("ro.feature.dfr.appeye")) || IS_BETA_VERSION) {
            Bundle topBundle = ActivityManagerEx.getTopActivity();
            if (topBundle == null) {
                Log.d(TAG, "can not get current top activity!");
                return;
            }
            ActivityInfo info = (ActivityInfo) topBundle.getParcelable("activityInfo");
            if (info != null && info.applicationInfo != null) {
                ApplicationInfo appInfo = info.applicationInfo;
                if (!pkgName.equals(appInfo.packageName)) {
                    data.remove(DataContract.BaseProperty.PACKAGE_NAME);
                    data.putString(DataContract.BaseProperty.PACKAGE_NAME, appInfo.packageName);
                }
                data.putInt(DataContract.BaseProperty.UID, appInfo.uid);
                data.putString("processName", appInfo.processName);
            }
        }
    }
}
