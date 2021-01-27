package com.huawei.android.biometric;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HwExHandler;
import android.os.IPowerManager;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SELinux;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.server.LocalServices;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.huawei.android.content.pm.UserInfoExt;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.pgmng.log.LogPower;
import huawei.android.hardware.fingerprint.FingerprintManagerEx;
import huawei.com.android.server.policy.BlurUtils;
import java.io.File;
import java.util.NoSuchElementException;
import java.util.Optional;
import vendor.huawei.hardware.biometrics.fingerprint.V2_2.IExtBiometricsFingerprint;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

public class FingerprintSupportEx {
    public static final int DE_SCENE_UD_ENROLL_FINGER_PRINT = 31;
    public static final String DISPLAY_SIZE_FORCED = "display_size_forced";
    public static final int FLAG_HW_HIDDENSPACE = 33554432;
    public static final int FP_MMI_TESTING = 2;
    public static final int FP_MMI_TEST_EXIT_LOCATION_TEST = 5;
    public static final int FP_MMI_TEST_NEED_LOCATION_TEST = 4;
    public static final int FP_MMI_TEST_PASS = 1;
    public static final int FP_MMI_TEST_PAUSE = 3;
    public static final int MMI_TYPE_AUTO_TEST = 11;
    public static final int MMI_TYPE_AUTO_TEST_UD = 18;
    public static final int MMI_TYPE_BUBBLE_TEST = 16;
    public static final int MMI_TYPE_BUBBLE_TEST_UD = 23;
    public static final int MMI_TYPE_CALIBRARION_OR_LOCATION_UD = 24;
    public static final int MMI_TYPE_CANCEL_TEST_UD = 932;
    public static final int MMI_TYPE_ENROL_IDENITFY_TEST_UD = 37;
    public static final int MMI_TYPE_FAKE_FINGER = 13;
    public static final int MMI_TYPE_FAKE_FINGER_UD = 20;
    public static final int MMI_TYPE_GET_BACKLIGHT_COLOR = 904;
    public static final int MMI_TYPE_GET_BRIGHTNESS_LEVEL = 909;
    public static final int MMI_TYPE_GET_HIGHLIGHT_APLHA = 907;
    public static final int MMI_TYPE_GET_HIGHLIGHT_COLOR = 903;
    public static final int MMI_TYPE_GET_HIGHLIGHT_LEVEL = 906;
    public static final int MMI_TYPE_GET_HIGHLIGHT_SHAPE = 901;
    public static final int MMI_TYPE_GET_HIGHLIGHT_SIZE = 902;
    public static final int MMI_TYPE_GET_LOCATION_CIRCLE_COLOR = 404;
    public static final int MMI_TYPE_GET_LOCATION_CIRCLE_COUNT = 911;
    public static final int MMI_TYPE_GET_LOCATION_CIRCLE_RADIUS = 403;
    public static final int MMI_TYPE_GET_LOCATION_CIRCLE_X_INDICATOR = 401;
    public static final int MMI_TYPE_GET_LOCATION_CIRCLE_Y_INDICATOR = 402;
    public static final int MMI_TYPE_GET_MESSAGE_COLOR = 905;
    public static final int MMI_TYPE_GET_RESULT = 31;
    public static final int MMI_TYPE_GET_RESULT_UD = 32;
    public static final int MMI_TYPE_INTERRUPT_TEST = 12;
    public static final int MMI_TYPE_INTERRUPT_TEST_UD = 19;
    public static final int MMI_TYPE_LOCATION_CIRCLE_TEST_UD = 930;
    public static final int MMI_TYPE_NAV_DISABLE = 42;
    public static final int MMI_TYPE_NAV_ENABLE = 41;
    public static final int MMI_TYPE_OPTICAL_CALIBRARION = 17;
    public static final int MMI_TYPE_OPTICAL_CALIBRARION_UD = 931;
    public static final int MMI_TYPE_REMOVE_LOCATION_CIRCLE = 931;
    public static final int MMI_TYPE_SENSOR_PARAM_BASE = 900;
    public static final int MMI_TYPE_SET_UI_UPDATE_COMPLETE = 908;
    public static final int MMI_TYPE_SNR_SINGAL_IMAGE = 14;
    public static final int MMI_TYPE_SNR_SINGAL_IMAGE_UD = 21;
    public static final int MMI_TYPE_SNR_WHITE_IMAGE = 15;
    public static final int MMI_TYPE_SNR_WHITE_IMAGE_UD = 22;
    public static final int RT2_FINGER_GET_RESULT = 36;
    public static final int RT2_FINGER_TEST = 35;
    public static final int RT_FINGER_GET_RESULT = 34;
    public static final int RT_FINGER_TEST = 33;
    private static final String TAG_SUPPORT = "FingerprintSupportEx";
    private static FingerprintSupportEx sInstance;
    private Context mContext;
    private FingerprintManagerEx mFingerprintManagerEx;
    private IPowerManager mIpowerManager = null;

    private FingerprintSupportEx() {
    }

    public static synchronized FingerprintSupportEx getInstance() {
        FingerprintSupportEx fingerprintSupportEx;
        synchronized (FingerprintSupportEx.class) {
            if (sInstance == null) {
                sInstance = new FingerprintSupportEx();
            }
            fingerprintSupportEx = sInstance;
        }
        return fingerprintSupportEx;
    }

    public Handler createHwExHandler(Looper looper, long timeout) {
        return new HwExHandler(looper, timeout);
    }

    public void setBrightnessNoLimit(int level, int lightLevelTime) {
        try {
            if (this.mIpowerManager == null) {
                this.mIpowerManager = IPowerManager.Stub.asInterface(ServiceManagerEx.getService("power"));
            }
            this.mIpowerManager.setBrightnessNoLimit(level, lightLevelTime);
        } catch (RemoteException e) {
            Log.e(TAG_SUPPORT, "setFingerprintviewHighlight catch RemoteException ");
        }
    }

    public static void surfaceControlOpenTransaction() {
        SurfaceControl.openTransaction();
    }

    public static void surfaceControlCloseTransaction() {
        SurfaceControl.closeTransaction();
    }

    public void setFpAuthState(boolean isAuthState) {
        Object object = LocalServices.getService(WindowManagerPolicy.class);
        if (object != null && (object instanceof HwPhoneWindowManager)) {
            ((HwPhoneWindowManager) object).getPhoneWindowManagerEx().setFPAuthState(isAuthState);
        }
    }

    public int getRemainingNum() {
        Context context;
        if (this.mFingerprintManagerEx == null && (context = this.mContext) != null) {
            this.mFingerprintManagerEx = new FingerprintManagerEx(context);
        }
        FingerprintManagerEx fingerprintManagerEx = this.mFingerprintManagerEx;
        if (fingerprintManagerEx == null) {
            return 0;
        }
        return fingerprintManagerEx.getRemainingNum();
    }

    public static boolean hasCallbacks(Handler handler, Runnable runnalbe) {
        if (handler == null || runnalbe == null) {
            return false;
        }
        return handler.hasCallbacks(runnalbe);
    }

    public Bitmap blurMaskImage(Bitmap input, Bitmap output, int radius) {
        return BlurUtils.blurMaskImage(this.mContext, input, output, radius);
    }

    public Bitmap screenShotBitmap(float scale) {
        return BlurUtils.screenShotBitmap(this.mContext, scale);
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public static void setLayoutParamsPrivateFlags(WindowManager.LayoutParams layoutParams, int privateFlags) {
        if (layoutParams != null) {
            layoutParams.privateFlags |= privateFlags;
        }
    }

    public static void setlayoutInDisplaySideMode(WindowManager.LayoutParams layoutParams, int sideMode) {
        if (layoutParams != null) {
            layoutParams.layoutInDisplaySideMode = sideMode;
        }
    }

    public static int getDeSceneFingerprintHbm() {
        return 34;
    }

    public static int getDeSceneUdEnrollFingerprint() {
        return 31;
    }

    public static int getDeSceneFingerprint() {
        return 29;
    }

    public static int getDeDataTypeUdFingerprintBacklight() {
        return 13;
    }

    public static void inputManagerInjectInputEvent(KeyEvent ev) {
        if (ev != null) {
            InputManager.getInstance().injectInputEvent(ev, 0);
        }
    }

    public static void logPower() {
        LogPower.push(179);
    }

    public static int getPrivateFlagShowForAllUsers() {
        return 16;
    }

    public static int getTypeSecureSystemOverlay() {
        return 2015;
    }

    public static int getPrivateFlagHideNaviBar() {
        return Integer.MIN_VALUE;
    }

    public static int getLayoutInDisplayCutoutModeAlways() {
        return 1;
    }

    public static void setLayoutInDisplayCutoutMode(WindowManager.LayoutParams layoutParams, int cutoutMode) {
        layoutParams.layoutInDisplayCutoutMode = cutoutMode;
    }

    public static boolean isRtlLocale(FrameLayout view) {
        if (view != null) {
            return view.isRtlLocale();
        }
        return false;
    }

    public static BiometricsFingerprintEx getFingerprintDaemon() {
        try {
            IExtBiometricsFingerprint daemonEx = IExtBiometricsFingerprint.getService();
            if (daemonEx != null) {
                BiometricsFingerprintEx biometricsFingerprintEx = new BiometricsFingerprintEx();
                biometricsFingerprintEx.setExtBiometricsFingerprint(daemonEx);
                return biometricsFingerprintEx;
            }
        } catch (NoSuchElementException e) {
            Log.e(TAG_SUPPORT, "Service doesn't exist or cannot be opened");
        } catch (RemoteException e2) {
            Log.e(TAG_SUPPORT, "Failed to get biometric interface");
        }
        Log.i(TAG_SUPPORT, "daemonEx empty");
        return null;
    }

    public static Optional<FingerprintSurfaceEx> getFingerprintSurfaceExFromView(View view) {
        if (view == null) {
            Log.i(TAG_SUPPORT, "getFingerprintSurfaceExFromView empty");
            return Optional.empty();
        } else if (view.getViewRootImpl() == null) {
            return Optional.empty();
        } else {
            Log.i(TAG_SUPPORT, "getFingerprintSurfaceExFromView");
            return Optional.of(new FingerprintSurfaceEx(view.getViewRootImpl().getSurfaceControl()));
        }
    }

    public static void registerContentObserver(ContentResolver contentResolver, Uri uri, boolean isNotifyForDescendents, ContentObserver observer, int userHandle) {
        if (contentResolver != null) {
            contentResolver.registerContentObserver(uri, isNotifyForDescendents, observer, userHandle);
        }
    }

    public static boolean isHwHiddenSpace(UserInfoExt userInfoExt) {
        if (userInfoExt == null || userInfoExt.getUserInfo() == null || (userInfoExt.getUserInfo().flags & 33554432) != 33554432) {
            return false;
        }
        return true;
    }

    public static boolean restoreconSeLinux(File file) {
        return SELinux.restorecon(file);
    }

    public TouchscreenEx getTouchscreenService() {
        try {
            ITouchscreen proxy = ITouchscreen.getService();
            if (proxy == null) {
                return null;
            }
            TouchscreenEx touchscreenEx = new TouchscreenEx();
            touchscreenEx.setTouchscreen(proxy);
            return touchscreenEx;
        } catch (NoSuchElementException e) {
            Log.e(TAG_SUPPORT, "getTouchscreenService NoSuchElementException");
            return null;
        } catch (RemoteException e2) {
            Log.w(TAG_SUPPORT, "getTouchscreenService");
            return null;
        }
    }
}
