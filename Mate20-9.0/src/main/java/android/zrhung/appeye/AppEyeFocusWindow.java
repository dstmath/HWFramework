package android.zrhung.appeye;

import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Slog;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;
import com.android.internal.os.BackgroundThread;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class AppEyeFocusWindow extends ZrHungImpl {
    public static final int CHECK_FOCUS_WINDOW_ERROR_MSG = 1;
    public static final String FOCUS_ACTIVITY_PARAM = "focusedActivityName";
    public static final String FOCUS_PACKAGE_PARAM = "focusedPackageName";
    public static final String FOCUS_WINDOW_PARAM = "focusedWindowName";
    public static final String HUNG_CONFIG_ENABLE = "1";
    private static final String NULL_STRING = "null";
    private static final String TAG = "ZrHung.AppEyeFocusWindow";
    private static AppEyeFocusWindow mAppEyeFocusWindow = null;
    private static int mCheckFreezeScreenDelayTime = 6000;
    private AppEyeFocusWindowHandler mAppEyeFocusWindowHandler;
    private StringBuilder mFocusWindowInfo;
    private String mFocusedActivity;
    private String mFocusedPackage;
    private String mFocusedWindow;
    private ZRHung.HungConfig mHungConfig;
    private String mHungConfigEnable;
    private int mHungConfigStatus;

    private class AppEyeFocusWindowHandler extends Handler {
        public AppEyeFocusWindowHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.d(AppEyeFocusWindow.TAG, "handleMessage CHECK_FOCUS_WINDOW_ERROR_MSG");
                AppEyeFocusWindow.this.sendEvent((ZrHungData) msg.obj);
            }
        }
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
        AppEyeFocusWindow appEyeFocusWindow;
        synchronized (AppEyeFocusWindow.class) {
            if (mAppEyeFocusWindow == null) {
                mAppEyeFocusWindow = new AppEyeFocusWindow(wpName);
            }
            appEyeFocusWindow = mAppEyeFocusWindow;
        }
        return appEyeFocusWindow;
    }

    public int init(ZrHungData args) {
        try {
            if (this.mHungConfig == null || this.mHungConfigStatus == 1) {
                this.mHungConfig = ZRHung.getHungConfig(272);
                if (this.mHungConfig != null) {
                    this.mHungConfigStatus = this.mHungConfig.status;
                    String[] value = this.mHungConfig.value.split(",");
                    this.mHungConfigEnable = value[0];
                    if (value.length > 1) {
                        mCheckFreezeScreenDelayTime = Integer.parseInt(value[1]);
                    }
                }
            }
            return 0;
        } catch (Exception ex) {
            Slog.e(TAG, "exception info ex:" + ex);
            return -2;
        }
    }

    public boolean check(ZrHungData args) {
        try {
            init(null);
            this.mFocusedWindow = (String) args.get("focusedWindowName");
            this.mFocusedPackage = (String) args.get("focusedPackageName");
            this.mFocusedActivity = (String) args.get("focusedActivityName");
            this.mAppEyeFocusWindowHandler.removeMessages(1);
            Message msg = this.mAppEyeFocusWindowHandler.obtainMessage(1);
            if (msg != null) {
                msg.obj = args;
                this.mAppEyeFocusWindowHandler.sendMessageDelayed(msg, (long) mCheckFreezeScreenDelayTime);
                Log.d(TAG, "FocusWindowErrorScene CheckFreezeScreen");
            }
            return true;
        } catch (Exception ex) {
            Slog.e(TAG, "exception info ex:" + ex);
            return false;
        }
    }

    public boolean cancelCheck(ZrHungData args) {
        try {
            this.mFocusedWindow = (String) args.get("focusedWindowName");
            this.mFocusedPackage = (String) args.get("focusedPackageName");
            this.mFocusedActivity = (String) args.get("focusedActivityName");
            if (this.mAppEyeFocusWindowHandler != null && this.mAppEyeFocusWindowHandler.hasMessages(1)) {
                this.mAppEyeFocusWindowHandler.removeMessages(1);
                Log.d(TAG, "FocusWindowErrorScene cancelCheckFreezeScreen");
            }
            this.mFocusWindowInfo.delete(0, this.mFocusWindowInfo.length());
            return true;
        } catch (Exception ex) {
            Slog.e(TAG, "exception info ex:" + ex);
            return false;
        }
    }

    public boolean sendEvent(ZrHungData args) {
        try {
            if (NULL_STRING.equals(this.mFocusedWindow)) {
                StringBuilder sb = new StringBuilder();
                sb.append("FocusWindowErrorScene find freezeScreen");
                sb.append("\n");
                sb.append("FOCUS_PACKAGE: ");
                sb.append(this.mFocusedPackage);
                sb.append("\n");
                sb.append("FOCUS_WINDOW: ");
                sb.append(this.mFocusedWindow);
                sb.append("\n");
                sb.append("FOCUS_ACTIVITY: ");
                sb.append(this.mFocusedActivity);
                sb.append("\n");
                sb.append("FocusWindowInfo: ");
                sb.append(this.mFocusWindowInfo.toString());
                sb.append("\n");
                Log.i(TAG, sb.toString());
                if (this.mHungConfig != null && this.mHungConfigStatus == 0 && "1".equals(this.mHungConfigEnable)) {
                    ZrHungData data = new ZrHungData();
                    data.putString("packageName", this.mFocusedPackage);
                    sendAppEyeEvent(272, data, null, sb.toString());
                }
            }
            this.mFocusWindowInfo.delete(0, this.mFocusWindowInfo.length());
            return true;
        } catch (Exception ex) {
            Slog.e(TAG, "exception info ex:" + ex);
            return false;
        }
    }

    public synchronized boolean addInfo(ZrHungData args) {
        try {
            if (10000 <= Binder.getCallingUid()) {
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
            sb.append(args.getString("addFocusWindowInfo"));
            sb.append("\n");
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "exception info ex:" + ex);
            return false;
        }
    }
}
