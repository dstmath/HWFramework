package com.android.server.security.ccmode;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import com.huawei.msdp.devicestatus.DeviceStatusConstant;
import huawei.android.security.IHwCCModePlugin.Stub;

public class HwCCModePlugin extends Stub implements IHwSecurityPlugin {
    public static final Object BINDLOCK = new Object();
    public static final int CLOSE_CCMODE_SUCCESS = 102;
    public static final Creator CREATOR = new Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            if (HwCCModePlugin.HWDBG) {
                Slog.d(HwCCModePlugin.TAG, "createPlugin");
            }
            return new HwCCModePlugin(context);
        }

        public String getPluginPermission() {
            return HwCCModePlugin.USE_CCMODE;
        }
    };
    private static final String ENCRYPTED_TAG = "encrypted";
    private static final int EVENT_SELFTEST_RESULT = 0;
    private static final boolean HWDBG;
    private static final String INTERVAL = "interval";
    private static final int INTERVAL_VALUE = 1;
    private static final int MSG_SELFTEST_FAILED = 0;
    private static final String NOWAIT = "nowait";
    private static final int NOWAIT_VALUE = 1;
    public static final int NO_SCREEN_LOCK_ERROR = 103;
    public static final int OPEN_CCMODE_SUCCESS = 101;
    public static final int PLATFORM_NOT_SUPPORTED = 107;
    public static final int PREPARE_ENABLECCMODE_SUCCESS = 106;
    private static final String PROPERTIES_CC_MODE = "persist.sys.cc_mode";
    private static final String RO_CRYPTO_STATE = "ro.crypto.state";
    private static final String RO_CRYPTO_STATE_DEFAULT = "0";
    public static final int SCREEN_LOOK_MANAGER_NULL_ERROR = 105;
    private static final int SELFTEST_ERROR = 1;
    private static final int SELFTEST_OK = 0;
    private static final String TAG = "HwCCMD";
    public static final int UNENCRYPT_STORAGE_ERROR = 104;
    private static final String USE_CCMODE = "com.huawei.permission.USE_CCMODE";
    private Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                HwCCModePlugin.this.waitForConfirm();
            }
        }
    };

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWDBG = isLoggable;
    }

    public HwCCModePlugin(Context context) {
        this.mContext = context;
    }

    public IBinder asBinder() {
        return this;
    }

    public void onStart() {
        if (isCCModeOn()) {
            new Thread() {
                public void run() {
                    boolean testOk;
                    long beginTime = System.currentTimeMillis();
                    try {
                        if (CCModeFuncInternal.native_dxCryptoSelftest() && CCModeFuncInternal.native_kernelCryptoSelftest()) {
                            testOk = CCModeFuncInternal.native_boringSslSelftest();
                        } else {
                            testOk = false;
                        }
                    } catch (UnsatisfiedLinkError e) {
                        testOk = true;
                        Slog.e(HwCCModePlugin.TAG, "self test jni error");
                    }
                    long costTime = System.currentTimeMillis() - beginTime;
                    if (HwCCModePlugin.HWDBG) {
                        Slog.w(HwCCModePlugin.TAG, "CC mode self test " + (testOk ? "OK" : "failed") + "!!!");
                        Slog.w(HwCCModePlugin.TAG, "selfTest cost:" + costTime);
                    }
                    if (!testOk) {
                        HwCCModePlugin.this.mHandler.sendEmptyMessage(0);
                    }
                }
            }.start();
        }
    }

    public void onStop() {
    }

    public int openCCMode() throws RemoteException {
        checkPermission(USE_CCMODE);
        if (HWDBG) {
            Slog.i(TAG, "Open CCMode !");
        }
        if (isCCModeOn()) {
            return 101;
        }
        int prepareRst = prepareEnableCCMode();
        if (106 != prepareRst) {
            return prepareRst;
        }
        setCCMode(true);
        if (HWDBG) {
            Slog.d(TAG, "reboot system");
        }
        Intent intent = new Intent("android.intent.action.REBOOT");
        intent.putExtra(NOWAIT, 1);
        intent.putExtra(INTERVAL, 1);
        this.mContext.sendBroadcast(intent);
        return 101;
    }

    public int closeCCMode() throws RemoteException {
        checkPermission(USE_CCMODE);
        if (HWDBG) {
            Slog.i(TAG, "Close CCMode !");
        }
        if (!isCCModeOn()) {
            return 102;
        }
        setCCMode(false);
        return 102;
    }

    private boolean isCCModeOn() {
        return SystemProperties.getBoolean(PROPERTIES_CC_MODE, false);
    }

    private void setCCMode(boolean isOn) {
        SystemProperties.set(PROPERTIES_CC_MODE, "" + isOn);
    }

    private int prepareEnableCCMode() {
        KeyguardManager screenLockManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (screenLockManager == null) {
            Slog.e(TAG, "screenLockManager is null !");
            return 105;
        } else if (!ENCRYPTED_TAG.equals(SystemProperties.get(RO_CRYPTO_STATE, "0"))) {
            return 104;
        } else {
            if (screenLockManager.isKeyguardSecure()) {
                return 106;
            }
            return 103;
        }
    }

    private void waitForConfirm() {
        if (HWDBG) {
            Slog.d(TAG, "wait for user to confirm");
        }
        AlertDialog dialog = new Builder(this.mContext).setTitle(this.mContext.getString(33686054)).setMessage(this.mContext.getString(33686057)).setNeutralButton(this.mContext.getString(33685531), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (HwCCModePlugin.HWDBG) {
                    Slog.d(HwCCModePlugin.TAG, "Entering recovery mode");
                }
                ((PowerManager) HwCCModePlugin.this.mContext.getSystemService("power")).reboot("recovery");
            }
        }).setCancelable(false).create();
        dialog.getWindow().setType(DeviceStatusConstant.MSDP_DEVICE_STATUS_MOVEMENT);
        dialog.show();
    }

    private void checkPermission(String permission) {
        this.mContext.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }
}
