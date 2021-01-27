package com.huawei.wifi2;

import android.content.Context;
import android.hardware.wifi.supplicant.V1_0.ISupplicant;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.hardware.wifi.supplicant.V1_1.ISupplicant;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiSsid;
import android.os.Handler;
import android.os.HidlSupport;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Pair;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.huawei.wifi2.HwWifi2Native;
import com.huawei.wifi2.HwWifi2SupplicantStaIfaceHal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback;

public class HwWifi2SupplicantStaIfaceHal {
    private static final String HAL_INSTANCE_NAME = "default";
    private static final int INVALID = -1;
    private static final int NOTIFY_TYPE_WPA3_FAIL_EVENT = 3;
    private static final String TAG = "HwWifi2SupplicantStaIfaceHal";
    private Context mContext;
    private HashMap<String, WifiConfiguration> mCurrentNetworkLocalConfigs = new HashMap<>();
    private HashMap<String, HwWifi2SupplicantStaNetworkHal> mCurrentNetworkRemoteHandles = new HashMap<>();
    private HwWifi2Native.SupplicantDeathEventHandler mDeathEventHandler = null;
    private long mDeathRecipientCookie = 0;
    private Handler mEventHandler;
    private final Object mLock = new Object();
    private IServiceManager mServiceManager = null;
    private ServiceManagerDeathRecipient mServiceManagerDeathRecipient;
    private final IServiceNotification mServiceNotificationCallback = new IServiceNotification.Stub() {
        /* class com.huawei.wifi2.HwWifi2SupplicantStaIfaceHal.AnonymousClass1 */

        public void onRegistration(String fqName, String name, boolean isPreexisting) {
            synchronized (HwWifi2SupplicantStaIfaceHal.this.mLock) {
                HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "IServiceNotification.onRegistration for: %{public}s, %{public}s, preexisting=%{public}b", new Object[]{fqName, name, Boolean.valueOf(isPreexisting)});
                if (!HwWifi2SupplicantStaIfaceHal.this.initSupplicantService()) {
                    HwHiLog.w(HwWifi2SupplicantStaIfaceHal.TAG, false, "initalizing ISupplicant failed.", new Object[0]);
                    HwWifi2SupplicantStaIfaceHal.this.supplicantServiceDiedHandler(HwWifi2SupplicantStaIfaceHal.this.mDeathRecipientCookie);
                } else {
                    HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "Completed initialization of ISupplicant.", new Object[0]);
                }
            }
        }
    };
    private ISupplicant mSupplicant;
    private SupplicantDeathRecipient mSupplicantDeathRecipient;
    private HashMap<String, ISupplicantStaIfaceCallback> mSupplicantStaIfaceCallbacks = new HashMap<>();
    private HashMap<String, ISupplicantStaIface> mSupplicantStaIfaces = new HashMap<>();
    private HwWifi2Monitor mWifi2Monitor = null;

    public HwWifi2SupplicantStaIfaceHal(Context context, HwWifi2Monitor monitor, Looper looper) {
        this.mContext = context;
        this.mWifi2Monitor = monitor;
        this.mEventHandler = new Handler(looper);
        this.mServiceManagerDeathRecipient = new ServiceManagerDeathRecipient();
        this.mSupplicantDeathRecipient = new SupplicantDeathRecipient();
    }

    public class ServiceManagerDeathRecipient implements IHwBinder.DeathRecipient {
        private ServiceManagerDeathRecipient() {
            HwWifi2SupplicantStaIfaceHal.this = r1;
        }

        public void serviceDied(long cookie) {
            HwWifi2SupplicantStaIfaceHal.this.mEventHandler.post(new Runnable(cookie) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2SupplicantStaIfaceHal$ServiceManagerDeathRecipient$XNrJRf2CHGVGW5cahsOJcKomA */
                private final /* synthetic */ long f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwWifi2SupplicantStaIfaceHal.ServiceManagerDeathRecipient.this.lambda$serviceDied$0$HwWifi2SupplicantStaIfaceHal$ServiceManagerDeathRecipient(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$serviceDied$0$HwWifi2SupplicantStaIfaceHal$ServiceManagerDeathRecipient(long cookie) {
            synchronized (HwWifi2SupplicantStaIfaceHal.this.mLock) {
                HwHiLog.w(HwWifi2SupplicantStaIfaceHal.TAG, false, "IServiceManager died: cookie=%{public}s", new Object[]{String.valueOf(cookie)});
                HwWifi2SupplicantStaIfaceHal.this.supplicantServiceDiedHandler(HwWifi2SupplicantStaIfaceHal.this.mDeathRecipientCookie);
                HwWifi2SupplicantStaIfaceHal.this.mServiceManager = null;
            }
        }
    }

    public class SupplicantDeathRecipient implements IHwBinder.DeathRecipient {
        private SupplicantDeathRecipient() {
            HwWifi2SupplicantStaIfaceHal.this = r1;
        }

        public void serviceDied(long cookie) {
            HwWifi2SupplicantStaIfaceHal.this.mEventHandler.post(new Runnable(cookie) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2SupplicantStaIfaceHal$SupplicantDeathRecipient$6YsGm8EnJ7WWI21YOuDNdpeG8k */
                private final /* synthetic */ long f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwWifi2SupplicantStaIfaceHal.SupplicantDeathRecipient.this.lambda$serviceDied$0$HwWifi2SupplicantStaIfaceHal$SupplicantDeathRecipient(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$serviceDied$0$HwWifi2SupplicantStaIfaceHal$SupplicantDeathRecipient(long cookie) {
            synchronized (HwWifi2SupplicantStaIfaceHal.this.mLock) {
                HwHiLog.w(HwWifi2SupplicantStaIfaceHal.TAG, false, "ISupplicant died: cookie=%{public}s", new Object[]{String.valueOf(cookie)});
                HwWifi2SupplicantStaIfaceHal.this.supplicantServiceDiedHandler(cookie);
            }
        }
    }

    public class SupplicantStaIfaceHalCallbackV3 extends ISupplicantStaIfaceCallback.Stub {
        private String mIfaceName;
        private boolean mIsAssociated = false;
        private boolean mIsFourway = false;

        SupplicantStaIfaceHalCallbackV3(String ifaceName) {
            HwWifi2SupplicantStaIfaceHal.this = r1;
            this.mIfaceName = ifaceName;
        }

        public void onNetworkAdded(int id) {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onNetworkAdded", new Object[0]);
        }

        public void onNetworkRemoved(int id) {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onNetworkRemoved", new Object[0]);
            synchronized (HwWifi2SupplicantStaIfaceHal.this.mLock) {
                this.mIsFourway = false;
            }
        }

        public void onStateChanged(int newState, byte[] bssid, int id, ArrayList<Byte> ssid) {
            synchronized (HwWifi2SupplicantStaIfaceHal.this.mLock) {
                SupplicantState newSupplicantState = HwWifi2SupplicantStaIfaceHal.supplicantHidlStateToFrameworkState(newState);
                HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onStateChanged newState is " + newSupplicantState.name(), new Object[0]);
                String bssidStr = NativeUtil.macAddressFromByteArray(bssid);
                boolean z = true;
                this.mIsFourway = newState == 7;
                if (newState != 6) {
                    z = false;
                }
                this.mIsAssociated = z;
                if (newSupplicantState == SupplicantState.COMPLETED) {
                    HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastNetworkConnectionEvent(this.mIfaceName, HwWifi2SupplicantStaIfaceHal.this.getCurrentNetworkId(this.mIfaceName), bssidStr);
                }
                HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastSupplicantStateChangeEvent(this.mIfaceName, HwWifi2SupplicantStaIfaceHal.this.getCurrentNetworkId(this.mIfaceName), WifiSsid.createFromByteArray(NativeUtil.byteArrayFromArrayList(ssid)), bssidStr, newSupplicantState);
            }
        }

        public void onAnqpQueryDone(byte[] bssid, ISupplicantStaIfaceCallback.AnqpData data, ISupplicantStaIfaceCallback.Hs20AnqpData hs20Data) {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onAnqpQueryDone", new Object[0]);
        }

        public void onHs20IconQueryDone(byte[] bssid, String fileName, ArrayList<Byte> arrayList) {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onHs20IconQueryDone", new Object[0]);
        }

        public void onHs20SubscriptionRemediation(byte[] bssid, byte osuMethod, String url) {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onHs20SubscriptionRemediation", new Object[0]);
        }

        public void onHs20DeauthImminentNotice(byte[] bssid, int reasonCode, int reAuthDelayInSec, String url) {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onHs20DeauthImminentNotice", new Object[0]);
        }

        public void onDisconnected(byte[] bssid, boolean isLocalGenerated, int reasonCode) {
            int i = 0;
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "onDisconnected 4way=%{public}b isLocalGenerated=%{public}b reasonCode=%{public}d", new Object[]{Boolean.valueOf(this.mIsFourway), Boolean.valueOf(isLocalGenerated), Integer.valueOf(reasonCode)});
            synchronized (HwWifi2SupplicantStaIfaceHal.this.mLock) {
                if (this.mIsFourway) {
                    if (!isLocalGenerated || reasonCode != 17) {
                        HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 2, -1);
                    }
                    if (isLocalGenerated && reasonCode == 17) {
                        HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "local generated IE_IN_4WAY_DIFFERS broadcast authentication failure event", new Object[0]);
                        HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 17, -1);
                    }
                }
                WifiConfiguration currentConfig = HwWifi2SupplicantStaIfaceHal.this.getCurrentNetworkLocalConfig(this.mIfaceName);
                if (this.mIsAssociated && currentConfig != null && !currentConfig.isOpenNetwork()) {
                    HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "For encrypted networks, connect auth failure when in associated", new Object[0]);
                    HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 3, -1);
                }
                HwWifi2Monitor hwWifi2Monitor = HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor;
                String str = this.mIfaceName;
                if (isLocalGenerated) {
                    i = 1;
                }
                hwWifi2Monitor.broadcastNetworkDisconnectionEvent(str, i, reasonCode, NativeUtil.macAddressFromByteArray(bssid));
            }
        }

        public void onAssociationRejected(byte[] bssid, int statusCode, boolean isTimedOut) {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onAssociationRejected", new Object[0]);
            synchronized (HwWifi2SupplicantStaIfaceHal.this.mLock) {
                if (statusCode == 1) {
                    WifiConfiguration curConfiguration = HwWifi2SupplicantStaIfaceHal.this.getCurrentNetworkLocalConfig(this.mIfaceName);
                    if (curConfiguration != null && curConfiguration.allowedKeyManagement.get(8)) {
                        HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 2, -1);
                        return;
                    }
                }
                HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastAssociationRejectionEvent(this.mIfaceName, statusCode, isTimedOut, NativeUtil.macAddressFromByteArray(bssid));
            }
        }

        public void onAuthenticationTimeout(byte[] bssid) {
            HwHiLog.w(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onAuthenticationTimeout", new Object[0]);
            synchronized (HwWifi2SupplicantStaIfaceHal.this.mLock) {
                HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 1, -1);
            }
        }

        public void onBssidChanged(byte reason, byte[] bssid) {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onBssidChanged", new Object[0]);
            synchronized (HwWifi2SupplicantStaIfaceHal.this.mLock) {
                if (reason == 0) {
                    try {
                        HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastTargetBssidEvent(this.mIfaceName, NativeUtil.macAddressFromByteArray(bssid));
                    } catch (Throwable th) {
                        throw th;
                    }
                } else if (reason == 1) {
                    HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastAssociatedBssidEvent(this.mIfaceName, NativeUtil.macAddressFromByteArray(bssid));
                } else {
                    HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "unknown bssid change reason", new Object[0]);
                }
            }
        }

        public void onEapFailure() {
            HwHiLog.w(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onEapFailure", new Object[0]);
            synchronized (HwWifi2SupplicantStaIfaceHal.this.mLock) {
                HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 3, -1);
            }
        }

        public void onEapFailure_1_1(int code) {
            HwHiLog.w(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onEapFailure_1_1", new Object[0]);
            synchronized (HwWifi2SupplicantStaIfaceHal.this.mLock) {
                HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 3, code);
            }
        }

        public void onWpsEventSuccess() {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onWpsEventSuccess", new Object[0]);
        }

        public void onWpsEventFail(byte[] bssid, short configError, short errorInd) {
            HwHiLog.w(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onWpsEventFail", new Object[0]);
        }

        public void onWpsEventPbcOverlap() {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onWpsEventPbcOverlap", new Object[0]);
        }

        public void onExtRadioWorkStart(int id) {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onExtRadioWorkStart", new Object[0]);
        }

        public void onExtRadioWorkTimeout(int id) {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onExtRadioWorkTimeout", new Object[0]);
        }

        public void onDppSuccessConfigReceived(ArrayList<Byte> arrayList, String password, byte[] psk, int securityAkm) {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onDppSuccessConfigReceived", new Object[0]);
        }

        public void onDppSuccessConfigSent() {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onDppSuccessConfigSent", new Object[0]);
        }

        public void onDppProgress(int code) {
            HwHiLog.i(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onDppProgress", new Object[0]);
        }

        public void onDppFailure(int code) {
            HwHiLog.w(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onDppFailure", new Object[0]);
        }

        public void onWapiCertInitFail() {
            HwHiLog.w(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onWapiCertInitFail", new Object[0]);
        }

        public void onWapiAuthFail() {
            HwHiLog.w(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onWapiAuthFail", new Object[0]);
        }

        public void onVoWifiIrqStr() {
            HwHiLog.d(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onVoWifiIrqStr", new Object[0]);
            HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastVoWifiIrqStrEvent(this.mIfaceName);
        }

        public void notifyStaIfaceEvent(int notifyType, String eventInfo) {
            if (eventInfo == null) {
                HwHiLog.e(HwWifi2SupplicantStaIfaceHal.TAG, false, "eventInfo is null, notifyType=%{public}d", new Object[]{Integer.valueOf(notifyType)});
                return;
            }
            HwHiLog.d(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: notifyStaIfaceEvent notifyType=%{public}d, event=%{private}s", new Object[]{Integer.valueOf(notifyType), eventInfo});
            if (notifyType == 3) {
                HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastWpa3ConnectFailEvent(this.mIfaceName, eventInfo);
            }
        }

        public void onHilinkStartWps(String arg) {
            HwHiLog.d(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onHilinkStartWps", new Object[0]);
            HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastHilinkStartWpsEvent(this.mIfaceName, arg);
        }

        public void onHilinkStartWps() {
            HwHiLog.d(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onHilinkStartWps", new Object[0]);
        }

        public void onAbsAntCoreRob() {
            HwHiLog.d(HwWifi2SupplicantStaIfaceHal.TAG, false, "SupplicantStaIfaceHalCallbackV3: onAbsAntCoreRob", new Object[0]);
            HwWifi2SupplicantStaIfaceHal.this.mWifi2Monitor.broadcastAbsAntCoreRobEvent(this.mIfaceName);
        }
    }

    public boolean isInitializationStarted() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mServiceManager != null;
        }
        return z;
    }

    public boolean isInitializationComplete() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mSupplicant != null;
        }
        return z;
    }

    public boolean isStaIfacesEmpty() {
        return this.mSupplicantStaIfaces.isEmpty();
    }

    public boolean registerDeathHandler(HwWifi2Native.SupplicantDeathEventHandler handler) {
        synchronized (this.mLock) {
            if (this.mDeathEventHandler != null) {
                HwHiLog.i(TAG, false, "Death handler already present", new Object[0]);
            }
            this.mDeathEventHandler = handler;
        }
        return true;
    }

    public boolean deregisterDeathHandler() {
        if (this.mDeathEventHandler == null) {
            HwHiLog.e(TAG, false, "No Death handler present", new Object[0]);
        }
        this.mDeathEventHandler = null;
        return true;
    }

    public boolean initialize() {
        synchronized (this.mLock) {
            HwHiLog.i(TAG, false, "initialize enter", new Object[0]);
            this.mSupplicant = null;
            if (this.mServiceManager != null) {
                return true;
            }
            try {
                this.mServiceManager = getServiceManagerMockable();
                if (this.mServiceManager == null) {
                    HwHiLog.e(TAG, false, "Failed to get HIDL Service Manager", new Object[0]);
                    return false;
                } else if (!linkToServiceManagerDeath()) {
                    return false;
                } else {
                    if (!this.mServiceManager.registerForNotifications("android.hardware.wifi.supplicant@1.0::ISupplicant", "", this.mServiceNotificationCallback)) {
                        HwHiLog.e(TAG, false, "Failed to register for notifications to %{public}s", new Object[]{"android.hardware.wifi.supplicant@1.0::ISupplicant"});
                        this.mServiceManager = null;
                        return false;
                    }
                    return true;
                }
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "Exception while trying to register listener for ISupplicant service", new Object[0]);
                supplicantServiceDiedHandler(this.mDeathRecipientCookie);
            }
        }
    }

    public boolean setupIface(String ifaceName) {
        if (checkSupplicantStaIfaceAndLogFailure(ifaceName, "setupIface") != null) {
            return false;
        }
        if (!isV1Point1()) {
            HwHiLog.e(TAG, false, "just support v1.1", new Object[0]);
            return false;
        }
        ISupplicantIface ifaceHwBinder = addIfaceV1Point1(ifaceName);
        HwHiLog.d(TAG, false, "setupIface iface: %{public}s", new Object[]{ifaceHwBinder});
        if (ifaceHwBinder == null) {
            HwHiLog.e(TAG, false, "setupIface got null iface", new Object[0]);
            return false;
        } else if (!isV1Point2()) {
            HwHiLog.e(TAG, false, "just support v1.2", new Object[0]);
            return false;
        } else if (!isV3Point0()) {
            HwHiLog.e(TAG, false, "just support v3.0", new Object[0]);
            return false;
        } else {
            vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIface iface = getVendorStaIfaceV3Point0(ifaceHwBinder);
            SupplicantStaIfaceHalCallbackV3 callback = new SupplicantStaIfaceHalCallbackV3(ifaceName);
            if (!hwStaRegisterCallback(iface, callback)) {
                return false;
            }
            this.mSupplicantStaIfaces.put(ifaceName, iface);
            this.mSupplicantStaIfaceCallbacks.put(ifaceName, callback);
            HwHiLog.d(TAG, false, "setupIface success: %{public}s", new Object[]{ifaceName});
            return true;
        }
    }

    public boolean connectToNetwork(String ifaceName, WifiConfiguration config) {
        synchronized (this.mLock) {
            HwHiLog.i(TAG, false, "connectToNetwork %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
            WifiConfiguration currentConfig = getCurrentNetworkLocalConfig(ifaceName);
            if (!WifiConfigurationUtil.isSameNetwork(config, currentConfig)) {
                this.mCurrentNetworkRemoteHandles.remove(ifaceName);
                this.mCurrentNetworkLocalConfigs.remove(ifaceName);
                if (!removeAllNetworks(ifaceName)) {
                    HwHiLog.e(TAG, false, "Failed to remove existing networks", new Object[0]);
                    return false;
                }
                Pair<HwWifi2SupplicantStaNetworkHal, WifiConfiguration> pair = addNetworkAndSaveConfig(ifaceName, config);
                if (pair == null) {
                    HwHiLog.e(TAG, false, "Failed to add/save network configuration: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
                    return false;
                }
                this.mCurrentNetworkRemoteHandles.put(ifaceName, (HwWifi2SupplicantStaNetworkHal) pair.first);
                this.mCurrentNetworkLocalConfigs.put(ifaceName, (WifiConfiguration) pair.second);
            } else if (Objects.equals(config.getNetworkSelectionStatus().getNetworkSelectionBSSID(), currentConfig.getNetworkSelectionStatus().getNetworkSelectionBSSID())) {
                HwHiLog.d(TAG, false, "Network is already saved, will not trigger remove and add operation.", new Object[0]);
            } else {
                HwHiLog.i(TAG, false, "Network is already saved, but need to update BSSID.", new Object[0]);
                if (!setCurrentNetworkBssid(ifaceName, config.getNetworkSelectionStatus().getNetworkSelectionBSSID())) {
                    HwHiLog.e(TAG, false, "Failed to set current network BSSID.", new Object[0]);
                    return false;
                }
                this.mCurrentNetworkLocalConfigs.put(ifaceName, new WifiConfiguration(config));
            }
            HwWifi2SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "connectToNetwork");
            if (networkHandle != null) {
                if (networkHandle.select()) {
                    return true;
                }
            }
            HwHiLog.e(TAG, false, "Failed to select network configuration: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
            return false;
        }
    }

    public boolean teardownIface(String ifaceName) {
        HwHiLog.e(TAG, false, "teardownIface: %{public}s", new Object[]{ifaceName});
        synchronized (this.mLock) {
            if (!isV1Point1()) {
                HwHiLog.e(TAG, false, "support v1.1", new Object[0]);
                return false;
            } else if (!removeIfaceV1Point1(ifaceName)) {
                HwHiLog.e(TAG, false, "Failed to remove iface = %{public}s", new Object[]{ifaceName});
                return false;
            } else if (this.mSupplicantStaIfaces.remove(ifaceName) == null) {
                HwHiLog.e(TAG, false, "Trying to teardown unknown inteface", new Object[0]);
                return false;
            } else {
                this.mSupplicantStaIfaceCallbacks.remove(ifaceName);
                return true;
            }
        }
    }

    public boolean setCurrentNetworkBssid(String ifaceName, String bssidStr) {
        synchronized (this.mLock) {
            HwWifi2SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "setCurrentNetworkBssid");
            if (networkHandle == null) {
                return false;
            }
            return networkHandle.setBssid(bssidStr);
        }
    }

    public boolean removeAllNetworks(String ifaceName) {
        synchronized (this.mLock) {
            ArrayList<Integer> networks = listNetworks(ifaceName);
            if (networks == null) {
                HwHiLog.e(TAG, false, "removeAllNetworks failed, got null networks", new Object[0]);
                return false;
            }
            Iterator<Integer> it = networks.iterator();
            while (it.hasNext()) {
                int id = it.next().intValue();
                if (!removeNetwork(ifaceName, id)) {
                    HwHiLog.e(TAG, false, "removeAllNetworks failed to remove network: %{public}d", new Object[]{Integer.valueOf(id)});
                    return false;
                }
            }
            this.mCurrentNetworkRemoteHandles.remove(ifaceName);
            this.mCurrentNetworkLocalConfigs.remove(ifaceName);
            return true;
        }
    }

    public boolean reassociate(String ifaceName) {
        HwHiLog.i(TAG, false, "reassociate: %{public}s", new Object[]{ifaceName});
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "reassociate");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.reassociate(), "reassociate");
            } catch (RemoteException e) {
                handleRemoteException("reassociate");
                return false;
            }
        }
    }

    public boolean reconnect(String ifaceName) {
        HwHiLog.i(TAG, false, "reconnect: %{public}s", new Object[]{ifaceName});
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "reconnect");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.reconnect(), "reconnect");
            } catch (RemoteException e) {
                handleRemoteException("reconnect");
                return false;
            }
        }
    }

    public boolean disconnect(String ifaceName) {
        HwHiLog.i(TAG, false, "disconnect: %{public}s", new Object[]{ifaceName});
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "disconnect");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.disconnect(), "disconnect");
            } catch (RemoteException e) {
                handleRemoteException("disconnect");
                return false;
            }
        }
    }

    public boolean setPowerSave(String ifaceName, boolean isEnable) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setPowerSave");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.setPowerSave(isEnable), "setPowerSave");
            } catch (RemoteException e) {
                handleRemoteException("setPowerSave");
                return false;
            }
        }
    }

    public String getMacAddress(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "getMacAddress");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotMac = new HidlSupport.Mutable<>();
            try {
                iface.getMacAddress(new ISupplicantStaIface.getMacAddressCallback(gotMac) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2SupplicantStaIfaceHal$OzLHwuejZQmZEuHiew8H6mHx3Q */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
                        HwWifi2SupplicantStaIfaceHal.this.lambda$getMacAddress$0$HwWifi2SupplicantStaIfaceHal(this.f$1, supplicantStatus, bArr);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException("getMacAddress");
            }
            return (String) gotMac.value;
        }
    }

    public /* synthetic */ void lambda$getMacAddress$0$HwWifi2SupplicantStaIfaceHal(HidlSupport.Mutable gotMac, SupplicantStatus status, byte[] macAddr) {
        if (checkStatusAndLogFailure(status, "getMacAddress")) {
            gotMac.value = NativeUtil.macAddressFromByteArray(macAddr);
        }
    }

    public boolean setBtCoexistenceScanModeEnabled(String ifaceName, boolean isEnabled) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setBtCoexistenceScanModeEnabled");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.setBtCoexistenceScanModeEnabled(isEnabled), "setBtCoexistenceScanModeEnabled");
            } catch (RemoteException e) {
                handleRemoteException("setBtCoexistenceScanModeEnabled");
                return false;
            }
        }
    }

    public boolean startRxFilter(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "startRxFilter");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.startRxFilter(), "startRxFilter");
            } catch (RemoteException e) {
                handleRemoteException("startRxFilter");
                return false;
            }
        }
    }

    public boolean stopRxFilter(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "stopRxFilter");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.stopRxFilter(), "stopRxFilter");
            } catch (RemoteException e) {
                handleRemoteException("stopRxFilter");
                return false;
            }
        }
    }

    public boolean addRxFilter(String ifaceName, int type) {
        byte halType;
        synchronized (this.mLock) {
            if (type == 0) {
                halType = 0;
            } else if (type != 1) {
                try {
                    HwHiLog.i(TAG, false, "Invalid Rx Filter type: %{public}d", new Object[]{Integer.valueOf(type)});
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                halType = 1;
            }
            return addRxFilter(ifaceName, halType);
        }
    }

    private boolean addRxFilter(String ifaceName, byte type) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "addRxFilter");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.addRxFilter(type), "addRxFilter");
            } catch (RemoteException e) {
                handleRemoteException("addRxFilter");
                return false;
            }
        }
    }

    public boolean setBtCoexistenceMode(String ifaceName, int mode) {
        byte halMode;
        synchronized (this.mLock) {
            if (mode == 0) {
                halMode = 0;
            } else if (mode == 1) {
                halMode = 1;
            } else if (mode != 2) {
                try {
                    HwHiLog.i(TAG, false, "Invalid Bt Coex mode: %{public}d", new Object[]{Integer.valueOf(mode)});
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                halMode = 2;
            }
            return setBtCoexistenceMode(ifaceName, halMode);
        }
    }

    private boolean setBtCoexistenceMode(String ifaceName, byte mode) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setBtCoexistenceMode");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.setBtCoexistenceMode(mode), "setBtCoexistenceMode");
            } catch (RemoteException e) {
                handleRemoteException("setBtCoexistenceMode");
                return false;
            }
        }
    }

    public boolean setSuspendModeEnabled(String ifaceName, boolean isEnable) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setSuspendModeEnabled");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.setSuspendModeEnabled(isEnable), "setSuspendModeEnabled");
            } catch (RemoteException e) {
                handleRemoteException("setSuspendModeEnabled");
                return false;
            }
        }
    }

    public boolean removeRxFilter(String ifaceName, int type) {
        byte halType;
        synchronized (this.mLock) {
            if (type == 0) {
                halType = 0;
            } else if (type != 1) {
                try {
                    HwHiLog.i(TAG, false, "Invalid Rx Filter type: %{public}d", new Object[]{Integer.valueOf(type)});
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                halType = 1;
            }
            return removeRxFilter(ifaceName, halType);
        }
    }

    private boolean removeRxFilter(String ifaceName, byte type) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "removeRxFilter");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.removeRxFilter(type), "removeRxFilter");
            } catch (RemoteException e) {
                handleRemoteException("removeRxFilter");
                return false;
            }
        }
    }

    private boolean initSupplicantService() {
        synchronized (this.mLock) {
            try {
                this.mSupplicant = getSupplicantMockable();
                if (this.mSupplicant == null) {
                    HwHiLog.e(TAG, false, "Got null ISupplicant service. Stopping supplicant HIDL startup", new Object[0]);
                    return false;
                } else if (!linkToSupplicantDeath()) {
                    return false;
                } else {
                    HwHiLog.i(TAG, false, "register supplicant success.", new Object[0]);
                    return true;
                }
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "ISupplicant.getService RemoteException ", new Object[0]);
                return false;
            } catch (NoSuchElementException e2) {
                HwHiLog.e(TAG, false, "ISupplicant.getService NoSuchElementException", new Object[0]);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private IServiceManager getServiceManagerMockable() {
        IServiceManager service;
        synchronized (this.mLock) {
            service = IServiceManager.getService();
        }
        return service;
    }

    private ISupplicant getSupplicantMockable() {
        ISupplicant service;
        synchronized (this.mLock) {
            service = ISupplicant.getService();
        }
        return service;
    }

    private boolean linkToSupplicantDeath() {
        synchronized (this.mLock) {
            if (this.mSupplicant == null) {
                return false;
            }
            try {
                ISupplicant iSupplicant = this.mSupplicant;
                SupplicantDeathRecipient supplicantDeathRecipient = this.mSupplicantDeathRecipient;
                long j = this.mDeathRecipientCookie + 1;
                this.mDeathRecipientCookie = j;
                if (iSupplicant.linkToDeath(supplicantDeathRecipient, j)) {
                    return true;
                }
                HwHiLog.e(TAG, false, "Error on linkToDeath on ISupplicant", new Object[0]);
                supplicantServiceDiedHandler(this.mDeathRecipientCookie);
                return false;
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "ISupplicant.linkToDeath exception", new Object[0]);
                return false;
            }
        }
    }

    private boolean linkToServiceManagerDeath() {
        synchronized (this.mLock) {
            if (this.mServiceManager == null) {
                return false;
            }
            try {
                if (this.mServiceManager.linkToDeath(this.mServiceManagerDeathRecipient, 0)) {
                    return true;
                }
                HwHiLog.e(TAG, false, "Error on linkToDeath on IServiceManager", new Object[0]);
                supplicantServiceDiedHandler(this.mDeathRecipientCookie);
                this.mServiceManager = null;
                return false;
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "IServiceManager.linkToDeath exception", new Object[0]);
                return false;
            }
        }
    }

    private void supplicantServiceDiedHandler(long cookie) {
        HwHiLog.d(TAG, false, "supplicantServiceDiedHandler: %{public}s, %{public}s", new Object[]{String.valueOf(this.mDeathRecipientCookie), String.valueOf(cookie)});
        synchronized (this.mLock) {
            if (this.mDeathRecipientCookie == cookie) {
                for (String ifaceName : this.mSupplicantStaIfaces.keySet()) {
                    this.mWifi2Monitor.broadcastSupplicantDisconnectionEvent(ifaceName);
                }
                clearState();
                if (this.mDeathEventHandler != null) {
                    this.mDeathEventHandler.onDeath();
                }
            }
        }
    }

    private ISupplicantIface addIfaceV1Point1(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicant.IfaceInfo ifaceInfo = new ISupplicant.IfaceInfo();
            ifaceInfo.name = ifaceName;
            ifaceInfo.type = 0;
            HidlSupport.Mutable<ISupplicantIface> supplicantIface = new HidlSupport.Mutable<>();
            try {
                android.hardware.wifi.supplicant.V1_1.ISupplicant supplicant = getSupplicantMockableV1Point1();
                if (supplicant == null) {
                    clearState();
                    HwHiLog.e(TAG, false, "Failed to getSupplicantV11", new Object[0]);
                    return null;
                }
                supplicant.addInterface(ifaceInfo, new ISupplicant.addInterfaceCallback(supplicantIface) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2SupplicantStaIfaceHal$pzeuvZb8DC88DPyREZCFohfzwpw */
                    private final /* synthetic */ HidlSupport.Mutable f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ISupplicantIface iSupplicantIface) {
                        HwWifi2SupplicantStaIfaceHal.lambda$addIfaceV1Point1$1(this.f$0, supplicantStatus, iSupplicantIface);
                    }
                });
                return (ISupplicantIface) supplicantIface.value;
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "ISupplicant.addInterface exception", new Object[0]);
                handleRemoteException("addInterface");
                return null;
            } catch (NoSuchElementException e2) {
                HwHiLog.e(TAG, false, "ISupplicant.addInterface exception", new Object[0]);
                handleNoSuchElementException("addInterface");
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$addIfaceV1Point1$1(HidlSupport.Mutable supplicantIface, SupplicantStatus status, ISupplicantIface iface) {
        if (status.code == 0 || status.code == 5) {
            HwHiLog.i(TAG, false, "create ISupplicantIface: %{public}d", new Object[]{Integer.valueOf(status.code)});
            supplicantIface.value = iface;
            return;
        }
        HwHiLog.e(TAG, false, "Failed to create ISupplicantIface %{public}d", new Object[]{Integer.valueOf(status.code)});
    }

    private boolean removeIfaceV1Point1(String ifaceName) {
        synchronized (this.mLock) {
            try {
                ISupplicant.IfaceInfo ifaceInfo = new ISupplicant.IfaceInfo();
                ifaceInfo.name = ifaceName;
                ifaceInfo.type = 0;
                android.hardware.wifi.supplicant.V1_1.ISupplicant supplicant = getSupplicantMockableV1Point1();
                if (supplicant == null) {
                    HwHiLog.e(TAG, false, "Failed to getSupplicant", new Object[0]);
                    return false;
                }
                SupplicantStatus status = supplicant.removeInterface(ifaceInfo);
                if (status.code == 0) {
                    return true;
                }
                HwHiLog.e(TAG, false, "Failed to remove iface %{public}d", new Object[]{Integer.valueOf(status.code)});
                return false;
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "ISupplicant.removeInterface exception", new Object[0]);
                handleRemoteException("removeInterface");
                return false;
            } catch (NoSuchElementException e2) {
                HwHiLog.e(TAG, false, "ISupplicant.removeInterface exception", new Object[0]);
                handleNoSuchElementException("removeInterface");
                return false;
            }
        }
    }

    private ArrayList<Integer> listNetworks(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "listNetworks");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<ArrayList<Integer>> networkIdList = new HidlSupport.Mutable<>();
            try {
                iface.listNetworks(new ISupplicantIface.listNetworksCallback(networkIdList) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2SupplicantStaIfaceHal$qzTYvDybL1NPofQmk8KPm7ATd58 */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        HwWifi2SupplicantStaIfaceHal.this.lambda$listNetworks$2$HwWifi2SupplicantStaIfaceHal(this.f$1, supplicantStatus, arrayList);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException("listNetworks");
            }
            return (ArrayList) networkIdList.value;
        }
    }

    public /* synthetic */ void lambda$listNetworks$2$HwWifi2SupplicantStaIfaceHal(HidlSupport.Mutable networkIdList, SupplicantStatus status, ArrayList networkIds) {
        if (checkStatusAndLogFailure(status, "listNetworks")) {
            networkIdList.value = networkIds;
        }
    }

    private boolean removeNetwork(String ifaceName, int id) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "removeNetwork");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.removeNetwork(id), "removeNetwork");
            } catch (RemoteException e) {
                handleRemoteException("removeNetwork");
                return false;
            }
        }
    }

    private HwWifi2SupplicantStaNetworkHal addNetwork(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "addNetwork");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<ISupplicantNetwork> newNetwork = new HidlSupport.Mutable<>();
            try {
                iface.addNetwork(new ISupplicantIface.addNetworkCallback(newNetwork) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2SupplicantStaIfaceHal$BFu2ADg3eMCX84FHiMWyPLmfyA */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
                        HwWifi2SupplicantStaIfaceHal.this.lambda$addNetwork$3$HwWifi2SupplicantStaIfaceHal(this.f$1, supplicantStatus, iSupplicantNetwork);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException("addNetwork");
            }
            if (newNetwork.value == null) {
                return null;
            }
            return getStaNetworkMockable(ifaceName, ISupplicantStaNetwork.asInterface(((ISupplicantNetwork) newNetwork.value).asBinder()));
        }
    }

    public /* synthetic */ void lambda$addNetwork$3$HwWifi2SupplicantStaIfaceHal(HidlSupport.Mutable newNetwork, SupplicantStatus status, ISupplicantNetwork network) {
        if (checkStatusAndLogFailure(status, "addNetwork")) {
            newNetwork.value = network;
        }
    }

    private HwWifi2SupplicantStaNetworkHal getNetwork(String ifaceName, int id) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "getNetwork");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<ISupplicantNetwork> gotNetwork = new HidlSupport.Mutable<>();
            try {
                iface.getNetwork(id, new ISupplicantIface.getNetworkCallback(gotNetwork) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2SupplicantStaIfaceHal$3CffPPAK02ZcKVfUXxH88sbJMYw */
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
                        HwWifi2SupplicantStaIfaceHal.this.lambda$getNetwork$4$HwWifi2SupplicantStaIfaceHal(this.f$1, supplicantStatus, iSupplicantNetwork);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException("getNetwork");
            }
            if (gotNetwork.value == null) {
                return null;
            }
            return getStaNetworkMockable(ifaceName, ISupplicantStaNetwork.asInterface(((ISupplicantNetwork) gotNetwork.value).asBinder()));
        }
    }

    public /* synthetic */ void lambda$getNetwork$4$HwWifi2SupplicantStaIfaceHal(HidlSupport.Mutable gotNetwork, SupplicantStatus status, ISupplicantNetwork network) {
        if (checkStatusAndLogFailure(status, "getNetwork")) {
            gotNetwork.value = network;
        }
    }

    private void clearState() {
        synchronized (this.mLock) {
            this.mSupplicant = null;
            this.mSupplicantStaIfaces.clear();
            this.mCurrentNetworkLocalConfigs.clear();
            this.mCurrentNetworkRemoteHandles.clear();
        }
    }

    private void handleNoSuchElementException(String methodStr) {
        synchronized (this.mLock) {
            clearState();
            HwHiLog.e(TAG, false, "%{public}s failed with exception", new Object[]{methodStr});
        }
    }

    private void handleRemoteException(String methodStr) {
        synchronized (this.mLock) {
            clearState();
            HwHiLog.e(TAG, false, "%{public}s failed with exception", new Object[]{methodStr});
        }
    }

    private WifiConfiguration getCurrentNetworkLocalConfig(String ifaceName) {
        return this.mCurrentNetworkLocalConfigs.get(ifaceName);
    }

    public static SupplicantState supplicantHidlStateToFrameworkState(int state) {
        switch (state) {
            case 0:
                return SupplicantState.DISCONNECTED;
            case 1:
                return SupplicantState.INTERFACE_DISABLED;
            case 2:
                return SupplicantState.INACTIVE;
            case 3:
                return SupplicantState.SCANNING;
            case 4:
                return SupplicantState.AUTHENTICATING;
            case 5:
                return SupplicantState.ASSOCIATING;
            case 6:
                return SupplicantState.ASSOCIATED;
            case 7:
                return SupplicantState.FOUR_WAY_HANDSHAKE;
            case 8:
                return SupplicantState.GROUP_HANDSHAKE;
            case 9:
                return SupplicantState.COMPLETED;
            default:
                throw new IllegalArgumentException("Invalid state: " + state);
        }
    }

    private int getCurrentNetworkId(String ifaceName) {
        synchronized (this.mLock) {
            WifiConfiguration currentConfig = getCurrentNetworkLocalConfig(ifaceName);
            if (currentConfig == null) {
                return -1;
            }
            return currentConfig.networkId;
        }
    }

    private ISupplicantStaIface checkSupplicantStaIfaceAndLogFailure(String ifaceName, String methodStr) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = getStaIface(ifaceName);
            if (iface != null) {
                return iface;
            }
            HwHiLog.e(TAG, false, "Can't call %{public}s, ISupplicantStaIface is null", new Object[]{methodStr});
            return null;
        }
    }

    private boolean checkStatusAndLogFailure(SupplicantStatus status, String methodStr) {
        synchronized (this.mLock) {
            if (status.code == 0) {
                return true;
            }
            HwHiLog.e(TAG, false, "ISupplicantStaIface %{public}s failed: %{public}s", new Object[]{methodStr, status});
            return false;
        }
    }

    private HwWifi2SupplicantStaNetworkHal checkSupplicantStaNetworkAndLogFailure(String ifaceName, String methodStr) {
        synchronized (this.mLock) {
            HwWifi2SupplicantStaNetworkHal networkHal = getCurrentNetworkRemoteHandle(ifaceName);
            if (networkHal != null) {
                return networkHal;
            }
            HwHiLog.e(TAG, false, "Can't call %{public}s, SupplicantStaNetwork is null", new Object[]{methodStr});
            return null;
        }
    }

    private boolean checkHalVersionByInterfaceName(String interfaceName) {
        boolean z = false;
        if (interfaceName == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (this.mServiceManager == null) {
                HwHiLog.e(TAG, false, "checkHalVersionByInterfaceName: called but mServiceManager is null", new Object[0]);
                return false;
            }
            try {
                if (this.mServiceManager.getTransport(interfaceName, HAL_INSTANCE_NAME) != 0) {
                    z = true;
                }
                return z;
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "Exception while operating on IServiceManager", new Object[0]);
                handleRemoteException("getTransport");
                return false;
            }
        }
    }

    private boolean isV1Point1() {
        return checkHalVersionByInterfaceName("android.hardware.wifi.supplicant@1.1::ISupplicant");
    }

    private boolean isV1Point2() {
        return checkHalVersionByInterfaceName("android.hardware.wifi.supplicant@1.2::ISupplicant");
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0018: APUT  
      (r5v1 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x0014: INVOKE  (r6v2 java.lang.String) = (r6v1 boolean) type: STATIC call: java.lang.String.valueOf(boolean):java.lang.String)
     */
    private boolean isV3Point0() {
        boolean z;
        synchronized (this.mLock) {
            z = false;
            try {
                Object[] objArr = new Object[1];
                objArr[0] = String.valueOf(getVendorSupplicantV3Point0() != null);
                HwHiLog.d(TAG, false, "isV3Point0() %{public}s", objArr);
                if (getVendorSupplicantV3Point0() != null) {
                    z = true;
                }
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "ISupplicant.getService exception", new Object[0]);
                supplicantServiceDiedHandler(this.mDeathRecipientCookie);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return z;
    }

    private vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIface getVendorStaIfaceV3Point0(ISupplicantIface iface) {
        vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIface castFrom;
        if (iface == null) {
            return null;
        }
        synchronized (this.mLock) {
            castFrom = vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIface.castFrom(iface);
        }
        return castFrom;
    }

    private vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant getVendorSupplicantV3Point0() {
        vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant castFrom;
        synchronized (this.mLock) {
            try {
                castFrom = vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant.castFrom(android.hardware.wifi.supplicant.V1_0.ISupplicant.getService());
            } catch (NoSuchElementException e) {
                HwHiLog.e(TAG, false, "Failed to get vendor V3_0 ISupplicant", new Object[0]);
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        return castFrom;
    }

    private Pair<HwWifi2SupplicantStaNetworkHal, WifiConfiguration> addNetworkAndSaveConfig(String ifaceName, WifiConfiguration config) {
        synchronized (this.mLock) {
            HwHiLog.i(TAG, false, "addSupplicantStaNetwork via HIDL", new Object[0]);
            HwWifi2SupplicantStaNetworkHal network = addNetwork(ifaceName);
            if (network == null) {
                HwHiLog.e(TAG, false, "Failed to add network!", new Object[0]);
                return null;
            }
            boolean isSaveSuccess = false;
            try {
                isSaveSuccess = network.saveWifiConfiguration(config);
            } catch (IllegalArgumentException e) {
                HwHiLog.e(TAG, false, "Exception while saving config params", new Object[0]);
            }
            if (!isSaveSuccess) {
                HwHiLog.e(TAG, false, "Failed to save variables for: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
                if (!removeAllNetworks(ifaceName)) {
                    HwHiLog.e(TAG, false, "Failed to remove all networks on failure.", new Object[0]);
                }
                return null;
            }
            return new Pair<>(network, new WifiConfiguration(config));
        }
    }

    private HwWifi2SupplicantStaNetworkHal getStaNetworkMockable(String ifaceName, ISupplicantStaNetwork supplicantStaNetwork) {
        HwWifi2SupplicantStaNetworkHal network;
        synchronized (this.mLock) {
            network = new HwWifi2SupplicantStaNetworkHal(supplicantStaNetwork, ifaceName, this.mContext, this.mWifi2Monitor);
        }
        return network;
    }

    private android.hardware.wifi.supplicant.V1_1.ISupplicant getSupplicantMockableV1Point1() {
        android.hardware.wifi.supplicant.V1_1.ISupplicant castFrom;
        synchronized (this.mLock) {
            castFrom = android.hardware.wifi.supplicant.V1_1.ISupplicant.castFrom(android.hardware.wifi.supplicant.V1_0.ISupplicant.getService());
        }
        return castFrom;
    }

    private boolean hwStaRegisterCallback(vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIface iface, vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicantStaIfaceCallback callback) {
        synchronized (this.mLock) {
            if (iface == null) {
                HwHiLog.e(TAG, false, "Got null iface when registering callback", new Object[0]);
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.hwStaRegisterCallback(callback), "hwStaRegisterCallback");
            } catch (RemoteException e) {
                handleRemoteException("hwStaRegisterCallback");
                return false;
            }
        }
    }

    private ISupplicantStaIface getStaIface(String ifaceName) {
        return this.mSupplicantStaIfaces.get(ifaceName);
    }

    private HwWifi2SupplicantStaNetworkHal getCurrentNetworkRemoteHandle(String ifaceName) {
        return this.mCurrentNetworkRemoteHandles.get(ifaceName);
    }

    public boolean initiateTdlsDiscover(String ifaceName, String macAddress) {
        boolean initiateTdlsDiscover;
        synchronized (this.mLock) {
            try {
                initiateTdlsDiscover = initiateTdlsDiscover(ifaceName, NativeUtil.macAddressToByteArray(macAddress));
            } catch (IllegalArgumentException e) {
                HwHiLog.i(TAG, false, "Illegal argument %{public}s", new Object[]{StringUtilEx.safeDisplayBssid(macAddress)});
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return initiateTdlsDiscover;
    }

    private boolean initiateTdlsDiscover(String ifaceName, byte[] macAddress) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "initiateTdlsDiscover");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.initiateTdlsDiscover(macAddress), "initiateTdlsDiscover");
            } catch (RemoteException e) {
                handleRemoteException("initiateTdlsDiscover");
                return false;
            }
        }
    }

    public String getCurrentNetworkEapAnonymousIdentity(String ifaceName) {
        synchronized (this.mLock) {
            HwWifi2SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "getCurrentNetworkEapAnonymousIdentity");
            if (networkHandle == null) {
                return null;
            }
            return networkHandle.fetchEapAnonymousIdentity();
        }
    }

    public boolean setConcurrencyPriority(boolean isStaHigherPriority) {
        synchronized (this.mLock) {
            if (isStaHigherPriority) {
                return setConcurrencyPriority(0);
            }
            return setConcurrencyPriority(1);
        }
    }

    private boolean setConcurrencyPriority(int type) {
        synchronized (this.mLock) {
            if (!checkSupplicantAndLogFailure("setConcurrencyPriority")) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(this.mSupplicant.setConcurrencyPriority(type), "setConcurrencyPriority");
            } catch (RemoteException e) {
                handleRemoteException("setConcurrencyPriority");
                return false;
            }
        }
    }

    public boolean setExternalSim(String ifaceName, boolean isUseExternalSim) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setExternalSim");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.setExternalSim(isUseExternalSim), "setExternalSim");
            } catch (RemoteException e) {
                handleRemoteException("setExternalSim");
                return false;
            }
        }
    }

    private boolean checkSupplicantAndLogFailure(String methodStr) {
        synchronized (this.mLock) {
            if (this.mSupplicant != null) {
                return true;
            }
            HwHiLog.e(TAG, false, "Can't call %{public}s, ISupplicant is null", new Object[]{methodStr});
            return false;
        }
    }

    public boolean initiateTdlsSetup(String ifaceName, String macAddress) {
        boolean initiateTdlsSetup;
        synchronized (this.mLock) {
            try {
                initiateTdlsSetup = initiateTdlsSetup(ifaceName, NativeUtil.macAddressToByteArray(macAddress));
            } catch (IllegalArgumentException e) {
                HwHiLog.e(TAG, false, "Illegal argument %{public}s", new Object[]{StringUtilEx.safeDisplayBssid(macAddress)});
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return initiateTdlsSetup;
    }

    private boolean initiateTdlsSetup(String ifaceName, byte[] macAddress) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "initiateTdlsSetup");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.initiateTdlsSetup(macAddress), "initiateTdlsSetup");
            } catch (RemoteException e) {
                handleRemoteException("initiateTdlsSetup");
                return false;
            }
        }
    }

    public boolean initiateTdlsTeardown(String ifaceName, String macAddress) {
        boolean initiateTdlsTeardown;
        synchronized (this.mLock) {
            try {
                initiateTdlsTeardown = initiateTdlsTeardown(ifaceName, NativeUtil.macAddressToByteArray(macAddress));
            } catch (IllegalArgumentException e) {
                HwHiLog.e(TAG, false, "Illegal argument %{public}s", new Object[]{StringUtilEx.safeDisplayBssid(macAddress)});
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return initiateTdlsTeardown;
    }

    private boolean initiateTdlsTeardown(String ifaceName, byte[] macAddress) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "initiateTdlsTeardown");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.initiateTdlsTeardown(macAddress), "initiateTdlsTeardown");
            } catch (RemoteException e) {
                handleRemoteException("initiateTdlsTeardown");
                return false;
            }
        }
    }

    public boolean roamToNetwork(String ifaceName, WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (getCurrentNetworkId(ifaceName) != config.networkId) {
                HwHiLog.i(TAG, false, "Cannot roam to a different network, initiate new connection. Current network ID: %{public}d", new Object[]{Integer.valueOf(getCurrentNetworkId(ifaceName))});
                return connectToNetwork(ifaceName, config);
            }
            String bssid = config.getNetworkSelectionStatus().getNetworkSelectionBSSID();
            HwWifi2SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "roamToNetwork");
            if (networkHandle != null) {
                if (networkHandle.setBssid(bssid)) {
                    if (reassociate(ifaceName)) {
                        return true;
                    }
                    HwHiLog.e(TAG, false, "Failed to trigger reassociate", new Object[0]);
                    return false;
                }
            }
            HwHiLog.e(TAG, false, "Failed to set new bssid on network: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
            return false;
        }
    }

    public boolean enableAutoReconnect(String ifaceName, boolean enable) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "enableAutoReconnect");
            if (iface == null) {
                return false;
            }
            try {
                return checkStatusAndLogFailure(iface.enableAutoReconnect(enable), "enableAutoReconnect");
            } catch (RemoteException e) {
                handleRemoteException("enableAutoReconnect");
                return false;
            }
        }
    }
}
