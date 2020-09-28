package huawei.com.android.internal.policy;

import android.app.KeyguardManager;
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
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.util.Log;
import java.lang.annotation.RCUnownedThisRef;
import org.json.JSONObject;

public class HiTouchSensor {
    private static final String ACCESSIBILITY_SCREENREADER_ENABLED = "accessibility_screenreader_enabled";
    private static final String CALL_PACKAGE_NAME = "packageName";
    private static final String GRESTURE = "gresture";
    public static final int GRESTURE_DOUBLE_FINGER = 0;
    public static final int GRESTURE_PEN_LONG_TOUCH = 1;
    private static final String HITOUCH_PKG_NAME = "com.huawei.hitouch";
    private static final String HITOUCH_SWITCH = "hitouch_enabled";
    private static final String INCALLUI_PKG_NAME = "com.android.incallui";
    private static final String INTENT_PACKAGE_NAME = "pkgName";
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", "default"));
    private static final String MAIN_TOUCH_POINT_X = "x";
    private static final String MAIN_TOUCH_POINT_Y = "y";
    private static final String METHOD_ISTOUCHEFFECTIVE_HIACTION = "isTouchEffective";
    private static final String MULTI_WINDOW_MODE = "split_screen_mode";
    private static final String SECOND_TOUCH_POINT_X = "x1";
    private static final String SECOND_TOUCH_POINT_Y = "y1";
    private static final int SECUREIME_POPUP = 1;
    private static final String SECUREIME_STATUS = "secure_input_status";
    private static final int SETTINGS_SWITCH_OFF = 0;
    private static final int SETTINGS_SWITCH_ON = 1;
    private static final String TAG = "HiTouch_HiTouchSensor";
    private static final String URI_HIACTION_MANAGER_PROVIDER = "content://com.huawei.hiaction.provider.HiActionManagerProvider";
    private static final String USER_GUIDE_SETUP_FLAG = "device_provisioned";
    private static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";
    private boolean isAccessibilityEnabled = false;
    private boolean isDeviceProvisionedChecked = false;
    private boolean isHiTouchEnabled = true;
    private boolean isInMultiWindowMode = false;
    private boolean isLandscapeOrient = false;
    private boolean isTouchEffective_HiAction = true;
    private Context mContext;
    private Context mContextActivity;
    private ContentObserver settingsHiTouchSwitchObserver = new ContentObserver(new Handler()) {
        /* class huawei.com.android.internal.policy.HiTouchSensor.AnonymousClass1 */

        @RCUnownedThisRef
        public void onChange(boolean selfChange) {
            HiTouchSensor.this.updateSwitchStatus();
        }
    };

    public HiTouchSensor(Context contextActivity) {
        this.mContextActivity = contextActivity;
        this.mContext = contextActivity;
    }

    public HiTouchSensor(Context context, Context contextActivity) {
        this.mContextActivity = contextActivity;
        this.mContext = context;
    }

    public boolean getStatus() {
        updateSwitchStatus();
        checkMultiWindowModeStatus();
        this.isTouchEffective_HiAction = checkEffectiveStatusFromHiAction(this.mContextActivity.getPackageName());
        boolean isSecurityIMEPopup = isSecurityIMEPopup();
        updateScreenOriatationStatus();
        updateAccessibilityStatus();
        hwLog(TAG, "isHiTouchEnabled: " + this.isHiTouchEnabled + " isSecurityIMEPopup: " + isSecurityIMEPopup + " isLandscapeOrient: " + this.isLandscapeOrient + " isInMultiWindowMode: " + this.isInMultiWindowMode + " isTouchEffective_HiAction: " + this.isTouchEffective_HiAction);
        return this.isHiTouchEnabled && !isSecurityIMEPopup && !this.isAccessibilityEnabled && !this.isLandscapeOrient && !this.isInMultiWindowMode && this.isTouchEffective_HiAction;
    }

    private boolean isLauncherApp() {
        String pkgName = this.mContext.getPackageName();
        if (TextUtils.isEmpty(pkgName)) {
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
        } else if (!pkgName.equals(res.activityInfo.packageName)) {
            return false;
        } else {
            Log.i(TAG, "HiTouch restricted: is Launcher App");
            return true;
        }
    }

    private boolean matchPackage(String pkgName) {
        if (!pkgName.equals(this.mContext.getPackageName())) {
            return false;
        }
        Log.i(TAG, "HiTouch restricted: match package " + pkgName);
        return true;
    }

    private boolean isComputerMode() {
        return HwPCUtils.enabledInPad() && HwPCUtils.isPcCastMode();
    }

    public boolean isUnsupportScence(int windowType) {
        boolean mHiTouchRestricted = true;
        if (!isHiTouchInstalled(this.mContext)) {
            Log.i(TAG, "HiTouch restricted: system app HiTouch don't exist.");
            return true;
        }
        boolean mHiTouchRestricted2 = windowType >= 1000;
        if (mHiTouchRestricted2) {
            Log.i(TAG, "HiTouch restricted: Sub windows restricted.");
            return mHiTouchRestricted2;
        }
        boolean mHiTouchRestricted3 = isLauncherApp() || matchPackage(INCALLUI_PKG_NAME);
        if (mHiTouchRestricted3) {
            return mHiTouchRestricted3;
        }
        boolean mHiTouchRestricted4 = isComputerMode() && IS_TABLET;
        if (true == mHiTouchRestricted4) {
            Log.i(TAG, "HiTouch restricted: tablet in computer mode.");
            return mHiTouchRestricted4;
        }
        if (!((KeyguardManager) this.mContext.getSystemService("keyguard")).isKeyguardLocked() || WECHAT_PACKAGE_NAME.equals(this.mContextActivity.getPackageName())) {
            mHiTouchRestricted = false;
        }
        if (mHiTouchRestricted) {
            Log.i(TAG, "HiTouch restricted: Keyguard locked restricted.");
        }
        return mHiTouchRestricted;
    }

    private boolean isHiTouchInstalled(Context context) {
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
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "depended package hiTouch does n't exist!");
            return false;
        }
    }

    private boolean isSecurityIMEPopup() {
        boolean isSecrutiyIMEPopup = false;
        if (1 == Settings.Global.getInt(this.mContext.getContentResolver(), SECUREIME_STATUS, 0)) {
            isSecrutiyIMEPopup = true;
        }
        Log.i(TAG, "isSecrutiyIMEPopup:" + isSecrutiyIMEPopup);
        return isSecrutiyIMEPopup;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        hwLog(TAG, "onConfigurationChanged");
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
        hwLog(TAG, "registerObserver");
        updateSwitchStatus();
        updateAccessibilityStatus();
        updateScreenOriatationStatus();
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(HITOUCH_SWITCH), true, this.settingsHiTouchSwitchObserver);
    }

    public void unregisterObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.settingsHiTouchSwitchObserver);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSwitchStatus() {
        if (1 == Settings.Global.getInt(this.mContextActivity.getContentResolver(), HITOUCH_SWITCH, 1)) {
            this.isHiTouchEnabled = true;
            Log.i(TAG, "HiTouch Setting Switch, ON");
            return;
        }
        this.isHiTouchEnabled = false;
        Log.i(TAG, "HiTouch Setting Switch, OFF");
    }

    private void updateAccessibilityStatus() {
        hwLog(TAG, "updateAccessibilityStatus before:" + this.isAccessibilityEnabled);
        boolean z = false;
        if (Settings.Secure.getInt(this.mContextActivity.getContentResolver(), ACCESSIBILITY_SCREENREADER_ENABLED, 0) == 1) {
            z = true;
        }
        this.isAccessibilityEnabled = z;
        hwLog(TAG, "updateAccessibilityStatus after:" + this.isAccessibilityEnabled);
    }

    private boolean isClonedProfile(int userId) {
        if (userId == 0) {
            return false;
        }
        try {
            return UserManager.get(this.mContextActivity).getUserInfo(userId).isClonedProfile();
        } catch (Exception e) {
            Log.e(TAG, "get Cloned Profile failed.");
            return false;
        }
    }

    private void updateScreenOriatationStatus() {
        hwLog(TAG, "updateScreenOriatationStatus");
        if (IS_TABLET) {
            this.isLandscapeOrient = false;
        } else if (this.mContextActivity.getResources().getConfiguration().orientation == 2) {
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
                    result = getHitouchStartUpContext().getContentResolver().call(uri, METHOD_ISTOUCHEFFECTIVE_HIACTION, jsonObject.toString(), (Bundle) null);
                }
                if (result == null) {
                    return false;
                }
                Log.i(TAG, "Checking pkgName: " + pkgName + " Checking result: " + result.getBoolean(METHOD_ISTOUCHEFFECTIVE_HIACTION));
                return result.getBoolean(METHOD_ISTOUCHEFFECTIVE_HIACTION);
            } catch (Exception e) {
                Log.e(TAG, "checkEffectiveStatusFromHiAction, Get error when calling isTouchEffective.");
                return false;
            }
        } catch (Exception e2) {
            Log.e(TAG, "checkEffectiveStatusFromHiAction, Get error when building jsonObject.");
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
        intent.putExtra("pkgName", this.mContextActivity.getPackageName());
        intent.putExtra(GRESTURE, gressture);
        Log.i(TAG, "launch HiTouch Service.");
        Context context = getHitouchStartUpContext();
        if (context != null) {
            try {
                context.startService(intent);
            } catch (Exception e) {
                Log.e(TAG, "launchHiTouchService, Get error when starting service");
            }
        } else {
            Log.i(TAG, "get context failed, do not launch Hitouch Service");
        }
    }

    public void processStylusGessture(Context context, int windowType, float x, float y) {
        hwLog(TAG, "processStylusGessture");
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

    private void hwLog(String tag, String msg) {
        if (Log.HWLog) {
            Log.i(tag, msg);
        }
    }
}
