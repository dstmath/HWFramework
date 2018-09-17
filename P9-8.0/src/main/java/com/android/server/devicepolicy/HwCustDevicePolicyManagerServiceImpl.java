package com.android.server.devicepolicy;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Slog;
import com.huawei.android.util.HwPasswordUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;

public class HwCustDevicePolicyManagerServiceImpl extends HwCustDevicePolicyManagerService {
    private static final String DB_KEY_WIPE_DATA = "wipedata_authentication";
    private static final int DEFAULT_ATT_SWITCH = 0;
    private static final int DEFAULT_MAX_ERROR_TIMES = 5;
    private static boolean FORBIDDEN_SIMPLE_PWD = SystemProperties.getBoolean("ro.config.not_allow_simple_pwd", false);
    private static final boolean IS_ATT;
    private static final boolean IS_NOT_ALLOWED_CERTI_NOTIF = SystemProperties.getBoolean("ro.config.hw_disable_certifNoti", false);
    private static final boolean IS_WIPE_STORAGE_DATA = SystemProperties.getBoolean("ro.config.hw_eas_sdformat", false);
    private static final String LOG_TAG = "HwCustDevicePolicyManagerServiceImpl";

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("07")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("840");
        } else {
            equals = false;
        }
        IS_ATT = equals;
    }

    public void wipeStorageData(Context context) {
        if (IS_WIPE_STORAGE_DATA && context != null) {
            wipeExternalStorage(context);
            Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
            intent.addFlags(268435456);
            intent.putExtra("masterClearWipeDataFactory", true);
            context.sendBroadcast(intent);
        }
    }

    private void wipeExternalStorage(Context context) {
        StorageManager mStorageManager = (StorageManager) context.getSystemService("storage");
        for (VolumeInfo vol : mStorageManager.getVolumes()) {
            if (vol.getDisk() != null && vol.getDisk().isSd()) {
                mStorageManager.partitionPublic(vol.getDisk().getId());
                return;
            }
        }
    }

    public boolean wipeDataAndReset(Context context) {
        if (context == null) {
            return false;
        }
        if (((UserManager) context.getSystemService("user")).hasUserRestriction("no_factory_reset")) {
            Slog.w(LOG_TAG, "Remote Wiping data is not allowed for this user.");
            return false;
        }
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.addFlags(285212672);
        intent.putExtra("masterClearWipeDataFactoryLowlevel", true);
        context.sendBroadcast(intent);
        return true;
    }

    public boolean isAttEraseDataOn(Context context) {
        if (!IS_ATT || context == null) {
            return false;
        }
        boolean isopen;
        try {
            isopen = Global.getInt(context.getContentResolver(), DB_KEY_WIPE_DATA, 0) == 1;
        } catch (Exception e) {
            isopen = false;
            Slog.w(LOG_TAG, "can't get erase data switch value " + e.toString());
        }
        return isopen;
    }

    public void isStartEraseAllDataForAtt(final Context context, int failedAttemps) {
        if (context != null) {
            int maxTimes;
            try {
                maxTimes = System.getIntForUser(context.getContentResolver(), "password_authentication_threshold", DEFAULT_MAX_ERROR_TIMES, -2);
            } catch (Exception e) {
                maxTimes = DEFAULT_MAX_ERROR_TIMES;
                Slog.w(LOG_TAG, "can't max error times return default " + e.toString());
            }
            Slog.i(LOG_TAG, "Start erase data, failedAttemps = " + failedAttemps + " maxTimes = " + maxTimes);
            if (failedAttemps >= maxTimes) {
                new AsyncTask<Void, Void, Void>() {
                    protected Void doInBackground(Void... params) {
                        HwCustDevicePolicyManagerServiceImpl.this.wipeExternalStorage(context);
                        return null;
                    }

                    protected void onPostExecute(Void result) {
                        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
                        intent.addFlags(285212672);
                        intent.putExtra("masterClearWipeDataFactoryLowlevel", true);
                        context.sendBroadcast(intent);
                    }
                }.execute(new Void[0]);
            }
        }
    }

    public boolean shouldActiveDeviceAdmins(String policyPath) {
        if (UserHandle.getCallingUserId() == 0 && !TextUtils.isEmpty(policyPath) && SystemProperties.getBoolean("ro.config.hw_preset_da", false)) {
            return new File(policyPath).exists() ^ 1;
        }
        return false;
    }

    public void activeDeviceAdmins(String policyPath) {
        if (UserHandle.getCallingUserId() != 0) {
            Slog.e(LOG_TAG, "activeDeviceAdmins only allowed for system user.");
        } else if (!TextUtils.isEmpty(policyPath)) {
            try {
                File devicePolices = HwCfgFilePolicy.getCfgFile("xml/device_policies.xml", 0);
                if (devicePolices != null && devicePolices.canRead()) {
                    Slog.d(LOG_TAG, "active preset device admins result : " + FileUtils.copyFile(devicePolices, new File(policyPath)));
                }
            } catch (NoClassDefFoundError e) {
                Slog.e(LOG_TAG, "HwCfgFilePolicy NoClassDefFoundError");
            }
        }
    }

    public boolean eraseStorageForEAS(final Context context) {
        if (!IS_WIPE_STORAGE_DATA || context == null) {
            return false;
        }
        if (((UserManager) context.getSystemService("user")).hasUserRestriction("no_factory_reset")) {
            Slog.w(LOG_TAG, "Remote Wiping data is not allowed for this user.");
            return false;
        }
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                HwCustDevicePolicyManagerServiceImpl.this.wipeExternalStorage(context);
                return null;
            }

            protected void onPostExecute(Void result) {
                HwCustDevicePolicyManagerServiceImpl.this.wipeDataAndReset(context);
            }
        }.execute(new Void[0]);
        return true;
    }

    public boolean isForbiddenSimplePwdFeatureEnable() {
        return FORBIDDEN_SIMPLE_PWD;
    }

    public boolean isNewPwdSimpleCheck(String password, Context context) {
        HwPasswordUtils.loadSimplePasswordTable(context);
        if (HwPasswordUtils.isSimpleAlphaNumericPassword(password) || HwPasswordUtils.isOrdinalCharatersPassword(password) || HwPasswordUtils.isSimplePasswordInDictationary(password)) {
            return true;
        }
        return false;
    }

    public boolean isCertNotificationAllowed(String applicationLable) {
        if (!IS_NOT_ALLOWED_CERTI_NOTIF || applicationLable == null || !applicationLable.equals("MobileIron")) {
            return true;
        }
        Slog.i(LOG_TAG, "not allowed certificate notification for application: " + applicationLable);
        return false;
    }
}
