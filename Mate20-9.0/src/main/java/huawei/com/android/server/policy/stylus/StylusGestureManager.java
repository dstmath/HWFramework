package huawei.com.android.server.policy.stylus;

import android.app.ActivityManager;
import android.app.SynchronousUserSwitchObserver;
import android.content.Context;
import android.database.ContentObserver;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

public class StylusGestureManager {
    private static final int FLAG_STYLUS = 1;
    private static final String KEY_STYLUS_ACTIVATE = "stylus_state_activate";
    private static final String KEY_STYLUS_STATE_ENABLE = "stylus_enable";
    private static final String KEY_STYLUS_STATE_INTRODUCE = "stylus_state_introduce";
    /* access modifiers changed from: private */
    public static int STYLUS_ACTIVATE_DISABLE = 0;
    private static int STYLUS_ACTIVATE_ENABLE = 1;
    private static final int STYLUS_DISABLE = 0;
    private static final int STYLUS_ENABLE = 1;
    /* access modifiers changed from: private */
    public static int STYLUS_INTRODUCED_NO = 0;
    private static final String STYLUS_TP_DISABLE = "0";
    private static final String STYLUS_TP_LOWFREQUENCY = "2";
    private static final String STYLUS_TP_NORMAL = "1";
    private static final String TAG = "StylusGestureManager";
    private static final int TP_HAL_DEATH_COOKIE = 1001;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public ITouchscreen mProxy = null;
    private final ServiceNotification mServiceNotification = new ServiceNotification();
    private ContentObserver mStylusActivateObserver;
    /* access modifiers changed from: private */
    public ContentObserver mStylusIntroduceObserver;
    /* access modifiers changed from: private */
    public int mStylusIntroduced = 0;
    /* access modifiers changed from: private */
    public ContentObserver mStylusObserver;
    /* access modifiers changed from: private */
    public int mStylusState = 0;

    final class DeathRecipient implements IHwBinder.DeathRecipient {
        DeathRecipient() {
        }

        public void serviceDied(long cookie) {
            if (cookie == 1001) {
                Log.d(StylusGestureManager.TAG, "tp hal service died cookie: " + cookie);
                synchronized (StylusGestureManager.this.mLock) {
                    ITouchscreen unused = StylusGestureManager.this.mProxy = null;
                }
            }
        }
    }

    final class ServiceNotification extends IServiceNotification.Stub {
        ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean preexisting) {
            Log.d(StylusGestureManager.TAG, "tp hal service started " + fqName + " " + name);
            StylusGestureManager.this.connectToProxy();
        }
    }

    public StylusGestureManager(Context context) {
        this.mContext = context;
        getTouchService();
        connectToProxy();
        initStylusStateObserver();
        initUserSwtichObserver();
        initStylusIntroducedObserver();
        initStylusActivateObserver();
    }

    private void initStylusStateObserver() {
        if (this.mContext == null) {
            Log.w(TAG, "mContext is null");
            return;
        }
        this.mStylusObserver = new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                int unused = StylusGestureManager.this.mStylusState = Settings.System.getIntForUser(StylusGestureManager.this.mContext.getContentResolver(), StylusGestureManager.KEY_STYLUS_STATE_ENABLE, 1, ActivityManager.getCurrentUser());
                Log.i(StylusGestureManager.TAG, "stylus_enable_state onChange: " + StylusGestureManager.this.mStylusState);
                int stylusActivated = Settings.Global.getInt(StylusGestureManager.this.mContext.getContentResolver(), StylusGestureManager.KEY_STYLUS_ACTIVATE, StylusGestureManager.STYLUS_ACTIVATE_DISABLE);
                Log.i(StylusGestureManager.TAG, "isStylusActivated: " + stylusActivated);
                StylusGestureManager.this.setStylusWakeupGestureToHal(StylusGestureManager.this.mStylusState, stylusActivated);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_STYLUS_STATE_ENABLE), false, this.mStylusObserver, -1);
        this.mStylusObserver.onChange(true);
    }

    private void initStylusIntroducedObserver() {
        if (this.mContext == null) {
            Log.w(TAG, "mContext is null");
            return;
        }
        this.mStylusIntroduceObserver = new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                int unused = StylusGestureManager.this.mStylusIntroduced = Settings.System.getIntForUser(StylusGestureManager.this.mContext.getContentResolver(), StylusGestureManager.KEY_STYLUS_STATE_INTRODUCE, StylusGestureManager.STYLUS_INTRODUCED_NO, ActivityManager.getCurrentUser());
                Log.i(StylusGestureManager.TAG, "stylus_state_introduce onChange: " + StylusGestureManager.this.mStylusIntroduced);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_STYLUS_STATE_INTRODUCE), false, this.mStylusIntroduceObserver, -1);
        this.mStylusIntroduceObserver.onChange(true);
    }

    private void initStylusActivateObserver() {
        if (this.mContext == null) {
            Log.w(TAG, "mContext is null");
            return;
        }
        this.mStylusActivateObserver = new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                int stylusState = Settings.System.getIntForUser(StylusGestureManager.this.mContext.getContentResolver(), StylusGestureManager.KEY_STYLUS_STATE_ENABLE, 1, ActivityManager.getCurrentUser());
                Log.i(StylusGestureManager.TAG, "get stylus state : " + stylusState);
                int stylusActivated = Settings.Global.getInt(StylusGestureManager.this.mContext.getContentResolver(), StylusGestureManager.KEY_STYLUS_ACTIVATE, StylusGestureManager.STYLUS_ACTIVATE_DISABLE);
                Log.i(StylusGestureManager.TAG, "stylus_state_activate onChange: " + stylusActivated);
                StylusGestureManager.this.setStylusWakeupGestureToHal(stylusState, stylusActivated);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(KEY_STYLUS_ACTIVATE), false, this.mStylusActivateObserver);
    }

    private void initUserSwtichObserver() {
        try {
            ActivityManager.getService().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                public void onUserSwitching(int newUserId) {
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                    Log.i(StylusGestureManager.TAG, "onUserSwitchComplete: " + newUserId);
                    if (StylusGestureManager.this.mStylusObserver != null) {
                        StylusGestureManager.this.mStylusObserver.onChange(true);
                    }
                    if (StylusGestureManager.this.mStylusIntroduceObserver != null) {
                        StylusGestureManager.this.mStylusIntroduceObserver.onChange(true);
                    }
                }
            }, TAG);
        } catch (RemoteException e) {
            Log.e(TAG, "registerUserSwitchObserver fail", e);
        } catch (Exception e2) {
            Log.w(TAG, "registerReceiverAsUser fail ", e2);
        }
    }

    public boolean isStylusEnabled() {
        return this.mStylusState == 1;
    }

    public boolean isStylusActivate() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), KEY_STYLUS_ACTIVATE, STYLUS_ACTIVATE_DISABLE) == STYLUS_ACTIVATE_ENABLE;
    }

    public boolean isStylusIntroduced() {
        boolean z = true;
        if (this.mStylusIntroduced != STYLUS_INTRODUCED_NO) {
            return true;
        }
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            z = false;
        }
        return z;
    }

    private void getTouchService() {
        try {
            if (!IServiceManager.getService().registerForNotifications("vendor.huawei.hardware.tp@1.0::ITouchscreen", "", this.mServiceNotification)) {
                Log.e(TAG, "Failed to register service start notification");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register service start notification", e);
        }
    }

    /* access modifiers changed from: private */
    public void connectToProxy() {
        synchronized (this.mLock) {
            if (this.mProxy != null) {
                Log.i(TAG, "mProxy has registered, do not register again");
                return;
            }
            try {
                this.mProxy = ITouchscreen.getService();
                if (this.mProxy != null) {
                    Log.d(TAG, "connectToProxy: mProxy get success.");
                    this.mProxy.linkToDeath(new DeathRecipient(), 1001);
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

    /* access modifiers changed from: private */
    public void setStylusWakeupGestureToHal(int status, int stylusActivated) {
        String tpStatus;
        synchronized (this.mLock) {
            if (this.mProxy == null) {
                Log.d(TAG, "mProxy is null, return");
                return;
            }
            if (status == 0) {
                tpStatus = "0";
            } else if (stylusActivated == STYLUS_ACTIVATE_DISABLE) {
                tpStatus = "2";
            } else {
                tpStatus = "1";
            }
            Log.i(TAG, "setStylusWakeupGestureToHal: " + tpStatus);
            try {
                if (this.mProxy.hwSetFeatureConfig(1, tpStatus) == 0) {
                    Log.d(TAG, "setStylusWakeupGestureToHal success");
                } else {
                    Log.d(TAG, "setStylusWakeupGestureToHal error");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to set stylus mode:", e);
            }
        }
    }
}
