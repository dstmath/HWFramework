package com.android.server;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hdm.HwDeviceManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.ncdft.HwNcDftConnManager;
import com.huawei.server.bluetooth.HwBluetoothPencilManager;

public final class HwBluetoothManagerServiceEx implements IHwBluetoothManagerServiceEx {
    private static final int AIRPLANE_MODE_CHANGE_DELAY_MS = 1500;
    private static final int BT_CHR_STAT_EVENT = 1002;
    private static final short BT_EXCEPTION_INFO = 0;
    public static final short BT_SERVICE_ERROR = 1;
    public static final int BT_SWITCH_OFF = 2;
    public static final int BT_SWITCH_ON = 1;
    public static final short BT_TIMEOUT_BIND_ERROR = 20;
    public static final String DEFAULT_PACKAGE_NAME = "NULL";
    private static final int DOMAIN_BT = 3;
    private static final int ENABLE_MESSAGE_REPEAT_MS = 1500;
    private static final int MESSAGE_AIRPLANE_MODE_CHANGE = 600;
    private static int MESSAGE_DISABLE = 2;
    private static int MESSAGE_ENABLE = 1;
    private static final String TAG = "HwBluetoothManagerServiceEx";
    private static BluetoothParaManager mBluetoothParaManager = null;
    private volatile boolean isAirplaneModeChanging = false;
    private final Context mContext;
    private Handler mHandler;
    private HwBluetoothPencilManager mHwBluetoothPencilManager;
    private HwNcDftConnManager mHwNcDftConnManager;
    private IHwBluetoothManagerInner mInner;
    private long mLastEnableMessageTime;
    private int mLastMessage;

    public HwBluetoothManagerServiceEx(IHwBluetoothManagerInner hwBluetoothManagerInner, Context context, Handler handler) {
        this.mInner = hwBluetoothManagerInner;
        this.mContext = context;
        this.mHandler = handler;
        this.mHwNcDftConnManager = new HwNcDftConnManager(context);
        IHwBluetoothManagerInner iHwBluetoothManagerInner = this.mInner;
        if (iHwBluetoothManagerInner != null) {
            MESSAGE_ENABLE = iHwBluetoothManagerInner.getMessageEnableValueInner();
            MESSAGE_DISABLE = this.mInner.getMessageDisableValueInner();
        }
        this.mLastMessage = MESSAGE_DISABLE;
        this.mLastEnableMessageTime = SystemClock.elapsedRealtime();
        if (mBluetoothParaManager == null) {
            mBluetoothParaManager = new BluetoothParaManager(context);
        }
        this.mHwBluetoothPencilManager = new HwBluetoothPencilManager(context, this.mHandler.getLooper());
    }

    public boolean refuseEnableForMDM() {
        if (!HwDeviceManager.disallowOp(8)) {
            return false;
        }
        HwLog.d(TAG, "bluetooth has been restricted.");
        return true;
    }

    public boolean refuseDisableForMDM(boolean duringAirplaneModeChange) {
        if (!HwDeviceManager.disallowOp(51)) {
            return false;
        }
        HwLog.d(TAG, "mdm force open bluetooth, not allow close bluetooth");
        if (duringAirplaneModeChange) {
            return true;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            /* class com.android.server.HwBluetoothManagerServiceEx.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                Toast.makeText(HwBluetoothManagerServiceEx.this.mContext, HwBluetoothManagerServiceEx.this.mContext.getResources().getString(33686053), 0).show();
            }
        });
        return true;
    }

    public void setLastMessageDisable() {
        this.mLastMessage = MESSAGE_DISABLE;
    }

    public boolean shouldIgnoreExtraEnableMessage(boolean enablingInQuietMode, boolean enabledInQuietMode) {
        long now = SystemClock.elapsedRealtime();
        if (now - this.mLastEnableMessageTime < 1500 && this.mLastMessage == MESSAGE_ENABLE && enabledInQuietMode == enablingInQuietMode) {
            HwLog.d(TAG, "MESSAGE_ENABLE message repeat in short time, return");
            this.mLastEnableMessageTime = now;
            return true;
        }
        this.mLastEnableMessageTime = now;
        this.mLastMessage = MESSAGE_ENABLE;
        return false;
    }

    public boolean sendMessageIfLastChangeNotFinish() {
        HwLog.d(TAG, "receiver Airplane Mode change isAirplaneModeChanging: " + this.isAirplaneModeChanging);
        this.mHandler.removeMessages(600);
        if (!this.isAirplaneModeChanging) {
            return false;
        }
        this.mHandler.sendEmptyMessageDelayed(600, 1500);
        return true;
    }

    public void setAirplaneModeChangingFlag(boolean isAirplaneModeOn, int state, boolean preStateOn) {
        if (isAirplaneModeOn && state != 10) {
            this.isAirplaneModeChanging = true;
        } else if (!isAirplaneModeOn && preStateOn && state != 12) {
            this.isAirplaneModeChanging = true;
        }
    }

    public void disableAirplaneModeChangingFlag(int newState) {
        if (!this.isAirplaneModeChanging) {
            return;
        }
        if (newState == 12 || newState == 10) {
            HwLog.d(TAG, "Entering STATE_OFF but mEnabled is true; restarting.");
            this.isAirplaneModeChanging = false;
        }
    }

    public void handleExternalMessageOfHandler(int msg) {
        if (msg == 600) {
            HwLog.d(TAG, "MESSAGE_AIRPLANE_MODE_CHANGE");
            this.mHandler.removeMessages(600);
            if (this.isAirplaneModeChanging) {
                this.mHandler.sendEmptyMessageDelayed(600, 1500);
                return;
            }
            IHwBluetoothManagerInner iHwBluetoothManagerInner = this.mInner;
            if (iHwBluetoothManagerInner != null) {
                iHwBluetoothManagerInner.changeBluetoothStateFromAirplaneModeInner();
            }
        }
    }

    public boolean needAllowByUser(int uid) {
        if (uid < 10000) {
            HwLog.d(TAG, "needAllowByUser got reserved system uid");
            return false;
        }
        String packageName = getPackageNameFromUid(uid);
        if (packageName == null) {
            HwLog.d(TAG, "needAllowByUser got null packageName");
            return false;
        } else if (packageName.contains("cts") || packageName.contains("com.google")) {
            HwLog.d(TAG, "needAllowByUser got special packageName");
            return false;
        } else if (isSpecialAllowed(packageName)) {
            HwLog.d(TAG, "needAllowByUser got special allowed package");
            return false;
        } else {
            PackageManager pm = this.mContext.getPackageManager();
            if (pm == null) {
                HwLog.d(TAG, "needAllowByUser got null packageManager");
                return false;
            } else if (pm.checkSignatures(packageName, "android") < 0) {
                return true;
            } else {
                HwLog.d(TAG, "needAllowByUser got system signature app");
                return false;
            }
        }
    }

    private String getPackageNameFromUid(int uid) {
        if (uid <= 0) {
            return null;
        }
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            HwLog.d(TAG, "getPackageNameFromUid got null packageManager");
            return null;
        }
        String[] packages = pm.getPackagesForUid(uid);
        if (packages == null || packages.length <= 0 || packages[0] == null) {
            return null;
        }
        String packageName = packages[0];
        HwLog.d(TAG, "getPackageNameFromUid apkInfo " + packageName);
        return packageName;
    }

    private void reportBtServiceChrToDft(int code, int subcode, String apkName) {
        HwLog.e(TAG, " reportBtServiceChrToDft enter");
        Bundle data = new Bundle();
        data.putString("btErrorCode", String.valueOf(code));
        data.putString("btSubErrorCode", String.valueOf(subcode));
        data.putString("apkName", apkName);
        HwNcDftConnManager hwNcDftConnManager = this.mHwNcDftConnManager;
        if (hwNcDftConnManager != null) {
            hwNcDftConnManager.reportToDft(3, 0, data);
        } else {
            HwLog.e(TAG, "reportBtServiceChrToDft,mClient is null");
        }
    }

    public void reportBtChrBindTimeout() {
        reportBtServiceChrToDft(1, 20, DEFAULT_PACKAGE_NAME);
    }

    public void reportBtChrThirdPartApkCalling(boolean isEnable, String packageName) {
        if (this.mHwNcDftConnManager != null) {
            HwLog.e(TAG, "reportBtServiceChrToDft isOpen:" + isEnable);
            int action = isEnable ? 1 : 2;
            Bundle data = new Bundle();
            data.putString("ApkPkgName", packageName);
            data.putInt("ApkAction", action);
            this.mHwNcDftConnManager.reportToDft(3, 1002, data);
        }
    }

    private boolean isSpecialAllowed(String packageName) {
        String allowedPackages = Settings.System.getString(this.mContext.getContentResolver(), "bt_allowed_pkgs");
        if (TextUtils.isEmpty(allowedPackages)) {
            return false;
        }
        for (String pkg : allowedPackages.split(AwarenessInnerConstants.SEMI_COLON_KEY)) {
            if (packageName.contains(pkg)) {
                return true;
            }
        }
        return false;
    }
}
