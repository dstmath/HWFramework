package com.android.server.gesture;

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
import android.util.Log;
import com.android.server.gesture.DefaultDeviceStateController;
import com.android.systemui.shared.recents.IOverviewProxy;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.os.UserHandleEx;
import java.io.PrintWriter;

public class OverviewProxyService {
    private static final String ACTION_QUICKSTEP = "android.intent.action.QUICKSTEP_SERVICE";
    private static final long BACKOFF_MILLIS = 1000;
    private static final long DEFERRED_CALLBACK_MILLIS = 5000;
    private static final long MAX_BACKOFF_MILLIS = 600000;
    private final BroadcastReceiver mBaseBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.gesture.OverviewProxyService.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.i(GestureNavConst.TAG_GESTURE_OPS, "User unlocked.");
            OverviewProxyService.this.startConnectionToCurrentUser();
        }
    };
    private int mConnectionBackoffAttempts;
    private final Runnable mConnectionRunnable = new Runnable() {
        /* class com.android.server.gesture.$$Lambda$OverviewProxyService$KOWDBHP6658WCoqHveYatMN2KJc */

        @Override // java.lang.Runnable
        public final void run() {
            OverviewProxyService.lambda$KOWDBHP6658WCoqHveYatMN2KJc(OverviewProxyService.this);
        }
    };
    private final Context mContext;
    private final Runnable mDeferredConnectionCallback = new Runnable() {
        /* class com.android.server.gesture.$$Lambda$OverviewProxyService$uA1mapK1w1fvQoRhlNlsAaLeiaM */

        @Override // java.lang.Runnable
        public final void run() {
            OverviewProxyService.this.lambda$new$0$OverviewProxyService();
        }
    };
    private final DefaultDeviceStateController.DeviceChangedListener mDeviceChangedCallback = new DefaultDeviceStateController.DeviceChangedListener() {
        /* class com.android.server.gesture.OverviewProxyService.AnonymousClass4 */

        public void onUserSetupChanged(boolean isSetup) {
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavConst.TAG_GESTURE_OPS, "onUserSetupChanged isCurrentUserSetup=" + isSetup);
            }
            if (OverviewProxyService.this.updateHomeWindow()) {
                OverviewProxyService.this.updateEnabledState();
            }
            if (isSetup) {
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
            OverviewProxyService.this.mConnectionBackoffAttempts = 0;
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
        /* class com.android.server.gesture.$$Lambda$OverviewProxyService$Oun8DXyLo1XpuLvrpUdaj8mbO7Y */

        @Override // java.lang.Runnable
        public final void run() {
            OverviewProxyService.lambda$Oun8DXyLo1XpuLvrpUdaj8mbO7Y(OverviewProxyService.this);
        }
    };
    private final Handler mHandler;
    private String mHomeWindow;
    private boolean mIsEnabled;
    private boolean mIsGesNavProxyEnable = true;
    private final BroadcastReceiver mLauncherStateChangedReceiver = new BroadcastReceiver() {
        /* class com.android.server.gesture.OverviewProxyService.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.i(GestureNavConst.TAG_GESTURE_OPS, "Launcher state changed, intent=" + intent);
            OverviewProxyService.this.updateEnabledState();
            OverviewProxyService.this.startConnectionToCurrentUser();
        }
    };
    private IOverviewProxy mOverviewProxy;
    private final ServiceConnection mOverviewServiceConnection = new ServiceConnection() {
        /* class com.android.server.gesture.OverviewProxyService.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            OverviewProxyService.this.mHandler.removeCallbacks(OverviewProxyService.this.mDeferredConnectionCallback);
            OverviewProxyService.this.mConnectionBackoffAttempts = 0;
            OverviewProxyService.this.mOverviewProxy = IOverviewProxy.Stub.asInterface(service);
            OverviewProxyService overviewProxyService = OverviewProxyService.this;
            overviewProxyService.mIsGesNavProxyEnable = GestureUtils.isLauncherGesNavProxyEnable(overviewProxyService.mContext);
            Log.i(GestureNavConst.TAG_GESTURE_OPS, "Launcher service connected, mOverviewProxy=" + OverviewProxyService.this.mOverviewProxy);
            try {
                service.linkToDeath(OverviewProxyService.this.mOverviewServiceDeathRcpt, 0);
            } catch (RemoteException e) {
                Log.e(GestureNavConst.TAG_GESTURE_OPS, "Lost connection to launcher service");
            }
        }

        @Override // android.content.ServiceConnection
        public void onNullBinding(ComponentName name) {
            Log.w(GestureNavConst.TAG_GESTURE_OPS, "Null binding of '" + name + "', try reconnecting");
            OverviewProxyService.this.retryConnectionToCurrentUser();
        }

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName name) {
            Log.w(GestureNavConst.TAG_GESTURE_OPS, "Binding died of '" + name + "', try reconnecting");
            OverviewProxyService.this.retryConnectionToCurrentUser();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Log.i(GestureNavConst.TAG_GESTURE_OPS, "Launcher service disconnected, name=" + name);
            OverviewProxyService.this.mConnectionBackoffAttempts = 0;
        }
    };
    private final IBinder.DeathRecipient mOverviewServiceDeathRcpt = new IBinder.DeathRecipient() {
        /* class com.android.server.gesture.$$Lambda$KzfAgRm1G2y9SIm_xTbHiQsHLwM */

        @Override // android.os.IBinder.DeathRecipient
        public final void binderDied() {
            OverviewProxyService.this.cleanupAfterDeath();
        }
    };
    private final Intent mQuickStepIntent;
    private final ComponentName mRecentsComponentName;
    private final Runnable mRetryConnectionRunnable = new Runnable() {
        /* class com.android.server.gesture.$$Lambda$OverviewProxyService$hDBXgCr8bo2BlBpnQR3HfU_FY */

        @Override // java.lang.Runnable
        public final void run() {
            OverviewProxyService.m0lambda$hDBXgCr8bo2BlBpnQR3HfU_FY(OverviewProxyService.this);
        }
    };

    public /* synthetic */ void lambda$new$0$OverviewProxyService() {
        Log.w(GestureNavConst.TAG_GESTURE_OPS, "Binder supposed established connection but actual connection to service timed out, trying again");
        retryConnectionWithBackoff();
    }

    public OverviewProxyService(Context context, Looper looper) {
        this.mContext = context;
        this.mHandler = new Handler(looper);
        this.mRecentsComponentName = new ComponentName("com.huawei.android.launcher", GestureNavConst.DEFAULT_QUICKSTEP_CLASS);
        this.mQuickStepIntent = new Intent(ACTION_QUICKSTEP).setPackage(this.mRecentsComponentName.getPackageName());
        this.mDeviceStateController = DeviceStateController.getInstance(this.mContext);
        this.mIsGesNavProxyEnable = GestureUtils.isLauncherGesNavProxyEnable(this.mContext);
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
        ContextEx.registerReceiverAsUser(this.mContext, this.mLauncherStateChangedReceiver, UserHandleEx.ALL, filter, (String) null, this.mHandler);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.USER_UNLOCKED");
        ContextEx.registerReceiverAsUser(this.mContext, this.mBaseBroadcastReceiver, UserHandleEx.ALL, filter2, (String) null, this.mHandler);
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
        boolean isBound = false;
        try {
            isBound = ContextEx.bindServiceAsUser(this.mContext, launcherServiceIntent, this.mOverviewServiceConnection, 1, UserHandleEx.of(this.mDeviceStateController.getCurrentUser()));
        } catch (SecurityException e) {
            Log.e(GestureNavConst.TAG_GESTURE_OPS, "Unable to bind because of security error");
        } catch (Exception e2) {
            Log.e(GestureNavConst.TAG_GESTURE_OPS, "bind service fail");
        }
        Log.i(GestureNavConst.TAG_GESTURE_OPS, "internalConnectToCurrentUser, bound=" + isBound + ", launcherServiceIntent=" + launcherServiceIntent);
        if (isBound) {
            this.mHandler.postDelayed(this.mDeferredConnectionCallback, DEFERRED_CALLBACK_MILLIS);
        } else {
            retryConnectionWithBackoff();
        }
    }

    /* access modifiers changed from: private */
    public void retryConnectionWithBackoff() {
        if (!this.mHandler.hasCallbacks(this.mConnectionRunnable)) {
            long timeoutMs = (long) Math.min(Math.scalb(1000.0f, this.mConnectionBackoffAttempts), 600000.0f);
            this.mHandler.postDelayed(this.mConnectionRunnable, timeoutMs);
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

    public boolean isGesNavProxyEnable() {
        return this.mIsGesNavProxyEnable;
    }

    /* access modifiers changed from: private */
    public void disconnectFromLauncherService() {
        if (this.mOverviewProxy != null) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_OPS, "disconnectFromLauncherService start");
            }
            try {
                this.mOverviewProxy.asBinder().unlinkToDeath(this.mOverviewServiceDeathRcpt, 0);
            } catch (IllegalArgumentException e) {
                Log.e(GestureNavConst.TAG_GESTURE_OPS, "unlinkToDeath IllegalArgumentException, mIsEnabled=" + this.mIsEnabled);
            } catch (Exception e2) {
                Log.e(GestureNavConst.TAG_GESTURE_OPS, "unlinkToDeath fail, mIsEnabled=" + this.mIsEnabled);
            }
            boolean isUnbind = true;
            try {
                this.mContext.unbindService(this.mOverviewServiceConnection);
            } catch (IllegalArgumentException e3) {
                isUnbind = false;
                Log.e(GestureNavConst.TAG_GESTURE_OPS, "unbind service IllegalArgumentException");
            } catch (Exception e4) {
                isUnbind = false;
                Log.e(GestureNavConst.TAG_GESTURE_OPS, "unbind service fail");
            }
            this.mOverviewProxy = null;
            Log.i(GestureNavConst.TAG_GESTURE_OPS, "unbind service:" + isUnbind);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateEnabledState() {
        this.mIsEnabled = PackageManagerExt.resolveServiceAsUser(this.mContext.getPackageManager(), this.mQuickStepIntent, 786432, ActivityManagerEx.getCurrentUser()) != null;
        Log.i(GestureNavConst.TAG_GESTURE_OPS, "mIsEnabled=" + this.mIsEnabled);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateHomeWindow() {
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
