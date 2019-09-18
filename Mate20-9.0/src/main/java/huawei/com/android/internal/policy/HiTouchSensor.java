package huawei.com.android.internal.policy;

import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import java.lang.annotation.RCUnownedThisRef;
import java.lang.reflect.InvocationTargetException;
import org.json.JSONObject;

public class HiTouchSensor {
    private static final String CALL_PACKAGE_NAME = "packageName";
    private static final String GRESTURE = "gresture";
    private static final int GRESTURE_COVER_SLIDE = 2;
    public static final int GRESTURE_DOUBLE_FINGER = 0;
    private static final int GRESTURE_PEN_LONG_TOUCH = 1;
    private static final String HITOUCH_PKG_NAME = "com.huawei.hitouch";
    private static final String HITOUCH_SWITCH = "hitouch_enabled";
    private static final String INCALLUI_PKG_NAME = "com.android.incallui";
    private static final String INTENT_PACKAGE_NAME = "pkgName";
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", "default"));
    private static final String LAUNCH_HITOUCH = "launchHiTouch";
    private static final String MAIN_TOUCH_POINT_X = "x";
    private static final String MAIN_TOUCH_POINT_Y = "y";
    private static final String METHOD_ISTOUCHEFFECTIVE_HIACTION = "isTouchEffective";
    private static final String MULTI_WINDOW_MODE = "split_screen_mode";
    private static final float POINT_FIRST_PERCENT = 0.45f;
    private static final float POINT_SECOND_PERCENT = 0.55f;
    private static final String SECOND_TOUCH_POINT_X = "x1";
    private static final String SECOND_TOUCH_POINT_Y = "y1";
    private static final int SECUREIME_POPUP = 1;
    private static final int SETTINGS_SWITCH_OFF = 0;
    private static final int SETTINGS_SWITCH_ON = 1;
    private static final int START_HIVOICE_DELAY_TIME = 300;
    private static final String TAG = "HiTouch_HiTouchSensor";
    private static final String TALK_BACK_PACKAGE = "com.google.android.marvin.talkback";
    private static final String TALK_BACK_SERVICE = "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService";
    private static final String URI_HIACTION_MANAGER_PROVIDER = "content://com.huawei.hiaction.provider.HiActionManagerProvider";
    private static final String USER_GUIDE_SETUP_FLAG = "device_provisioned";
    private static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";
    private ContentObserver accessibilitySwitchObserver = new ContentObserver(new Handler()) {
        @RCUnownedThisRef
        public void onChange(boolean selfChange) {
            HiTouchSensor.this.updateAccessibilityStatus();
        }
    };
    private boolean isAccessibilityEnabled = false;
    private boolean isDeviceProvisionedChecked = false;
    private boolean isHiTouchEnabled = true;
    private boolean isInMultiWindowMode = false;
    private boolean isLandscapeOrient = false;
    private boolean isTouchEffective_HiAction = true;
    /* access modifiers changed from: private */
    public Context mContext;
    private Context mContextActivity;
    private String mPackageName;
    private ContentObserver settingsHiTouchSwitchObserver = new ContentObserver(new Handler()) {
        @RCUnownedThisRef
        public void onChange(boolean selfChange) {
            HiTouchSensor.this.updateSwitchStatus();
        }
    };

    public HiTouchSensor(Context contextActivity) {
        this.mContextActivity = contextActivity;
        this.mContext = contextActivity;
        this.mPackageName = this.mContext.getPackageName();
    }

    public HiTouchSensor(Context context, Context contextActivity) {
        this.mContextActivity = contextActivity;
        this.mContext = context;
        this.mPackageName = this.mContext.getPackageName();
    }

    public boolean getStatus() {
        updateSwitchStatus();
        checkMultiWindowModeStatus();
        this.isTouchEffective_HiAction = checkEffectiveStatusFromHiAction(this.mPackageName);
        return this.isHiTouchEnabled && !this.isAccessibilityEnabled && !this.isLandscapeOrient && !this.isInMultiWindowMode && this.isTouchEffective_HiAction;
    }

    private boolean isLauncherApp() {
        if (this.mPackageName == null) {
            Log.i(TAG, "Can't get package name info, enable textboom by default");
            return false;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        ResolveInfo res = this.mContext.getPackageManager().resolveActivity(intent, 0);
        if (res == null) {
            Log.i(TAG, "ResolveInfo is null.");
            return false;
        } else if (res.activityInfo == null) {
            Log.i(TAG, "ActivityInfo is null.");
            return false;
        } else if (!this.mPackageName.equals(res.activityInfo.packageName)) {
            return false;
        } else {
            Log.i(TAG, "HiTouch restricted: is Launcher App");
            return true;
        }
    }

    private boolean matchPackage(String pkgName) {
        if (!pkgName.equals(this.mPackageName)) {
            return false;
        }
        Log.i(TAG, "HiTouch restricted: match package " + pkgName);
        return true;
    }

    private boolean isComputerMode() {
        boolean z = false;
        try {
            Class<?> hwPCUtils = Class.forName("android.util.HwPCUtils");
            if (hwPCUtils != null) {
                Boolean isInPad = (Boolean) hwPCUtils.getMethod("enabledInPad", new Class[0]).invoke(hwPCUtils, new Object[0]);
                Boolean isInPcMode = (Boolean) hwPCUtils.getMethod("isPcCastMode", new Class[0]).invoke(hwPCUtils, new Object[0]);
                Log.i(TAG, "enabledInPad = " + isInPad + ",isPcCastMode = " + isInPcMode);
                if (isInPad.booleanValue() && isInPcMode.booleanValue()) {
                    z = true;
                }
                return z;
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "fail to getIsInPCScreen ClassNotFoundException");
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "fail to getIsInPCScreen NoSuchMethodException");
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "fail to getIsInPCScreen IllegalAccessException");
        } catch (InvocationTargetException e4) {
            Log.e(TAG, "fail to getIsInPCScreen InvocationTargetException");
        } catch (IllegalArgumentException e5) {
            Log.e(TAG, "fail to getIsInPCScreen IllegalArgumentException");
        }
        return false;
    }

    public boolean isUnsupportScence(int windowType) {
        boolean z = true;
        if (!isHiTouchInstalled(this.mContext)) {
            Log.i(TAG, "HiTouch restricted: system app HiTouch don't exist.");
            return true;
        }
        boolean mHiTouchRestricted = windowType >= 1000;
        if (mHiTouchRestricted) {
            Log.i(TAG, "HiTouch restricted: Sub windows restricted.");
            return mHiTouchRestricted;
        }
        boolean mHiTouchRestricted2 = isLauncherApp() || matchPackage(INCALLUI_PKG_NAME);
        if (mHiTouchRestricted2) {
            return mHiTouchRestricted2;
        }
        boolean mHiTouchRestricted3 = isComputerMode() && IS_TABLET;
        if (true == mHiTouchRestricted3) {
            Log.i(TAG, "HiTouch restricted: tablet in computer mode.");
            return mHiTouchRestricted3;
        }
        if (!((KeyguardManager) this.mContext.getSystemService("keyguard")).isKeyguardLocked() || WECHAT_PACKAGE_NAME.equals(this.mPackageName)) {
            z = false;
        }
        boolean mHiTouchRestricted4 = z;
        if (mHiTouchRestricted4) {
            Log.i(TAG, "HiTouch restricted: Keyguard locked restricted.");
        }
        return mHiTouchRestricted4;
    }

    private boolean isHiTouchInstalled(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(HITOUCH_PKG_NAME, 0);
            if (info == null) {
                return false;
            }
            boolean isSystemApp = (info.flags & 1) > 0;
            boolean isUpdatedSystemApp = (info.flags & 128) > 0;
            if (isSystemApp || isUpdatedSystemApp) {
                z = true;
            }
            return z;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "depended package hiTouch does n't exist!");
            return false;
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        updateScreenOriatationStatus();
    }

    public boolean checkDeviceProvisioned() {
        if (this.isDeviceProvisionedChecked) {
            return true;
        }
        if (Settings.Secure.getInt(this.mContextActivity.getContentResolver(), USER_GUIDE_SETUP_FLAG, 1) == 0) {
            Log.v(TAG, "User guide setup is undergoing...");
            return false;
        }
        Log.v(TAG, "User setup is finished.");
        this.isDeviceProvisionedChecked = true;
        return true;
    }

    public void registerObserver() {
        updateSwitchStatus();
        updateAccessibilityStatus();
        updateScreenOriatationStatus();
        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.registerContentObserver(Settings.Global.getUriFor(HITOUCH_SWITCH), true, this.settingsHiTouchSwitchObserver);
        resolver.registerContentObserver(Settings.Secure.getUriFor("enabled_accessibility_services"), true, this.accessibilitySwitchObserver);
    }

    public void unregisterObserver() {
        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.unregisterContentObserver(this.settingsHiTouchSwitchObserver);
        resolver.unregisterContentObserver(this.accessibilitySwitchObserver);
    }

    /* access modifiers changed from: private */
    public void updateSwitchStatus() {
        if (1 == Settings.Global.getInt(this.mContextActivity.getContentResolver(), HITOUCH_SWITCH, 1)) {
            this.isHiTouchEnabled = true;
            Log.i(TAG, "HiTouch Setting Switch, ON");
            return;
        }
        this.isHiTouchEnabled = false;
        Log.i(TAG, "HiTouch Setting Switch, OFF");
    }

    /* access modifiers changed from: private */
    public void updateAccessibilityStatus() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.mContextActivity.getContentResolver(), "accessibility_enabled");
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(this.mContextActivity.getContentResolver(), "enabled_accessibility_services");
            Log.d(TAG, "services:" + services);
            if (services == null) {
                return;
            }
            if (TALK_BACK_SERVICE.equals(services)) {
                this.isAccessibilityEnabled = true;
            } else {
                this.isAccessibilityEnabled = false;
            }
        } else {
            this.isAccessibilityEnabled = false;
        }
    }

    private boolean isClonedProfile(int userId) {
        boolean isClonedProfile = false;
        if (userId == 0) {
            return false;
        }
        try {
            isClonedProfile = UserManager.get(this.mContextActivity).getUserInfo(userId).isClonedProfile();
        } catch (Exception e) {
            Log.e(TAG, "get Cloned Profile failed.");
        }
        return isClonedProfile;
    }

    private void updateScreenOriatationStatus() {
        if (IS_TABLET) {
            this.isLandscapeOrient = false;
            return;
        }
        if (this.mContextActivity.getResources().getConfiguration().orientation == 2) {
            this.isLandscapeOrient = true;
            Log.i(TAG, "ORIENTATION_LANDSCAPE");
        } else {
            this.isLandscapeOrient = false;
        }
    }

    private void checkMultiWindowModeStatus() {
        if (1 == Settings.Secure.getInt(this.mContextActivity.getContentResolver(), MULTI_WINDOW_MODE, 0)) {
            this.isInMultiWindowMode = true;
            Log.i(TAG, "Check MultiWindow Mode: " + this.isInMultiWindowMode);
            return;
        }
        this.isInMultiWindowMode = false;
    }

    private boolean checkEffectiveStatusFromHiAction(String pkgName) {
        Bundle result = null;
        Uri uri = Uri.parse(URI_HIACTION_MANAGER_PROVIDER);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("packageName", pkgName);
            try {
                if (!(getHitouchStartUpContext() == null || getHitouchStartUpContext().getContentResolver() == null)) {
                    result = getHitouchStartUpContext().getContentResolver().call(uri, METHOD_ISTOUCHEFFECTIVE_HIACTION, jsonObject.toString(), null);
                }
                if (result == null) {
                    return false;
                }
                Log.i(TAG, "Checking pkgName: " + pkgName + " Checking result: " + result.getBoolean(METHOD_ISTOUCHEFFECTIVE_HIACTION));
                return result.getBoolean(METHOD_ISTOUCHEFFECTIVE_HIACTION);
            } catch (Exception e) {
                Log.e(TAG, "Get error " + e.getMessage() + " when calling isTouchEffective.");
                return false;
            }
        } catch (Exception e2) {
            Log.e(TAG, "Get error " + e2.getMessage() + " when building jsonObject.");
            return false;
        }
    }

    private Context getHitouchStartUpContext() {
        if (!isClonedProfile(UserHandle.myUserId())) {
            return this.mContextActivity;
        }
        Log.i(TAG, "Cloned profile is true.");
        Context context = null;
        try {
            context = this.mContextActivity.createPackageContextAsUser(HITOUCH_PKG_NAME, 0, new UserHandle(0));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getHitouchStartUpContext:" + e);
        }
        if (context != null) {
            return context;
        }
        Log.d(TAG, "context is null");
        return this.mContextActivity;
    }

    public void launchHiTouchService(float x1, float y1, int gressture) {
        launchHiTouchService(x1, y1, x1 > 0.0f ? x1 - 1.0f : x1 + 1.0f, y1 > 0.0f ? y1 - 1.0f : 1.0f + y1, gressture);
    }

    public void launchHiTouchService(float x1, float y1, float x2, float y2, int gressture) {
        Intent intent = new Intent();
        intent.setClassName(HITOUCH_PKG_NAME, "com.huawei.hitouch.HiTouchService");
        intent.putExtra(MAIN_TOUCH_POINT_X, x1);
        intent.putExtra(MAIN_TOUCH_POINT_Y, y1);
        intent.putExtra(SECOND_TOUCH_POINT_X, x2);
        intent.putExtra(SECOND_TOUCH_POINT_Y, y2);
        intent.putExtra("pkgName", this.mPackageName);
        intent.putExtra(GRESTURE, gressture);
        Log.i(TAG, "launch HiTouch Service.");
        Context context = getHitouchStartUpContext();
        if (context != null) {
            try {
                context.startService(intent);
            } catch (Exception e) {
                Log.e(TAG, "Get error " + e.getMessage() + " when starting service");
            }
        } else {
            Log.i(TAG, "get context failed, do not launch Hitouch Service");
        }
    }

    public void processStylusGessture(Context context, int windowType, float x, float y) {
        updateSwitchStatus();
        updateAccessibilityStatus();
        updateScreenOriatationStatus();
        Log.d(TAG, "check HiTouch");
        boolean isUnsupport = isUnsupportScence(windowType);
        boolean status = getStatus();
        Log.d(TAG, "isUnsupport:" + isUnsupport + ", status:" + status);
        if (isUnsupport || !status) {
            Log.d(TAG, "cannot start HiTouch!");
            return;
        }
        Log.d(TAG, "can start  HiTouch");
        launchHiTouchService(x, y, 1);
    }

    public void processTonySlide(String packageName, int windowType, final Intent voiceIntent, Handler handler) {
        if (handler == null || voiceIntent == null) {
            Log.e(TAG, "param invalid!");
            return;
        }
        this.mPackageName = packageName;
        updateSwitchStatus();
        updateAccessibilityStatus();
        updateScreenOriatationStatus();
        Log.d(TAG, "check HiTouch pkg " + packageName + ", type = " + windowType);
        boolean isUnsupport = isUnsupportScence(windowType);
        boolean status = getStatus();
        Log.d(TAG, "isUnsupport:" + isUnsupport + ", status:" + status);
        if (isUnsupport || !status) {
            Log.d(TAG, "cannot start HiTouch!");
            this.mContext.startActivityAsUser(voiceIntent, UserHandle.CURRENT);
        } else {
            DisplayMetrics dm = this.mContext.getResources().getDisplayMetrics();
            Log.d(TAG, "can start  HiTouch width=" + dm.widthPixels + ", height=" + dm.heightPixels);
            launchHiTouchService(((float) dm.widthPixels) * POINT_FIRST_PERCENT, ((float) dm.heightPixels) * POINT_FIRST_PERCENT, ((float) dm.widthPixels) * POINT_SECOND_PERCENT, ((float) dm.heightPixels) * POINT_SECOND_PERCENT, 2);
            voiceIntent.putExtra(LAUNCH_HITOUCH, true);
            handler.postDelayed(new Runnable() {
                public void run() {
                    HiTouchSensor.this.mContext.startActivityAsUser(voiceIntent, UserHandle.CURRENT);
                }
            }, 300);
        }
    }
}
