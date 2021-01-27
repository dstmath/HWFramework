package com.android.server.pm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareConstant;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.huawei.android.biometric.FingerprintServiceEx;
import java.util.Map;

public class HwForceDarkModeConfig {
    private static final int APP_FORCE_DARK_USER_SET_FLAG = 128;
    public static final int FORCE_DARK_IN_3RD_BLACK_LIST = 3;
    public static final String FORCE_DARK_PKGNAME_BLACK_LIST_SUFFIX = "@black_list";
    private static final boolean IS_DEBUG = "on".equals(SystemProperties.get("ro.dbg.pms_log", "0"));
    private static final long REFRESH_FORCE_DARK_MODE_DELAY_TIME = 15000;
    private static final int REFRESH_FORCE_DARK_MODE_MESSAGE = 119;
    private static final int REFRESH_FORCE_DARK_MODE_TIMES = 10;
    private static final String TAG = "HwForceDarkModeConfig";
    private static HwForceDarkModeConfig instance;
    private AppTypeRecoManager mAppTypeRecoManager = AppTypeRecoManager.getInstance();
    private ContentObserver mContentObserver = new ContentObserver(this.mHandler) {
        /* class com.android.server.pm.HwForceDarkModeConfig.AnonymousClass2 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            long currentTime = System.currentTimeMillis();
            long delayTime = HwForceDarkModeConfig.REFRESH_FORCE_DARK_MODE_DELAY_TIME;
            if (HwForceDarkModeConfig.this.mDataUpdateTime != 0) {
                delayTime = HwForceDarkModeConfig.REFRESH_FORCE_DARK_MODE_DELAY_TIME - (currentTime - HwForceDarkModeConfig.this.mDataUpdateTime);
                if (delayTime < 0) {
                    delayTime = 0;
                }
            }
            HwForceDarkModeConfig.this.sendMessageRefreshData(delayTime);
        }
    };
    private long mDataUpdateTime = 0;
    private Handler mHandler = new Handler() {
        /* class com.android.server.pm.HwForceDarkModeConfig.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HwForceDarkModeConfig.this.mDataUpdateTime = System.currentTimeMillis();
            if (HwForceDarkModeConfig.this.mAppTypeRecoManager.isReady()) {
                HwForceDarkModeConfig.this.initForceDarkModeConfig();
            } else if (HwForceDarkModeConfig.this.mRetryTime < 10) {
                HwForceDarkModeConfig.this.mRetryTime++;
                HwForceDarkModeConfig.this.mAppTypeRecoManager.init(HwForceDarkModeConfig.this.mPms.mContext);
                HwForceDarkModeConfig.this.sendMessageRefreshData(HwForceDarkModeConfig.REFRESH_FORCE_DARK_MODE_DELAY_TIME);
            }
        }
    };
    private PackageManagerService mPms = ServiceManager.getService("package");
    private ForceDarkModeReceiver mReceiver = new ForceDarkModeReceiver();
    private int mRetryTime = 0;

    private HwForceDarkModeConfig() {
    }

    public void registerAppTypeRecoReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FingerprintServiceEx.ACTION_USER_PRESENT);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
        this.mPms.mContext.registerReceiver(this.mReceiver, filter);
        this.mPms.mContext.getContentResolver().registerContentObserver(AwareConstant.Database.APPTYPE_URI, true, this.mContentObserver);
    }

    /* access modifiers changed from: private */
    public class ForceDarkModeReceiver extends BroadcastReceiver {
        private ForceDarkModeReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (HwForceDarkModeConfig.this.mReceiver != null) {
                HwForceDarkModeConfig.this.mPms.mContext.unregisterReceiver(HwForceDarkModeConfig.this.mReceiver);
            }
            HwForceDarkModeConfig.this.sendMessageRefreshData(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessageRefreshData(long delayTime) {
        if (this.mHandler.hasMessages(REFRESH_FORCE_DARK_MODE_MESSAGE)) {
            this.mHandler.removeMessages(REFRESH_FORCE_DARK_MODE_MESSAGE);
        }
        Message message = this.mHandler.obtainMessage();
        message.what = REFRESH_FORCE_DARK_MODE_MESSAGE;
        this.mHandler.sendMessageDelayed(message, delayTime);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initForceDarkModeConfig() {
        AppTypeRecoManager appTypeRecoManager;
        Slog.d(TAG, "do init");
        if (this.mPms == null || (appTypeRecoManager = this.mAppTypeRecoManager) == null) {
            Slog.e(TAG, "init failed, pms or AppTypeRecoManager is null");
            return;
        }
        appTypeRecoManager.loadInstalledAppTypeInfo();
        synchronized (this.mPms.getPackagesLock()) {
            boolean isChanged = false;
            Map<String, PackageSetting> packageSettings = this.mPms.getSettings().mPackages;
            for (String packageName : packageSettings.keySet()) {
                PackageSetting pkgSetting = packageSettings.get(packageName);
                if (!(pkgSetting == null || pkgSetting.pkg == null)) {
                    if (pkgSetting.pkg.applicationInfo != null) {
                        if (!pkgSetting.isSystem()) {
                            if (!pkgSetting.isUpdatedSystem()) {
                                boolean isUserSet = (pkgSetting.getForceDarkMode() & 128) != 0;
                                int oldMode = pkgSetting.getForceDarkMode() & -129;
                                int mode = getForceDarkModeFromAppTypeRecoManager(packageName, pkgSetting);
                                if (IS_DEBUG) {
                                    Slog.d(TAG, "initForceDarkMode packageName: " + packageName + ", isUserSet: " + isUserSet + ", oldMode: " + oldMode + ", mode: " + mode);
                                }
                                if (oldMode != mode && (mode == 2 || (mode != 2 && !isUserSet))) {
                                    isChanged = true;
                                    pkgSetting.setForceDarkMode(mode);
                                    pkgSetting.pkg.applicationInfo.forceDarkMode = mode;
                                }
                            }
                        }
                    }
                }
                Slog.d(TAG, "initForceDarkMode pkgSetting is null for packageName: " + packageName);
            }
            if (isChanged) {
                this.mPms.getSettings().writeLPr();
            }
        }
        Slog.d(TAG, "end to init");
    }

    public int getForceDarkModeFromAppTypeRecoManager(String pkgName, PackageSetting pkgSetting) {
        if (TextUtils.isEmpty(pkgName) || pkgSetting == null || !isSupportForceDark(pkgSetting)) {
            return 2;
        }
        int appAttr = this.mAppTypeRecoManager.getAppAttribute(pkgName);
        if (IS_DEBUG) {
            Slog.d(TAG, "get app attribute from AppTypeRecoManager, pkgName = " + pkgName + ", appAttr = " + appAttr);
        }
        if (appAttr != -1 && (appAttr & 536870912) == 536870912) {
            return 1;
        }
        return 2;
    }

    public boolean checkForceDark3rdBlackListFromAppTypeRecoManager(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        int appAttr = this.mAppTypeRecoManager.getAppAttribute(pkgName);
        if (IS_DEBUG) {
            Slog.d(TAG, "get app attribute from AppTypeRecoManager, pkgName = " + pkgName + ", appAttr = " + appAttr);
        }
        if (appAttr != -1 && (appAttr & 134217728) == 134217728) {
            return true;
        }
        return false;
    }

    private boolean isSupportForceDark(PackageSetting pkgSetting) {
        if (pkgSetting == null || pkgSetting.pkg == null || pkgSetting.pkg.applicationInfo == null) {
            Slog.i(TAG, "pkgSetting is null.");
            return false;
        } else if (pkgSetting.isSystem() || pkgSetting.isUpdatedSystem()) {
            return false;
        } else {
            return true;
        }
    }

    public static synchronized HwForceDarkModeConfig getInstance() {
        HwForceDarkModeConfig hwForceDarkModeConfig;
        synchronized (HwForceDarkModeConfig.class) {
            if (instance == null) {
                instance = new HwForceDarkModeConfig();
            }
            hwForceDarkModeConfig = instance;
        }
        return hwForceDarkModeConfig;
    }
}
