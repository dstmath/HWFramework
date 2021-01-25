package com.android.server.input;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

public class HwCustInputManagerServiceImpl extends HwCustInputManagerService {
    public static final String ACTION_USER_SWITCHED = "android.intent.action.USER_SWITCHED";
    private static final String DB_GLOVE_FILE_NODE = "glove_file_node";
    private static final int GLOVE_MODE_OFF = 0;
    private static final int GLOVE_MODE_ON = 1;
    protected static final boolean HWDBG = true;
    protected static final boolean HWFLOW = true;
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "HwCustInputManagerServiceImpl";
    private static final String TAG_FLOW = "HwCustInputManagerServiceImpl_FLOW";
    private static final String TAG_INIT = "HwCustInputManagerServiceImpl_INIT";
    private static final int TP_HAL_DEATH_COOKIE = 1000;
    Context mContext;
    private final Object mLock = new Object();
    private ITouchscreen mProxy = null;
    private UserSwitchingReceiver mReceiver;
    private final ServiceNotification mServiceNotification = new ServiceNotification();

    public HwCustInputManagerServiceImpl(Object object) {
        super(object);
        try {
            if (!IServiceManager.getService().registerForNotifications("vendor.huawei.hardware.tp@1.0::ITouchscreen", "", this.mServiceNotification)) {
                Log.e(TAG, "Failed to register service start notification");
            }
            connectToProxy();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register service start notification", e);
        }
    }

    /* access modifiers changed from: package-private */
    public final class DeathRecipient implements IHwBinder.DeathRecipient {
        DeathRecipient() {
        }

        public void serviceDied(long cookie) {
            if (cookie == 1000) {
                Log.e(HwCustInputManagerServiceImpl.TAG, "tp hal service died cookie: " + cookie);
                synchronized (HwCustInputManagerServiceImpl.this.mLock) {
                    HwCustInputManagerServiceImpl.this.mProxy = null;
                }
            }
        }
    }

    final class ServiceNotification extends IServiceNotification.Stub {
        ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean isPreexisting) {
            Log.e(HwCustInputManagerServiceImpl.TAG, "tp hal service started " + fqName + " " + name);
            HwCustInputManagerServiceImpl.this.connectToProxy();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectToProxy() {
        synchronized (this.mLock) {
            if (this.mProxy != null) {
                Log.i(TAG, "mProxy has registered, donnot regitster again");
                return;
            }
            try {
                this.mProxy = ITouchscreen.getService();
                if (this.mProxy != null) {
                    Log.d(TAG, "connectToProxy: mProxy get success.");
                    this.mProxy.linkToDeath(new DeathRecipient(), 1000);
                } else {
                    Log.d(TAG, "connectToProxy: mProxy get failed.");
                }
            } catch (NoSuchElementException e) {
                Log.e(TAG, "connectToProxy: tp hal service not found. Did the service fail to start?", e);
            } catch (RemoteException e2) {
                Log.e(TAG, "connectToProxy: tp hal service not responding", e2);
            }
        }
    }

    private void setGloveModeHal(boolean isEnable) {
        synchronized (this.mLock) {
            if (this.mProxy == null) {
                Log.e(TAG, "mProxy is null, return");
                return;
            }
            try {
                if (!this.mProxy.hwTsSetGloveMode(isEnable)) {
                    Log.d(TAG, "hwTsSetGloveMode error");
                } else {
                    Log.d(TAG, "hwTsSetGloveMode success");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to set glove mode:", e);
            }
        }
    }

    class UserSwitchingReceiver extends BroadcastReceiver {
        UserSwitchingReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(HwCustInputManagerServiceImpl.TAG, "Intent is null.");
            } else if (HwCustInputManagerServiceImpl.ACTION_USER_SWITCHED.equals(intent.getAction())) {
                Log.d(HwCustInputManagerServiceImpl.TAG, "receive ACTION_USER_SWITCHED");
                HwCustInputManagerServiceImpl.this.setGloveMode();
            }
        }
    }

    private class GloveModeObserver extends ContentObserver {
        GloveModeObserver() {
            super(null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            HwCustInputManagerServiceImpl.this.setGloveMode();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setGloveMode() {
        Context context = this.mContext;
        if (context != null) {
            int isGloveMode = Settings.System.getIntForUser(context.getContentResolver(), DB_GLOVE_FILE_NODE, GLOVE_MODE_OFF, ActivityManager.getCurrentUser());
            Log.d(TAG, "setGloveMode:" + isGloveMode);
            boolean z = true;
            if (isGloveMode != GLOVE_MODE_ON) {
                z = GLOVE_MODE_OFF;
            }
            setGloveModeHal(z);
        }
    }

    public int registerContentObserverForSetGloveMode(Context context) {
        Log.d(TAG, "registerContentObserverForSetGloveMode 1");
        this.mContext = context;
        Context context2 = this.mContext;
        if (context2 == null) {
            return GLOVE_MODE_ON;
        }
        context2.getContentResolver().registerContentObserver(Settings.System.getUriFor(DB_GLOVE_FILE_NODE), false, new GloveModeObserver(), -1);
        IntentFilter filter = new IntentFilter(ACTION_USER_SWITCHED);
        this.mReceiver = new UserSwitchingReceiver();
        this.mContext.registerReceiver(this.mReceiver, filter);
        return GLOVE_MODE_ON;
    }
}
