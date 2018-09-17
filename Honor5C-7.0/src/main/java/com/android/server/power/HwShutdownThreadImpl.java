package com.android.server.power;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.PPPOEStateMachine;
import com.android.server.jankshield.TableJankEvent;
import com.android.server.policy.HwPolicyFactory;
import com.android.server.power.ShutdownThread.CloseDialogReceiver;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.wm.WindowManagerService;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;

public class HwShutdownThreadImpl implements IHwShutdownThread {
    private static final boolean DEBUG;
    private static final int MAX_SHUTDOWN_ANIM_WAIT_MSEC = 15000;
    private static final int PHONE_STATE_POLL_SLEEP_MSEC = 500;
    private static final int SHUTDOWN_ANIMATION_WAIT_TIME = 2000;
    private static final String TAG = "HwShutdownThread";
    private static String mShutdown_path1;
    private static String mShutdown_path2;
    private static String mShutdown_path3;
    private static AlertDialog sRootConfirmDialog;
    private final String SHUTDOWN_ANIMATION_STATUS_PROPERTY;
    private boolean isHaveShutdownAnimation;
    private boolean mHwShutDownAnimationStart;

    /* renamed from: com.android.server.power.HwShutdownThreadImpl.1 */
    static class AnonymousClass1 implements OnClickListener {
        final /* synthetic */ Context val$context;

        AnonymousClass1(Context val$context) {
            this.val$context = val$context;
        }

        public void onClick(DialogInterface dialog, int which) {
            ShutdownThread.beginShutdownSequence(this.val$context);
        }
    }

    public HwShutdownThreadImpl() {
        this.SHUTDOWN_ANIMATION_STATUS_PROPERTY = "sys.shutdown.animationstate";
        this.isHaveShutdownAnimation = DEBUG;
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : DEBUG : true;
        DEBUG = isLoggable;
        mShutdown_path1 = "/data/cust/media/shutdownanimation.zip";
        mShutdown_path2 = "/data/local/shutdownanimation.zip";
        mShutdown_path3 = "/system/media/shutdownanimation.zip";
    }

    public boolean isDoShutdownAnimation() {
        boolean doseShutDownAnimationExist = DEBUG;
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
        if (!doseShutDownAnimationExist) {
            return DEBUG;
        }
        try {
            ((WindowManagerService) ServiceManager.getService("window")).freezeOrThawRotation(0);
        } catch (Exception e2) {
        }
        this.isHaveShutdownAnimation = true;
        try {
            Runtime.getRuntime().exec("/system/bin/shutdownanimation");
        } catch (IOException e3) {
            Log.e(TAG, "run shutdown animation failed", e3);
        }
        this.mHwShutDownAnimationStart = true;
        return true;
    }

    private static boolean isIccidCustZipExist() {
        String iccid = SystemProperties.get("persist.sys.iccid", PPPOEStateMachine.PHASE_DEAD);
        if (!PPPOEStateMachine.PHASE_DEAD.equals(iccid)) {
            try {
                if (HwCfgFilePolicy.getCfgFile("media/shutdownanimation_" + iccid + ".zip", 0) != null) {
                    return true;
                }
            } catch (NoClassDefFoundError e) {
                Log.d(TAG, "HwCfgFilePolicy Iccid NoClassDefFoundError");
            }
        }
        return DEBUG;
    }

    private static boolean isMccmncCustZipExist() {
        String mccmnc = SystemProperties.get("persist.sys.mccmnc", PPPOEStateMachine.PHASE_DEAD);
        if (!PPPOEStateMachine.PHASE_DEAD.equals(mccmnc)) {
            try {
                if (HwCfgFilePolicy.getCfgFile("media/shutdownanimation_" + mccmnc + ".zip", 0) != null) {
                    return true;
                }
            } catch (NoClassDefFoundError e) {
                Log.d(TAG, "HwCfgFilePolicy Mccmnc NoClassDefFoundError");
            }
        }
        return DEBUG;
    }

    public void waitShutdownAnimation() {
        if (this.isHaveShutdownAnimation) {
            try {
                Thread.sleep(TableJankEvent.recMAXCOUNT);
            } catch (InterruptedException e) {
                Log.e(TAG, "shutdown animation thread sleep 2s failed", e);
            }
        }
    }

    public boolean needRebootDialog(String rebootReason, Context context) {
        if (rebootReason == null || !rebootReason.equals("huawei_reboot")) {
            return DEBUG;
        }
        rebootDialog(context);
        return true;
    }

    public boolean needRebootProgressDialog(boolean reboot, Context context) {
        if (!reboot || HwPolicyFactory.isHwGlobalActionsShowing()) {
            return DEBUG;
        }
        rebootProgressDialog(context);
        return true;
    }

    private static synchronized void rebootDialog(Context context) {
        synchronized (HwShutdownThreadImpl.class) {
            CloseDialogReceiver closer = new CloseDialogReceiver(context);
            if (sRootConfirmDialog != null) {
                sRootConfirmDialog.dismiss();
            }
            sRootConfirmDialog = new Builder(context, 33947691).setTitle(33685517).setMessage(33685518).setIcon(17301543).setPositiveButton(17039379, new AnonymousClass1(context)).setNegativeButton(17039369, null).create();
            closer.dialog = sRootConfirmDialog;
            sRootConfirmDialog.setOnDismissListener(closer);
            sRootConfirmDialog.getWindow().setType(2009);
            sRootConfirmDialog.show();
        }
    }

    private static void rebootProgressDialog(Context context) {
        ProgressDialog pd = new ProgressDialog(context, 33947691);
        pd.setTitle(context.getText(33685515));
        pd.setMessage(context.getText(33685557));
        pd.setIndeterminate(true);
        pd.setCancelable(DEBUG);
        pd.getWindow().setType(2009);
        pd.show();
    }

    public void resetValues() {
        this.mHwShutDownAnimationStart = DEBUG;
    }

    public void waitShutdownAnimationComplete(Context context, long shutDownBegin) {
        boolean shutdownAnimationCheckNeed = true;
        if (this.mHwShutDownAnimationStart && shutDownBegin > 0) {
            if (TextUtils.isEmpty(SystemProperties.get("persist.sys.mccmnc", AppHibernateCst.INVALID_PKG)) && TextUtils.isEmpty(SystemProperties.get("persist.sys.iccid", AppHibernateCst.INVALID_PKG))) {
                shutdownAnimationCheckNeed = DEBUG;
            }
            if (!shutdownAnimationCheckNeed) {
                return;
            }
            if (isMccmncCustZipExist() || isIccidCustZipExist()) {
                long endTime = shutDownBegin + 15000;
                long delay = endTime - SystemClock.elapsedRealtime();
                boolean shutDownAnimationFinish = "true".equals(SystemProperties.get("sys.shutdown.animationstate", "false"));
                Log.i(TAG, "ShutDown Animation Status delay:" + delay + ", shutDownAnimationFinish:" + shutDownAnimationFinish + ", shutDownBegin:" + shutDownBegin + ", endTime:" + endTime);
                while (delay > 0 && !shutDownAnimationFinish) {
                    SystemClock.sleep(500);
                    delay = endTime - SystemClock.elapsedRealtime();
                    shutDownAnimationFinish = "true".equals(SystemProperties.get("sys.shutdown.animationstate", "false"));
                }
                SystemProperties.set("sys.shutdown.animationstate", "false");
            }
        }
    }
}
