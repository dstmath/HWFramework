package android.zrhung.appeye;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.ZRHung;
import android.view.WindowManager;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;
import com.huawei.android.internal.os.BackgroundThreadEx;
import com.huawei.android.os.HandlerEx;
import com.huawei.android.view.WindowManagerEx;

public final class AppEyeTransparentWindow extends ZrHungImpl {
    public static final int CHECK_TRANS_WINDOW_ERROR_MSG = 1;
    public static final String FOCUS_ACTIVITY_PARAM = "focusedActivityName";
    public static final String FOCUS_PACKAGE_PARAM = "focusedWinPackageName";
    public static final String FOCUS_WINDOW_PARAM = "focusedWindowName";
    public static final String HUNG_CONFIG_ENABLE = "1";
    public static final String LAYOUT_PARAM = "layoutParams";
    private static final String NULL_STRING = "null";
    private static final String TAG = "ZrHung.AppEyeTransparentWindow";
    private static AppEyeTransparentWindow appEyeTransparentWindow = null;
    private static int checkFreezeScreenDelayTime = 6000;
    private AppEyeTransparentWindowHandler mAppEyeTransparentWindowHandler;
    private String mFocusedWindow;
    private ZRHung.HungConfig mHungConfig;
    private String mHungConfigEnable;
    private int mHungConfigStatus;
    private String mTransActivity;
    private String mTransPackage;

    public AppEyeTransparentWindow(String wpName) {
        super(wpName);
        this.mAppEyeTransparentWindowHandler = null;
        this.mTransPackage = null;
        this.mTransActivity = null;
        this.mFocusedWindow = null;
        this.mHungConfig = null;
        this.mHungConfigStatus = -1;
        this.mHungConfigEnable = NULL_STRING;
        this.mAppEyeTransparentWindowHandler = new AppEyeTransparentWindowHandler(BackgroundThreadEx.getHandler().getLooper());
    }

    public static synchronized AppEyeTransparentWindow getInstance(String wpName) {
        AppEyeTransparentWindow appEyeTransparentWindow2;
        synchronized (AppEyeTransparentWindow.class) {
            if (appEyeTransparentWindow == null) {
                appEyeTransparentWindow = new AppEyeTransparentWindow(wpName);
            }
            appEyeTransparentWindow2 = appEyeTransparentWindow;
        }
        return appEyeTransparentWindow2;
    }

    private class AppEyeTransparentWindowHandler extends HandlerEx {
        AppEyeTransparentWindowHandler(Looper looper) {
            super(looper, (Handler.Callback) null, false);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.d(AppEyeTransparentWindow.TAG, "handleMessage CHECK_TRANS_WINDOW_ERROR_MSG");
                if (msg.obj instanceof ZrHungData) {
                    AppEyeTransparentWindow.this.sendEvent((ZrHungData) msg.obj);
                }
            }
        }
    }

    @Override // android.zrhung.ZrHungImpl
    public int init(ZrHungData zrHungData) {
        if (this.mHungConfig == null || this.mHungConfigStatus == 1) {
            this.mHungConfig = ZRHung.getHungConfig(273);
            ZRHung.HungConfig hungConfig = this.mHungConfig;
            if (hungConfig != null) {
                this.mHungConfigStatus = hungConfig.status;
                String[] values = this.mHungConfig.value.split(",");
                this.mHungConfigEnable = values[0];
                if (values.length > 1) {
                    checkFreezeScreenDelayTime = parseInt(values[1]);
                    Log.e(TAG, "no such method exception");
                }
            }
        }
        return 0;
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseInt NumberFormatException e = " + e.getMessage());
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
        Object transPackage = zrHungData.get(FOCUS_PACKAGE_PARAM);
        Object transActivity = zrHungData.get("focusedActivityName");
        if (!(focusedWindow instanceof String) || !(transPackage instanceof String) || !(transActivity instanceof String)) {
            return false;
        }
        this.mFocusedWindow = (String) focusedWindow;
        this.mTransPackage = (String) transPackage;
        this.mTransActivity = (String) transActivity;
        Log.d(TAG, "TransparentWindow mHungConfigStatus = " + this.mHungConfigStatus + ",mHungConfigEnable = " + this.mHungConfigEnable);
        Message msg = this.mAppEyeTransparentWindowHandler.obtainMessage(1);
        if (msg != null) {
            msg.obj = zrHungData;
            this.mAppEyeTransparentWindowHandler.sendMessageDelayed(msg, (long) checkFreezeScreenDelayTime);
        }
        return true;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean cancelCheck(ZrHungData zrHungData) {
        AppEyeTransparentWindowHandler appEyeTransparentWindowHandler = this.mAppEyeTransparentWindowHandler;
        if (appEyeTransparentWindowHandler != null && appEyeTransparentWindowHandler.hasMessages(1)) {
            this.mAppEyeTransparentWindowHandler.removeMessages(1);
            Log.d(TAG, "TransparentWindow cancelCheckFreezeScreen");
        }
        return true;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean sendEvent(ZrHungData zrHungData) {
        if (zrHungData == null) {
            return false;
        }
        try {
            Object layoutParam = zrHungData.get(LAYOUT_PARAM);
            if (!(layoutParam instanceof WindowManager.LayoutParams)) {
                return false;
            }
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) layoutParam;
            if (!isWinMayCauseFreezeScreen(layoutParams)) {
                return true;
            }
            StringBuilder eventBuffer = new StringBuilder();
            boolean isTranslucent = isTranslucentThemeWithoutActionBar(layoutParams);
            boolean isZeroPixel = isZeroPixelWindow(layoutParams);
            Object obj = zrHungData.get("pid");
            int pid = obj instanceof Integer ? ((Integer) obj).intValue() : 0;
            eventBuffer.append("TransparentActivityScene find freezeScreen");
            eventBuffer.append(System.lineSeparator());
            eventBuffer.append("CURR_PACKAGE: ");
            eventBuffer.append(this.mTransPackage);
            eventBuffer.append(System.lineSeparator());
            eventBuffer.append("CURR_WINDOW: ");
            eventBuffer.append(this.mFocusedWindow);
            eventBuffer.append(System.lineSeparator());
            eventBuffer.append("CURR_ACTIVITY: ");
            eventBuffer.append(this.mTransActivity);
            eventBuffer.append(System.lineSeparator());
            eventBuffer.append("ALPHA: ");
            eventBuffer.append(layoutParams.alpha);
            eventBuffer.append(System.lineSeparator());
            eventBuffer.append("TRANSLUCENT: ");
            eventBuffer.append(isTranslucent);
            eventBuffer.append(System.lineSeparator());
            eventBuffer.append("ZEROPIXEL: ");
            eventBuffer.append(isZeroPixel);
            eventBuffer.append(System.lineSeparator());
            if (isZeroPixel) {
                eventBuffer.append("isFault = true");
            }
            Log.i(TAG, eventBuffer.toString());
            if (this.mHungConfig == null || this.mHungConfigStatus != 0 || !"1".equals(this.mHungConfigEnable)) {
                return true;
            }
            ZrHungData data = new ZrHungData();
            data.putString("packageName", this.mTransPackage);
            data.putInt("pid", pid);
            sendAppEyeEvent(273, data, null, eventBuffer.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isWinMayCauseFreezeScreen(WindowManager.LayoutParams layoutParams) {
        if (layoutParams == null) {
            return false;
        }
        if (isZeroAlphaWindow(layoutParams) || isZeroPixelWindow(layoutParams)) {
            return true;
        }
        return false;
    }

    private boolean isZeroAlphaWindow(WindowManager.LayoutParams layoutParams) {
        return layoutParams.alpha < 0.01f && isNotTouchableWindow(layoutParams);
    }

    private boolean isZeroPixelWindow(WindowManager.LayoutParams layoutParams) {
        if (layoutParams.width > 1 || layoutParams.width < 0 || layoutParams.height > 1 || layoutParams.height < 0 || !isNotTouchableWindow(layoutParams)) {
            return false;
        }
        return true;
    }

    private boolean isTranslucentThemeWithoutActionBar(WindowManager.LayoutParams layoutParams) {
        return ((WindowManagerEx.LayoutParamsEx.getHwFlags(layoutParams) & 268435456) == 0 || (WindowManagerEx.LayoutParamsEx.getHwFlags(layoutParams) & 536870912) == 0) ? false : true;
    }

    private boolean isNotTouchableWindow(WindowManager.LayoutParams layoutParams) {
        return (layoutParams.flags & 16) == 0;
    }
}
