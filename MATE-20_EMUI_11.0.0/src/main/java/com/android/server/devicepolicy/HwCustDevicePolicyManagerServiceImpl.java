package com.android.server.devicepolicy;

import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.huawei.android.util.HwPasswordUtils;
import com.huawei.systemmanager.appcontrol.iaware.HwAppStartupSettingEx;
import com.huawei.systemmanager.appcontrol.iaware.HwIAwareManager;
import com.huawei.systemmanager.appcontrol.iaware.IMultiTaskManager;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HwCustDevicePolicyManagerServiceImpl extends HwCustDevicePolicyManagerService {
    private static final int AS_MODIFIER_USER = 1;
    private static final int AS_TP_SHW = 0;
    private static final int AS_TP_SLF = 1;
    private static final int AS_TP_SMT = 0;
    private static final String DB_KEY_WIPE_DATA = "wipedata_authentication";
    private static final int DEFAULT_ATT_SWITCH = 0;
    private static final int DEFAULT_MAX_ERROR_TIMES = 5;
    private static final boolean FORBIDDEN_SIMPLE_PWD = SystemProperties.getBoolean("ro.config.not_allow_simple_pwd", false);
    private static final boolean IS_ATT = (SystemProperties.get("ro.config.hw_opta", "0").equals("07") && SystemProperties.get("ro.config.hw_optb", "0").equals("840"));
    private static final boolean IS_NOT_ALLOWED_CERTI_NOTIF = SystemProperties.getBoolean("ro.config.hw_disable_certifNoti", false);
    private static final boolean IS_WIPE_STORAGE_DATA = SystemProperties.getBoolean("ro.config.hw_eas_sdformat", false);
    private static final String LOG_TAG = "HwCustDevicePolicyManagerServiceImpl";
    private static final double MAX_RETRY_TIMES = 3.0d;
    private static final int[] MODIFY_CATEGARY = {1, 1, 1, 1};
    private static final int[] POLICY_CATEGARY = {0, 1, 1, 1};
    private static final int[] SHOW_CATAGARY = {0, 0, 0, 0};
    private static final long WAIT_FOR_IAWAREREADY_INTERVAL = 3000;
    private HwFrameworkMonitor mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();

    public void wipeStorageData(Context context) {
        if (IS_WIPE_STORAGE_DATA && context != null) {
            wipeExternalStorage(context);
            Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
            intent.addFlags(268435456);
            intent.putExtra("masterClearWipeDataFactory", true);
            context.sendBroadcast(intent);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wipeExternalStorage(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService("storage");
        for (VolumeInfo vol : storageManager.getVolumes()) {
            if (vol.getDisk() != null && vol.getDisk().isSd()) {
                storageManager.partitionPublic(vol.getDisk().getId());
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
        try {
            boolean isOpen = true;
            if (Settings.Global.getInt(context.getContentResolver(), DB_KEY_WIPE_DATA, 0) != 1) {
                isOpen = false;
            }
            return isOpen;
        } catch (SecurityException e) {
            Slog.w(LOG_TAG, "can't get erase data switch value " + e.getMessage());
            return false;
        } catch (Exception e2) {
            Slog.w(LOG_TAG, "can't get erase data switch value " + e2.getMessage());
            return false;
        }
    }

    public void isStartEraseAllDataForAtt(final Context context, int failedAttemps) {
        int maxTimes;
        if (context != null) {
            try {
                maxTimes = Settings.System.getIntForUser(context.getContentResolver(), "password_authentication_threshold", DEFAULT_MAX_ERROR_TIMES, -2);
            } catch (SecurityException e) {
                maxTimes = DEFAULT_MAX_ERROR_TIMES;
                Slog.w(LOG_TAG, "can't max error times return default " + e.getMessage());
            } catch (Exception e2) {
                maxTimes = DEFAULT_MAX_ERROR_TIMES;
                Slog.w(LOG_TAG, "can't max error times return default " + e2.getMessage());
            }
            Slog.i(LOG_TAG, "Start erase data, failedAttemps = " + failedAttemps + " maxTimes = " + maxTimes);
            if (failedAttemps >= maxTimes) {
                new AsyncTask<Void, Void, Void>() {
                    /* class com.android.server.devicepolicy.HwCustDevicePolicyManagerServiceImpl.AnonymousClass1 */

                    /* access modifiers changed from: protected */
                    public Void doInBackground(Void... params) {
                        HwCustDevicePolicyManagerServiceImpl.this.wipeExternalStorage(context);
                        return null;
                    }

                    /* access modifiers changed from: protected */
                    public void onPostExecute(Void result) {
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
            return !new File(policyPath).exists();
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
                    boolean ret = FileUtils.copyFile(devicePolices, new File(policyPath));
                    Slog.d(LOG_TAG, "active preset device admins result : " + ret);
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
            /* class com.android.server.devicepolicy.HwCustDevicePolicyManagerServiceImpl.AnonymousClass2 */

            /* access modifiers changed from: protected */
            public Void doInBackground(Void... params) {
                HwCustDevicePolicyManagerServiceImpl.this.wipeExternalStorage(context);
                return null;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void result) {
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
        if (!IS_NOT_ALLOWED_CERTI_NOTIF || !"MobileIron".equals(applicationLable)) {
            return true;
        }
        Slog.i(LOG_TAG, "not allowed certificate notification for application: " + applicationLable);
        return false;
    }

    public void monitorFactoryReset(String component, String reason) {
        if (this.mMonitor == null || TextUtils.isEmpty(reason)) {
            Slog.e(LOG_TAG, "monitorFactoryReset: Invalid parameter,mMonitor=" + this.mMonitor + ", reason=" + reason);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("component", component);
        bundle.putString("reason", reason);
        this.mMonitor.monitor(907400018, bundle);
    }

    public void clearWipeDataFactoryLowlevel(Context context, String reason, boolean wipeEuicc) {
        Slog.d(LOG_TAG, "wipeData, reason=" + reason + ", wipeEuicc=" + wipeEuicc);
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.addFlags(285212672);
        intent.putExtra("masterClearWipeDataFactoryLowlevel", true);
        intent.putExtra("com.android.internal.intent.extra.WIPE_ESIMS", wipeEuicc);
        context.sendBroadcastAsUser(intent, UserHandle.SYSTEM);
    }

    public void setDeviceOwnerEx(Context context, ComponentName admin) {
        if (admin != null) {
            DftUtils.collectMdmDoSuccessDftData(context, admin.getPackageName());
            setPackageLaunchableAndBackgroundRunable(admin.getPackageName());
        }
    }

    public void setProfileOwnerEx(Context context, ComponentName admin) {
        if (admin != null) {
            DftUtils.collectMdmWpSuccessDftData(context, admin.getPackageName());
            setPackageLaunchableAndBackgroundRunable(admin.getPackageName());
        }
    }

    private void setPackageLaunchableAndBackgroundRunable(String packageName) {
        final List<HwAppStartupSettingEx> aePackageConfigs = new ArrayList<>();
        aePackageConfigs.add(new HwAppStartupSettingEx(packageName, POLICY_CATEGARY, MODIFY_CATEGARY, SHOW_CATAGARY));
        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
            /* class com.android.server.devicepolicy.HwCustDevicePolicyManagerServiceImpl.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                boolean isIAwareAvailable = false;
                int i = 0;
                while (true) {
                    if (((double) i) >= HwCustDevicePolicyManagerServiceImpl.MAX_RETRY_TIMES) {
                        break;
                    }
                    IMultiTaskManager itf = HwIAwareManager.getMultiTaskManager();
                    if (itf != null) {
                        long identity = Binder.clearCallingIdentity();
                        try {
                            boolean isResult = itf.updateAppStartupSettings(aePackageConfigs, false);
                            isIAwareAvailable = true;
                            Slog.i(HwCustDevicePolicyManagerServiceImpl.LOG_TAG, "setPackageLaunchableAndBackgroundRunable result:" + isResult);
                        } catch (RemoteException e) {
                            Slog.e(HwCustDevicePolicyManagerServiceImpl.LOG_TAG, "updateStartupSettings ex");
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(identity);
                            throw th;
                        }
                        Binder.restoreCallingIdentity(identity);
                        break;
                    }
                    try {
                        Thread.sleep(HwCustDevicePolicyManagerServiceImpl.WAIT_FOR_IAWAREREADY_INTERVAL);
                    } catch (InterruptedException e2) {
                        Slog.e(HwCustDevicePolicyManagerServiceImpl.LOG_TAG, "reTry app whitelist failed");
                    }
                    i++;
                }
                if (!isIAwareAvailable) {
                    Slog.e(HwCustDevicePolicyManagerServiceImpl.LOG_TAG, "IMultiTskMngerService unavailable after times retry ");
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }
}
