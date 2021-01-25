package com.huawei.server.security.ccmode;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.app.admin.SecurityLogEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwpartsecurityservices.BuildConfig;
import com.huawei.server.security.core.IHwSecurityPlugin;
import com.huawei.util.LogEx;
import com.huawei.utils.HwPartResourceUtils;
import huawei.android.security.IHwCCModePlugin;
import java.util.HashMap;
import java.util.Map;

public class HwCCModePlugin extends IHwCCModePlugin.Stub implements IHwSecurityPlugin {
    public static final Object BINDLOCK = new Object();
    private static final String CC_MODE_DISABLE = "disable";
    private static final String CC_MODE_ENABLE = "enable";
    public static final int CLOSE_CCMODE_SUCCESS = 102;
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.ccmode.HwCCModePlugin.AnonymousClass2 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            if (HwCCModePlugin.HWDBG) {
                Log.d(HwCCModePlugin.TAG, "createPlugin");
            }
            return new HwCCModePlugin(context);
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return HwCCModePlugin.USE_CCMODE;
        }
    };
    private static final String DEFAULT_BUTTON = "Enter recovery mode";
    private static final String DEFAULT_MESSAGE = "System error. Please enter recovery mode and restart your device. If the problem persists, please restore your device to factory settings.";
    private static final Map<String, String> DEFAULT_STRINGS = new HashMap<String, String>(3) {
        /* class com.huawei.server.security.ccmode.HwCCModePlugin.AnonymousClass1 */

        {
            put(HwCCModePlugin.RESOURCER_ID_TITLE, HwCCModePlugin.DEFAULT_TITLE);
            put(HwCCModePlugin.RESOURCER_ID_MESSAGE, HwCCModePlugin.DEFAULT_MESSAGE);
            put(HwCCModePlugin.RESOURCER_ID_BUTTON, HwCCModePlugin.DEFAULT_BUTTON);
        }
    };
    private static final String DEFAULT_TITLE = "Note";
    private static final String ENCRYPTED_TAG = "encrypted";
    private static final int EVENT_SELFTEST_RESULT = 0;
    private static final boolean HWDBG = (LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 4)));
    private static final String INTERVAL = "interval";
    private static final int INTERVAL_VALUE = 1;
    private static final int MSG_SELFTEST_FAILED = 0;
    private static final String NOWAIT = "nowait";
    private static final int NOWAIT_VALUE = 1;
    public static final int NO_SCREEN_LOCK_ERROR = 103;
    public static final int OPEN_CCMODE_SUCCESS = 101;
    public static final int PLATFORM_NOT_SUPPORTED = 107;
    public static final int PREPARE_ENABLECCMODE_SUCCESS = 106;
    private static final String PROPERTIES_CC_MODE = "ro.boot.sys.ccmode";
    private static final String RESOURCER_ID_BUTTON = "button_selftest_fail_recovery";
    private static final String RESOURCER_ID_MESSAGE = "popupwindow_selftest_fail";
    private static final String RESOURCER_ID_TITLE = "popuptitle_selftest_fail_info";
    private static final String RO_CRYPTO_STATE = "ro.crypto.state";
    private static final String RO_CRYPTO_STATE_DEFAULT = "0";
    public static final int SCREEN_LOOK_MANAGER_NULL_ERROR = 105;
    private static final int SELFTEST_ERROR = 0;
    private static final int SELFTEST_OK = 1;
    private static final String TAG = "HwCCMD";
    public static final int UNENCRYPT_STORAGE_ERROR = 104;
    private static final String USE_CCMODE = "com.huawei.permission.USE_CCMODE";
    private Context mContext;
    private Handler mHandler = new Handler() {
        /* class com.huawei.server.security.ccmode.HwCCModePlugin.AnonymousClass3 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                HwCCModePlugin.this.waitForConfirm();
            }
        }
    };

    public HwCCModePlugin(Context context) {
        this.mContext = context;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.server.security.ccmode.HwCCModePlugin */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        return this;
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        if (isCCModeOn()) {
            new Thread() {
                /* class com.huawei.server.security.ccmode.HwCCModePlugin.AnonymousClass4 */

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    boolean testOk;
                    long beginTime = System.currentTimeMillis();
                    SecurityLogEx.writeEvent(210038, new Object[0]);
                    try {
                        testOk = CCModeFuncInternal.native_dxCryptoSelftest() && CCModeFuncInternal.native_kernelCryptoSelftest() && CCModeFuncInternal.native_boringSslSelftest();
                        SecurityLogEx.writeEvent(210031, new Object[]{Integer.valueOf(testOk ? 1 : 0)});
                    } catch (UnsatisfiedLinkError e) {
                        testOk = true;
                        Log.e(HwCCModePlugin.TAG, "self test jni error");
                    }
                    long costTime = System.currentTimeMillis() - beginTime;
                    if (HwCCModePlugin.HWDBG) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("CC mode self test ");
                        sb.append(testOk ? "OK" : "failed");
                        sb.append("!!!");
                        Log.w(HwCCModePlugin.TAG, sb.toString());
                        Log.w(HwCCModePlugin.TAG, "selfTest cost:" + costTime);
                    }
                    if (!testOk) {
                        HwCCModePlugin.this.mHandler.sendEmptyMessage(0);
                    }
                }
            }.start();
        }
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
    }

    public int openCCMode() throws RemoteException {
        checkPermission(USE_CCMODE);
        if (HWDBG) {
            Log.i(TAG, "Open CCMode !");
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
            Log.d(TAG, "reboot system");
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
            Log.i(TAG, "Close CCMode !");
        }
        if (!isCCModeOn()) {
            return 102;
        }
        setCCMode(false);
        return 102;
    }

    private boolean isCCModeOn() {
        return CC_MODE_ENABLE.equalsIgnoreCase(SystemPropertiesEx.get(PROPERTIES_CC_MODE, CC_MODE_DISABLE));
    }

    private void setCCMode(boolean isOn) {
        SystemPropertiesEx.set(PROPERTIES_CC_MODE, BuildConfig.FLAVOR + isOn);
    }

    private int prepareEnableCCMode() {
        KeyguardManager screenLockManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (screenLockManager == null) {
            Log.e(TAG, "screenLockManager is null !");
            return SCREEN_LOOK_MANAGER_NULL_ERROR;
        } else if (!ENCRYPTED_TAG.equals(SystemPropertiesEx.get(RO_CRYPTO_STATE, RO_CRYPTO_STATE_DEFAULT))) {
            return UNENCRYPT_STORAGE_ERROR;
        } else {
            if (!screenLockManager.isKeyguardSecure()) {
                return NO_SCREEN_LOCK_ERROR;
            }
            return PREPARE_ENABLECCMODE_SUCCESS;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void waitForConfirm() {
        if (HWDBG) {
            Log.d(TAG, "wait for user to confirm");
        }
        AlertDialog dialog = new AlertDialog.Builder(this.mContext).setTitle(getStringRes(RESOURCER_ID_TITLE)).setMessage(getStringRes(RESOURCER_ID_MESSAGE)).setNeutralButton(getStringRes(RESOURCER_ID_BUTTON), new DialogInterface.OnClickListener() {
            /* class com.huawei.server.security.ccmode.HwCCModePlugin.AnonymousClass5 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (HwCCModePlugin.HWDBG) {
                    Log.d(HwCCModePlugin.TAG, "Entering recovery mode");
                }
                ((PowerManager) HwCCModePlugin.this.mContext.getSystemService("power")).reboot("recovery");
            }
        }).setCancelable(false).create();
        dialog.getWindow().setType(2003);
        dialog.show();
    }

    private void checkPermission(String permission) {
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    private String getStringRes(String resName) {
        if (!DEFAULT_STRINGS.containsKey(resName)) {
            return BuildConfig.FLAVOR;
        }
        int resId = HwPartResourceUtils.getResourceId(resName);
        if (resId == -1) {
            return DEFAULT_STRINGS.get(resName);
        }
        return this.mContext.getString(resId);
    }
}
