package com.android.server.gesture;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.gesture.DeviceStateController;
import com.android.systemui.shared.recents.IOverviewProxy;
import java.io.PrintWriter;

public class OverviewProxyService {
    private static final String ACTION_QUICKSTEP = "android.intent.action.QUICKSTEP_SERVICE";
    private static final long BACKOFF_MILLIS = 1000;
    private static final long DEFERRED_CALLBACK_MILLIS = 5000;
    private static final long MAX_BACKOFF_MILLIS = 600000;
    private final BroadcastReceiver mBaseBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(GestureNavConst.TAG_GESTURE_OPS, "User unlocked.");
            OverviewProxyService.this.startConnectionToCurrentUser();
        }
    };
    /* access modifiers changed from: private */
    public int mConnectionBackoffAttempts;
    private final Runnable mConnectionRunnable = new Runnable() {
        public final void run() {
            OverviewProxyService.this.internalConnectToCurrentUser();
        }
    };
    private final Context mContext;
    /* access modifiers changed from: private */
    public final Runnable mDeferredConnectionCallback = new Runnable() {
        public final void run() {
            OverviewProxyService.lambda$new$0(OverviewProxyService.this);
        }
    };
    private final DeviceStateController.DeviceChangedListener mDeviceChangedCallback = new DeviceStateController.DeviceChangedListener() {
        public void onUserSetupChanged(boolean setup) {
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavConst.TAG_GESTURE_OPS, "onUserSetupChanged isCurrentUserSetup=" + setup);
            }
            if (OverviewProxyService.this.updateHomeWindow()) {
                OverviewProxyService.this.updateEnabledState();
            }
            if (setup) {
                OverviewProxyService.this.startConnectionToCurrentUser();
            }
        }

        public void onUserSwitched(int newUserId) {
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavConst.TAG_GESTURE_OPS, "onUserSwitched newUserId=" + newUserId);
            }
            if (OverviewProxyService.this.updateHomeWindow()) {
                OverviewProxyService.this.updateEnabledState();
            }
            int unused = OverviewProxyService.this.mConnectionBackoffAttempts = 0;
            OverviewProxyService.this.startConnectionToCurrentUser();
        }

        public void onPreferredActivityChanged(boolean isPrefer) {
            if (OverviewProxyService.this.updateHomeWindow()) {
                OverviewProxyService.this.updateEnabledState();
                OverviewProxyService.this.startConnectionToCurrentUser();
            }
        }
    };
    private final DeviceStateController mDeviceStateController;
    private final Runnable mDisConnectionRunnable = new Runnable() {
        public final void run() {
            OverviewProxyService.this.disconnectFromLauncherService();
        }
    };
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private String mHomeWindow;
    private boolean mIsEnabled;
    private final BroadcastReceiver mLauncherStateChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(GestureNavConst.TAG_GESTURE_OPS, "Launcher state changed, intent=" + intent);
            OverviewProxyService.this.updateEnabledState();
            OverviewProxyService.this.startConnectionToCurrentUser();
        }
    };
    /* access modifiers changed from: private */
    public IOverviewProxy mOverviewProxy;
    private final ServiceConnection mOverviewServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            OverviewProxyService.this.mHandler.removeCallbacks(OverviewProxyService.this.mDeferredConnectionCallback);
            int unused = OverviewProxyService.this.mConnectionBackoffAttempts = 0;
            IOverviewProxy unused2 = OverviewProxyService.this.mOverviewProxy = IOverviewProxy.Stub.asInterface(service);
            Log.i(GestureNavConst.TAG_GESTURE_OPS, "Launcher service connected, mOverviewProxy=" + OverviewProxyService.this.mOverviewProxy);
            try {
                service.linkToDeath(OverviewProxyService.this.mOverviewServiceDeathRcpt, 0);
            } catch (RemoteException e) {
                Log.e(GestureNavConst.TAG_GESTURE_OPS, "Lost connection to launcher service", e);
            }
        }

        public void onNullBinding(ComponentName name) {
            Log.w(GestureNavConst.TAG_GESTURE_OPS, "Null binding of '" + name + "', try reconnecting");
            OverviewProxyService.this.retryConnectionToCurrentUser();
        }

        public void onBindingDied(ComponentName name) {
            Log.w(GestureNavConst.TAG_GESTURE_OPS, "Binding died of '" + name + "', try reconnecting");
            OverviewProxyService.this.retryConnectionToCurrentUser();
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.i(GestureNavConst.TAG_GESTURE_OPS, "Launcher service disconnected, name=" + name);
            int unused = OverviewProxyService.this.mConnectionBackoffAttempts = 0;
        }
    };
    /* access modifiers changed from: private */
    public final IBinder.DeathRecipient mOverviewServiceDeathRcpt = new IBinder.DeathRecipient() {
        public final void binderDied() {
            OverviewProxyService.this.cleanupAfterDeath();
        }
    };
    private final Intent mQuickStepIntent;
    private final ComponentName mRecentsComponentName;
    private final Runnable mRetryConnectionRunnable = new Runnable() {
        public final void run() {
            OverviewProxyService.this.retryConnectionWithBackoff();
        }
    };

    public static /* synthetic */ void lambda$new$0(OverviewProxyService overviewProxyService) {
        Log.w(GestureNavConst.TAG_GESTURE_OPS, "Binder supposed established connection but actual connection to service timed out, trying again");
        overviewProxyService.retryConnectionWithBackoff();
    }

    public OverviewProxyService(Context context, Looper looper) {
        this.mContext = context;
        this.mHandler = new Handler(looper);
        this.mRecentsComponentName = new ComponentName(GestureNavConst.DEFAULT_LAUNCHER_PACKAGE, GestureNavConst.DEFAULT_QUICKSTEP_CLASS);
        this.mQuickStepIntent = new Intent(ACTION_QUICKSTEP).setPackage(this.mRecentsComponentName.getPackageName());
        this.mDeviceStateController = DeviceStateController.getInstance(this.mContext);
    }

    public void notifyStart() {
        this.mConnectionBackoffAttempts = 0;
        this.mHomeWindow = this.mDeviceStateController.getCurrentHomeActivity();
        this.mDeviceStateController.addCallback(this.mDeviceChangedCallback);
        Log.i(GestureNavConst.TAG_GESTURE_OPS, "mQuickStepIntent=" + this.mQuickStepIntent);
        updateEnabledState();
        startConnectionToCurrentUser();
        IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        filter.addDataScheme("package");
        filter.addDataSchemeSpecificPart(this.mRecentsComponentName.getPackageName(), 0);
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        this.mContext.registerReceiverAsUser(this.mLauncherStateChangedReceiver, UserHandle.ALL, filter, null, this.mHandler);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(this.mBaseBroadcastReceiver, UserHandle.ALL, filter2, null, this.mHandler);
    }

    public void notifyStop() {
        this.mContext.unregisterReceiver(this.mLauncherStateChangedReceiver);
        this.mContext.unregisterReceiver(this.mBaseBroadcastReceiver);
        this.mDeviceStateController.removeCallback(this.mDeviceChangedCallback);
        stopConnectionToCurrentUser();
    }

    public void cleanupAfterDeath() {
        Log.i(GestureNavConst.TAG_GESTURE_OPS, "service binder died");
        startConnectionToCurrentUser();
    }

    public void startConnectionToCurrentUser() {
        if (this.mHandler.getLooper() != Looper.myLooper()) {
            this.mHandler.post(this.mConnectionRunnable);
        } else {
            internalConnectToCurrentUser();
        }
    }

    public void retryConnectionToCurrentUser() {
        if (this.mHandler.getLooper() != Looper.myLooper()) {
            this.mHandler.post(this.mRetryConnectionRunnable);
        } else {
            retryConnectionWithBackoff();
        }
    }

    public void stopConnectionToCurrentUser() {
        this.mHandler.removeCallbacks(this.mConnectionRunnable);
        this.mHandler.removeCallbacks(this.mRetryConnectionRunnable);
        this.mHandler.removeCallbacks(this.mDeferredConnectionCallback);
        if (this.mHandler.getLooper() != Looper.myLooper()) {
            this.mHandler.post(this.mDisConnectionRunnable);
        } else {
            disconnectFromLauncherService();
        }
    }

    /* access modifiers changed from: private */
    public void internalConnectToCurrentUser() {
        disconnectFromLauncherService();
        if (!this.mDeviceStateController.isCurrentUserSetup() || !isEnabled()) {
            Log.i(GestureNavConst.TAG_GESTURE_OPS, "Not setup or not enable, isEnabled=" + isEnabled());
            return;
        }
        this.mHandler.removeCallbacks(this.mConnectionRunnable);
        this.mHandler.removeCallbacks(this.mRetryConnectionRunnable);
        Intent launcherServiceIntent = new Intent(ACTION_QUICKSTEP).setPackage(this.mRecentsComponentName.getPackageName());
        boolean bound = false;
        try {
            bound = this.mContext.bindServiceAsUser(launcherServiceIntent, this.mOverviewServiceConnection, 1, UserHandle.of(this.mDeviceStateController.getCurrentUser()));
        } catch (SecurityException e) {
            Log.e(GestureNavConst.TAG_GESTURE_OPS, "Unable to bind because of security error", e);
        } catch (Exception e2) {
            Log.e(GestureNavConst.TAG_GESTURE_OPS, "bind service fail", e2);
        }
        Log.i(GestureNavConst.TAG_GESTURE_OPS, "internalConnectToCurrentUser, bound=" + bound + ", launcherServiceIntent=" + launcherServiceIntent);
        if (bound) {
            this.mHandler.postDelayed(this.mDeferredConnectionCallback, DEFERRED_CALLBACK_MILLIS);
        } else {
            retryConnectionWithBackoff();
        }
    }

    /* access modifiers changed from: private */
    public void retryConnectionWithBackoff() {
        if (!this.mHandler.hasCallbacks(this.mConnectionRunnable)) {
            this.mHandler.postDelayed(this.mConnectionRunnable, (long) Math.min(Math.scalb(1000.0f, this.mConnectionBackoffAttempts), 600000.0f));
            this.mConnectionBackoffAttempts++;
            Log.w(GestureNavConst.TAG_GESTURE_OPS, "Failed to connect on attempt " + this.mConnectionBackoffAttempts + " will try again in " + timeoutMs + "ms");
        }
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public IOverviewProxy getProxy() {
        return this.mOverviewProxy;
    }

    /* access modifiers changed from: private */
    public void disconnectFromLauncherService() {
        if (this.mOverviewProxy != null) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_OPS, "disconnectFromLauncherService start");
            }
            try {
                this.mOverviewProxy.asBinder().unlinkToDeath(this.mOverviewServiceDeathRcpt, 0);
            } catch (Exception e) {
                Log.e(GestureNavConst.TAG_GESTURE_OPS, "unlinkToDeath fail, mIsEnabled=" + this.mIsEnabled, e);
            }
            boolean unbind = true;
            try {
                this.mContext.unbindService(this.mOverviewServiceConnection);
            } catch (Exception e2) {
                unbind = false;
                Log.e(GestureNavConst.TAG_GESTURE_OPS, "unbind service fail", e2);
            }
            this.mOverviewProxy = null;
            Log.i(GestureNavConst.TAG_GESTURE_OPS, "unbind service:" + unbind);
        }
    }

    /* access modifiers changed from: private */
    public void updateEnabledState() {
        this.mIsEnabled = this.mContext.getPackageManager().resolveServiceAsUser(this.mQuickStepIntent, 786432, ActivityManager.getCurrentUser()) != null;
        Log.i(GestureNavConst.TAG_GESTURE_OPS, "mIsEnabled=" + this.mIsEnabled);
    }

    /* access modifiers changed from: private */
    public boolean updateHomeWindow() {
        String homeWindow = this.mDeviceStateController.getCurrentHomeActivity();
        if (homeWindow == null || homeWindow.equals(this.mHomeWindow)) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_OPS, "Home changed, newHome=" + homeWindow + ", oldHome=" + this.mHomeWindow);
        }
        this.mHomeWindow = homeWindow;
        return true;
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        pw.print(prefix);
        pw.println("mQuickStepIntent=" + this.mQuickStepIntent);
        pw.print(prefix);
        pw.print("mConnectionBackoffAttempts=" + this.mConnectionBackoffAttempts);
        pw.print(" mIsEnabled=" + this.mIsEnabled);
        pw.println();
        pw.print(prefix);
        pw.println("mHomeWindow=" + this.mHomeWindow);
        pw.print(prefix);
        pw.println("mOverviewProxy=" + this.mOverviewProxy);
    }
}
