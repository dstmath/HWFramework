package com.android.server.gesture;

import android.app.ActivityManager;
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
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.server.LocalServices;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.systemui.shared.system.PackageManagerWrapper;
import java.util.ArrayList;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;

public class DeviceStateController {
    private static final String TAG = "DeviceStateController";
    private static DeviceStateController sInstance;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentUserId;
    /* access modifiers changed from: private */
    public final Uri mDeviceProvisionedUri;
    private final ArrayList<DeviceChangedListener> mListeners = new ArrayList<>();
    private WindowManagerPolicy mPolicy;
    private final BroadcastReceiver mPreferChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean isPrefer;
            if (!PackageManagerWrapper.ACTION_PREFERRED_ACTIVITY_CHANGED.equals(intent.getAction())) {
                isPrefer = false;
            } else if (intent.getIntExtra("android.intent.extra.user_handle", -10000) != -10000) {
                isPrefer = true;
            } else {
                return;
            }
            DeviceStateController.this.notifyPreferredActivityChanged(isPrefer);
        }
    };
    protected final ContentObserver mSettingsObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (DeviceStateController.this.mDeviceProvisionedUri.equals(uri)) {
                DeviceStateController.this.notifyProvisionedChanged(DeviceStateController.this.isDeviceProvisioned());
            } else if (DeviceStateController.this.mUserSetupUri.equals(uri)) {
                DeviceStateController.this.notifySetupChanged(DeviceStateController.this.isCurrentUserSetup());
            }
        }
    };
    /* access modifiers changed from: private */
    public final Uri mUserSetupUri;
    private final BroadcastReceiver mUserSwitchedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int unused = DeviceStateController.this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", 0);
            Log.i(DeviceStateController.TAG, "User swtiched receiver, userId=" + DeviceStateController.this.mCurrentUserId);
            DeviceStateController.this.onUserSwitched(DeviceStateController.this.mCurrentUserId);
        }
    };

    public static abstract class DeviceChangedListener {
        /* access modifiers changed from: package-private */
        public void onDeviceProvisionedChanged(boolean provisioned) {
        }

        /* access modifiers changed from: package-private */
        public void onUserSwitched(int newUserId) {
        }

        /* access modifiers changed from: package-private */
        public void onUserSetupChanged(boolean setup) {
        }

        /* access modifiers changed from: package-private */
        public void onConfigurationChanged() {
        }

        /* access modifiers changed from: package-private */
        public void onPreferredActivityChanged(boolean isPrefer) {
        }
    }

    private DeviceStateController(Context context) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mDeviceProvisionedUri = Settings.Global.getUriFor("device_provisioned");
        this.mUserSetupUri = Settings.Secure.getUriFor("user_setup_complete");
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        this.mPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
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
        return Settings.Secure.getIntForUser(this.mContentResolver, "user_setup_complete", 0, -2) != 0;
    }

    public int getCurrentUser() {
        return ActivityManager.getCurrentUser();
    }

    public boolean isKeyguardOccluded() {
        return this.mPolicy.isKeyguardOccluded();
    }

    public boolean isKeyguardShowingOrOccluded() {
        return this.mPolicy.isKeyguardShowingOrOccluded();
    }

    public boolean isKeyguardLocked() {
        return this.mPolicy.isKeyguardLocked();
    }

    public boolean isKeyguardShowingAndNotOccluded() {
        return this.mPolicy.isKeyguardShowingAndNotOccluded();
    }

    public WindowManagerPolicy.WindowState getFocusWindow() {
        if (this.mPolicy instanceof HwPhoneWindowManager) {
            return this.mPolicy.getFocusedWindow();
        }
        return null;
    }

    public String getFocusWindowName() {
        WindowManagerPolicy.WindowState focusWindowState = getFocusWindow();
        if (focusWindowState == null || focusWindowState.getAttrs() == null) {
            return null;
        }
        return focusWindowState.getAttrs().getTitle().toString();
    }

    public String getFocusPackageName() {
        WindowManagerPolicy.WindowState focusWindowState = getFocusWindow();
        if (focusWindowState == null || focusWindowState.getAttrs() == null) {
            return null;
        }
        return focusWindowState.getAttrs().packageName;
    }

    public int getCurrentRotation() {
        if (this.mPolicy instanceof HwPhoneWindowManager) {
            return this.mPolicy.getCurrentRotation();
        }
        return GestureNavConst.DEFAULT_ROTATION;
    }

    public boolean isNavBarAtBottom() {
        return this.mPolicy.getNavBarPosition() == 4;
    }

    public int getSystemUIFlag() {
        if (this.mPolicy instanceof HwPhoneWindowManager) {
            return this.mPolicy.getLastSystemUiFlags();
        }
        return 0;
    }

    public boolean isWindowBackDisabled() {
        return (getSystemUIFlag() & 4194304) != 0;
    }

    public boolean isWindowHomeDisabled() {
        return (getSystemUIFlag() & HighBitsCompModeID.MODE_EYE_PROTECT) != 0;
    }

    public boolean isWindowRecentDisabled() {
        return (getSystemUIFlag() & 16777216) != 0;
    }

    public String getCurrentHomeActivity() {
        return getCurrentHomeActivity(this.mCurrentUserId);
    }

    public String getCurrentHomeActivity(int userId) {
        ResolveInfo resolveInfo = this.mContext.getPackageManager().resolveActivityAsUser(getHomeIntent(), 786432, userId);
        if (resolveInfo == null || resolveInfo.activityInfo == null) {
            return null;
        }
        return resolveInfo.activityInfo.packageName + "/" + resolveInfo.activityInfo.name;
    }

    public void onConfigurationChanged() {
        notifyConfigurationChanged();
    }

    public void addCallback(DeviceChangedListener listener) {
        this.mListeners.add(listener);
        if (this.mListeners.size() == 1) {
            startListening(getCurrentUser());
        }
        listener.onDeviceProvisionedChanged(isDeviceProvisioned());
        listener.onUserSetupChanged(isCurrentUserSetup());
    }

    public void removeCallback(DeviceChangedListener listener) {
        this.mListeners.remove(listener);
        if (this.mListeners.size() == 0) {
            stopListening();
        }
    }

    private void registerObserver(int userId) {
        this.mContentResolver.registerContentObserver(this.mDeviceProvisionedUri, true, this.mSettingsObserver, 0);
        this.mContentResolver.registerContentObserver(this.mUserSetupUri, true, this.mSettingsObserver, userId);
    }

    private void startListening(int userId) {
        Log.i(TAG, "start listening.");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiverAsUser(this.mUserSwitchedReceiver, UserHandle.ALL, filter, null, null);
        IntentFilter filter2 = new IntentFilter(PackageManagerWrapper.ACTION_PREFERRED_ACTIVITY_CHANGED);
        filter2.setPriority(1000);
        this.mContext.registerReceiverAsUser(this.mPreferChangedReceiver, UserHandle.ALL, filter2, null, null);
        IntentFilter packageFilter = new IntentFilter("android.intent.action.PACKAGE_CHANGED");
        packageFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mPreferChangedReceiver, UserHandle.ALL, packageFilter, null, null);
        registerObserver(userId);
    }

    private void stopListening() {
        this.mContext.unregisterReceiver(this.mUserSwitchedReceiver);
        this.mContext.unregisterReceiver(this.mPreferChangedReceiver);
        this.mContentResolver.unregisterContentObserver(this.mSettingsObserver);
        Log.i(TAG, "stop listening.");
    }

    /* access modifiers changed from: private */
    public void onUserSwitched(int newUserId) {
        this.mContentResolver.unregisterContentObserver(this.mSettingsObserver);
        registerObserver(newUserId);
        notifyUserChanged(newUserId);
    }

    private void notifyUserChanged(int newUserId) {
        for (int i = this.mListeners.size() - 1; i >= 0; i--) {
            this.mListeners.get(i).onUserSwitched(newUserId);
        }
    }

    /* access modifiers changed from: private */
    public void notifyProvisionedChanged(boolean provisioned) {
        for (int i = this.mListeners.size() - 1; i >= 0; i--) {
            this.mListeners.get(i).onDeviceProvisionedChanged(provisioned);
        }
    }

    /* access modifiers changed from: private */
    public void notifySetupChanged(boolean setup) {
        for (int i = this.mListeners.size() - 1; i >= 0; i--) {
            this.mListeners.get(i).onUserSetupChanged(setup);
        }
    }

    private void notifyConfigurationChanged() {
        for (int i = this.mListeners.size() - 1; i >= 0; i--) {
            this.mListeners.get(i).onConfigurationChanged();
        }
    }

    /* access modifiers changed from: private */
    public void notifyPreferredActivityChanged(boolean isPrefer) {
        for (int i = this.mListeners.size() - 1; i >= 0; i--) {
            this.mListeners.get(i).onPreferredActivityChanged(isPrefer);
        }
    }

    private Intent getHomeIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        return intent;
    }
}
