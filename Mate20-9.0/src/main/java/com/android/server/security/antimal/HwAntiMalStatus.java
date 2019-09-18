package com.android.server.security.antimal;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Slog;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.pm.HwCustPackageManagerService;
import com.android.server.security.deviceusage.HwDeviceUsageOEMINFO;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.cust.HwCustUtils;
import java.util.List;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;

public class HwAntiMalStatus {
    public static final int ANTIMAL_TYPE_ALLOWED_BY_MDM = 10;
    private static final int ANTIMAL_TYPE_ALLOWED_BY_SYSTEMAPP = 11;
    private static final int ANTIMAL_TYPE_NOT_ALLOWED_SET_HOME = 20;
    private static final int ANTIMAL_TYPE_RESTORE_DEFAULT_LAUNCHER = 30;
    private static final long CALL_ENOUGH_TIME = 300;
    private static final long CHARGING_ENOUGH_TIME = 20;
    private static final String DEFAULT_HW_LAUNCHER = "com.huawei.android.launcher";
    private static final String HWSYSTEMMANAGER_PACKAGE = "com.huawei.systemmanager";
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final String[] NOT_ALLOWED_DISABLE_WHITELIST = {"com.huawei.android.launcher", "com.huawei.systemmanager"};
    private static final String POLITY_CURR_ACC_OTHERNAME = "othername";
    private static final String POLITY_IS_THIRDPARTYLAUNCHER_ALLOWED_FLAG = "isallowed";
    private static final String POLITY_THIRDPARTYLAUNCHER_CONFIG_FILE = "tlConfigFile";
    private static final long SCREENON_ENOUGH_TIME = 36000;
    private static final String TAG = "HwAntiMalStatus";
    private static final boolean mAntimalProtection = "true".equalsIgnoreCase(SystemProperties.get("ro.product.antimal_protection", "true"));
    private static HwCustPackageManagerService mCpms = ((HwCustPackageManagerService) HwCustUtils.createObj(HwCustPackageManagerService.class, new Object[0]));
    private String DEFAULT_CUST_LAUNCHER = "";
    private Context mContext;

    public HwAntiMalStatus(Context ctx) {
        this.mContext = ctx;
    }

    public boolean isAllowedSetHomeActivityForAntiMal(PackageInfo pkgInfo, int userId) {
        if (isAllowedSetHomeActivityForAntiMalInternal(pkgInfo, userId, true)) {
            return true;
        }
        if (hasMDMPermission(pkgInfo)) {
            Slog.d(TAG, "found MDM permission!");
            return true;
        } else if (isCurrentAccountInWhiteList()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean hasMDMPermission(PackageInfo pkgInfo) {
        if (pkgInfo == null) {
            Slog.d(TAG, "Invalid params.");
            return true;
        }
        int uid = pkgInfo.applicationInfo.uid;
        Slog.i(TAG, "uid:" + uid);
        if (ActivityManager.checkUidPermission("com.huawei.permission.sec.SDK_LAUNCHER", uid) != 0) {
            return false;
        }
        Slog.i(TAG, "have launcher permission!");
        return true;
    }

    public boolean isAllowedSetHomeActivityForAntiMalInternal(PackageInfo pkgInfo, int userId, boolean internal) {
        if (this.mContext == null || pkgInfo == null) {
            Slog.d(TAG, "Invalid params.");
            return true;
        } else if (!IS_CHINA_AREA) {
            Slog.d(TAG, "Not in valid area !");
            return true;
        } else if (!mAntimalProtection) {
            Slog.d(TAG, "AntimalProtection control is close!");
            return true;
        } else if (isSupportHomeScreen()) {
            Slog.d(TAG, "Support home screen.");
            return true;
        } else {
            if (internal) {
                if ((pkgInfo.applicationInfo.flags & 1) == 1) {
                    Slog.d(TAG, "System app.");
                    Context context = this.mContext;
                    Flog.bdReport(context, 128, "{package:" + pkgInfo.packageName + ",type:" + 11 + "}");
                    return true;
                } else if (isCustDefaultLauncher(pkgInfo.packageName)) {
                    Slog.d(TAG, "Cust default launcher.");
                    return true;
                }
            }
            Context context2 = this.mContext;
            Flog.bdReport(context2, 128, "{package:" + pkgInfo.packageName + ",type:" + 20 + "}");
            Slog.d(TAG, "not allowed to set home activity at normal state.");
            return false;
        }
    }

    public void handleUserClearLockForAntiMal(int userId) {
        if (this.mContext == null) {
            Slog.d(TAG, "invalid params.");
        } else if (ActivityManager.getCurrentUser() != 0) {
            Slog.d(TAG, "not owner.");
        } else if (!isNeedRestrictForAntimal(false)) {
            Slog.d(TAG, "no need restrict for antimal.");
        } else if (isDefaultHwLauncher()) {
            Slog.d(TAG, "current launcher is default launcher.");
        } else {
            checkIfDefaultLauncherIsDisabled();
            Slog.d(TAG, "No need to set default launcher.");
        }
    }

    private boolean hasSetPassword() {
        return new LockPatternUtils(this.mContext).isSecure(ActivityManager.getCurrentUser());
    }

    public boolean isNeedRestrictForAntimal(boolean needRestrict) {
        if (!needRestrict) {
            return false;
        }
        HwDeviceUsageOEMINFO mHwDeviceUsageOEMINFO = HwDeviceUsageOEMINFO.getInstance();
        if (mHwDeviceUsageOEMINFO.getScreenOnTime() < SCREENON_ENOUGH_TIME && mHwDeviceUsageOEMINFO.getTalkTime() < CALL_ENOUGH_TIME && mHwDeviceUsageOEMINFO.getChargeTime() < CHARGING_ENOUGH_TIME && !hasSimCard()) {
            return true;
        }
        Slog.d(TAG, "No need to restrict");
        return false;
    }

    private static Intent getMainIntent() {
        Intent mainIntent = new Intent("android.intent.action.MAIN");
        mainIntent.addCategory("android.intent.category.HOME");
        mainIntent.addCategory("android.intent.category.DEFAULT");
        return mainIntent;
    }

    private boolean isDefaultHwLauncher() {
        ResolveInfo res = this.mContext.getPackageManager().resolveActivity(getMainIntent(), 0);
        if (res == null || res.activityInfo == null || TextUtils.isEmpty(res.activityInfo.packageName)) {
            Slog.e(TAG, "isDefaultHwLauncher param is null.");
            return false;
        } else if (res.activityInfo.packageName.equals("com.huawei.android.launcher") || (!TextUtils.isEmpty(getCustDefaultLauncher()) && res.activityInfo.packageName.equals(getCustDefaultLauncher()))) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isCustDefaultLauncher(String pkgName) {
        if (TextUtils.equals(pkgName, getCustDefaultLauncher())) {
            return true;
        }
        return false;
    }

    public void checkIfTopAppsDisabled() {
        for (String checkIfDisabled : NOT_ALLOWED_DISABLE_WHITELIST) {
            checkIfDisabled(checkIfDisabled);
        }
        if (!TextUtils.isEmpty(getCustDefaultLauncher())) {
            checkIfDisabled(getCustDefaultLauncher());
        }
    }

    private void checkIfDefaultLauncherIsDisabled() {
        PackageManager packageManager = this.mContext.getPackageManager();
        int currState = packageManager.getApplicationEnabledSetting("com.huawei.android.launcher");
        if (currState == 2 || currState == 3 || currState == 4) {
            Slog.i(TAG, "Huawei Launcher is Disabled! enable it!");
            packageManager.setApplicationEnabledSetting("com.huawei.android.launcher", 1, 1);
        }
        if (!TextUtils.isEmpty(getCustDefaultLauncher())) {
            int currState2 = packageManager.getApplicationEnabledSetting(getCustDefaultLauncher());
            if (currState2 == 2 || currState2 == 3 || currState2 == 4) {
                Slog.i(TAG, "Cust Launcher is Disabled! enable it!");
                packageManager.setApplicationEnabledSetting(getCustDefaultLauncher(), 1, 1);
            }
        }
    }

    private void checkIfDisabled(String pkgName) {
        PackageManager packageManager = this.mContext.getPackageManager();
        try {
            int currState = packageManager.getApplicationEnabledSetting(pkgName);
            if (currState == 2 || currState == 3 || currState == 4) {
                Slog.i(TAG, pkgName + " is Disabled! enable it!");
                packageManager.setApplicationEnabledSetting(pkgName, 1, 1);
            }
        } catch (Exception e) {
            Slog.d(TAG, "getApplicationEnabledSetting fail:" + e.getMessage());
        }
    }

    private void setDefaultLauncher() {
        int i = 0;
        List<ResolveInfo> resolveInfos = this.mContext.getPackageManager().queryIntentActivities(getMainIntent(), 0);
        if (resolveInfos != null) {
            int infoSize = resolveInfos.size();
            for (int i2 = 0; i2 < infoSize; i2++) {
                ResolveInfo resolveInfo = resolveInfos.get(i2);
                if (resolveInfo != null) {
                    this.mContext.getPackageManager().clearPackagePreferredActivities(resolveInfo.activityInfo.packageName);
                }
            }
            int find = -1;
            ComponentName[] set = new ComponentName[infoSize];
            while (true) {
                if (i >= infoSize) {
                    break;
                }
                ResolveInfo info = resolveInfos.get(i);
                set[i] = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                if (info.activityInfo.packageName.equals("com.huawei.android.launcher") || (!TextUtils.isEmpty(getCustDefaultLauncher()) && info.activityInfo.packageName.equals(getCustDefaultLauncher()))) {
                    find = i;
                } else {
                    i++;
                }
            }
            find = i;
            if (find != -1) {
                IntentFilter inf = new IntentFilter("android.intent.action.MAIN");
                inf.addCategory("android.intent.category.HOME");
                inf.addCategory("android.intent.category.DEFAULT");
                this.mContext.getPackageManager().addPreferredActivity(inf, HighBitsCompModeID.MODE_COLOR_ENHANCE, set, set[find]);
            }
        }
    }

    private String getCustDefaultLauncher() {
        if (TextUtils.isEmpty(this.DEFAULT_CUST_LAUNCHER)) {
            this.DEFAULT_CUST_LAUNCHER = mCpms.getCustDefaultLauncher(this.mContext, this.DEFAULT_CUST_LAUNCHER);
        }
        return this.DEFAULT_CUST_LAUNCHER;
    }

    public boolean isSupportHomeScreen() {
        return this.mContext.getPackageManager().hasSystemFeature("android.software.home_screen");
    }

    public boolean isAllowedToBeDisabled(String packageName) {
        return true;
    }

    private boolean hasSimCard() {
        int simState = ((TelephonyManager) this.mContext.getSystemService("phone")).getSimState();
        if (simState != 1 && simState != 0) {
            return true;
        }
        Slog.i(TAG, "no SIM card!");
        return false;
    }

    private boolean isCurrentAccountInWhiteList() {
        boolean isInWhiteList = false;
        try {
            isInWhiteList = this.mContext.createPackageContext("com.huawei.systemmanager", 2).getSharedPreferences(POLITY_THIRDPARTYLAUNCHER_CONFIG_FILE, 4).getBoolean(POLITY_IS_THIRDPARTYLAUNCHER_ALLOWED_FLAG, false);
            if (isInWhiteList) {
                Flog.bdReport(this.mContext, 128, "{accothername:" + sp.getString(POLITY_CURR_ACC_OTHERNAME, "null") + ",type:" + 30 + "}");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, e.getMessage());
        } catch (Exception e2) {
            Slog.e(TAG, e2.getMessage());
        }
        return isInWhiteList;
    }
}
