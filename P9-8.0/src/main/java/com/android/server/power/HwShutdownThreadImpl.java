package com.android.server.power;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.policy.HwPolicyFactory;
import com.android.server.power.ShutdownThread.CloseDialogReceiver;
import com.android.server.wm.WindowManagerService;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;

public class HwShutdownThreadImpl implements IHwShutdownThread {
    private static final boolean DEBUG;
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

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public static boolean hasNotchInScreen() {
        return TextUtils.isEmpty(mNotchProp) ^ 1;
    }

    public boolean isDoShutdownAnimation() {
        if (hasNotchInScreen()) {
            transferSwitchStatusToSurfaceFlinger(0);
        }
        boolean doseShutDownAnimationExist = false;
        try {
            if (HwCfgFilePolicy.getCfgFile("media/shutdownanimation.zip", 0) != null || isMccmncCustZipExist() || isIccidCustZipExist()) {
                doseShutDownAnimationExist = true;
            }
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (!doseShutDownAnimationExist) {
            if (new File(mShutdown_path1).exists() || new File(mShutdown_path2).exists()) {
                doseShutDownAnimationExist = true;
            } else {
                doseShutDownAnimationExist = new File(mShutdown_path3).exists();
            }
        }
        boolean useShutdownAnimation = SystemProperties.getBoolean("ro.config.use_shutdown_anim", true);
        if (!doseShutDownAnimationExist || !useShutdownAnimation) {
            return false;
        }
        try {
            ((WindowManagerService) ServiceManager.getService("window")).freezeOrThawRotation(0);
        } catch (Exception e2) {
        }
        this.isHaveShutdownAnimation = true;
        try {
            Log.d(TAG, "ctl.start shutanim service!");
            SystemProperties.set("ctl.start", "shutanim");
        } catch (Exception e3) {
            Log.e(TAG, "run shutdown animation failed", e3);
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
            if (!(sfBinder == null || (sfBinder.transact(SCREEN_ROTATION_SETTING, dataIn, null, 1) ^ 1) == 0)) {
                Log.e(TAG, "transferSwitchStatusToSurfaceFlinger error!");
            }
            dataIn.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "transferSwitchStatusToSurfaceFlinger RemoteException on notify screen rotation animation end");
            dataIn.recycle();
        } catch (Throwable th) {
            dataIn.recycle();
            throw th;
        }
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
        if (!reboot || (HwPolicyFactory.isHwGlobalActionsShowing() ^ 1) == 0) {
            return false;
        }
        rebootProgressDialog(context);
        return true;
    }

    private static synchronized void rebootDialog(final Context context) {
        synchronized (HwShutdownThreadImpl.class) {
            CloseDialogReceiver closer = new CloseDialogReceiver(context);
            if (sRootConfirmDialog != null) {
                sRootConfirmDialog.dismiss();
            }
            sRootConfirmDialog = new Builder(context, 33947691).setTitle(33685517).setMessage(33685518).setIcon(17301543).setPositiveButton(17039379, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ShutdownThread.beginShutdownSequence(context);
                }
            }).setNegativeButton(17039369, null).create();
            closer.dialog = sRootConfirmDialog;
            sRootConfirmDialog.setOnDismissListener(closer);
            sRootConfirmDialog.getWindow().setType(2009);
            sRootConfirmDialog.show();
        }
    }

    private static void rebootProgressDialog(Context context) {
        ProgressDialog pd = new ProgressDialog(context, 33947691);
        pd.setTitle(context.getText(33685515));
        pd.setMessage(context.getText(33685565));
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.getWindow().setType(2009);
        pd.show();
    }

    public void resetValues() {
        this.mHwShutDownAnimationStart = false;
    }

    public void waitShutdownAnimationComplete(Context context, long shutDownBegin) {
        if (this.mHwShutDownAnimationStart && shutDownBegin > 0) {
            long endTime = shutDownBegin + 15000;
            long delay = endTime - SystemClock.elapsedRealtime();
            boolean shutDownAnimationFinish = StorageUtils.SDCARD_ROMOUNTED_STATE.equals(SystemProperties.get(SHUTDOWN_ANIMATION_STATUS_PROPERTY, StorageUtils.SDCARD_RWMOUNTED_STATE));
            Log.i(TAG, "ShutDown Animation Status delay:" + delay + ", shutDownAnimationFinish:" + shutDownAnimationFinish + ", shutDownBegin:" + shutDownBegin + ", endTime:" + endTime);
            while (delay > 0 && (shutDownAnimationFinish ^ 1) != 0) {
                SystemClock.sleep(500);
                delay = endTime - SystemClock.elapsedRealtime();
                shutDownAnimationFinish = StorageUtils.SDCARD_ROMOUNTED_STATE.equals(SystemProperties.get(SHUTDOWN_ANIMATION_STATUS_PROPERTY, StorageUtils.SDCARD_RWMOUNTED_STATE));
            }
            SystemProperties.set(SHUTDOWN_ANIMATION_STATUS_PROPERTY, StorageUtils.SDCARD_RWMOUNTED_STATE);
        }
    }
}
