package com.android.server.security.ukey;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import com.android.internal.content.PackageMonitor;
import com.android.server.hidata.wavemapping.chr.BuildBenefitStatisticsChrInfo;
import com.android.server.pfw.autostartup.comm.XmlConst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.tsmagent.utils.HwLog;
import com.android.server.security.ukey.UKeyApplicationScanner;
import com.android.server.security.ukey.jni.UKeyJNI;
import com.huawei.tips.ITipsInterface;
import huawei.android.security.IUKeyManager;
import java.io.File;
import org.json.JSONException;
import org.json.JSONObject;

public class UKeyManagerService extends IUKeyManager.Stub implements IHwSecurityPlugin {
    public static final Object BINDLOCK = new Object();
    private static final String BIND_ACTION = "com.huawei.decision.service.DecisionReceiverService";
    /* access modifiers changed from: private */
    public static final int CH_ALG_VERSION = SystemProperties.getInt(CH_ALG_VERSION_PRO, 0);
    private static final String CH_ALG_VERSION_PRO = "ro.config.hw_ch_alg";
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            Log.d(UKeyManagerService.TAG, "create UKeyManagerService");
            return new UKeyManagerService(context);
        }

        public String getPluginPermission() {
            return null;
        }
    };
    private static final int FAILED = -1;
    private static final boolean IS_UKEY_SWITCH_ON = SystemProperties.getBoolean(UKEY_SWITCH_PRO, false);
    private static final String SERVER_PAKAGE_NAME = "com.huawei.tips";
    private static final String TAG = "UKeyManagerService";
    private static final String UKEY_DIR = "ukey";
    private static final String UKEY_FEATURE_ID = "SF-10084572_f001";
    private static final String UKEY_MANAGER_PERMISSION = "com.huawei.ukey.permission.UKEY_MANAGER";
    private static final String UKEY_RECOMMEND_TYPE = "2";
    private static final String UKEY_SWITCH_PRO = "ro.config.hw_ukey_on";
    private static final String UKEY_SWITCH_STATUS = "UKEY_SWITCH_STATUS";
    private static final int UKEY_UNSUPPORTED = 0;
    private static final int UKEY_VERSION = SystemProperties.getInt(UKEY_VERSION_PRO, 1);
    private static final int UKEY_VERSION_ONE = 1;
    private static final String UKEY_VERSION_PRO = "ro.config.hw_ukey_version";
    private static final int UKEY_VERSION_TWO = 2;
    /* access modifiers changed from: private */
    public ServiceConnection mActionCommonConnection;
    /* access modifiers changed from: private */
    public Context mContext;
    private final MyPackageMonitor mMyPackageMonitor = new MyPackageMonitor();
    private AppProcessObserver mProcessObserver = new AppProcessObserver();
    /* access modifiers changed from: private */
    public ITipsInterface mTipsService;
    private UKeyApplicationScanner uKeyApplicationScanner = null;

    private final class AppProcessObserver extends IProcessObserver.Stub {
        /* access modifiers changed from: private */
        public ServiceConnection mConnection;

        private AppProcessObserver() {
            this.mConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Slog.i(UKeyManagerService.TAG, "service connect.");
                    ITipsInterface unused = UKeyManagerService.this.mTipsService = ITipsInterface.Stub.asInterface(service);
                    AppProcessObserver.this.sendTipsAndStopService();
                }

                public void onServiceDisconnected(ComponentName name) {
                    Slog.i(UKeyManagerService.TAG, "service disconnect.");
                }
            };
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            try {
                String packageName = UKeyManagerService.this.mContext.getPackageManager().getNameForUid(uid);
                if (packageName != null && UserHandle.getUserId(uid) == 0 && "com.eg.android.AlipayGphone".equalsIgnoreCase(packageName) && UKeyManagerService.this.isSwitchFeatureOn() == 2 && UKeyManagerService.CH_ALG_VERSION >= 2) {
                    try {
                        SharedPreferences shared = UKeyManagerService.this.mContext.getSharedPreferences(new File(Environment.getUserSystemDirectory(0), "ukey/ukey_recommend.xml"), 0);
                        if (shared.getBoolean("Alipay", true)) {
                            shared.edit().putBoolean("Alipay", false).commit();
                            Slog.i(UKeyManagerService.TAG, "欢迎使用手机盾！");
                            startTipsService();
                        }
                    } catch (Exception e) {
                        Slog.i(UKeyManagerService.TAG, "getPinnedSharedPrefs failed " + e.getMessage());
                    }
                }
            } catch (Exception e2) {
                Slog.i(UKeyManagerService.TAG, "getPackageManager failed " + e2.getMessage());
            }
        }

        private void startTipsService() {
            if (UKeyManagerService.this.mContext == null) {
                Slog.i(UKeyManagerService.TAG, "context  is null!");
            } else {
                new Thread(new Runnable() {
                    public void run() {
                        Intent intent = new Intent();
                        intent.setAction(UKeyManagerService.BIND_ACTION);
                        intent.setPackage(UKeyManagerService.SERVER_PAKAGE_NAME);
                        ServiceConnection unused = UKeyManagerService.this.mActionCommonConnection = AppProcessObserver.this.mConnection;
                        try {
                            UKeyManagerService.this.mContext.bindService(intent, UKeyManagerService.this.mActionCommonConnection, 1);
                        } catch (Exception e) {
                            Slog.i(UKeyManagerService.TAG, "get Tips service connect fail!");
                        }
                    }
                }).start();
            }
        }

        /* access modifiers changed from: private */
        public void _sendTipsAndStopService() {
            new Thread(new Runnable() {
                public void run() {
                    if (UKeyManagerService.this.mTipsService != null) {
                        try {
                            UKeyManagerService.this.mTipsService.sendTips(null, "2", UKeyManagerService.UKEY_FEATURE_ID);
                        } catch (Exception e) {
                            Slog.i(UKeyManagerService.TAG, "ServiceConnection.onServiceConnected  exception:" + e.getMessage());
                        }
                    }
                    AppProcessObserver.this.unBindService();
                }
            }).start();
        }

        /* access modifiers changed from: private */
        public void sendTipsAndStopService() {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    AppProcessObserver.this._sendTipsAndStopService();
                }
            }, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        }

        /* access modifiers changed from: private */
        public void unBindService() {
            Slog.i(UKeyManagerService.TAG, "unBindService");
            try {
                if (UKeyManagerService.this.mContext != null && UKeyManagerService.this.mActionCommonConnection != null) {
                    UKeyManagerService.this.mContext.unbindService(UKeyManagerService.this.mActionCommonConnection);
                    ServiceConnection unused = UKeyManagerService.this.mActionCommonConnection = null;
                    ITipsInterface unused2 = UKeyManagerService.this.mTipsService = null;
                }
            } catch (Exception e) {
                Slog.i(UKeyManagerService.TAG, "release exception");
            }
        }

        public void onProcessDied(int pid, int uid) {
        }
    }

    private final class MyPackageMonitor extends PackageMonitor {
        private MyPackageMonitor() {
        }

        public void onPackageAdded(String packageName, int uid) {
            Slog.i(UKeyManagerService.TAG, "app is added. The packageName is : " + packageName + "uid = " + uid);
            if (UserHandle.getUserId(uid) == 0 && UKeyJNI.isUKeySwitchDisabled(packageName) != 1) {
                UKeyManagerService.this.setUKeySwitchDisabled(packageName, false);
            }
        }
    }

    public UKeyManagerService(Context context) {
        this.mContext = context;
        this.uKeyApplicationScanner = new UKeyApplicationScanner(this.mContext);
    }

    public void onStart() {
        this.uKeyApplicationScanner = new UKeyApplicationScanner(this.mContext);
        this.uKeyApplicationScanner.loadUKeyApkWhitelist();
        this.mMyPackageMonitor.register(this.mContext, null, UserHandle.OWNER, false);
        if (isSwitchFeatureOn() >= UKEY_VERSION) {
            registerObserver();
        }
    }

    public void onStop() {
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.security.ukey.UKeyManagerService, android.os.IBinder] */
    public IBinder asBinder() {
        return this;
    }

    private void registerObserver() {
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (Exception e) {
            Slog.i(TAG, "AppProcessObserver register process observer failed");
        }
    }

    public int isSwitchFeatureOn() {
        if (UserHandle.getUserId(Binder.getCallingUid()) == 0 && IS_UKEY_SWITCH_ON) {
            return UKEY_VERSION;
        }
        return 0;
    }

    public int isUKeySwitchDisabled(String packageName) {
        if (isSwitchFeatureOn() < 2 || !isValidPackageName(packageName)) {
            return -1;
        }
        if (!isWalletCalling()) {
            return UKeyJNI.isUKeySwitchDisabled(this.uKeyApplicationScanner.getRealUKeyPkgName(packageName));
        }
        String[] element = packageName.split("[|;]");
        if (element.length != 2) {
            return -1;
        }
        Slog.d(TAG, "wallet calling! get " + element[0] + " " + element[1]);
        return UKeyJNI.isUKeySwitchDisabled(element[0]);
    }

    public int setUKeySwitchDisabled(String packageName, boolean isDisabled) {
        String ukeyId;
        JSONObject obj = new JSONObject();
        try {
            obj.put("PKG", packageName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isDisabled) {
            try {
                obj.put(BuildBenefitStatisticsChrInfo.E909009052_TOTALSWITCH_INT, "off");
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            Flog.bdReport(this.mContext, 561, obj.toString());
        } else {
            try {
                obj.put(BuildBenefitStatisticsChrInfo.E909009052_TOTALSWITCH_INT, XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
            } catch (JSONException e3) {
                e3.printStackTrace();
            }
            Flog.bdReport(this.mContext, 562, obj.toString());
        }
        this.mContext.enforceCallingOrSelfPermission(UKEY_MANAGER_PERMISSION, "does not have ukey manager permission!");
        if (isSwitchFeatureOn() < 2) {
            return -1;
        }
        boolean isUkeySwitchOn = false;
        if (isWalletCalling()) {
            String[] element = packageName.split("[|;]");
            if (element.length != 2) {
                return -1;
            }
            Slog.d(TAG, "wallet calling!  set " + element[0] + " " + element[1] + " " + isDisabled);
            return UKeyJNI.setUKeySwitchDisabled(element[0], element[1], isDisabled);
        }
        String realPkgName = this.uKeyApplicationScanner.getRealUKeyPkgName(packageName);
        UKeyApplicationScanner.UKeyApkInfo uKeyApkInfo = this.uKeyApplicationScanner.getUKeyApkInfo(realPkgName);
        if (uKeyApkInfo != null) {
            return UKeyJNI.setUKeySwitchDisabled(realPkgName, uKeyApkInfo.mUKeyId, isDisabled);
        }
        if (!isDisabled) {
            Log.d(TAG, "setUKeySwitchDisabled: ON");
            if (UKeyJNI.isUKeySwitchDisabled(packageName) == 1) {
                isUkeySwitchOn = true;
            }
            if (!isUkeySwitchOn) {
                try {
                    Log.d(TAG, "current is off to on, [ packageName:" + packageName + ", ukeyId:" + ukeyId + " ]");
                    return UKeyJNI.setUKeySwitchDisabled(packageName, ukeyId, isDisabled);
                } catch (Exception e4) {
                    HwLog.e(e4.getMessage());
                }
            }
        }
        return -1;
    }

    public Bundle getUKeyApkInfo(String packageName) {
        this.mContext.enforceCallingOrSelfPermission(UKEY_MANAGER_PERMISSION, "does not have ukey manager permission!");
        if (isSwitchFeatureOn() < 2 || !isValidPackageName(packageName)) {
            return null;
        }
        Bundle bundle = this.uKeyApplicationScanner.getUKeyApkInfoData(packageName);
        if (bundle != null) {
            bundle.putInt(UKEY_SWITCH_STATUS, isUKeySwitchDisabled(packageName));
        }
        return bundle;
    }

    private boolean isValidPackageName(String packageName) {
        boolean z = false;
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        if (isWalletCalling()) {
            if (packageName.contains("|") || packageName.contains(":")) {
                z = true;
            }
            return z;
        } else if (this.uKeyApplicationScanner.isWhiteListedUKeyApp(packageName)) {
            return true;
        } else {
            Slog.i(TAG, "The app is not ukey application, packageName : " + packageName);
            return false;
        }
    }

    private boolean isWalletCalling() {
        try {
            if (this.mContext != null) {
                String s = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
                if (s != null) {
                    boolean ret = s.equals("com.huawei.wallet");
                    Slog.d(TAG, "calling in " + ret + " " + s);
                    return ret;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
