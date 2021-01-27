package com.android.server.policy;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.huawei.android.biometric.FingerprintServiceEx;

public class HwGameSpaceToggleManager {
    private static final String GAMESPACE_ACTION = "com.huawei.iconnect.action.SHOW_DEVICE";
    private static final String GAMESPACE_PACKAGE = "com.huawei.gameassistant";
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT));
    private static final boolean IS_TOGGLE_KEY_DEVICE = SystemProperties.getBoolean("ro.config.togglekey", false);
    private static final String KEY_START_FROM = "START_FROM";
    private static final String KEY_START_TYPE = "START_TYPE";
    private static final String KIDSMODE_STATE_KEY = "hwkidsmode_running";
    private static final int KIDS_MODE_NO_RUNNING = 0;
    private static final int KIDS_MODE_RUNNING = 1;
    private static final String TAG = "HwGameSpaceToggleManager";
    private static final String VALUE_START_FROM_SPACE = "SPACE";
    private static final int VALUE_START_TYPE_START = 0;
    private static final int VALUE_START_TYPE_STOP = 2;
    private static HwGameSpaceToggleManager mHwGameSpaceToggleManager;
    private static boolean mIsDiviceProvisioned = false;
    private static boolean mIsGameSpaceKeyDown = false;
    private static boolean mIsKidsModeRunning = false;
    private static boolean mIsScreenOff = true;
    private GameSpaceObserverCallback mCallback;
    private Context mContext;
    private ContentObserver mDeviceProvisionedObserver = new ContentObserver(new Handler()) {
        /* class com.android.server.policy.HwGameSpaceToggleManager.AnonymousClass4 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isChanged) {
            super.onChange(isChanged);
            boolean unused = HwGameSpaceToggleManager.mIsDiviceProvisioned = HwGameSpaceToggleManager.this.isDeviceProvisioned();
            HwGameSpaceToggleManager.this.mCallback.onDataChanged(HwGameSpaceToggleManager.mIsDiviceProvisioned);
        }
    };
    private ContentObserver mKidsModeObserver = new ContentObserver(new Handler()) {
        /* class com.android.server.policy.HwGameSpaceToggleManager.AnonymousClass3 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isChanged) {
            super.onChange(isChanged);
            boolean unused = HwGameSpaceToggleManager.mIsKidsModeRunning = HwGameSpaceToggleManager.this.isKidsModeRunning();
            HwGameSpaceToggleManager.this.mCallback.onDataChanged(!HwGameSpaceToggleManager.mIsKidsModeRunning);
        }
    };
    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.HwGameSpaceToggleManager.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (SmartDualCardConsts.SYSTEM_STATE_NAME_ACTION_SHUTDOWN.equals(action)) {
                    HwGameSpaceToggleManager.this.handleGameSpaceOff();
                    HwGameSpaceToggleManager.this.unregisterKidsModeStateObserver();
                    HwGameSpaceToggleManager.this.unregisterScreenReceiver();
                    HwGameSpaceToggleManager.this.unregisterDeviceProvisionedObserver();
                } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                    boolean unused = HwGameSpaceToggleManager.mIsScreenOff = true;
                } else if (FingerprintServiceEx.ACTION_USER_PRESENT.equals(action)) {
                    boolean unused2 = HwGameSpaceToggleManager.mIsScreenOff = false;
                    if (HwGameSpaceToggleManager.mIsKidsModeRunning || !HwGameSpaceToggleManager.mIsDiviceProvisioned) {
                        Slog.d(HwGameSpaceToggleManager.TAG, "USER_PRESENT: kidsmode is running or device not provisioned.");
                    } else if (HwGameSpaceToggleManager.mIsGameSpaceKeyDown && !HwGameSpaceToggleManager.this.isGameSpaceRunning()) {
                        Slog.d(HwGameSpaceToggleManager.TAG, "GameSpaceKey is down, handleGameSpaceOn go.");
                        HwGameSpaceToggleManager.this.handleGameSpaceOn();
                    } else if (HwGameSpaceToggleManager.mIsGameSpaceKeyDown || !HwGameSpaceToggleManager.this.isGameSpaceRunning()) {
                        Slog.d(HwGameSpaceToggleManager.TAG, "Screen present, not satisfied, do nothing: isDown? " + HwGameSpaceToggleManager.mIsGameSpaceKeyDown);
                    } else {
                        Slog.d(HwGameSpaceToggleManager.TAG, "GameSpaceKey is up, handleGameSpaceOff go.");
                        HwGameSpaceToggleManager.this.handleGameSpaceOff();
                    }
                } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(action)) {
                    boolean unused3 = HwGameSpaceToggleManager.mIsScreenOff = false;
                } else {
                    Slog.w(HwGameSpaceToggleManager.TAG, "received an unexpecteda action: " + action);
                }
            }
        }
    };
    private BroadcastReceiver mUserPresentReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.HwGameSpaceToggleManager.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                boolean unused = HwGameSpaceToggleManager.mIsScreenOff = false;
                if (HwGameSpaceToggleManager.mIsKidsModeRunning || !HwGameSpaceToggleManager.mIsDiviceProvisioned) {
                    Slog.d(HwGameSpaceToggleManager.TAG, "USER_UNLOCKED: kidsmode is running or device not provisioned.");
                    return;
                }
                if (HwGameSpaceToggleManager.mIsGameSpaceKeyDown) {
                    Slog.d(HwGameSpaceToggleManager.TAG, "GameSpaceKey is down, handleGameSpaceOn go.");
                    HwGameSpaceToggleManager.this.handleGameSpaceOn();
                } else {
                    Slog.d(HwGameSpaceToggleManager.TAG, "GameSpaceKey is up, do not need to handleGameSpaceOff.");
                }
                HwGameSpaceToggleManager.this.unregisterUserPresentReceiver();
            }
        }
    };

    public interface GameSpaceObserverCallback {
        void onDataChanged(boolean z);
    }

    private HwGameSpaceToggleManager(Context context) {
        this.mContext = context;
        mIsDiviceProvisioned = isDeviceProvisioned();
        mIsKidsModeRunning = isKidsModeRunning();
    }

    public static synchronized HwGameSpaceToggleManager getInstance(Context context) {
        synchronized (HwGameSpaceToggleManager.class) {
            if (IS_TABLET && IS_TOGGLE_KEY_DEVICE) {
                if (context != null) {
                    if (mHwGameSpaceToggleManager == null) {
                        mHwGameSpaceToggleManager = new HwGameSpaceToggleManager(context);
                    }
                    return mHwGameSpaceToggleManager;
                }
            }
            return null;
        }
    }

    public void init() {
        this.mCallback = new GameSpaceObserverCallback() {
            /* class com.android.server.policy.HwGameSpaceToggleManager.AnonymousClass5 */

            @Override // com.android.server.policy.HwGameSpaceToggleManager.GameSpaceObserverCallback
            public void onDataChanged(boolean isExpect) {
                Slog.i(HwGameSpaceToggleManager.TAG, "onDataChanged, can go on ? " + isExpect + " isGameSpaceKeyDown = " + HwGameSpaceToggleManager.mIsGameSpaceKeyDown);
                if (isExpect && HwGameSpaceToggleManager.mIsGameSpaceKeyDown) {
                    HwGameSpaceToggleManager.this.handleGameSpaceOn();
                }
            }
        };
        registerDeviceProvisionedObserver();
        registerUserPresentReceiver();
        registerScreenReceiver();
        registerKidsModeStateObserver();
    }

    public void handleGameSpace(KeyEvent event) {
        mIsGameSpaceKeyDown = event.getAction() == 0;
        if (mIsKidsModeRunning || mIsScreenOff || !mIsDiviceProvisioned) {
            Slog.i(TAG, "kidsmode is running or screenoff or device not provisioned, return, iskeydown? " + mIsGameSpaceKeyDown);
            return;
        }
        if (mIsGameSpaceKeyDown) {
            handleGameSpaceOn();
        } else if (event.getAction() == 1) {
            handleGameSpaceOff();
        } else {
            Slog.e(TAG, "wrong action " + event.getAction());
            return;
        }
        Slog.i(TAG, "notify gamespace " + event.getAction());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void handleGameSpaceOn() {
        Intent intent = new Intent(GAMESPACE_ACTION);
        intent.setPackage(GAMESPACE_PACKAGE);
        intent.putExtra(KEY_START_FROM, VALUE_START_FROM_SPACE);
        intent.putExtra(KEY_START_TYPE, 0);
        try {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (ActivityNotFoundException e) {
            Slog.e(TAG, "notify gamespaceOn err: " + e.getMessage());
        }
        Slog.i(TAG, "notify gamespaceOn out");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void handleGameSpaceOff() {
        Intent intent = new Intent(GAMESPACE_ACTION);
        intent.setPackage(GAMESPACE_PACKAGE);
        intent.putExtra(KEY_START_FROM, VALUE_START_FROM_SPACE);
        intent.putExtra(KEY_START_TYPE, 2);
        try {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (ActivityNotFoundException e) {
            Slog.e(TAG, "notify gamespaceOff err: " + e.getMessage());
        }
        Slog.i(TAG, "notify gamespaceOff out.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isKidsModeRunning() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), KIDSMODE_STATE_KEY, 0) == 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDeviceProvisioned() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    private void registerKidsModeStateObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(KIDSMODE_STATE_KEY), true, this.mKidsModeObserver);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterKidsModeStateObserver() {
        if (this.mKidsModeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mKidsModeObserver);
        }
    }

    private void registerDeviceProvisionedObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, this.mDeviceProvisionedObserver);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterDeviceProvisionedObserver() {
        if (this.mDeviceProvisionedObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mDeviceProvisionedObserver);
        }
    }

    private void registerUserPresentReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiver(this.mUserPresentReceiver, filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterUserPresentReceiver() {
        BroadcastReceiver broadcastReceiver = this.mUserPresentReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
        }
    }

    private void registerScreenReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_ACTION_SHUTDOWN);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
        filter.addAction(FingerprintServiceEx.ACTION_USER_PRESENT);
        this.mContext.registerReceiver(this.mScreenReceiver, filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterScreenReceiver() {
        BroadcastReceiver broadcastReceiver = this.mScreenReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isGameSpaceRunning() {
        return GAMESPACE_PACKAGE.equals(getCurrentDefualtLaucher());
    }

    private String getCurrentDefualtLaucher() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        return this.mContext.getPackageManager().resolveActivity(intent, 65536).activityInfo.packageName;
    }
}
