package huawei.com.android.server.policy.stylus;

import android.content.Context;
import android.database.ContentObserver;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.UserSwitchObserverEx;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.hidl.IServiceManagerHidlAdapter;
import com.huawei.android.hidl.IServiceNotificationHidlAdapter;
import com.huawei.android.hidl.ITouchscreenHidlAdapter;
import com.huawei.android.os.HwBinderEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.hwpartstylusgestureopt.BuildConfig;
import java.util.NoSuchElementException;

public class StylusGestureManager {
    private static final int FLAG_STYLUS = 1;
    private static final String KEY_STYLUS_ACTIVATE = "stylus_state_activate";
    private static final String KEY_STYLUS_STATE_ENABLE = "stylus_enable";
    private static final String KEY_STYLUS_STATE_INTRODUCE = "stylus_state_introduce";
    private static final int MPEN_DFLT_VALUE = (SystemPropertiesEx.getBoolean("ro.mpen.default.support", true) ? 1 : 0);
    private static final int STYLUS_ACTIVATE_DISABLE = 0;
    private static final int STYLUS_ACTIVATE_ENABLE = 1;
    private static final int STYLUS_DISABLE = 0;
    private static final int STYLUS_ENABLE = 1;
    private static final int STYLUS_INTRODUCED_NO = 0;
    private static final String STYLUS_TP_DISABLE = "0";
    private static final String STYLUS_TP_LOWFREQUENCY = "2";
    private static final String STYLUS_TP_NORMAL = "1";
    private static final String TAG = "StylusGestureManager";
    private static final int TP_HAL_DEATH_COOKIE = 1001;
    private Context mContext;
    private final Object mLock = new Object();
    private ITouchscreenHidlAdapter mProxy = null;
    private final ServiceNotification mServiceNotification = new ServiceNotification();
    private ContentObserver mStylusActivateObserver;
    private ContentObserver mStylusIntroduceObserver;
    private int mStylusIntroduced = 0;
    private ContentObserver mStylusObserver;
    private int mStylusState = 0;

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
            Log.w(TAG, "initStylusStateObserver mContext is null");
            return;
        }
        this.mStylusObserver = new ContentObserver(null) {
            /* class huawei.com.android.server.policy.stylus.StylusGestureManager.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                StylusGestureManager stylusGestureManager = StylusGestureManager.this;
                stylusGestureManager.mStylusState = SettingsEx.System.getIntForUser(stylusGestureManager.mContext.getContentResolver(), StylusGestureManager.KEY_STYLUS_STATE_ENABLE, StylusGestureManager.MPEN_DFLT_VALUE, ActivityManagerEx.getCurrentUser());
                Log.i(StylusGestureManager.TAG, "stylus_enable_state onChange: " + StylusGestureManager.this.mStylusState);
                int stylusActivated = Settings.Global.getInt(StylusGestureManager.this.mContext.getContentResolver(), StylusGestureManager.KEY_STYLUS_ACTIVATE, 0);
                Log.i(StylusGestureManager.TAG, "isStylusActivated: " + stylusActivated);
                StylusGestureManager stylusGestureManager2 = StylusGestureManager.this;
                stylusGestureManager2.setStylusWakeupGestureToHal(stylusGestureManager2.mStylusState, stylusActivated);
            }
        };
        ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.System.getUriFor(KEY_STYLUS_STATE_ENABLE), false, this.mStylusObserver, -1);
        this.mStylusObserver.onChange(true);
    }

    private void initStylusIntroducedObserver() {
        if (this.mContext == null) {
            Log.w(TAG, "initStylusIntroducedObserver mContext is null");
            return;
        }
        this.mStylusIntroduceObserver = new ContentObserver(null) {
            /* class huawei.com.android.server.policy.stylus.StylusGestureManager.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                StylusGestureManager stylusGestureManager = StylusGestureManager.this;
                stylusGestureManager.mStylusIntroduced = SettingsEx.System.getIntForUser(stylusGestureManager.mContext.getContentResolver(), StylusGestureManager.KEY_STYLUS_STATE_INTRODUCE, 0, ActivityManagerEx.getCurrentUser());
                Log.i(StylusGestureManager.TAG, "stylus_state_introduce onChange: " + StylusGestureManager.this.mStylusIntroduced);
            }
        };
        ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.System.getUriFor(KEY_STYLUS_STATE_INTRODUCE), false, this.mStylusIntroduceObserver, -1);
        this.mStylusIntroduceObserver.onChange(true);
    }

    private void initStylusActivateObserver() {
        if (this.mContext == null) {
            Log.w(TAG, "initStylusActivateObserver mContext is null");
            return;
        }
        this.mStylusActivateObserver = new ContentObserver(null) {
            /* class huawei.com.android.server.policy.stylus.StylusGestureManager.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                int stylusState = SettingsEx.System.getIntForUser(StylusGestureManager.this.mContext.getContentResolver(), StylusGestureManager.KEY_STYLUS_STATE_ENABLE, StylusGestureManager.MPEN_DFLT_VALUE, ActivityManagerEx.getCurrentUser());
                Log.i(StylusGestureManager.TAG, "get stylus state : " + stylusState);
                int stylusActivated = Settings.Global.getInt(StylusGestureManager.this.mContext.getContentResolver(), StylusGestureManager.KEY_STYLUS_ACTIVATE, 0);
                Log.i(StylusGestureManager.TAG, "stylus_state_activate onChange: " + stylusActivated);
                StylusGestureManager.this.setStylusWakeupGestureToHal(stylusState, stylusActivated);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(KEY_STYLUS_ACTIVATE), false, this.mStylusActivateObserver);
    }

    private void initUserSwtichObserver() {
        try {
            ActivityManagerEx.registerUserSwitchObserver(new UserSwitchObserverEx() {
                /* class huawei.com.android.server.policy.stylus.StylusGestureManager.AnonymousClass4 */

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
            Log.e(TAG, "registerUserSwitchObserver fail" + e.getMessage());
        } catch (Exception e2) {
            Log.e(TAG, "registerReceiverAsUser fail.");
        }
    }

    public boolean isStylusEnabled() {
        return this.mStylusState == 1;
    }

    public boolean isStylusActivate() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), KEY_STYLUS_ACTIVATE, 0) == 1;
    }

    public boolean isStylusIntroduced() {
        if (this.mStylusIntroduced != 0 || Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0) {
            return true;
        }
        return false;
    }

    private void getTouchService() {
        try {
            if (!IServiceManagerHidlAdapter.getService().registerForNotifications("vendor.huawei.hardware.tp@1.0::ITouchscreen", BuildConfig.FLAVOR, this.mServiceNotification)) {
                Log.e(TAG, "Failed to register service start notification 1");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register service start notification");
        }
    }

    /* access modifiers changed from: package-private */
    public final class DeathRecipient extends HwBinderEx.DeathRecipientEx {
        DeathRecipient() {
        }

        public void serviceDied(long cookie) {
            if (cookie == 1001) {
                Log.d(StylusGestureManager.TAG, "tp hal service died cookie: " + cookie);
                synchronized (StylusGestureManager.this.mLock) {
                    StylusGestureManager.this.mProxy = null;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class ServiceNotification extends IServiceNotificationHidlAdapter {
        ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean isPreexisting) {
            Log.d(StylusGestureManager.TAG, "tp hal service started " + fqName + " " + name);
            StylusGestureManager.this.connectToProxy();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectToProxy() {
        synchronized (this.mLock) {
            if (this.mProxy != null) {
                Log.i(TAG, "mProxy has registered, do not register again");
                return;
            }
            try {
                this.mProxy = ITouchscreenHidlAdapter.getService();
                if (this.mProxy != null) {
                    Log.d(TAG, "connectToProxy: mProxy get success.");
                    this.mProxy.linkToDeath(new DeathRecipient(), (int) TP_HAL_DEATH_COOKIE);
                } else {
                    Log.d(TAG, "connectToProxy: mProxy get failed.");
                }
            } catch (NoSuchElementException e) {
                Log.e(TAG, "connectToProxy: tp hal service not found. Did the service fail to start?" + e.getMessage());
            } catch (RemoteException e2) {
                Log.e(TAG, "connectToProxy: tp hal service not responding" + e2.getMessage());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setStylusWakeupGestureToHal(int status, int stylusActivated) {
        synchronized (this.mLock) {
            if (this.mProxy == null) {
                Log.d(TAG, "mProxy is null, return");
                return;
            }
            String tpStatus = status == 0 ? STYLUS_TP_DISABLE : stylusActivated == 0 ? STYLUS_TP_LOWFREQUENCY : STYLUS_TP_NORMAL;
            Log.i(TAG, "setStylusWakeupGestureToHal: " + tpStatus);
            try {
                if (this.mProxy.hwSetFeatureConfig(1, tpStatus) == 0) {
                    Log.d(TAG, "setStylusWakeupGestureToHal success");
                } else {
                    Log.d(TAG, "setStylusWakeupGestureToHal error");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to set stylus mode:" + e.getMessage());
            }
        }
    }
}
