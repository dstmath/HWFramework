package android.zrhung.appeye;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Slog;
import android.util.ZRHung;
import android.view.WindowManager;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;
import com.android.internal.os.BackgroundThread;
import com.huawei.hsm.permission.StubController;

public final class AppEyeTransparentWindow extends ZrHungImpl {
    public static final int CHECK_TRANS_WINDOW_ERROR_MSG = 1;
    public static final String FOCUS_ACTIVITY_PARAM = "focusedActivityName";
    public static final String FOCUS_PACKAGE_PARAM = "focusedPackageName";
    public static final String FOCUS_WINDOW_PARAM = "focusedWindowName";
    public static final String HUNG_CONFIG_ENABLE = "1";
    public static final String LAYOUT_PARAM = "layoutParams";
    private static final String NULL_STRING = "null";
    private static final String TAG = "ZrHung.AppEyeTransparentWindow";
    private static AppEyeTransparentWindow mAppEyeTransparentWindow = null;
    private static int mCheckFreezeScreenDelayTime = 6000;
    private AppEyeTransparentWindowHandler mAppEyeTransparentWindowHandler;
    private String mFocusedWindow;
    private ZRHung.HungConfig mHungConfig;
    private String mHungConfigEnable;
    private int mHungConfigStatus;
    private String mTransActivity;
    private String mTransPackage;

    private class AppEyeTransparentWindowHandler extends Handler {
        public AppEyeTransparentWindowHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.d(AppEyeTransparentWindow.TAG, "handleMessage CHECK_TRANS_WINDOW_ERROR_MSG");
                AppEyeTransparentWindow.this.sendEvent((ZrHungData) msg.obj);
            }
        }
    }

    public AppEyeTransparentWindow(String wpName) {
        super(wpName);
        this.mAppEyeTransparentWindowHandler = null;
        this.mTransPackage = null;
        this.mTransActivity = null;
        this.mFocusedWindow = null;
        this.mHungConfig = null;
        this.mHungConfigStatus = -1;
        this.mHungConfigEnable = NULL_STRING;
        this.mAppEyeTransparentWindowHandler = new AppEyeTransparentWindowHandler(BackgroundThread.getHandler().getLooper());
    }

    public static synchronized AppEyeTransparentWindow getInstance(String wpName) {
        AppEyeTransparentWindow appEyeTransparentWindow;
        synchronized (AppEyeTransparentWindow.class) {
            if (mAppEyeTransparentWindow == null) {
                mAppEyeTransparentWindow = new AppEyeTransparentWindow(wpName);
            }
            appEyeTransparentWindow = mAppEyeTransparentWindow;
        }
        return appEyeTransparentWindow;
    }

    public int init(ZrHungData args) {
        try {
            if (this.mHungConfig == null || this.mHungConfigStatus == 1) {
                this.mHungConfig = ZRHung.getHungConfig(273);
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
            this.mTransPackage = (String) args.get("focusedPackageName");
            this.mTransActivity = (String) args.get("focusedActivityName");
            Log.d(TAG, "TransparentWindow mHungConfigStatus = " + this.mHungConfigStatus + ",mHungConfigEnable = " + this.mHungConfigEnable);
            Message msg = this.mAppEyeTransparentWindowHandler.obtainMessage(1);
            if (msg != null) {
                msg.obj = args;
                this.mAppEyeTransparentWindowHandler.sendMessageDelayed(msg, (long) mCheckFreezeScreenDelayTime);
            }
            return true;
        } catch (Exception ex) {
            Slog.e(TAG, "exception info ex:" + ex);
            return false;
        }
    }

    public boolean cancelCheck(ZrHungData args) {
        try {
            if (this.mAppEyeTransparentWindowHandler != null && this.mAppEyeTransparentWindowHandler.hasMessages(1)) {
                this.mAppEyeTransparentWindowHandler.removeMessages(1);
                Log.d(TAG, "TransparentWindow cancelCheckFreezeScreen");
            }
            return true;
        } catch (Exception ex) {
            Slog.e(TAG, "exception info ex:" + ex);
            return false;
        }
    }

    public boolean sendEvent(ZrHungData args) {
        try {
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) args.get("layoutParams");
            if (isWinMayCauseFreezeScreen(layoutParams)) {
                StringBuilder sb = new StringBuilder();
                Boolean isTranslucent = Boolean.valueOf(isTranslucentThemeWithoutActionBar(layoutParams));
                Boolean isZeroPixel = Boolean.valueOf(isZeroPixelWindow(layoutParams));
                Object obj = args.get("pid");
                int pid = obj instanceof Integer ? ((Integer) obj).intValue() : 0;
                sb.append("TransparentActivityScene find freezeScreen");
                sb.append("\n");
                sb.append("CURR_PACKAGE: ");
                sb.append(this.mTransPackage);
                sb.append("\n");
                sb.append("CURR_WINDOW: ");
                sb.append(this.mFocusedWindow);
                sb.append("\n");
                sb.append("CURR_ACTIVITY: ");
                sb.append(this.mTransActivity);
                sb.append("\n");
                sb.append("ALPHA: ");
                sb.append(layoutParams.alpha);
                sb.append("\n");
                sb.append("TRANSLUCENT: ");
                sb.append(isTranslucent);
                sb.append("\n");
                sb.append("ZEROPIXEL: ");
                sb.append(isZeroPixel);
                sb.append("\n");
                if (isZeroPixel.booleanValue()) {
                    sb.append("isFault = true");
                }
                Log.i(TAG, sb.toString());
                if (this.mHungConfig != null && this.mHungConfigStatus == 0 && "1".equals(this.mHungConfigEnable)) {
                    ZrHungData data = new ZrHungData();
                    data.putString("packageName", this.mTransPackage);
                    data.putInt("pid", pid);
                    sendAppEyeEvent(273, data, null, sb.toString());
                }
            }
            return true;
        } catch (Exception ex) {
            Slog.e(TAG, "exception info ex:" + ex);
            return false;
        }
    }

    private final boolean isWinMayCauseFreezeScreen(WindowManager.LayoutParams l) {
        if (l == null) {
            return false;
        }
        if (isZeroAlphaWindow(l) || isZeroPixelWindow(l)) {
            return true;
        }
        return false;
    }

    private final boolean isZeroAlphaWindow(WindowManager.LayoutParams l) {
        return l.alpha < 0.01f;
    }

    private final boolean isZeroPixelWindow(WindowManager.LayoutParams l) {
        return l.width <= 1 && l.width >= 0 && l.height <= 1 && l.height >= 0;
    }

    private final boolean isTranslucentThemeWithoutActionBar(WindowManager.LayoutParams l) {
        return ((l.hwFlags & 268435456) == 0 || (l.hwFlags & StubController.PERMISSION_DELETE_CALENDAR) == 0) ? false : true;
    }
}
