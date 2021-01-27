package com.huawei.server.security.antimal;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.securitydiagnose.HwSecurityDiagnoseManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwpartsecurityservices.BuildConfig;
import com.huawei.server.security.core.IHwSecurityPlugin;
import com.huawei.server.security.permissionmanager.util.PermConst;
import huawei.android.security.IHwAntiMalPlugin;

public class HwAntiMalPlugin implements IHwSecurityPlugin {
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.antimal.HwAntiMalPlugin.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            Log.i(HwAntiMalPlugin.TAG, "createPlugin");
            return new HwAntiMalPlugin(context);
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return HwAntiMalPlugin.MANAGE_USE_SECURITY;
        }
    };
    private static final long DEFAULT_CALL_ENOUGH_TIME = 300;
    private static final long DEFAULT_CHARGING_ENOUGH_TIME = 20;
    private static final long DEFAULT_SCREENED_ENOUGH_TIME = 36000;
    private static final String DOMESTIC_BETA_VERSION = "3";
    private static final String DOMESTIC_RELEASE_VERSION = "1";
    private static final boolean IS_ANTIMAL_PROTECTION = "true".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.antimal_protection", "true"));
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", BuildConfig.FLAVOR));
    private static final boolean IS_DOMESTIC_RELEASE = "1".equalsIgnoreCase(SystemPropertiesEx.get("ro.logsystem.usertype", DOMESTIC_BETA_VERSION));
    private static final boolean IS_ROOT = SWITCH_OFF.equalsIgnoreCase(SystemPropertiesEx.get("ro.secure", "1"));
    private static final boolean IS_TABLET = "tablet".equalsIgnoreCase(SystemPropertiesEx.get("ro.build.characteristics", "default"));
    private static final boolean IS_TV_DEVICE = "tv".equalsIgnoreCase(SystemPropertiesEx.get("ro.build.characteristics", "default"));
    private static final String MANAGE_USE_SECURITY = "com.huawei.permission.MANAGE_USE_SECURITY";
    private static final String[] NOT_ALLOWED_DISABLE_WHITELIST = {"com.huawei.android.launcher", PermConst.SYSTEM_MANAGER_PACKAGE_NAME};
    private static final String SWITCH_OFF = "0";
    private static final String SWITCH_ON = "1";
    private static final String TAG = HwAntiMalPlugin.class.getSimpleName();
    private Context mContext;
    private String mDefaultCustLauncher = BuildConfig.FLAVOR;
    private HwDeviceUsageCollection mHwDeviceUsageCollection;
    private boolean mIsStopToSetMalData = false;

    public HwAntiMalPlugin(Context context) {
        this.mContext = context;
        checkIfTopAppsDisabled();
    }

    public static boolean isNeedRegisterAntiMalPlugin() {
        return IS_CHINA_AREA && IS_ANTIMAL_PROTECTION && !IS_TV_DEVICE;
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.huawei.server.security.antimal.HwAntiMalPlugin$HwAntiMalPluginImpl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        return new HwAntiMalPluginImpl();
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        this.mHwDeviceUsageCollection = new HwDeviceUsageCollection(this.mContext);
        this.mHwDeviceUsageCollection.onStart();
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
        Log.e(TAG, "AntiMal service stopped.");
    }

    private void checkIfTopAppsDisabled() {
        int len = NOT_ALLOWED_DISABLE_WHITELIST.length;
        for (int i = 0; i < len; i++) {
            checkIfDisabled(NOT_ALLOWED_DISABLE_WHITELIST[i]);
        }
        if (!TextUtils.isEmpty(getCustDefaultLauncher())) {
            checkIfDisabled(getCustDefaultLauncher());
        }
    }

    private String getCustDefaultLauncher() {
        return this.mDefaultCustLauncher;
    }

    private void checkIfDisabled(String pkgName) {
        PackageManager packageManager = this.mContext.getPackageManager();
        try {
            int currState = packageManager.getApplicationEnabledSetting(pkgName);
            if (currState == 2 || currState == 3 || currState == 4) {
                String str = TAG;
                Log.i(str, pkgName + " is disabled! enable it!");
                packageManager.setApplicationEnabledSetting(pkgName, 1, 1);
            }
        } catch (Exception e) {
            Log.e(TAG, "getApplicationEnabledSetting fail!");
        }
    }

    private class HwAntiMalPluginImpl extends IHwAntiMalPlugin.Stub {
        private static final String ANTIMAL_MODULE = "antiMalware";
        private static final String CHARGE_TIME = "CHARGETIME";
        private static final String DATA_SRC = "datasrc";
        private static final String INSERT_DATA_SWITCH = "stopset";
        private static final int INSERT_MAL_DATA = 0;
        private static final String INSERT_RESULT = "result";
        private static final String INTERFACE_TYPE = "interfacetype";
        private static final String OEM_CALL_TIME = "OEMTALKTIME";
        private static final String OEM_SCREEN_ON_TIME = "OEMSCREENONTIME";
        private static final String PROTECTION_TYPE = "prt";
        private static final String PROTECT_RESULT = "protectresult";
        private static final int REQUEST_PROTECTION_POLICY = 1;

        private HwAntiMalPluginImpl() {
        }

        @Deprecated
        public boolean isAntiMalProtectionOn(Bundle params) {
            return false;
        }

        private boolean isSimCardInserted(Context context) {
            Object object = context.getSystemService("phone");
            if (!(object instanceof TelephonyManager)) {
                return true;
            }
            int simState = ((TelephonyManager) object).getSimState();
            if (simState == 1 || simState == 0) {
                return false;
            }
            return true;
        }

        private int handleGetProtectionPolicyFailure(Bundle composedPolices) {
            if (composedPolices.getInt(PROTECTION_TYPE) != HwSecurityDiagnoseManager.AntiMalProtectType.ADB.ordinal()) {
                return 1;
            }
            if (!HwAntiMalPlugin.IS_ROOT && HwAntiMalPlugin.IS_DOMESTIC_RELEASE && !HwAntiMalPlugin.IS_TABLET && composedPolices.getLong(OEM_SCREEN_ON_TIME) < HwAntiMalPlugin.DEFAULT_SCREENED_ENOUGH_TIME && composedPolices.getLong(OEM_CALL_TIME) < HwAntiMalPlugin.DEFAULT_CALL_ENOUGH_TIME && composedPolices.getLong(CHARGE_TIME) < HwAntiMalPlugin.DEFAULT_CHARGING_ENOUGH_TIME && !isSimCardInserted(HwAntiMalPlugin.this.mContext)) {
                return 1;
            }
            return 0;
        }

        public int getAntimalProtectionPolicy(int type, Bundle policy) {
            if (!HwAntiMalPlugin.IS_CHINA_AREA) {
                Log.i(HwAntiMalPlugin.TAG, "Not in valid area!");
                return 0;
            } else if (!isProtectValidType(type)) {
                String str = HwAntiMalPlugin.TAG;
                Log.e(str, "getAntimalProtectionPolicy-type error:" + type);
                return 0;
            } else {
                AntiMalDataPipeline antiMalDataPipeline = AntiMalDataPipeline.getInstance();
                Bundle composedPolices = composePolices(policy, type);
                Bundle resBundle = antiMalDataPipeline.transferMalInformation(ANTIMAL_MODULE, composedPolices);
                if (resBundle != null) {
                    return resBundle.getInt(PROTECT_RESULT);
                }
                Log.e(HwAntiMalPlugin.TAG, "getAntimalProtectionPolicy: resBundle is null.");
                return handleGetProtectionPolicyFailure(composedPolices);
            }
        }

        public boolean setMalData(int type, Bundle features) {
            if (!HwAntiMalPlugin.IS_CHINA_AREA) {
                Log.i(HwAntiMalPlugin.TAG, "Not in valid area!");
                return false;
            } else if (HwAntiMalPlugin.this.mIsStopToSetMalData) {
                Log.i(HwAntiMalPlugin.TAG, "No need to set malData");
                return false;
            } else if (features == null) {
                Log.e(HwAntiMalPlugin.TAG, "Invalid input params!");
                return false;
            } else if (!isDataSrcValidType(type)) {
                String str = HwAntiMalPlugin.TAG;
                Log.e(str, "setMalData-type error:" + type);
                return false;
            } else {
                Bundle resBundle = AntiMalDataPipeline.getInstance().transferMalInformation(ANTIMAL_MODULE, composeMalFeatures(features, type));
                if (resBundle == null) {
                    Log.w(HwAntiMalPlugin.TAG, "setMalData: resBundle is null.");
                    return false;
                }
                HwAntiMalPlugin.this.mIsStopToSetMalData = resBundle.getBoolean(INSERT_DATA_SWITCH);
                return resBundle.getBoolean(INSERT_RESULT);
            }
        }

        private boolean isProtectValidType(int type) {
            return type > HwSecurityDiagnoseManager.AntiMalProtectType.BEGIN.ordinal() && type < HwSecurityDiagnoseManager.AntiMalProtectType.END.ordinal();
        }

        private Bundle composePolices(@Nullable Bundle originPolicy, int type) {
            Bundle composedPolices;
            if (originPolicy == null) {
                composedPolices = new Bundle();
            } else {
                composedPolices = new Bundle(originPolicy);
            }
            composedPolices.putInt(INTERFACE_TYPE, 1);
            composedPolices.putInt(PROTECTION_TYPE, type);
            HwDeviceUsageOEMINFO deviceUsage = HwDeviceUsageOEMINFO.getInstance();
            composedPolices.putLong(OEM_SCREEN_ON_TIME, deviceUsage.getScreenOnTime());
            composedPolices.putLong(OEM_CALL_TIME, deviceUsage.getTalkTime());
            composedPolices.putLong(CHARGE_TIME, deviceUsage.getChargeTime());
            return composedPolices;
        }

        private boolean isDataSrcValidType(int type) {
            return type > HwSecurityDiagnoseManager.AntiMalDataSrcType.BEGIN.ordinal() && type < HwSecurityDiagnoseManager.AntiMalDataSrcType.END.ordinal();
        }

        private Bundle composeMalFeatures(@NonNull Bundle originFeature, int type) {
            Bundle composedFeatures = new Bundle(originFeature);
            composedFeatures.putInt(INTERFACE_TYPE, 0);
            composedFeatures.putInt(DATA_SRC, type);
            return composedFeatures;
        }
    }
}
