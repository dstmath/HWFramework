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
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.policy.HwPolicyFactory;
import com.android.server.power.ShutdownThread;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;

public class HwShutdownThreadImpl implements IHwShutdownThread {
    private static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int MAX_SHUTDOWN_ANIM_WAIT_MSEC = 15000;
    private static final int PHONE_STATE_POLL_SLEEP_MSEC = 500;
    private static final int SCREEN_ROTATION_SETTING = 8002;
    private static final String SHUTDOWN_ANIMATION_STATUS_PROPERTY = "sys.shutdown.animationstate";
    private static final int SHUTDOWN_ANIMATION_WAIT_TIME = 2000;
    private static final String TAG = "HwShutdownThread";
    public static final String mNotchProp = SystemProperties.get("ro.config.hw_notch_size", "");
    private static String mShutdown_path1 = "/data/cust/media/shutdownanimation.zip";
    private static String mShutdown_path2 = "/data/local/shutdownanimation.zip";
    private static String mShutdown_path3 = "/system/media/shutdownanimation.zip";
    private static AlertDialog sRootConfirmDialog;
    private boolean isHaveShutdownAnimation = false;
    private boolean mHwShutDownAnimationStart;

    public boolean isShutDownAnimationAvailable() {
        boolean doseShutDownAnimationExist = false;
        try {
            if (HwCfgFilePolicy.getCfgFile("media/shutdownanimation.zip", 0) != null || isMccmncCustZipExist() || isIccidCustZipExist() || isBootCustZipExist() || isDataZipExist() || isEnterpriseExist()) {
                doseShutDownAnimationExist = true;
            }
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (!doseShutDownAnimationExist) {
            doseShutDownAnimationExist = new File(mShutdown_path1).exists() || new File(mShutdown_path2).exists() || new File(mShutdown_path3).exists();
        }
        boolean useShutdownAnimation = SystemProperties.getBoolean("ro.config.use_shutdown_anim", true);
        if (!doseShutDownAnimationExist || !useShutdownAnimation) {
            return false;
        }
        return true;
    }

    public static boolean hasNotchInScreen() {
        return !TextUtils.isEmpty(mNotchProp);
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
        } catch (Exception e) {
        }
        this.isHaveShutdownAnimation = true;
        try {
            Log.d(TAG, "ctl.start shutanim service!");
            SystemProperties.set("ctl.start", "shutanim");
        } catch (Exception e2) {
            Log.e(TAG, "run shutdown animation failed", e2);
        }
        this.mHwShutDownAnimationStart = true;
        return true;
    }

    public static void transferSwitchStatusToSurfaceFlinger(int val) {
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

    private static boolean isIccidCustZipExist() {
        String iccid = SystemProperties.get("persist.sys.iccid", "0");
        if (!"0".equals(iccid)) {
            try {
                if (HwCfgFilePolicy.getCfgFile("media/shutdownanimation_" + iccid + ".zip", 0) != null) {
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
                if (HwCfgFilePolicy.getCfgFile("media/shutdownanimation_" + mccmnc + ".zip", 0) != null) {
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
                if (HwCfgFilePolicy.getCfgFile("media/shutdownanimation_" + boot + ".zip", 0) != null) {
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
                if (new File("/data/hw_init" + shutdowAnimationFiles[i], "media/shutdownanimation.zip").exists()) {
                    Log.d(TAG, "file name is" + file.getPath());
                    return true;
                }
            }
            if (!"0".equals(SystemProperties.get("persist.sys.boot", "0"))) {
                String matchPath_boot = "media/shutdownanimation_" + boot + ".zip";
                for (int i2 = shutdowAnimationFiles.length - 1; i2 >= 0; i2--) {
                    if (new File("/data/hw_init" + shutdowAnimationFiles[i2], matchPath_boot).exists()) {
                        Log.d(TAG, "file name is" + file.getPath());
                        return true;
                    }
                }
            }
            if (!"0".equals(SystemProperties.get("persist.sys.mccmnc", "0"))) {
                String matchPath_mccmnc = "media/shutdownanimation_" + mccmnc + ".zip";
                for (int i3 = shutdowAnimationFiles.length - 1; i3 >= 0; i3--) {
                    if (new File("/data/hw_init" + shutdowAnimationFiles[i3], matchPath_mccmnc).exists()) {
                        Log.d(TAG, "file name is" + file.getPath());
                        return true;
                    }
                }
            }
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "HwCfgFilePolicy boot NoClassDefFoundError");
        }
        return false;
    }

    private static boolean isEnterpriseExist() {
        File file = new File("/data/cota/cloud/enterprise/media/shutdownanimation.zip");
        if (!file.exists()) {
            return false;
        }
        Log.d(TAG, "file name is" + file.getPath());
        return true;
    }

    public void waitShutdownAnimation() {
        if (this.isHaveShutdownAnimation) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "shutdown animation thread sleep 2s failed", e);
            }
        }
    }

    public boolean needRebootDialog(String rebootReason, Context context) {
        if (rebootReason == null || !rebootReason.equals("huawei_reboot")) {
            return false;
        }
        rebootDialog(context);
        return true;
    }

    public boolean needRebootProgressDialog(boolean reboot, Context context) {
        if (!reboot || HwPolicyFactory.isHwGlobalActionsShowing()) {
            return false;
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
                public void onClick(DialogInterface dialog, int which) {
                    ShutdownThread.beginShutdownSequence(context);
                }
            }).setNegativeButton(17039369, null).create();
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
        this.mHwShutDownAnimationStart = false;
    }

    public void waitShutdownAnimationComplete(Context context, long shutDownBegin) {
        if (this.mHwShutDownAnimationStart && shutDownBegin > 0) {
            long endTime = 15000 + shutDownBegin;
            long delay = endTime - SystemClock.elapsedRealtime();
            boolean shutDownAnimationFinish = "true".equals(SystemProperties.get(SHUTDOWN_ANIMATION_STATUS_PROPERTY, "false"));
            Log.i(TAG, "ShutDown Animation Status delay:" + delay + ", shutDownAnimationFinish:" + shutDownAnimationFinish + ", shutDownBegin:" + shutDownBegin + ", endTime:" + endTime);
            while (delay > 0 && !shutDownAnimationFinish) {
                SystemClock.sleep(500);
                delay = endTime - SystemClock.elapsedRealtime();
                shutDownAnimationFinish = "true".equals(SystemProperties.get(SHUTDOWN_ANIMATION_STATUS_PROPERTY, "false"));
            }
            SystemProperties.set(SHUTDOWN_ANIMATION_STATUS_PROPERTY, "false");
        }
    }
}
