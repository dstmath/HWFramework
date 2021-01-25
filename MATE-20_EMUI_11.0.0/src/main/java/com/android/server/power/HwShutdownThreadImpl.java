package com.android.server.power;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.LocalServices;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.policy.HwPolicyFactory;
import com.android.server.power.ShutdownThread;
import com.android.server.tv.HwTvPowerManagerPolicy;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;

public class HwShutdownThreadImpl implements IHwShutdownThread {
    private static final String BASEFILEPATH = "/data/hw_init";
    private static final String BASEPATH = "media/shutdownanimation_";
    private static final String INVALID_VALUE = "0";
    private static final int MAX_SHUTDOWN_ANIM_WAIT_MSEC = 15000;
    private static final String NOTCHPROP = SystemProperties.get("ro.config.hw_notch_size", "");
    private static final int PHONE_STATE_POLL_SLEEP_MSEC = 500;
    private static final int SCREEN_ROTATION_SETTING = 8002;
    private static final String SHUTDOWN_ANIMATION_STATUS_PROPERTY = "sys.shutdown.animationstate";
    private static final int SHUTDOWN_ANIMATION_WAIT_TIME = 2000;
    private static final String SHUTDOWN_PATH1 = "/data/cust/media/shutdownanimation.zip";
    private static final String SHUTDOWN_PATH2 = "/data/local/shutdownanimation.zip";
    private static final String SHUTDOWN_PATH3 = "/system/media/shutdownanimation.zip";
    private static final String TAG = "HwShutdownThread";
    private static final String ZIP = ".zip";
    private static AlertDialog sRootConfirmDialog;
    private boolean isHaveShutdownAnimation = false;
    private boolean isHwShutDownAnimationStart;

    private static boolean isIccidCustZipExist() {
        String iccid = SystemProperties.get("persist.sys.iccid", "0");
        if (!"0".equals(iccid)) {
            try {
                if (HwCfgFilePolicy.getCfgFile(BASEPATH + iccid + ZIP, 0) != null) {
                    return true;
                }
            } catch (NoClassDefFoundError e) {
                Log.d(TAG, "HwCfgFilePolicy Iccid NoClassDefFoundError");
            }
        }
        return false;
    }

    private static boolean isMccmncCustZipExist() {
        String mccmnc = SystemProperties.get("persist.sys.mccmnc", "0");
        if (!"0".equals(mccmnc)) {
            try {
                if (HwCfgFilePolicy.getCfgFile(BASEPATH + mccmnc + ZIP, 0) != null) {
                    return true;
                }
            } catch (NoClassDefFoundError e) {
                Log.d(TAG, "HwCfgFilePolicy Mccmnc NoClassDefFoundError");
            }
        }
        return false;
    }

    private static boolean isBootCustZipExist() {
        String boot = SystemProperties.get("persist.sys.boot", "0");
        if (!"0".equals(boot)) {
            try {
                if (HwCfgFilePolicy.getCfgFile(BASEPATH + boot + ZIP, 0) != null) {
                    return true;
                }
            } catch (NoClassDefFoundError e) {
                Log.e(TAG, "HwCfgFilePolicy boot NoClassDefFoundError");
            }
        }
        return false;
    }

    private static boolean isDataZipExist() {
        try {
            String[] shutdowAnimationFiles = HwCfgFilePolicy.getCfgPolicyDir(0);
            for (int i = shutdowAnimationFiles.length - 1; i >= 0; i--) {
                File file = new File(BASEFILEPATH + shutdowAnimationFiles[i], "media/shutdownanimation.zip");
                if (file.exists()) {
                    Log.d(TAG, "filename is" + file.getPath());
                    return true;
                }
            }
            String boot = SystemProperties.get("persist.sys.boot", "0");
            if (!"0".equals(boot)) {
                String bootPath = BASEPATH + boot + ZIP;
                for (int i2 = shutdowAnimationFiles.length - 1; i2 >= 0; i2--) {
                    File file2 = new File(BASEFILEPATH + shutdowAnimationFiles[i2], bootPath);
                    if (file2.exists()) {
                        Log.d(TAG, "boot filename is" + file2.getPath());
                        return true;
                    }
                }
            }
            String mccmnc = SystemProperties.get("persist.sys.mccmnc", "0");
            if (!"0".equals(mccmnc)) {
                String mccmncPath = BASEPATH + mccmnc + ZIP;
                for (int i3 = shutdowAnimationFiles.length - 1; i3 >= 0; i3--) {
                    File file3 = new File(BASEFILEPATH + shutdowAnimationFiles[i3], mccmncPath);
                    if (file3.exists()) {
                        Log.d(TAG, "mccmnc filename is" + file3.getPath());
                        return true;
                    }
                }
            }
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "HwCfgFilePolicy boot NoClassDefFoundError");
        }
        return false;
    }

    private boolean isEnterpriseExist() {
        File file = new File("/data/cota/cloud/enterprise/media/shutdownanimation.zip");
        if (!file.exists()) {
            return false;
        }
        Log.d(TAG, "enterprise filename is" + file.getPath());
        return true;
    }

    public boolean isShutDownAnimationAvailable() {
        boolean isShutDownAnimationExist = false;
        try {
            if (HwCfgFilePolicy.getCfgFile("media/shutdownanimation.zip", 0) != null || isMccmncCustZipExist() || isIccidCustZipExist() || isBootCustZipExist() || isDataZipExist() || isEnterpriseExist()) {
                isShutDownAnimationExist = true;
            }
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (!isShutDownAnimationExist) {
            isShutDownAnimationExist = new File(SHUTDOWN_PATH1).exists() || new File(SHUTDOWN_PATH2).exists() || new File(SHUTDOWN_PATH3).exists();
        }
        boolean isUseShutdownAnimation = SystemProperties.getBoolean("ro.config.use_shutdown_anim", true);
        if (!isShutDownAnimationExist || !isUseShutdownAnimation) {
            return false;
        }
        return true;
    }

    private static boolean hasNotchInScreen() {
        return !TextUtils.isEmpty(NOTCHPROP);
    }

    public boolean isDoShutdownAnimation() {
        if (hasNotchInScreen()) {
            transferSwitchStatusToSurfaceFlinger(0);
        }
        if (!isShutDownAnimationAvailable()) {
            return false;
        }
        try {
            ServiceManager.getService("window").freezeOrThawRotation(0);
        } catch (NoSuchMethodError e) {
            Log.e(TAG, "NoSuchMethod freezeOrThawRotation");
        }
        this.isHaveShutdownAnimation = true;
        Log.d(TAG, "ctl.start shutanim service!");
        SystemProperties.set("ctl.start", "shutanim");
        this.isHwShutDownAnimationStart = true;
        return true;
    }

    private static void transferSwitchStatusToSurfaceFlinger(int val) {
        Log.i(TAG, "Transfer Switch status to SurfaceFlinger , val = " + val);
        Parcel dataIn = Parcel.obtain();
        try {
            IBinder sfBinder = ServiceManager.getService("SurfaceFlinger");
            dataIn.writeInt(val);
            if (sfBinder != null && !sfBinder.transact(SCREEN_ROTATION_SETTING, dataIn, null, 1)) {
                Log.e(TAG, "transferSwitchStatusToSurfaceFlinger error!");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "transferSwitchStatusToSurfaceFlinger RemoteException on notify screen rotation animation end");
        } catch (Throwable th) {
            dataIn.recycle();
            throw th;
        }
        dataIn.recycle();
    }

    public void waitShutdownAnimation() {
        if (this.isHaveShutdownAnimation) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "shutdown animation thread sleep 2s failed");
            }
        }
    }

    public boolean needRebootDialog(String rebootReason, Context context) {
        if (rebootReason == null || !"huawei_reboot".equals(rebootReason)) {
            return false;
        }
        rebootDialog(context);
        return true;
    }

    public boolean needRebootProgressDialog(boolean isReboot, Context context) {
        if (!isReboot || HwPolicyFactory.isHwGlobalActionsShowing()) {
            return false;
        }
        if ("tv".equals(SystemProperties.get("ro.build.characteristics", "0")) || "mobiletv".equals(SystemProperties.get("ro.build.characteristics", "0"))) {
            return true;
        }
        rebootProgressDialog(context);
        return true;
    }

    private static synchronized void rebootDialog(final Context context) {
        synchronized (HwShutdownThreadImpl.class) {
            ShutdownThread.CloseDialogReceiver closer = new ShutdownThread.CloseDialogReceiver(context);
            if (sRootConfirmDialog != null) {
                sRootConfirmDialog.dismiss();
            }
            sRootConfirmDialog = new AlertDialog.Builder(context, 33947691).setTitle(33685517).setMessage(33685518).setIcon(17301543).setPositiveButton(17039379, new DialogInterface.OnClickListener() {
                /* class com.android.server.power.HwShutdownThreadImpl.AnonymousClass1 */

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    ShutdownThread.beginShutdownSequence(context);
                }
            }).setNegativeButton(17039369, (DialogInterface.OnClickListener) null).create();
            closer.dialog = sRootConfirmDialog;
            sRootConfirmDialog.setOnDismissListener(closer);
            sRootConfirmDialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_UNBIND_SUCCESS);
            sRootConfirmDialog.show();
        }
    }

    private static void rebootProgressDialog(Context context) {
        ProgressDialog pd = new ProgressDialog(context, 33947691);
        pd.setTitle(context.getText(33685515));
        pd.setMessage(context.getText(33685565));
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_UNBIND_SUCCESS);
        pd.show();
    }

    public void resetValues() {
        this.isHwShutDownAnimationStart = false;
    }

    public void waitShutdownAnimationComplete(Context context, long shutDownBegin) {
        if (this.isHwShutDownAnimationStart && shutDownBegin > 0) {
            long endTime = HwArbitrationDEFS.WIFI_RX_BYTES_THRESHOLD + shutDownBegin;
            long delay = endTime - SystemClock.elapsedRealtime();
            boolean isShutDownAnimationFinish = AppActConstant.VALUE_TRUE.equals(SystemProperties.get(SHUTDOWN_ANIMATION_STATUS_PROPERTY, AppActConstant.VALUE_FALSE));
            Log.i(TAG, "ShutDown Animation Status delay:" + delay + ", isShutDownAnimationFinish:" + isShutDownAnimationFinish + ", shutDownBegin:" + shutDownBegin + ", endTime:" + endTime);
            while (delay > 0 && !isShutDownAnimationFinish) {
                SystemClock.sleep(500);
                delay = endTime - SystemClock.elapsedRealtime();
                isShutDownAnimationFinish = AppActConstant.VALUE_TRUE.equals(SystemProperties.get(SHUTDOWN_ANIMATION_STATUS_PROPERTY, AppActConstant.VALUE_FALSE));
            }
            SystemProperties.set(SHUTDOWN_ANIMATION_STATUS_PROPERTY, AppActConstant.VALUE_FALSE);
        }
    }

    public void onEarlyShutdownBegin(boolean isReboot, boolean isRebootSafeMode, String reason) {
        HwTvPowerManagerPolicy tvPolicy = (HwTvPowerManagerPolicy) LocalServices.getService(HwTvPowerManagerPolicy.class);
        if (tvPolicy != null) {
            tvPolicy.onEarlyShutdownBegin(isReboot);
        }
    }
}
