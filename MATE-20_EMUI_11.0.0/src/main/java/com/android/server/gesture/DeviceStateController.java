package com.android.server.gesture;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import com.android.server.gesture.DefaultDeviceStateController;
import com.android.server.policy.HwGameDockGesture;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicyEx;
import com.android.systemui.shared.system.PackageManagerWrapper;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.WindowManagerExt;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.IntentExEx;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.server.LocalServicesExt;
import huawei.com.android.server.policy.HwFalseTouchMonitor;
import java.util.ArrayList;
import java.util.List;

public class DeviceStateController extends DefaultDeviceStateController {
    private static final String TAG = "DeviceStateController";
    private static DeviceStateController sInstance;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private int mCurrentUserId;
    private final Uri mDeviceProvisionedUri;
    private final ArrayList<DefaultDeviceStateController.DeviceChangedListener> mListeners = new ArrayList<>();
    private Looper mLooper;
    private HwPhoneWindowManager mPolicy;
    private final BroadcastReceiver mPreferChangedReceiver = new BroadcastReceiver() {
        /* class com.android.server.gesture.DeviceStateController.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            boolean isPrefer;
            if (context != null && intent != null) {
                if (!PackageManagerWrapper.ACTION_PREFERRED_ACTIVITY_CHANGED.equals(intent.getAction())) {
                    isPrefer = false;
                } else if (intent.getIntExtra("android.intent.extra.user_handle", -10000) != -10000) {
                    isPrefer = true;
                } else {
                    return;
                }
                DeviceStateController.this.notifyPreferredActivityChanged(isPrefer);
            }
        }
    };
    private SettingsObserver mSettingsObserver;
    private final Uri mUserSetupUri;
    private final BroadcastReceiver mUserSwitchedReceiver = new BroadcastReceiver() {
        /* class com.android.server.gesture.DeviceStateController.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null) {
                DeviceStateController deviceStateController = DeviceStateController.this;
                deviceStateController.mCurrentUserId = deviceStateController.getCurrentUser();
                Log.i(DeviceStateController.TAG, "User switched receiver, userId=" + DeviceStateController.this.mCurrentUserId);
                DeviceStateController deviceStateController2 = DeviceStateController.this;
                deviceStateController2.onUserSwitched(deviceStateController2.mCurrentUserId);
            }
        }
    };

    private DeviceStateController(Context context) {
        super(context);
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mDeviceProvisionedUri = Settings.Global.getUriFor("device_provisioned");
        this.mUserSetupUri = Settings.Secure.getUriFor("user_setup_complete");
        this.mCurrentUserId = getCurrentUser();
        this.mPolicy = WindowManagerPolicyEx.getInstance().getHwPhoneWindowManager();
    }

    public static DeviceStateController getInstance(Context context) {
        DeviceStateController deviceStateController;
        synchronized (DeviceStateController.class) {
            if (sInstance == null) {
                sInstance = new DeviceStateController(context);
            }
            deviceStateController = sInstance;
        }
        return deviceStateController;
    }

    public boolean isDeviceProvisioned() {
        return Settings.Global.getInt(this.mContentResolver, "device_provisioned", 0) != 0;
    }

    public boolean isCurrentUserSetup() {
        return SettingsEx.Secure.getIntForUser(this.mContentResolver, "user_setup_complete", 0, -2) != 0;
    }

    public boolean isOOBEActivityEnabled() {
        if (this.mContext == null) {
            Log.d(TAG, "mContext is null.");
            return false;
        }
        List<ResolveInfo> resolveInfo = this.mContext.getPackageManager().queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT").setPackage(GestureNavConst.OOBE_MAIN_PACKAGE), 0);
        if (resolveInfo == null || resolveInfo.size() <= 0) {
            return false;
        }
        return true;
    }

    public boolean isSetupWizardEnabled() {
        if (this.mContext == null) {
            Log.d(TAG, "mContext is null.");
            return false;
        }
        List<ResolveInfo> resolveInfo = this.mContext.getPackageManager().queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT").setPackage(GestureNavConst.SETUP_WIZARD_PACKAGE), 0);
        if (resolveInfo == null || resolveInfo.size() <= 0) {
            return false;
        }
        return true;
    }

    public int getCurrentUser() {
        return ActivityManagerEx.getCurrentUser();
    }

    public boolean isKeyguardOccluded() {
        HwPhoneWindowManager hwPhoneWindowManager = this.mPolicy;
        if (hwPhoneWindowManager != null) {
            return hwPhoneWindowManager.isKeyguardOccluded();
        }
        return false;
    }

    public boolean isKeyguardShowingOrOccluded() {
        HwPhoneWindowManager hwPhoneWindowManager = this.mPolicy;
        if (hwPhoneWindowManager != null) {
            return hwPhoneWindowManager.isKeyguardShowingOrOccluded();
        }
        return false;
    }

    public boolean isKeyguardLocked() {
        HwPhoneWindowManager hwPhoneWindowManager = this.mPolicy;
        if (hwPhoneWindowManager != null) {
            return hwPhoneWindowManager.isKeyguardLocked();
        }
        return false;
    }

    public boolean isKeyguardShowingAndNotOccluded() {
        HwPhoneWindowManager hwPhoneWindowManager = this.mPolicy;
        if (hwPhoneWindowManager != null) {
            return hwPhoneWindowManager.isKeyguardShowingAndNotOccluded();
        }
        return false;
    }

    public WindowManagerPolicyEx.WindowStateEx getNavigationBar() {
        return WindowManagerPolicyEx.getNavigationBar();
    }

    public WindowManagerPolicyEx.WindowStateEx getFocusWindow() {
        return WindowManagerPolicyEx.getFocusedWindow();
    }

    public WindowManagerPolicyEx.WindowStateEx getInputMethodWindow() {
        return WindowManagerPolicyEx.getInputMethodWindow();
    }

    public String getFocusWindowName() {
        WindowManagerPolicyEx.WindowStateEx focusWindowState = getFocusWindow();
        if (focusWindowState == null || focusWindowState.getAttrs() == null) {
            return null;
        }
        return focusWindowState.getAttrs().getTitle().toString();
    }

    public String getFocusPackageName() {
        WindowManagerPolicyEx.WindowStateEx focusWindowState = getFocusWindow();
        if (focusWindowState == null || focusWindowState.getAttrs() == null) {
            return null;
        }
        return focusWindowState.getAttrs().packageName;
    }

    public int getCurrentRotation() {
        int i = GestureNavConst.DEFAULT_ROTATION;
        int rotation = WindowManagerExt.getDefaultDisplayRotation();
        if (rotation == -1) {
            return GestureNavConst.DEFAULT_ROTATION;
        }
        return rotation;
    }

    public int getShrinkIdByDockPosition() {
        HwGameDockGesture gameDock = (HwGameDockGesture) LocalServicesExt.getService(HwGameDockGesture.class);
        if (gameDock != null) {
            return gameDock.getShrinkIdByDockPosition();
        }
        return 0;
    }

    private int getSystemUiFlag() {
        HwPhoneWindowManager hwPhoneWindowManager = this.mPolicy;
        if (hwPhoneWindowManager != null) {
            return hwPhoneWindowManager.getLastSystemUiFlags();
        }
        return 0;
    }

    public boolean isWindowBackDisabled() {
        return (getSystemUiFlag() & 4194304) != 0;
    }

    public boolean isWindowHomeDisabled() {
        return (getSystemUiFlag() & 2097152) != 0;
    }

    public boolean isWindowRecentDisabled() {
        return (getSystemUiFlag() & 16777216) != 0;
    }

    public String getCurrentHomeActivity() {
        return getCurrentHomeActivity(this.mCurrentUserId);
    }

    public String getCurrentHomeActivity(int userId) {
        ResolveInfo resolveInfo = PackageManagerExt.resolveActivityAsUser(this.mContext.getPackageManager(), getHomeIntent(), 786432, userId);
        if (resolveInfo == null || resolveInfo.activityInfo == null) {
            return null;
        }
        return resolveInfo.activityInfo.packageName + "/" + resolveInfo.activityInfo.name;
    }

    public void onConfigurationChanged() {
        notifyConfigurationChanged();
    }

    public void addCallback(DefaultDeviceStateController.DeviceChangedListener listener) {
        this.mListeners.add(listener);
        if (this.mListeners.size() == 1) {
            startListening();
        }
        listener.onDeviceProvisionedChanged(isDeviceProvisioned());
        listener.onUserSetupChanged(isCurrentUserSetup());
    }

    public void removeCallback(DefaultDeviceStateController.DeviceChangedListener listener) {
        this.mListeners.remove(listener);
        if (this.mListeners.size() == 0) {
            stopListening();
        }
    }

    private void startListening() {
        this.mCurrentUserId = getCurrentUser();
        this.mLooper = Looper.myLooper();
        Log.i(TAG, "start listening, userId:" + this.mCurrentUserId);
        Handler handler = new Handler(this.mLooper);
        IntentFilter filter = new IntentFilter();
        filter.addAction(IntentExEx.getActionUserSwitched());
        ContextEx.registerReceiverAsUser(this.mContext, this.mUserSwitchedReceiver, UserHandleEx.ALL, filter, (String) null, handler);
        IntentFilter filter2 = new IntentFilter(PackageManagerWrapper.ACTION_PREFERRED_ACTIVITY_CHANGED);
        filter2.setPriority(HwFalseTouchMonitor.NoEffectClickChecker.CLICK_INTERVAL_TIMEOUT);
        ContextEx.registerReceiverAsUser(this.mContext, this.mPreferChangedReceiver, UserHandleEx.ALL, filter2, (String) null, handler);
        IntentFilter packageFilter = new IntentFilter("android.intent.action.PACKAGE_CHANGED");
        packageFilter.addDataScheme("package");
        ContextEx.registerReceiverAsUser(this.mContext, this.mPreferChangedReceiver, UserHandleEx.ALL, packageFilter, (String) null, handler);
        registerObserver(this.mCurrentUserId);
    }

    private void stopListening() {
        this.mContext.unregisterReceiver(this.mUserSwitchedReceiver);
        this.mContext.unregisterReceiver(this.mPreferChangedReceiver);
        unregisterObserver();
        Log.i(TAG, "stop listening.");
    }

    private void registerObserver(int userId) {
        if (this.mSettingsObserver == null) {
            Looper looper = this.mLooper;
            if (looper == null) {
                looper = Looper.myLooper();
            }
            this.mSettingsObserver = new SettingsObserver(new Handler(looper));
            ContentResolverExt.registerContentObserver(this.mContentResolver, this.mDeviceProvisionedUri, true, this.mSettingsObserver, 0);
            ContentResolverExt.registerContentObserver(this.mContentResolver, this.mUserSetupUri, true, this.mSettingsObserver, userId);
        }
    }

    private void unregisterObserver() {
        SettingsObserver settingsObserver = this.mSettingsObserver;
        if (settingsObserver != null) {
            this.mContentResolver.unregisterContentObserver(settingsObserver);
            this.mSettingsObserver = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserSwitched(int newUserId) {
        unregisterObserver();
        registerObserver(newUserId);
        notifyUserChanged(newUserId);
    }

    private void notifyUserChanged(int newUserId) {
        for (int i = this.mListeners.size() - 1; i >= 0; i--) {
            this.mListeners.get(i).onUserSwitched(newUserId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyProvisionedChanged(boolean isProvisioned) {
        for (int i = this.mListeners.size() - 1; i >= 0; i--) {
            this.mListeners.get(i).onDeviceProvisionedChanged(isProvisioned);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifySetupChanged(boolean isSetup) {
        for (int i = this.mListeners.size() - 1; i >= 0; i--) {
            this.mListeners.get(i).onUserSetupChanged(isSetup);
        }
    }

    private void notifyConfigurationChanged() {
        for (int i = this.mListeners.size() - 1; i >= 0; i--) {
            this.mListeners.get(i).onConfigurationChanged();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyPreferredActivityChanged(boolean isPrefer) {
        for (int i = this.mListeners.size() - 1; i >= 0; i--) {
            this.mListeners.get(i).onPreferredActivityChanged(isPrefer);
        }
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange, Uri uri) {
            if (DeviceStateController.this.mDeviceProvisionedUri.equals(uri)) {
                DeviceStateController deviceStateController = DeviceStateController.this;
                deviceStateController.notifyProvisionedChanged(deviceStateController.isDeviceProvisioned());
            } else if (DeviceStateController.this.mUserSetupUri.equals(uri)) {
                DeviceStateController deviceStateController2 = DeviceStateController.this;
                deviceStateController2.notifySetupChanged(deviceStateController2.isCurrentUserSetup());
            }
        }
    }

    private Intent getHomeIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        return intent;
    }
}
