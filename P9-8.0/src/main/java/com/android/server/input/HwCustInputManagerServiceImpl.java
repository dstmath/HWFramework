package com.android.server.input;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification.Stub;
import android.os.RemoteException;
import android.provider.Settings.System;
import android.util.Log;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

public class HwCustInputManagerServiceImpl extends HwCustInputManagerService {
    public static final String ACTION_USER_SWITCHED = "android.intent.action.USER_SWITCHED";
    private static final int GLOVE_MODE_OFF = 0;
    private static final int GLOVE_MODE_ON = 1;
    protected static boolean HWDBG = HWLOGW_E;
    protected static boolean HWFLOW = HWLOGW_E;
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "HwCustInputManagerServiceImpl";
    private static final String TAG_FLOW = "HwCustInputManagerServiceImpl_FLOW";
    private static final String TAG_INIT = "HwCustInputManagerServiceImpl_INIT";
    private static final int TP_HAL_DEATH_COOKIE = 1000;
    private final String DB_GLOVE_FILE_NODE = "glove_file_node";
    Context mContext;
    private final Object mLock = new Object();
    private ITouchscreen mProxy = null;
    private UserSwitchingReceiver mReceiver;
    private final ServiceNotification mServiceNotification = new ServiceNotification();

    final class DeathRecipient implements android.os.IHwBinder.DeathRecipient {
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

    private class GloveModeObserver extends ContentObserver {
        public GloveModeObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            HwCustInputManagerServiceImpl.this.setGloveMode();
        }
    }

    final class ServiceNotification extends Stub {
        ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean preexisting) {
            Log.e(HwCustInputManagerServiceImpl.TAG, "tp hal service started " + fqName + " " + name);
            HwCustInputManagerServiceImpl.this.connectToProxy();
        }
    }

    class UserSwitchingReceiver extends BroadcastReceiver {
        UserSwitchingReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(HwCustInputManagerServiceImpl.TAG, "Intent is null.");
                return;
            }
            if (HwCustInputManagerServiceImpl.ACTION_USER_SWITCHED.equals(intent.getAction())) {
                Log.d(HwCustInputManagerServiceImpl.TAG, "receive ACTION_USER_SWITCHED");
                HwCustInputManagerServiceImpl.this.setGloveMode();
            }
        }
    }

    public HwCustInputManagerServiceImpl(Object obj) {
        super(obj);
        try {
            if (!IServiceManager.getService().registerForNotifications("vendor.huawei.hardware.tp@1.0::ITouchscreen", "", this.mServiceNotification)) {
                Log.e(TAG, "Failed to register service start notification");
            }
            connectToProxy();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register service start notification", e);
        }
    }

    private void connectToProxy() {
        synchronized (this.mLock) {
            if (this.mProxy != null) {
                Log.i(TAG, "mProxy has registered, donnot regitster again");
                return;
            }
            try {
                this.mProxy = ITouchscreen.getService();
                if (this.mProxy != null) {
                    if (HWDBG) {
                        Log.d(TAG, "connectToProxy: mProxy get success.");
                    }
                    this.mProxy.linkToDeath(new DeathRecipient(), 1000);
                } else if (HWDBG) {
                    Log.d(TAG, "connectToProxy: mProxy get failed.");
                }
            } catch (NoSuchElementException e) {
                Log.e(TAG, "connectToProxy: tp hal service not found. Did the service fail to start?", e);
            } catch (RemoteException e2) {
                Log.e(TAG, "connectToProxy: tp hal service not responding", e2);
            }
        }
    }

    private void setGloveModeHal(boolean enable) {
        synchronized (this.mLock) {
            if (this.mProxy == null) {
                Log.e(TAG, "mProxy is null, return");
                return;
            }
            try {
                if (this.mProxy.hwTsSetGloveMode(enable)) {
                    if (HWDBG) {
                        Log.d(TAG, "hwTsSetGloveMode success");
                    }
                } else if (HWDBG) {
                    Log.d(TAG, "hwTsSetGloveMode error");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to set glove mode:", e);
            }
        }
    }

    private void setGloveMode() {
        boolean z = HWLOGW_E;
        if (this.mContext != null) {
            int isGloveMode = System.getIntForUser(this.mContext.getContentResolver(), "glove_file_node", 0, ActivityManager.getCurrentUser());
            if (HWDBG) {
                Log.d(TAG, "setGloveMode:" + isGloveMode);
            }
            if (isGloveMode != 1) {
                z = false;
            }
            setGloveModeHal(z);
        }
    }

    public int registerContentObserverForSetGloveMode(Context context) {
        if (HWDBG) {
            Log.d(TAG, "registerContentObserverForSetGloveMode 1");
        }
        this.mContext = context;
        if (this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor("glove_file_node"), false, new GloveModeObserver(), -1);
            IntentFilter filter = new IntentFilter(ACTION_USER_SWITCHED);
            this.mReceiver = new UserSwitchingReceiver();
            this.mContext.registerReceiver(this.mReceiver, filter);
        }
        return 1;
    }
}
