package com.android.server.wifi;

import android.content.Context;
import android.hardware.wifi.supplicant.V1_0.ISupplicant;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatusCode;
import android.hardware.wifi.supplicant.V1_0.WpsConfigMethods;
import android.hardware.wifi.supplicant.V1_1.ISupplicant;
import android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.net.IpConfiguration;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiSsid;
import android.os.HidlSupport;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.AnqpEvent;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.WnmData;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.ANQPParser;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.StringUtil;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.concurrent.ThreadSafe;
import vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface;
import vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIfaceCallback;

@ThreadSafe
public class SupplicantStaIfaceHal {
    public static final int HAL_CALL_THRESHOLD_MS = 300;
    private static final String TAG = "SupplicantStaIfaceHal";
    private static final Pattern WPS_DEVICE_TYPE_PATTERN = Pattern.compile("^(\\d{1,2})-([0-9a-fA-F]{8})-(\\d{1,2})$");
    private final Context mContext;
    private HashMap<String, WifiConfiguration> mCurrentNetworkLocalConfigs = new HashMap<>();
    private HashMap<String, SupplicantStaNetworkHal> mCurrentNetworkRemoteHandles = new HashMap<>();
    private WifiNative.SupplicantDeathEventHandler mDeathEventHandler;
    /* access modifiers changed from: private */
    public long mDeathRecipientCookie = 0;
    private long mHalCallStartTime;
    private String mHalMethod = "";
    private IServiceManager mIServiceManager = null;
    private ISupplicant mISupplicant;
    private HashMap<String, ISupplicantStaIfaceCallback> mISupplicantStaIfaceCallbacks = new HashMap<>();
    private HashMap<String, ISupplicantStaIface> mISupplicantStaIfaces = new HashMap<>();
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final IHwBinder.DeathRecipient mServiceManagerDeathRecipient = new IHwBinder.DeathRecipient() {
        public final void serviceDied(long j) {
            SupplicantStaIfaceHal.lambda$new$0(SupplicantStaIfaceHal.this, j);
        }
    };
    private final IServiceNotification mServiceNotificationCallback = new IServiceNotification.Stub() {
        public void onRegistration(String fqName, String name, boolean preexisting) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                if (SupplicantStaIfaceHal.this.mVerboseLoggingEnabled) {
                    Log.i(SupplicantStaIfaceHal.TAG, "IServiceNotification.onRegistration for: " + fqName + ", " + name + " preexisting=" + preexisting);
                }
                if (!SupplicantStaIfaceHal.this.initSupplicantService()) {
                    Log.e(SupplicantStaIfaceHal.TAG, "initalizing ISupplicant failed.");
                    SupplicantStaIfaceHal.this.supplicantServiceDiedHandler(SupplicantStaIfaceHal.this.mDeathRecipientCookie);
                } else {
                    Log.i(SupplicantStaIfaceHal.TAG, "Completed initialization of ISupplicant.");
                }
            }
        }
    };
    private final IHwBinder.DeathRecipient mSupplicantDeathRecipient = new IHwBinder.DeathRecipient() {
        public final void serviceDied(long j) {
            SupplicantStaIfaceHal.lambda$new$1(SupplicantStaIfaceHal.this, j);
        }
    };
    /* access modifiers changed from: private */
    public boolean mVerboseLoggingEnabled = false;
    /* access modifiers changed from: private */
    public final WifiMonitor mWifiMonitor;

    private class SupplicantStaIfaceHalCallback extends ISupplicantStaIfaceCallback.Stub {
        private String mIfaceName;
        private boolean mStateIsFourway = false;

        SupplicantStaIfaceHalCallback(String ifaceName) {
            this.mIfaceName = ifaceName;
        }

        private ANQPElement parseAnqpElement(Constants.ANQPElementType infoID, ArrayList<Byte> payload) {
            ANQPElement aNQPElement;
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                try {
                    if (Constants.getANQPElementID(infoID) != null) {
                        aNQPElement = ANQPParser.parseElement(infoID, ByteBuffer.wrap(NativeUtil.byteArrayFromArrayList(payload)));
                    } else {
                        aNQPElement = ANQPParser.parseHS20Element(infoID, ByteBuffer.wrap(NativeUtil.byteArrayFromArrayList(payload)));
                    }
                } catch (IOException | BufferUnderflowException e) {
                    Log.e(SupplicantStaIfaceHal.TAG, "Failed parsing ANQP element payload: " + infoID, e);
                    return null;
                } catch (Throwable th) {
                    throw th;
                }
            }
            return aNQPElement;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x001e, code lost:
            return;
         */
        private void addAnqpElementToMap(Map<Constants.ANQPElementType, ANQPElement> elementsMap, Constants.ANQPElementType infoID, ArrayList<Byte> payload) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                if (payload != null) {
                    try {
                        if (!payload.isEmpty()) {
                            ANQPElement element = parseAnqpElement(infoID, payload);
                            if (element != null) {
                                elementsMap.put(infoID, element);
                            }
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            }
        }

        public void onNetworkAdded(int id) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onNetworkAdded");
            }
        }

        public void onNetworkRemoved(int id) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onNetworkRemoved");
            }
        }

        public void onStateChanged(int newState, byte[] bssid, int id, ArrayList<Byte> ssid) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onStateChanged");
                SupplicantState newSupplicantState = SupplicantStaIfaceHal.supplicantHidlStateToFrameworkState(newState);
                WifiSsid wifiSsid = WifiSsid.createFromByteArray(NativeUtil.byteArrayFromArrayList(ssid));
                String bssidStr = NativeUtil.macAddressFromByteArray(bssid);
                this.mStateIsFourway = newState == 7;
                if (newSupplicantState == SupplicantState.COMPLETED) {
                    SupplicantStaIfaceHal.this.mWifiMonitor.broadcastNetworkConnectionEvent(this.mIfaceName, SupplicantStaIfaceHal.this.getCurrentNetworkId(this.mIfaceName), bssidStr);
                }
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastSupplicantStateChangeEvent(this.mIfaceName, SupplicantStaIfaceHal.this.getCurrentNetworkId(this.mIfaceName), wifiSsid, bssidStr, newSupplicantState);
            }
        }

        public void onAnqpQueryDone(byte[] bssid, ISupplicantStaIfaceCallback.AnqpData data, ISupplicantStaIfaceCallback.Hs20AnqpData hs20Data) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onAnqpQueryDone");
                Map<Constants.ANQPElementType, ANQPElement> elementsMap = new HashMap<>();
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.ANQPVenueName, data.venueName);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.ANQPRoamingConsortium, data.roamingConsortium);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.ANQPIPAddrAvailability, data.ipAddrTypeAvailability);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.ANQPNAIRealm, data.naiRealm);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.ANQP3GPPNetwork, data.anqp3gppCellularNetwork);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.ANQPDomName, data.domainName);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.HSFriendlyName, hs20Data.operatorFriendlyName);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.HSWANMetrics, hs20Data.wanMetrics);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.HSConnCapability, hs20Data.connectionCapability);
                addAnqpElementToMap(elementsMap, Constants.ANQPElementType.HSOSUProviders, hs20Data.osuProvidersList);
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAnqpDoneEvent(this.mIfaceName, new AnqpEvent(NativeUtil.macAddressToLong(bssid).longValue(), elementsMap));
            }
        }

        public void onHs20IconQueryDone(byte[] bssid, String fileName, ArrayList<Byte> data) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onHs20IconQueryDone");
                WifiMonitor access$800 = SupplicantStaIfaceHal.this.mWifiMonitor;
                String str = this.mIfaceName;
                IconEvent iconEvent = new IconEvent(NativeUtil.macAddressToLong(bssid).longValue(), fileName, data.size(), NativeUtil.byteArrayFromArrayList(data));
                access$800.broadcastIconDoneEvent(str, iconEvent);
            }
        }

        public void onHs20SubscriptionRemediation(byte[] bssid, byte osuMethod, String url) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onHs20SubscriptionRemediation");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastWnmEvent(this.mIfaceName, new WnmData(NativeUtil.macAddressToLong(bssid).longValue(), url, osuMethod));
            }
        }

        public void onHs20DeauthImminentNotice(byte[] bssid, int reasonCode, int reAuthDelayInSec, String url) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onHs20DeauthImminentNotice");
                WifiMonitor access$800 = SupplicantStaIfaceHal.this.mWifiMonitor;
                String str = this.mIfaceName;
                long longValue = NativeUtil.macAddressToLong(bssid).longValue();
                boolean z = true;
                if (reasonCode != 1) {
                    z = false;
                }
                WnmData wnmData = new WnmData(longValue, url, z, reAuthDelayInSec);
                access$800.broadcastWnmEvent(str, wnmData);
            }
        }

        public void onDisconnected(byte[] bssid, boolean locallyGenerated, int reasonCode) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onDisconnected");
                if (SupplicantStaIfaceHal.this.mVerboseLoggingEnabled) {
                    Log.e(SupplicantStaIfaceHal.TAG, "onDisconnected 4way=" + this.mStateIsFourway + " locallyGenerated=" + locallyGenerated + " reasonCode=" + reasonCode);
                }
                if (this.mStateIsFourway && (!locallyGenerated || reasonCode != 17)) {
                    SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 2, -1);
                }
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastNetworkDisconnectionEvent(this.mIfaceName, locallyGenerated, reasonCode, NativeUtil.macAddressFromByteArray(bssid));
            }
        }

        public void onAssociationRejected(byte[] bssid, int statusCode, boolean timedOut) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onAssociationRejected");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAssociationRejectionEvent(this.mIfaceName, statusCode, timedOut, NativeUtil.macAddressFromByteArray(bssid));
            }
        }

        public void onAuthenticationTimeout(byte[] bssid) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onAuthenticationTimeout");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 1, -1);
            }
        }

        public void onBssidChanged(byte reason, byte[] bssid) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onBssidChanged");
                if (reason == 0) {
                    SupplicantStaIfaceHal.this.mWifiMonitor.broadcastTargetBssidEvent(this.mIfaceName, NativeUtil.macAddressFromByteArray(bssid));
                } else if (reason == 1) {
                    SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAssociatedBssidEvent(this.mIfaceName, NativeUtil.macAddressFromByteArray(bssid));
                }
            }
        }

        public void onEapFailure() {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onEapFailure");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 3, -1);
            }
        }

        public void onWpsEventSuccess() {
            SupplicantStaIfaceHal.this.logCallback("onWpsEventSuccess");
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastWpsSuccessEvent(this.mIfaceName);
            }
        }

        public void onWpsEventFail(byte[] bssid, short configError, short errorInd) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onWpsEventFail");
                if (configError == 16 && errorInd == 0) {
                    SupplicantStaIfaceHal.this.mWifiMonitor.broadcastWpsTimeoutEvent(this.mIfaceName);
                } else {
                    SupplicantStaIfaceHal.this.mWifiMonitor.broadcastWpsFailEvent(this.mIfaceName, configError, errorInd);
                }
            }
        }

        public void onWpsEventPbcOverlap() {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onWpsEventPbcOverlap");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastWpsOverlapEvent(this.mIfaceName);
            }
        }

        public void onExtRadioWorkStart(int id) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onExtRadioWorkStart");
            }
        }

        public void onExtRadioWorkTimeout(int id) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onExtRadioWorkTimeout");
            }
        }
    }

    private class SupplicantStaIfaceHalCallbackV1_1 extends ISupplicantStaIfaceCallback.Stub {
        private SupplicantStaIfaceHalCallback mCallbackV1_0;
        private String mIfaceName;

        SupplicantStaIfaceHalCallbackV1_1(String ifaceName, SupplicantStaIfaceHalCallback callback) {
            this.mIfaceName = ifaceName;
            this.mCallbackV1_0 = callback;
        }

        public void onNetworkAdded(int id) {
            this.mCallbackV1_0.onNetworkAdded(id);
        }

        public void onNetworkRemoved(int id) {
            this.mCallbackV1_0.onNetworkRemoved(id);
        }

        public void onStateChanged(int newState, byte[] bssid, int id, ArrayList<Byte> ssid) {
            this.mCallbackV1_0.onStateChanged(newState, bssid, id, ssid);
        }

        public void onAnqpQueryDone(byte[] bssid, ISupplicantStaIfaceCallback.AnqpData data, ISupplicantStaIfaceCallback.Hs20AnqpData hs20Data) {
            this.mCallbackV1_0.onAnqpQueryDone(bssid, data, hs20Data);
        }

        public void onHs20IconQueryDone(byte[] bssid, String fileName, ArrayList<Byte> data) {
            this.mCallbackV1_0.onHs20IconQueryDone(bssid, fileName, data);
        }

        public void onHs20SubscriptionRemediation(byte[] bssid, byte osuMethod, String url) {
            this.mCallbackV1_0.onHs20SubscriptionRemediation(bssid, osuMethod, url);
        }

        public void onHs20DeauthImminentNotice(byte[] bssid, int reasonCode, int reAuthDelayInSec, String url) {
            this.mCallbackV1_0.onHs20DeauthImminentNotice(bssid, reasonCode, reAuthDelayInSec, url);
        }

        public void onDisconnected(byte[] bssid, boolean locallyGenerated, int reasonCode) {
            this.mCallbackV1_0.onDisconnected(bssid, locallyGenerated, reasonCode);
        }

        public void onAssociationRejected(byte[] bssid, int statusCode, boolean timedOut) {
            this.mCallbackV1_0.onAssociationRejected(bssid, statusCode, timedOut);
        }

        public void onAuthenticationTimeout(byte[] bssid) {
            this.mCallbackV1_0.onAuthenticationTimeout(bssid);
        }

        public void onBssidChanged(byte reason, byte[] bssid) {
            this.mCallbackV1_0.onBssidChanged(reason, bssid);
        }

        public void onEapFailure() {
            this.mCallbackV1_0.onEapFailure();
        }

        public void onEapFailure_1_1(int code) {
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.logCallback("onEapFailure_1_1");
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAuthenticationFailureEvent(this.mIfaceName, 3, code);
            }
        }

        public void onWpsEventSuccess() {
            this.mCallbackV1_0.onWpsEventSuccess();
        }

        public void onWpsEventFail(byte[] bssid, short configError, short errorInd) {
            this.mCallbackV1_0.onWpsEventFail(bssid, configError, errorInd);
        }

        public void onWpsEventPbcOverlap() {
            this.mCallbackV1_0.onWpsEventPbcOverlap();
        }

        public void onExtRadioWorkStart(int id) {
            this.mCallbackV1_0.onExtRadioWorkStart(id);
        }

        public void onExtRadioWorkTimeout(int id) {
            this.mCallbackV1_0.onExtRadioWorkTimeout(id);
        }
    }

    private class VendorSupplicantStaIfaceHalCallbackV2_0 extends ISupplicantStaIfaceCallback.Stub {
        private SupplicantStaIfaceHalCallbackV1_1 mCallbackV1_1;
        private String mIfaceName;

        VendorSupplicantStaIfaceHalCallbackV2_0(String ifaceName, SupplicantStaIfaceHalCallbackV1_1 callback) {
            this.mIfaceName = ifaceName;
            this.mCallbackV1_1 = callback;
        }

        public void onNetworkAdded(int id) {
            this.mCallbackV1_1.onNetworkAdded(id);
        }

        public void onNetworkRemoved(int id) {
            this.mCallbackV1_1.onNetworkRemoved(id);
        }

        public void onStateChanged(int newState, byte[] bssid, int id, ArrayList<Byte> ssid) {
            this.mCallbackV1_1.onStateChanged(newState, bssid, id, ssid);
        }

        public void onAnqpQueryDone(byte[] bssid, ISupplicantStaIfaceCallback.AnqpData data, ISupplicantStaIfaceCallback.Hs20AnqpData hs20Data) {
            this.mCallbackV1_1.onAnqpQueryDone(bssid, data, hs20Data);
        }

        public void onHs20IconQueryDone(byte[] bssid, String fileName, ArrayList<Byte> data) {
            this.mCallbackV1_1.onHs20IconQueryDone(bssid, fileName, data);
        }

        public void onHs20SubscriptionRemediation(byte[] bssid, byte osuMethod, String url) {
            this.mCallbackV1_1.onHs20SubscriptionRemediation(bssid, osuMethod, url);
        }

        public void onHs20DeauthImminentNotice(byte[] bssid, int reasonCode, int reAuthDelayInSec, String url) {
            this.mCallbackV1_1.onHs20DeauthImminentNotice(bssid, reasonCode, reAuthDelayInSec, url);
        }

        public void onDisconnected(byte[] bssid, boolean locallyGenerated, int reasonCode) {
            this.mCallbackV1_1.onDisconnected(bssid, locallyGenerated, reasonCode);
        }

        public void onAssociationRejected(byte[] bssid, int statusCode, boolean timedOut) {
            this.mCallbackV1_1.onAssociationRejected(bssid, statusCode, timedOut);
        }

        public void onAuthenticationTimeout(byte[] bssid) {
            this.mCallbackV1_1.onAuthenticationTimeout(bssid);
        }

        public void onBssidChanged(byte reason, byte[] bssid) {
            this.mCallbackV1_1.onBssidChanged(reason, bssid);
        }

        public void onEapFailure() {
            this.mCallbackV1_1.onEapFailure();
        }

        public void onEapFailure_1_1(int code) {
            this.mCallbackV1_1.onEapFailure_1_1(code);
        }

        public void onWpsEventSuccess() {
            this.mCallbackV1_1.onWpsEventSuccess();
        }

        public void onWpsEventFail(byte[] bssid, short configError, short errorInd) {
            this.mCallbackV1_1.onWpsEventFail(bssid, configError, errorInd);
        }

        public void onWpsEventPbcOverlap() {
            this.mCallbackV1_1.onWpsEventPbcOverlap();
        }

        public void onExtRadioWorkStart(int id) {
            this.mCallbackV1_1.onExtRadioWorkStart(id);
        }

        public void onExtRadioWorkTimeout(int id) {
            this.mCallbackV1_1.onExtRadioWorkTimeout(id);
        }

        public void onWapiCertInitFail() {
            Log.d(SupplicantStaIfaceHal.TAG, "ISupplicantStaIfaceCallback. onWapiCertInitFail received");
            SupplicantStaIfaceHal.this.mWifiMonitor.broadcastWapiCertInitFailEvent(this.mIfaceName);
        }

        public void onWapiAuthFail() {
            Log.d(SupplicantStaIfaceHal.TAG, "ISupplicantStaIfaceCallback. onWapiAuthFail received");
            synchronized (SupplicantStaIfaceHal.this.mLock) {
                SupplicantStaIfaceHal.this.mWifiMonitor.broadcastWapiAuthFailEvent(this.mIfaceName);
            }
        }

        public void onVoWifiIrqStr() {
            Log.d(SupplicantStaIfaceHal.TAG, "ISupplicantStaIfaceCallback. onVoWifiIrqStr received");
            SupplicantStaIfaceHal.this.mWifiMonitor.broadcastVoWifiIrqStrEvent(this.mIfaceName);
        }

        public void onHilinkStartWps(String arg) {
            Log.d(SupplicantStaIfaceHal.TAG, "ISupplicantStaIfaceCallback. onHilinkStartWps received");
            SupplicantStaIfaceHal.this.mWifiMonitor.broadcastHilinkStartWpsEvent(this.mIfaceName, arg);
        }

        public void onHilinkStartWps() {
            Log.d(SupplicantStaIfaceHal.TAG, "ISupplicantStaIfaceCallback. onHilinkStartWps received");
        }

        public void onAbsAntCoreRob() {
            Log.d(SupplicantStaIfaceHal.TAG, "ISupplicantStaIfaceCallback. onAbsAntCoreRob received");
            SupplicantStaIfaceHal.this.mWifiMonitor.broadcastAbsAntCoreRobEvent(this.mIfaceName);
        }
    }

    public static /* synthetic */ void lambda$new$0(SupplicantStaIfaceHal supplicantStaIfaceHal, long cookie) {
        synchronized (supplicantStaIfaceHal.mLock) {
            Log.w(TAG, "IServiceManager died: cookie=" + cookie);
            supplicantStaIfaceHal.supplicantServiceDiedHandler(supplicantStaIfaceHal.mDeathRecipientCookie);
            supplicantStaIfaceHal.mIServiceManager = null;
        }
    }

    public static /* synthetic */ void lambda$new$1(SupplicantStaIfaceHal supplicantStaIfaceHal, long cookie) {
        synchronized (supplicantStaIfaceHal.mLock) {
            Log.w(TAG, "ISupplicant died: cookie=" + cookie);
            supplicantStaIfaceHal.supplicantServiceDiedHandler(cookie);
        }
    }

    public SupplicantStaIfaceHal(Context context, WifiMonitor monitor) {
        this.mContext = context;
        this.mWifiMonitor = monitor;
    }

    /* access modifiers changed from: package-private */
    public void enableVerboseLogging(boolean enable) {
        synchronized (this.mLock) {
            this.mVerboseLoggingEnabled = enable;
        }
    }

    private boolean linkToServiceManagerDeath() {
        synchronized (this.mLock) {
            if (this.mIServiceManager == null) {
                return false;
            }
            try {
                if (this.mIServiceManager.linkToDeath(this.mServiceManagerDeathRecipient, 0)) {
                    return true;
                }
                Log.wtf(TAG, "Error on linkToDeath on IServiceManager");
                supplicantServiceDiedHandler(this.mDeathRecipientCookie);
                this.mIServiceManager = null;
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "IServiceManager.linkToDeath exception", e);
                return false;
            }
        }
    }

    public boolean initialize() {
        synchronized (this.mLock) {
            if (this.mVerboseLoggingEnabled) {
                Log.i(TAG, "Registering ISupplicant service ready callback.");
            }
            this.mISupplicant = null;
            this.mISupplicantStaIfaces.clear();
            if (this.mIServiceManager != null) {
                return true;
            }
            try {
                this.mIServiceManager = getServiceManagerMockable();
                if (this.mIServiceManager == null) {
                    Log.e(TAG, "Failed to get HIDL Service Manager");
                    return false;
                } else if (!linkToServiceManagerDeath()) {
                    return false;
                } else {
                    if (!this.mIServiceManager.registerForNotifications(ISupplicant.kInterfaceName, "", this.mServiceNotificationCallback)) {
                        Log.e(TAG, "Failed to register for notifications to android.hardware.wifi.supplicant@1.0::ISupplicant");
                        this.mIServiceManager = null;
                        return false;
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Exception while trying to register a listener for ISupplicant service: " + e);
                supplicantServiceDiedHandler(this.mDeathRecipientCookie);
            }
        }
        return true;
    }

    private boolean linkToSupplicantDeath() {
        synchronized (this.mLock) {
            if (this.mISupplicant == null) {
                return false;
            }
            try {
                ISupplicant iSupplicant = this.mISupplicant;
                IHwBinder.DeathRecipient deathRecipient = this.mSupplicantDeathRecipient;
                long j = this.mDeathRecipientCookie + 1;
                this.mDeathRecipientCookie = j;
                if (iSupplicant.linkToDeath(deathRecipient, j)) {
                    return true;
                }
                Log.wtf(TAG, "Error on linkToDeath on ISupplicant");
                supplicantServiceDiedHandler(this.mDeathRecipientCookie);
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.linkToDeath exception", e);
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean initSupplicantService() {
        synchronized (this.mLock) {
            try {
                this.mISupplicant = getSupplicantMockable();
                if (this.mISupplicant == null) {
                    Log.e(TAG, "Got null ISupplicant service. Stopping supplicant HIDL startup");
                    return false;
                } else if (!linkToSupplicantDeath()) {
                    return false;
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.getService exception: " + e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public int getCurrentNetworkId(String ifaceName) {
        synchronized (this.mLock) {
            WifiConfiguration currentConfig = getCurrentNetworkLocalConfig(ifaceName);
            if (currentConfig == null) {
                return -1;
            }
            int i = currentConfig.networkId;
            return i;
        }
    }

    public boolean setupIface(String ifaceName) {
        ISupplicantIface ifaceHwBinder;
        if (checkSupplicantStaIfaceAndLogFailure(ifaceName, "setupIface") != null) {
            return false;
        }
        if (isV1_1()) {
            ifaceHwBinder = addIfaceV1_1(ifaceName);
        } else {
            ifaceHwBinder = getIfaceV1_0(ifaceName);
        }
        if (ifaceHwBinder == null) {
            Log.e(TAG, "setupIface got null iface");
            return false;
        }
        SupplicantStaIfaceHalCallback callback = new SupplicantStaIfaceHalCallback(ifaceName);
        if (!isV1_1()) {
            ISupplicantStaIface iface = getStaIfaceMockable(ifaceHwBinder);
            if (!registerCallback(iface, callback)) {
                return false;
            }
            this.mISupplicantStaIfaces.put(ifaceName, iface);
            this.mISupplicantStaIfaceCallbacks.put(ifaceName, callback);
        } else if (trySetupForVendorV2_0(ifaceName, ifaceHwBinder, callback)) {
            return true;
        } else {
            android.hardware.wifi.supplicant.V1_1.ISupplicantStaIface iface2 = getStaIfaceMockableV1_1(ifaceHwBinder);
            SupplicantStaIfaceHalCallbackV1_1 callbackV1_1 = new SupplicantStaIfaceHalCallbackV1_1(ifaceName, callback);
            if (!registerCallbackV1_1(iface2, callbackV1_1)) {
                return false;
            }
            this.mISupplicantStaIfaces.put(ifaceName, iface2);
            this.mISupplicantStaIfaceCallbacks.put(ifaceName, callbackV1_1);
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r9.mISupplicant.getInterface(r5, new com.android.server.wifi.$$Lambda$SupplicantStaIfaceHal$RyQnT_v7B4l3vVijvOVBxHlvVoY(r3));
     */
    private ISupplicantIface getIfaceV1_0(String ifaceName) {
        synchronized (this.mLock) {
            ArrayList<ISupplicant.IfaceInfo> supplicantIfaces = new ArrayList<>();
            try {
                this.mISupplicant.listInterfaces(new ISupplicant.listInterfacesCallback(supplicantIfaces) {
                    private final /* synthetic */ ArrayList f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantStaIfaceHal.lambda$getIfaceV1_0$2(this.f$0, supplicantStatus, arrayList);
                    }
                });
                if (supplicantIfaces.size() == 0) {
                    Log.e(TAG, "Got zero HIDL supplicant ifaces. Stopping supplicant HIDL startup.");
                    return null;
                }
                HidlSupport.Mutable<ISupplicantIface> supplicantIface = new HidlSupport.Mutable<>();
                Iterator<ISupplicant.IfaceInfo> it = supplicantIfaces.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ISupplicant.IfaceInfo ifaceInfo = it.next();
                    if (ifaceInfo.type == 0 && ifaceName.equals(ifaceInfo.name)) {
                        try {
                            break;
                        } catch (RemoteException e) {
                            Log.e(TAG, "ISupplicant.getInterface exception: " + e);
                            handleRemoteException(e, "getInterface");
                            return null;
                        }
                    }
                }
                ISupplicantIface iSupplicantIface = (ISupplicantIface) supplicantIface.value;
                return iSupplicantIface;
            } catch (RemoteException e2) {
                Log.e(TAG, "ISupplicant.listInterfaces exception: " + e2);
                handleRemoteException(e2, "listInterfaces");
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$getIfaceV1_0$2(ArrayList supplicantIfaces, SupplicantStatus status, ArrayList ifaces) {
        if (status.code != 0) {
            Log.e(TAG, "Getting Supplicant Interfaces failed: " + status.code);
            return;
        }
        supplicantIfaces.addAll(ifaces);
    }

    static /* synthetic */ void lambda$getIfaceV1_0$3(HidlSupport.Mutable supplicantIface, SupplicantStatus status, ISupplicantIface iface) {
        if (status.code != 0) {
            Log.e(TAG, "Failed to get ISupplicantIface " + status.code);
            return;
        }
        supplicantIface.value = iface;
    }

    private ISupplicantIface addIfaceV1_1(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicant.IfaceInfo ifaceInfo = new ISupplicant.IfaceInfo();
            ifaceInfo.name = ifaceName;
            ifaceInfo.type = 0;
            HidlSupport.Mutable<ISupplicantIface> supplicantIface = new HidlSupport.Mutable<>();
            try {
                if (getSupplicantMockableV1_1() == null) {
                    clearState();
                    Log.e(TAG, "Failed to getSupplicant ");
                    return null;
                }
                getSupplicantMockableV1_1().addInterface(ifaceInfo, new ISupplicant.addInterfaceCallback() {
                    public final void onValues(SupplicantStatus supplicantStatus, ISupplicantIface iSupplicantIface) {
                        SupplicantStaIfaceHal.lambda$addIfaceV1_1$4(HidlSupport.Mutable.this, supplicantStatus, iSupplicantIface);
                    }
                });
                ISupplicantIface iSupplicantIface = (ISupplicantIface) supplicantIface.value;
                return iSupplicantIface;
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.addInterface exception: " + e);
                handleRemoteException(e, "addInterface");
                return null;
            } catch (NullPointerException ex) {
                clearState();
                Log.e(TAG, "ISupplicant.addInterface NullPointerException ", ex);
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$addIfaceV1_1$4(HidlSupport.Mutable supplicantIface, SupplicantStatus status, ISupplicantIface iface) {
        if (status.code == 0 || status.code == 5) {
            supplicantIface.value = iface;
            return;
        }
        Log.e(TAG, "Failed to create ISupplicantIface " + status.code);
    }

    public boolean teardownIface(String ifaceName) {
        synchronized (this.mLock) {
            if (checkSupplicantStaIfaceAndLogFailure(ifaceName, "teardownIface") == null) {
                return false;
            }
            if (isV1_1() && !removeIfaceV1_1(ifaceName)) {
                Log.e(TAG, "Failed to remove iface = " + ifaceName);
                return false;
            } else if (this.mISupplicantStaIfaces.remove(ifaceName) == null) {
                Log.e(TAG, "Trying to teardown unknown inteface");
                return false;
            } else {
                this.mISupplicantStaIfaceCallbacks.remove(ifaceName);
                return true;
            }
        }
    }

    private boolean removeIfaceV1_1(String ifaceName) {
        synchronized (this.mLock) {
            try {
                ISupplicant.IfaceInfo ifaceInfo = new ISupplicant.IfaceInfo();
                ifaceInfo.name = ifaceName;
                ifaceInfo.type = 0;
                android.hardware.wifi.supplicant.V1_1.ISupplicant supplicantV11 = getSupplicantMockableV1_1();
                if (supplicantV11 == null) {
                    Log.e(TAG, "Failed to getSupplicant");
                    return false;
                }
                SupplicantStatus status = supplicantV11.removeInterface(ifaceInfo);
                if (status.code == 0) {
                    return true;
                }
                Log.e(TAG, "Failed to remove iface " + status.code);
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.removeInterface exception: " + e);
                handleRemoteException(e, "removeInterface");
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public boolean registerDeathHandler(WifiNative.SupplicantDeathEventHandler handler) {
        if (this.mDeathEventHandler != null) {
            Log.e(TAG, "Death handler already present");
        }
        this.mDeathEventHandler = handler;
        return true;
    }

    public boolean deregisterDeathHandler() {
        if (this.mDeathEventHandler == null) {
            Log.e(TAG, "No Death handler present");
        }
        this.mDeathEventHandler = null;
        return true;
    }

    private void clearState() {
        synchronized (this.mLock) {
            this.mISupplicant = null;
            this.mISupplicantStaIfaces.clear();
            this.mCurrentNetworkLocalConfigs.clear();
            this.mCurrentNetworkRemoteHandles.clear();
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003b, code lost:
        return;
     */
    public void supplicantServiceDiedHandler(long cookie) {
        synchronized (this.mLock) {
            if (this.mDeathRecipientCookie != cookie) {
                Log.i(TAG, "Ignoring stale death recipient notification");
                return;
            }
            for (String ifaceName : this.mISupplicantStaIfaces.keySet()) {
                this.mWifiMonitor.broadcastSupplicantDisconnectionEvent(ifaceName);
            }
            clearState();
            if (this.mDeathEventHandler != null) {
                this.mDeathEventHandler.onDeath();
            }
        }
    }

    public boolean isInitializationStarted() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mIServiceManager != null;
        }
        return z;
    }

    public boolean isInitializationComplete() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mISupplicant != null;
        }
        return z;
    }

    public void terminate() {
        synchronized (this.mLock) {
            if (checkSupplicantAndLogFailure("terminate")) {
                try {
                    if (isV1_1()) {
                        android.hardware.wifi.supplicant.V1_1.ISupplicant supplicantV11 = getSupplicantMockableV1_1();
                        if (supplicantV11 == null) {
                            Log.e(TAG, "Failed to getSupplicant");
                            return;
                        }
                        supplicantV11.terminate();
                    }
                } catch (RemoteException e) {
                    handleRemoteException(e, "terminate");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public IServiceManager getServiceManagerMockable() throws RemoteException {
        IServiceManager service;
        synchronized (this.mLock) {
            service = IServiceManager.getService();
        }
        return service;
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.supplicant.V1_0.ISupplicant getSupplicantMockable() throws RemoteException {
        android.hardware.wifi.supplicant.V1_0.ISupplicant service;
        synchronized (this.mLock) {
            try {
                service = android.hardware.wifi.supplicant.V1_0.ISupplicant.getService();
            } catch (NoSuchElementException e) {
                Log.e(TAG, "Failed to get ISupplicant", e);
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        return service;
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.supplicant.V1_1.ISupplicant getSupplicantMockableV1_1() throws RemoteException {
        android.hardware.wifi.supplicant.V1_1.ISupplicant castFrom;
        synchronized (this.mLock) {
            try {
                castFrom = android.hardware.wifi.supplicant.V1_1.ISupplicant.castFrom(android.hardware.wifi.supplicant.V1_0.ISupplicant.getService());
            } catch (NoSuchElementException e) {
                Log.e(TAG, "Failed to get ISupplicant", e);
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        return castFrom;
    }

    /* access modifiers changed from: protected */
    public ISupplicantStaIface getStaIfaceMockable(ISupplicantIface iface) {
        ISupplicantStaIface asInterface;
        synchronized (this.mLock) {
            asInterface = ISupplicantStaIface.asInterface(iface.asBinder());
        }
        return asInterface;
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.supplicant.V1_1.ISupplicantStaIface getStaIfaceMockableV1_1(ISupplicantIface iface) {
        android.hardware.wifi.supplicant.V1_1.ISupplicantStaIface asInterface;
        synchronized (this.mLock) {
            asInterface = android.hardware.wifi.supplicant.V1_1.ISupplicantStaIface.asInterface(iface.asBinder());
        }
        return asInterface;
    }

    private boolean isV1_1() {
        boolean z;
        synchronized (this.mLock) {
            z = false;
            try {
                if (getSupplicantMockableV1_1() != null) {
                    z = true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.getService exception: " + e);
                handleRemoteException(e, "getSupplicantMockable");
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return z;
    }

    private ISupplicantStaIface getStaIface(String ifaceName) {
        return this.mISupplicantStaIfaces.get(ifaceName);
    }

    private SupplicantStaNetworkHal getCurrentNetworkRemoteHandle(String ifaceName) {
        return this.mCurrentNetworkRemoteHandles.get(ifaceName);
    }

    private WifiConfiguration getCurrentNetworkLocalConfig(String ifaceName) {
        return this.mCurrentNetworkLocalConfigs.get(ifaceName);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0063, code lost:
        return null;
     */
    private Pair<SupplicantStaNetworkHal, WifiConfiguration> addNetworkAndSaveConfig(String ifaceName, WifiConfiguration config) {
        synchronized (this.mLock) {
            logi("addSupplicantStaNetwork via HIDL");
            if (config == null) {
                loge("Cannot add NULL network!");
                return null;
            }
            SupplicantStaNetworkHal network = addNetwork(ifaceName);
            if (network == null) {
                loge("Failed to add a network!");
                return null;
            }
            boolean saveSuccess = false;
            try {
                saveSuccess = network.saveWifiConfiguration(config);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Exception while saving config params: " + config, e);
            }
            if (!saveSuccess) {
                loge("Failed to save variables for: " + config.configKey());
                if (!removeAllNetworks(ifaceName)) {
                    loge("Failed to remove all networks on failure.");
                }
            } else {
                Pair<SupplicantStaNetworkHal, WifiConfiguration> pair = new Pair<>(network, new WifiConfiguration(config));
                return pair;
            }
        }
    }

    public boolean connectToNetwork(String ifaceName, WifiConfiguration config) {
        synchronized (this.mLock) {
            logd("connectToNetwork " + config.configKey());
            WifiConfiguration currentConfig = getCurrentNetworkLocalConfig(ifaceName);
            if (!WifiConfigurationUtil.isSameNetwork(config, currentConfig)) {
                this.mCurrentNetworkRemoteHandles.remove(ifaceName);
                this.mCurrentNetworkLocalConfigs.remove(ifaceName);
                if (!removeAllNetworks(ifaceName)) {
                    loge("Failed to remove existing networks");
                    return false;
                }
                Pair<SupplicantStaNetworkHal, WifiConfiguration> pair = addNetworkAndSaveConfig(ifaceName, config);
                if (pair == null) {
                    loge("Failed to add/save network configuration: " + config.configKey());
                    return false;
                }
                this.mCurrentNetworkRemoteHandles.put(ifaceName, (SupplicantStaNetworkHal) pair.first);
                this.mCurrentNetworkLocalConfigs.put(ifaceName, (WifiConfiguration) pair.second);
            } else if (Objects.equals(config.getNetworkSelectionStatus().getNetworkSelectionBSSID(), currentConfig.getNetworkSelectionStatus().getNetworkSelectionBSSID())) {
                logd("Network is already saved, will not trigger remove and add operation.");
            } else {
                logd("Network is already saved, but need to update BSSID.");
                if (!setCurrentNetworkBssid(ifaceName, config.getNetworkSelectionStatus().getNetworkSelectionBSSID())) {
                    loge("Failed to set current network BSSID.");
                    return false;
                }
                this.mCurrentNetworkLocalConfigs.put(ifaceName, new WifiConfiguration(config));
            }
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "connectToNetwork");
            if (networkHandle != null) {
                if (networkHandle.select()) {
                    return true;
                }
            }
            loge("Failed to select network configuration: " + config.configKey());
            return false;
        }
    }

    public boolean roamToNetwork(String ifaceName, WifiConfiguration config) {
        synchronized (this.mLock) {
            if (getCurrentNetworkId(ifaceName) != config.networkId) {
                Log.w(TAG, "Cannot roam to a different network, initiate new connection. Current network ID: " + getCurrentNetworkId(ifaceName));
                boolean connectToNetwork = connectToNetwork(ifaceName, config);
                return connectToNetwork;
            }
            String bssid = config.getNetworkSelectionStatus().getNetworkSelectionBSSID();
            logd("roamToNetwork" + config.configKey() + " (bssid " + StringUtil.safeDisplayBssid(bssid) + ")");
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "roamToNetwork");
            if (networkHandle != null) {
                if (networkHandle.setBssid(bssid)) {
                    if (reassociate(ifaceName)) {
                        return true;
                    }
                    loge("Failed to trigger reassociate");
                    return false;
                }
            }
            loge("Failed to set new bssid on network: " + config.configKey());
            return false;
        }
    }

    public boolean loadNetworks(String ifaceName, Map<String, WifiConfiguration> configs, SparseArray<Map<String, String>> networkExtras) {
        WifiConfiguration config;
        String str = ifaceName;
        SparseArray<Map<String, String>> sparseArray = networkExtras;
        synchronized (this.mLock) {
            try {
                List<Integer> networkIds = listNetworks(ifaceName);
                boolean z = false;
                if (networkIds == null) {
                    Log.e(TAG, "Failed to list networks");
                    return false;
                }
                for (Integer networkId : networkIds) {
                    SupplicantStaNetworkHal network = getNetwork(str, networkId.intValue());
                    if (network == null) {
                        Log.e(TAG, "Failed to get network with ID: " + networkId);
                        return z;
                    }
                    config = new WifiConfiguration();
                    Map<String, String> networkExtra = new HashMap<>();
                    boolean loadSuccess = z;
                    loadSuccess = network.loadWifiConfiguration(config, networkExtra);
                    int appId = UserHandle.getAppId(config.creatorUid);
                    if (config.BSSID != null && (appId == 0 || appId == 1000 || appId == 1010)) {
                        Log.w(TAG, "loadNetworks creater: " + config.creatorUid + ", ssid: " + config.SSID + ", Bssid:" + ScanResultUtil.getConfusedBssid(config.BSSID));
                        config.BSSID = null;
                    }
                    if (!loadSuccess) {
                        Log.e(TAG, "Failed to load wifi configuration for network with ID: " + networkId + ". Skipping...");
                    } else {
                        config.setIpAssignment(IpConfiguration.IpAssignment.DHCP);
                        config.setProxySettings(IpConfiguration.ProxySettings.NONE);
                        sparseArray.put(networkId.intValue(), networkExtra);
                        String configKey = networkExtra.get("configKey");
                        try {
                            WifiConfiguration duplicateConfig = configs.put(configKey, config);
                            if (duplicateConfig != null) {
                                StringBuilder sb = new StringBuilder();
                                String str2 = configKey;
                                sb.append("Replacing duplicate network: ");
                                sb.append(duplicateConfig.networkId);
                                Log.i(TAG, sb.toString());
                                removeNetwork(str, duplicateConfig.networkId);
                                sparseArray.remove(duplicateConfig.networkId);
                            }
                            z = false;
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                }
                Map<String, WifiConfiguration> map = configs;
                return true;
            } catch (IllegalArgumentException e) {
                Log.wtf(TAG, "Exception while loading config params: " + config, e);
            } catch (Throwable th2) {
                th = th2;
                Map<String, WifiConfiguration> map2 = configs;
                throw th;
            }
        }
    }

    public void removeNetworkIfCurrent(String ifaceName, int networkId) {
        synchronized (this.mLock) {
            if (getCurrentNetworkId(ifaceName) == networkId) {
                removeAllNetworks(ifaceName);
            }
        }
    }

    public boolean removeAllNetworks(String ifaceName) {
        synchronized (this.mLock) {
            ArrayList<Integer> networks = listNetworks(ifaceName);
            if (networks == null) {
                Log.e(TAG, "removeAllNetworks failed, got null networks");
                return false;
            }
            Iterator<Integer> it = networks.iterator();
            while (it.hasNext()) {
                int id = it.next().intValue();
                if (!removeNetwork(ifaceName, id)) {
                    Log.e(TAG, "removeAllNetworks failed to remove network: " + id);
                    return false;
                }
            }
            this.mCurrentNetworkRemoteHandles.remove(ifaceName);
            this.mCurrentNetworkLocalConfigs.remove(ifaceName);
            return true;
        }
    }

    public boolean setCurrentNetworkBssid(String ifaceName, String bssidStr) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "setCurrentNetworkBssid");
            if (networkHandle == null) {
                return false;
            }
            boolean bssid = networkHandle.setBssid(bssidStr);
            return bssid;
        }
    }

    public String getCurrentNetworkWpsNfcConfigurationToken(String ifaceName) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "getCurrentNetworkWpsNfcConfigurationToken");
            if (networkHandle == null) {
                return null;
            }
            String wpsNfcConfigurationToken = networkHandle.getWpsNfcConfigurationToken();
            return wpsNfcConfigurationToken;
        }
    }

    public String getCurrentNetworkEapAnonymousIdentity(String ifaceName) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "getCurrentNetworkEapAnonymousIdentity");
            if (networkHandle == null) {
                return null;
            }
            String fetchEapAnonymousIdentity = networkHandle.fetchEapAnonymousIdentity();
            return fetchEapAnonymousIdentity;
        }
    }

    public boolean sendCurrentNetworkEapIdentityResponse(String ifaceName, String identity, String encryptedIdentity) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "sendCurrentNetworkEapIdentityResponse");
            if (networkHandle == null) {
                return false;
            }
            boolean sendNetworkEapIdentityResponse = networkHandle.sendNetworkEapIdentityResponse(identity, encryptedIdentity);
            return sendNetworkEapIdentityResponse;
        }
    }

    public boolean sendCurrentNetworkEapSimGsmAuthResponse(String ifaceName, String paramsStr) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "sendCurrentNetworkEapSimGsmAuthResponse");
            if (networkHandle == null) {
                return false;
            }
            boolean sendNetworkEapSimGsmAuthResponse = networkHandle.sendNetworkEapSimGsmAuthResponse(paramsStr);
            return sendNetworkEapSimGsmAuthResponse;
        }
    }

    public boolean sendCurrentNetworkEapSimGsmAuthFailure(String ifaceName) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "sendCurrentNetworkEapSimGsmAuthFailure");
            if (networkHandle == null) {
                return false;
            }
            boolean sendNetworkEapSimGsmAuthFailure = networkHandle.sendNetworkEapSimGsmAuthFailure();
            return sendNetworkEapSimGsmAuthFailure;
        }
    }

    public boolean sendCurrentNetworkEapSimUmtsAuthResponse(String ifaceName, String paramsStr) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "sendCurrentNetworkEapSimUmtsAuthResponse");
            if (networkHandle == null) {
                return false;
            }
            boolean sendNetworkEapSimUmtsAuthResponse = networkHandle.sendNetworkEapSimUmtsAuthResponse(paramsStr);
            return sendNetworkEapSimUmtsAuthResponse;
        }
    }

    public boolean sendCurrentNetworkEapSimUmtsAutsResponse(String ifaceName, String paramsStr) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "sendCurrentNetworkEapSimUmtsAutsResponse");
            if (networkHandle == null) {
                return false;
            }
            boolean sendNetworkEapSimUmtsAutsResponse = networkHandle.sendNetworkEapSimUmtsAutsResponse(paramsStr);
            return sendNetworkEapSimUmtsAutsResponse;
        }
    }

    public boolean sendCurrentNetworkEapSimUmtsAuthFailure(String ifaceName) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHandle = checkSupplicantStaNetworkAndLogFailure(ifaceName, "sendCurrentNetworkEapSimUmtsAuthFailure");
            if (networkHandle == null) {
                return false;
            }
            boolean sendNetworkEapSimUmtsAuthFailure = networkHandle.sendNetworkEapSimUmtsAuthFailure();
            return sendNetworkEapSimUmtsAuthFailure;
        }
    }

    private SupplicantStaNetworkHal addNetwork(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "addNetwork");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<ISupplicantNetwork> newNetwork = new HidlSupport.Mutable<>();
            try {
                iface.addNetwork(new ISupplicantIface.addNetworkCallback(newNetwork) {
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
                        SupplicantStaIfaceHal.lambda$addNetwork$5(SupplicantStaIfaceHal.this, this.f$1, supplicantStatus, iSupplicantNetwork);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "addNetwork");
            }
            if (newNetwork.value == null) {
                return null;
            }
            SupplicantStaNetworkHal staNetworkMockable = getStaNetworkMockable(ifaceName, ISupplicantStaNetwork.asInterface(((ISupplicantNetwork) newNetwork.value).asBinder()));
            return staNetworkMockable;
        }
    }

    public static /* synthetic */ void lambda$addNetwork$5(SupplicantStaIfaceHal supplicantStaIfaceHal, HidlSupport.Mutable newNetwork, SupplicantStatus status, ISupplicantNetwork network) {
        if (supplicantStaIfaceHal.checkStatusAndLogFailure(status, "addNetwork")) {
            newNetwork.value = network;
        }
    }

    private boolean removeNetwork(String ifaceName, int id) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "removeNetwork");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.removeNetwork(id), "removeNetwork");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "removeNetwork");
                return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public SupplicantStaNetworkHal getStaNetworkMockable(String ifaceName, ISupplicantStaNetwork iSupplicantStaNetwork) {
        SupplicantStaNetworkHal network;
        synchronized (this.mLock) {
            network = new SupplicantStaNetworkHal(iSupplicantStaNetwork, ifaceName, this.mContext, this.mWifiMonitor);
            network.enableVerboseLogging(this.mVerboseLoggingEnabled);
        }
        return network;
    }

    private SupplicantStaNetworkHal getNetwork(String ifaceName, int id) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "getNetwork");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<ISupplicantNetwork> gotNetwork = new HidlSupport.Mutable<>();
            try {
                iface.getNetwork(id, new ISupplicantIface.getNetworkCallback(gotNetwork) {
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
                        SupplicantStaIfaceHal.lambda$getNetwork$6(SupplicantStaIfaceHal.this, this.f$1, supplicantStatus, iSupplicantNetwork);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "getNetwork");
            }
            if (gotNetwork.value == null) {
                return null;
            }
            SupplicantStaNetworkHal staNetworkMockable = getStaNetworkMockable(ifaceName, ISupplicantStaNetwork.asInterface(((ISupplicantNetwork) gotNetwork.value).asBinder()));
            return staNetworkMockable;
        }
    }

    public static /* synthetic */ void lambda$getNetwork$6(SupplicantStaIfaceHal supplicantStaIfaceHal, HidlSupport.Mutable gotNetwork, SupplicantStatus status, ISupplicantNetwork network) {
        if (supplicantStaIfaceHal.checkStatusAndLogFailure(status, "getNetwork")) {
            gotNetwork.value = network;
        }
    }

    private boolean registerCallback(ISupplicantStaIface iface, android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback callback) {
        synchronized (this.mLock) {
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.registerCallback(callback), "registerCallback");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "registerCallback");
                return false;
            }
        }
    }

    private boolean registerCallbackV1_1(android.hardware.wifi.supplicant.V1_1.ISupplicantStaIface iface, android.hardware.wifi.supplicant.V1_1.ISupplicantStaIfaceCallback callback) {
        synchronized (this.mLock) {
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.registerCallback_1_1(callback), "registerCallback_1_1");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "registerCallback_1_1");
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
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantStaIfaceHal.lambda$listNetworks$7(SupplicantStaIfaceHal.this, this.f$1, supplicantStatus, arrayList);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "listNetworks");
            }
            ArrayList<Integer> arrayList = (ArrayList) networkIdList.value;
            return arrayList;
        }
    }

    public static /* synthetic */ void lambda$listNetworks$7(SupplicantStaIfaceHal supplicantStaIfaceHal, HidlSupport.Mutable networkIdList, SupplicantStatus status, ArrayList networkIds) {
        if (supplicantStaIfaceHal.checkStatusAndLogFailure(status, "listNetworks")) {
            networkIdList.value = networkIds;
        }
    }

    public boolean setWpsDeviceName(String ifaceName, String name) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsDeviceName");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsDeviceName(name), "setWpsDeviceName");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsDeviceName");
                return false;
            }
        }
    }

    public boolean setWpsDeviceType(String ifaceName, String typeStr) {
        synchronized (this.mLock) {
            try {
                Matcher match = WPS_DEVICE_TYPE_PATTERN.matcher(typeStr);
                if (match.find()) {
                    if (match.groupCount() == 3) {
                        short categ = Short.parseShort(match.group(1));
                        byte[] oui = NativeUtil.hexStringToByteArray(match.group(2));
                        short subCateg = Short.parseShort(match.group(3));
                        byte[] bytes = new byte[8];
                        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
                        byteBuffer.putShort(categ);
                        byteBuffer.put(oui);
                        byteBuffer.putShort(subCateg);
                        boolean wpsDeviceType = setWpsDeviceType(ifaceName, bytes);
                        return wpsDeviceType;
                    }
                }
                Log.e(TAG, "Malformed WPS device type " + typeStr);
                return false;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + typeStr, e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private boolean setWpsDeviceType(String ifaceName, byte[] type) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsDeviceType");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsDeviceType(type), "setWpsDeviceType");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsDeviceType");
                return false;
            }
        }
    }

    public boolean setWpsManufacturer(String ifaceName, String manufacturer) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsManufacturer");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsManufacturer(manufacturer), "setWpsManufacturer");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsManufacturer");
                return false;
            }
        }
    }

    public boolean setWpsModelName(String ifaceName, String modelName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsModelName");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsModelName(modelName), "setWpsModelName");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsModelName");
                return false;
            }
        }
    }

    public boolean setWpsModelNumber(String ifaceName, String modelNumber) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsModelNumber");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsModelNumber(modelNumber), "setWpsModelNumber");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsModelNumber");
                return false;
            }
        }
    }

    public boolean setWpsSerialNumber(String ifaceName, String serialNumber) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsSerialNumber");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsSerialNumber(serialNumber), "setWpsSerialNumber");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsSerialNumber");
                return false;
            }
        }
    }

    public boolean setWpsConfigMethods(String ifaceName, String configMethodsStr) {
        boolean wpsConfigMethods;
        synchronized (this.mLock) {
            short configMethodsMask = 0;
            String[] configMethodsStrArr = configMethodsStr.split("\\s+");
            for (String stringToWpsConfigMethod : configMethodsStrArr) {
                configMethodsMask = (short) (stringToWpsConfigMethod(stringToWpsConfigMethod) | configMethodsMask);
            }
            wpsConfigMethods = setWpsConfigMethods(ifaceName, configMethodsMask);
        }
        return wpsConfigMethods;
    }

    private boolean setWpsConfigMethods(String ifaceName, short configMethods) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setWpsConfigMethods");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setWpsConfigMethods(configMethods), "setWpsConfigMethods");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setWpsConfigMethods");
                return false;
            }
        }
    }

    public boolean reassociate(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "reassociate");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.reassociate(), "reassociate");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "reassociate");
                return false;
            }
        }
    }

    public boolean reconnect(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "reconnect");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.reconnect(), "reconnect");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "reconnect");
                return false;
            }
        }
    }

    public boolean disconnect(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "disconnect");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.disconnect(), "disconnect");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "disconnect");
                return false;
            }
        }
    }

    public boolean setPowerSave(String ifaceName, boolean enable) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setPowerSave");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setPowerSave(enable), "setPowerSave");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setPowerSave");
                return false;
            }
        }
    }

    public boolean initiateTdlsDiscover(String ifaceName, String macAddress) {
        boolean initiateTdlsDiscover;
        synchronized (this.mLock) {
            try {
                initiateTdlsDiscover = initiateTdlsDiscover(ifaceName, NativeUtil.macAddressToByteArray(macAddress));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + macAddress, e);
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
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.initiateTdlsDiscover(macAddress), "initiateTdlsDiscover");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "initiateTdlsDiscover");
                return false;
            }
        }
    }

    public boolean initiateTdlsSetup(String ifaceName, String macAddress) {
        boolean initiateTdlsSetup;
        synchronized (this.mLock) {
            try {
                initiateTdlsSetup = initiateTdlsSetup(ifaceName, NativeUtil.macAddressToByteArray(macAddress));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + macAddress, e);
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
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.initiateTdlsSetup(macAddress), "initiateTdlsSetup");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "initiateTdlsSetup");
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
                Log.e(TAG, "Illegal argument " + macAddress, e);
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
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.initiateTdlsTeardown(macAddress), "initiateTdlsTeardown");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "initiateTdlsTeardown");
                return false;
            }
        }
    }

    public boolean initiateAnqpQuery(String ifaceName, String bssid, ArrayList<Short> infoElements, ArrayList<Integer> hs20SubTypes) {
        boolean initiateAnqpQuery;
        synchronized (this.mLock) {
            try {
                initiateAnqpQuery = initiateAnqpQuery(ifaceName, NativeUtil.macAddressToByteArray(bssid), infoElements, hs20SubTypes);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + StringUtil.safeDisplayBssid(bssid), e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return initiateAnqpQuery;
    }

    private boolean initiateAnqpQuery(String ifaceName, byte[] macAddress, ArrayList<Short> infoElements, ArrayList<Integer> subTypes) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "initiateAnqpQuery");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.initiateAnqpQuery(macAddress, infoElements, subTypes), "initiateAnqpQuery");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "initiateAnqpQuery");
                return false;
            }
        }
    }

    public boolean initiateHs20IconQuery(String ifaceName, String bssid, String fileName) {
        boolean initiateHs20IconQuery;
        synchronized (this.mLock) {
            try {
                initiateHs20IconQuery = initiateHs20IconQuery(ifaceName, NativeUtil.macAddressToByteArray(bssid), fileName);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + StringUtil.safeDisplayBssid(bssid), e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return initiateHs20IconQuery;
    }

    private boolean initiateHs20IconQuery(String ifaceName, byte[] macAddress, String fileName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "initiateHs20IconQuery");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.initiateHs20IconQuery(macAddress, fileName), "initiateHs20IconQuery");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "initiateHs20IconQuery");
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
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
                        SupplicantStaIfaceHal.lambda$getMacAddress$8(SupplicantStaIfaceHal.this, this.f$1, supplicantStatus, bArr);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "getMacAddress");
            }
            String str = (String) gotMac.value;
            return str;
        }
    }

    public static /* synthetic */ void lambda$getMacAddress$8(SupplicantStaIfaceHal supplicantStaIfaceHal, HidlSupport.Mutable gotMac, SupplicantStatus status, byte[] macAddr) {
        if (supplicantStaIfaceHal.checkStatusAndLogFailure(status, "getMacAddress")) {
            gotMac.value = NativeUtil.macAddressFromByteArray(macAddr);
        }
    }

    public boolean startRxFilter(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "startRxFilter");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.startRxFilter(), "startRxFilter");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "startRxFilter");
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
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.stopRxFilter(), "stopRxFilter");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "stopRxFilter");
                return false;
            }
        }
    }

    public boolean addRxFilter(String ifaceName, int type) {
        byte halType;
        synchronized (this.mLock) {
            switch (type) {
                case 0:
                    halType = 0;
                    break;
                case 1:
                    halType = 1;
                    break;
                default:
                    Log.e(TAG, "Invalid Rx Filter type: " + type);
                    return false;
            }
            boolean addRxFilter = addRxFilter(ifaceName, halType);
            return addRxFilter;
        }
    }

    private boolean addRxFilter(String ifaceName, byte type) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "addRxFilter");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.addRxFilter(type), "addRxFilter");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "addRxFilter");
                return false;
            }
        }
    }

    public boolean removeRxFilter(String ifaceName, int type) {
        byte halType;
        synchronized (this.mLock) {
            switch (type) {
                case 0:
                    halType = 0;
                    break;
                case 1:
                    halType = 1;
                    break;
                default:
                    Log.e(TAG, "Invalid Rx Filter type: " + type);
                    return false;
            }
            boolean removeRxFilter = removeRxFilter(ifaceName, halType);
            return removeRxFilter;
        }
    }

    private boolean removeRxFilter(String ifaceName, byte type) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "removeRxFilter");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.removeRxFilter(type), "removeRxFilter");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "removeRxFilter");
                return false;
            }
        }
    }

    public boolean setBtCoexistenceMode(String ifaceName, int mode) {
        byte halMode;
        synchronized (this.mLock) {
            switch (mode) {
                case 0:
                    halMode = 0;
                    break;
                case 1:
                    halMode = 1;
                    break;
                case 2:
                    halMode = 2;
                    break;
                default:
                    Log.e(TAG, "Invalid Bt Coex mode: " + mode);
                    return false;
            }
            boolean btCoexistenceMode = setBtCoexistenceMode(ifaceName, halMode);
            return btCoexistenceMode;
        }
    }

    private boolean setBtCoexistenceMode(String ifaceName, byte mode) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setBtCoexistenceMode");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setBtCoexistenceMode(mode), "setBtCoexistenceMode");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setBtCoexistenceMode");
                return false;
            }
        }
    }

    public boolean setBtCoexistenceScanModeEnabled(String ifaceName, boolean enable) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setBtCoexistenceScanModeEnabled");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setBtCoexistenceScanModeEnabled(enable), "setBtCoexistenceScanModeEnabled");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setBtCoexistenceScanModeEnabled");
                return false;
            }
        }
    }

    public boolean setSuspendModeEnabled(String ifaceName, boolean enable) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setSuspendModeEnabled");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setSuspendModeEnabled(enable), "setSuspendModeEnabled");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setSuspendModeEnabled");
                return false;
            }
        }
    }

    public boolean setCountryCode(String ifaceName, String codeStr) {
        synchronized (this.mLock) {
            if (TextUtils.isEmpty(codeStr)) {
                return false;
            }
            boolean countryCode = setCountryCode(ifaceName, NativeUtil.stringToByteArray(codeStr));
            return countryCode;
        }
    }

    private boolean setCountryCode(String ifaceName, byte[] code) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setCountryCode");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setCountryCode(code), "setCountryCode");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setCountryCode");
                return false;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003a, code lost:
        return false;
     */
    public boolean startWpsRegistrar(String ifaceName, String bssidStr, String pin) {
        synchronized (this.mLock) {
            if (!TextUtils.isEmpty(bssidStr) && !TextUtils.isEmpty(pin)) {
                try {
                    boolean startWpsRegistrar = startWpsRegistrar(ifaceName, NativeUtil.macAddressToByteArray(bssidStr), pin);
                    return startWpsRegistrar;
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Illegal argument " + StringUtil.safeDisplayBssid(bssidStr), e);
                    return false;
                }
            }
        }
    }

    private boolean startWpsRegistrar(String ifaceName, byte[] bssid, String pin) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "startWpsRegistrar");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.startWpsRegistrar(bssid, pin), "startWpsRegistrar");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "startWpsRegistrar");
                return false;
            }
        }
    }

    public boolean startWpsPbc(String ifaceName, String bssidStr) {
        boolean startWpsPbc;
        synchronized (this.mLock) {
            try {
                startWpsPbc = startWpsPbc(ifaceName, NativeUtil.macAddressToByteArray(bssidStr));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + StringUtil.safeDisplayBssid(bssidStr), e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return startWpsPbc;
    }

    private boolean startWpsPbc(String ifaceName, byte[] bssid) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "startWpsPbc");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.startWpsPbc(bssid), "startWpsPbc");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "startWpsPbc");
                return false;
            }
        }
    }

    public boolean startWpsPinKeypad(String ifaceName, String pin) {
        if (TextUtils.isEmpty(pin)) {
            return false;
        }
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "startWpsPinKeypad");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.startWpsPinKeypad(pin), "startWpsPinKeypad");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "startWpsPinKeypad");
                return false;
            }
        }
    }

    public String startWpsPinDisplay(String ifaceName, String bssidStr) {
        String startWpsPinDisplay;
        synchronized (this.mLock) {
            try {
                startWpsPinDisplay = startWpsPinDisplay(ifaceName, NativeUtil.macAddressToByteArray(bssidStr));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument " + StringUtil.safeDisplayBssid(bssidStr), e);
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        return startWpsPinDisplay;
    }

    private String startWpsPinDisplay(String ifaceName, byte[] bssid) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "startWpsPinDisplay");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotPin = new HidlSupport.Mutable<>();
            try {
                iface.startWpsPinDisplay(bssid, new ISupplicantStaIface.startWpsPinDisplayCallback(gotPin) {
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaIfaceHal.lambda$startWpsPinDisplay$9(SupplicantStaIfaceHal.this, this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "startWpsPinDisplay");
            }
            String str = (String) gotPin.value;
            return str;
        }
    }

    public static /* synthetic */ void lambda$startWpsPinDisplay$9(SupplicantStaIfaceHal supplicantStaIfaceHal, HidlSupport.Mutable gotPin, SupplicantStatus status, String pin) {
        if (supplicantStaIfaceHal.checkStatusAndLogFailure(status, "startWpsPinDisplay")) {
            gotPin.value = pin;
        }
    }

    public boolean cancelWps(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "cancelWps");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.cancelWps(), "cancelWps");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "cancelWps");
                return false;
            }
        }
    }

    public boolean setExternalSim(String ifaceName, boolean useExternalSim) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "setExternalSim");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setExternalSim(useExternalSim), "setExternalSim");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setExternalSim");
                return false;
            }
        }
    }

    public boolean enableAutoReconnect(String ifaceName, boolean enable) {
        synchronized (this.mLock) {
            ISupplicantStaIface iface = checkSupplicantStaIfaceAndLogFailure(ifaceName, "enableAutoReconnect");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.enableAutoReconnect(enable), "enableAutoReconnect");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "enableAutoReconnect");
                return false;
            }
        }
    }

    public boolean setLogLevel(boolean turnOnVerbose) {
        boolean debugParams;
        synchronized (this.mLock) {
            Log.i(TAG, "Force Supplicant log to debug level");
            debugParams = setDebugParams(2, false, false);
        }
        return debugParams;
    }

    private boolean setDebugParams(int level, boolean showTimestamp, boolean showKeys) {
        synchronized (this.mLock) {
            if (!checkSupplicantAndLogFailure("setDebugParams")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicant.setDebugParams(level, showTimestamp, showKeys), "setDebugParams");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setDebugParams");
                return false;
            }
        }
    }

    public boolean setConcurrencyPriority(boolean isStaHigherPriority) {
        synchronized (this.mLock) {
            if (isStaHigherPriority) {
                try {
                    boolean concurrencyPriority = setConcurrencyPriority(0);
                    return concurrencyPriority;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                boolean concurrencyPriority2 = setConcurrencyPriority(1);
                return concurrencyPriority2;
            }
        }
    }

    private boolean setConcurrencyPriority(int type) {
        synchronized (this.mLock) {
            if (!checkSupplicantAndLogFailure("setConcurrencyPriority")) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(this.mISupplicant.setConcurrencyPriority(type), "setConcurrencyPriority");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setConcurrencyPriority");
                return false;
            }
        }
    }

    private boolean checkSupplicantAndLogFailure(String methodStr) {
        synchronized (this.mLock) {
            this.mHalMethod = methodStr;
            this.mHalCallStartTime = SystemClock.uptimeMillis();
            if (this.mISupplicant != null) {
                return true;
            }
            Log.e(TAG, "Can't call " + methodStr + ", ISupplicant is null");
            return false;
        }
    }

    private ISupplicantStaIface checkSupplicantStaIfaceAndLogFailure(String ifaceName, String methodStr) {
        synchronized (this.mLock) {
            this.mHalMethod = methodStr;
            this.mHalCallStartTime = SystemClock.uptimeMillis();
            ISupplicantStaIface iface = getStaIface(ifaceName);
            if (iface != null) {
                return iface;
            }
            Log.e(TAG, "Can't call " + methodStr + ", ISupplicantStaIface is null");
            return null;
        }
    }

    private SupplicantStaNetworkHal checkSupplicantStaNetworkAndLogFailure(String ifaceName, String methodStr) {
        synchronized (this.mLock) {
            SupplicantStaNetworkHal networkHal = getCurrentNetworkRemoteHandle(ifaceName);
            if (networkHal != null) {
                return networkHal;
            }
            Log.e(TAG, "Can't call " + methodStr + ", SupplicantStaNetwork is null");
            return null;
        }
    }

    private boolean checkStatusAndLogFailure(SupplicantStatus status, String methodStr) {
        checkHalCallThresholdMs(status, methodStr);
        synchronized (this.mLock) {
            if (status.code != 0) {
                Log.e(TAG, "ISupplicantStaIface." + methodStr + " failed: " + status);
                return false;
            }
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "ISupplicantStaIface." + methodStr + " succeeded");
            }
            return true;
        }
    }

    private void checkHalCallThresholdMs(SupplicantStatus status, String methodStr) {
        long mHalCallEndTime = SystemClock.uptimeMillis();
        if (!this.mHalMethod.equals(methodStr)) {
            Log.w(TAG, "error, mHalCallStartTime in:" + this.mHalMethod + ", mHalCallEndTime in:" + methodStr);
        } else if (mHalCallEndTime - this.mHalCallStartTime > 300) {
            Log.w(TAG, "Hal call took " + (mHalCallEndTime - this.mHalCallStartTime) + "ms on " + methodStr + ", status.code:" + SupplicantStatusCode.toString(status.code), new Exception());
        }
    }

    /* access modifiers changed from: private */
    public void logCallback(String methodStr) {
        synchronized (this.mLock) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "ISupplicantStaIfaceCallback." + methodStr + " received");
            }
        }
    }

    private void handleRemoteException(RemoteException e, String methodStr) {
        synchronized (this.mLock) {
            clearState();
            Log.e(TAG, "ISupplicantStaIface." + methodStr + " failed with exception", e);
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private static short stringToWpsConfigMethod(String configMethod) {
        char c;
        switch (configMethod.hashCode()) {
            case -1781962557:
                if (configMethod.equals("virtual_push_button")) {
                    c = 9;
                    break;
                }
            case -1419358249:
                if (configMethod.equals("ethernet")) {
                    c = 1;
                    break;
                }
            case -1134657068:
                if (configMethod.equals("keypad")) {
                    c = 8;
                    break;
                }
            case -614489202:
                if (configMethod.equals("virtual_display")) {
                    c = 12;
                    break;
                }
            case -522593958:
                if (configMethod.equals("physical_display")) {
                    c = 13;
                    break;
                }
            case -423872603:
                if (configMethod.equals("nfc_interface")) {
                    c = 6;
                    break;
                }
            case -416734217:
                if (configMethod.equals("push_button")) {
                    c = 7;
                    break;
                }
            case 3388229:
                if (configMethod.equals("p2ps")) {
                    c = 11;
                    break;
                }
            case 3599197:
                if (configMethod.equals("usba")) {
                    c = 0;
                    break;
                }
            case 102727412:
                if (configMethod.equals("label")) {
                    c = 2;
                    break;
                }
            case 179612103:
                if (configMethod.equals("ext_nfc_token")) {
                    c = 5;
                    break;
                }
            case 1146869903:
                if (configMethod.equals("physical_push_button")) {
                    c = 10;
                    break;
                }
            case 1671764162:
                if (configMethod.equals("display")) {
                    c = 3;
                    break;
                }
            case 2010140181:
                if (configMethod.equals("int_nfc_token")) {
                    c = 4;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 4;
            case 3:
                return 8;
            case 4:
                return 32;
            case 5:
                return 16;
            case 6:
                return 64;
            case 7:
                return WpsConfigMethods.PUSHBUTTON;
            case 8:
                return WpsConfigMethods.KEYPAD;
            case 9:
                return WpsConfigMethods.VIRT_PUSHBUTTON;
            case 10:
                return WpsConfigMethods.PHY_PUSHBUTTON;
            case 11:
                return WpsConfigMethods.P2PS;
            case 12:
                return WpsConfigMethods.VIRT_DISPLAY;
            case 13:
                return WpsConfigMethods.PHY_DISPLAY;
            default:
                throw new IllegalArgumentException("Invalid WPS config method: " + configMethod);
        }
    }

    /* access modifiers changed from: private */
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

    private static void logd(String s) {
        Log.d(TAG, s);
    }

    private static void logi(String s) {
        Log.i(TAG, s);
    }

    private static void loge(String s) {
        Log.e(TAG, s);
    }

    private boolean trySetupForVendorV2_0(String ifaceName, ISupplicantIface ifaceHwBinder, SupplicantStaIfaceHalCallback callback) {
        if (!isVendorV2_0()) {
            return false;
        }
        logi("Start to setup vendor ISupplicantStaIface");
        vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = getVendorStaIfaceV2_0(ifaceHwBinder);
        VendorSupplicantStaIfaceHalCallbackV2_0 callbackV2_0 = new VendorSupplicantStaIfaceHalCallbackV2_0(ifaceName, new SupplicantStaIfaceHalCallbackV1_1(ifaceName, callback));
        if (!hwStaRegisterCallback(iface, callbackV2_0)) {
            return false;
        }
        this.mISupplicantStaIfaces.put(ifaceName, iface);
        this.mISupplicantStaIfaceCallbacks.put(ifaceName, callbackV2_0);
        logi("Successfully setup vendor ISupplicantStaIface");
        return true;
    }

    private boolean isVendorV2_0() {
        boolean z;
        synchronized (this.mLock) {
            z = false;
            try {
                if (getVendorSupplicantV2_0() != null) {
                    z = true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.getService exception: " + e);
                supplicantServiceDiedHandler(this.mDeathRecipientCookie);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return z;
    }

    private vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicant getVendorSupplicantV2_0() throws RemoteException {
        vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicant castFrom;
        synchronized (this.mLock) {
            try {
                castFrom = vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicant.castFrom(android.hardware.wifi.supplicant.V1_0.ISupplicant.getService());
            } catch (NoSuchElementException e) {
                Log.e(TAG, "Failed to get vendor V2_0 ISupplicant", e);
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        return castFrom;
    }

    private vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface getVendorStaIfaceV2_0(ISupplicantIface iface) {
        vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface castFrom;
        if (iface == null) {
            return null;
        }
        synchronized (this.mLock) {
            castFrom = vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface.castFrom(iface);
        }
        return castFrom;
    }

    private boolean hwStaRegisterCallback(vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface, vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIfaceCallback callback) {
        synchronized (this.mLock) {
            if (iface == null) {
                Log.e(TAG, "Got null iface when registering callback");
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.hwStaRegisterCallback(callback), "hwStaRegisterCallback");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "hwStaRegisterCallback");
                return false;
            }
        }
    }

    private vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface checkVendorSupplicantStaIfaceAndLogFailure(String ifaceName, String methodStr) {
        return getVendorStaIfaceV2_0(checkSupplicantStaIfaceAndLogFailure(ifaceName, methodStr));
    }

    public String voWifiDetect(String ifaceName, String cmd) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "VowifiDetect");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotResult = new HidlSupport.Mutable<>();
            try {
                iface.VowifiDetect(cmd, new ISupplicantStaIface.VowifiDetectCallback(gotResult) {
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaIfaceHal.lambda$voWifiDetect$10(SupplicantStaIfaceHal.this, this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "VowifiDetect");
            }
            String str = (String) gotResult.value;
            return str;
        }
    }

    public static /* synthetic */ void lambda$voWifiDetect$10(SupplicantStaIfaceHal supplicantStaIfaceHal, HidlSupport.Mutable gotResult, SupplicantStatus status, String result) {
        if (supplicantStaIfaceHal.checkStatusAndLogFailure(status, "VowifiDetect")) {
            gotResult.value = result;
            logd(result);
        }
    }

    public String heartBeat(String ifaceName, String param) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "heartBeat");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotResult = new HidlSupport.Mutable<>();
            try {
                iface.heartBeat(param, new ISupplicantStaIface.heartBeatCallback(gotResult) {
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaIfaceHal.lambda$heartBeat$11(SupplicantStaIfaceHal.this, this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "heartBeat");
            }
            String str = (String) gotResult.value;
            return str;
        }
    }

    public static /* synthetic */ void lambda$heartBeat$11(SupplicantStaIfaceHal supplicantStaIfaceHal, HidlSupport.Mutable gotResult, SupplicantStatus status, String result) {
        if (supplicantStaIfaceHal.checkStatusAndLogFailure(status, "heartBeat")) {
            gotResult.value = result;
            logd(result);
        }
    }

    public boolean enableHiLinkHandshake(String ifaceName, boolean uiEnable, String bssid) {
        Log.d(TAG, "enableHiLinkHandshake:uiEnable=" + uiEnable + " bssid=" + StringUtil.safeDisplayBssid(bssid));
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "enableHiLinkHandshake");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.enableHiLinkHandshake(uiEnable, bssid), "enableHiLinkHandshake");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "enableHiLinkHandshake");
                return false;
            }
        }
    }

    public boolean setTxPower(String ifaceName, int level) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "setTxPower");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setTxPower(level), "setTxPower");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setTxPower");
                return false;
            }
        }
    }

    public boolean setAbsCapability(String ifaceName, int capability) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "SetAbsCapability");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.SetAbsCapability(capability), "SetAbsCapability");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "SetAbsCapability");
                return false;
            }
        }
    }

    public boolean absPowerCtrl(String ifaceName, int type) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "AbsPowerCtrl");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.AbsPowerCtrl(type), "AbsPowerCtrl");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "AbsPowerCtrl");
                return false;
            }
        }
    }

    public boolean setAbsBlacklist(String ifaceName, String bssidList) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "SetAbsBlacklist");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.SetAbsBlacklist(bssidList), "SetAbsBlacklist");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "SetAbsBlacklist");
                return false;
            }
        }
    }

    public boolean query11vRoamingNetwork(String ifaceName, int reason) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "wnmBssQurey");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.wnmBssQurey(reason), "wnmBssQurey");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "wnmBssQurey");
                return false;
            }
        }
    }

    public String getRsdbCapability(String ifaceName) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "getCapabRsdb");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotRsdb = new HidlSupport.Mutable<>();
            try {
                iface.getCapabRsdb(new ISupplicantStaIface.getCapabRsdbCallback(gotRsdb) {
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaIfaceHal.lambda$getRsdbCapability$12(SupplicantStaIfaceHal.this, this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "getCapabRsdb");
            }
            String str = (String) gotRsdb.value;
            return str;
        }
    }

    public static /* synthetic */ void lambda$getRsdbCapability$12(SupplicantStaIfaceHal supplicantStaIfaceHal, HidlSupport.Mutable gotRsdb, SupplicantStatus status, String rsdb) {
        if (supplicantStaIfaceHal.checkStatusAndLogFailure(status, "getCapabRsdb")) {
            gotRsdb.value = rsdb;
        }
    }

    public String getWpasConfig(String ifaceName, int psktype) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "getWpasConfig");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotPsk = new HidlSupport.Mutable<>();
            try {
                iface.getWpasConfig(psktype, new ISupplicantStaIface.getWpasConfigCallback(gotPsk) {
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaIfaceHal.lambda$getWpasConfig$13(SupplicantStaIfaceHal.this, this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "getWpasConfig");
            }
            String str = (String) gotPsk.value;
            return str;
        }
    }

    public static /* synthetic */ void lambda$getWpasConfig$13(SupplicantStaIfaceHal supplicantStaIfaceHal, HidlSupport.Mutable gotPsk, SupplicantStatus status, String psk) {
        if (supplicantStaIfaceHal.checkStatusAndLogFailure(status, "getWpasConfig")) {
            gotPsk.value = psk;
        }
    }

    public boolean pwrPercentBoostModeset(String ifaceName, int rssi) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "pwrPercentBoostModeset");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.pwrPercentBoostModeset(rssi), "pwrPercentBoostModeset");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "pwrPercentBoostModeset");
                return false;
            }
        }
    }

    public String getMssState(String ifaceName) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "getMssState");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotMss = new HidlSupport.Mutable<>();
            try {
                iface.getMssState(new ISupplicantStaIface.getMssStateCallback(gotMss) {
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaIfaceHal.lambda$getMssState$14(SupplicantStaIfaceHal.this, this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "getMssState");
            }
            String str = (String) gotMss.value;
            return str;
        }
    }

    public static /* synthetic */ void lambda$getMssState$14(SupplicantStaIfaceHal supplicantStaIfaceHal, HidlSupport.Mutable gotMss, SupplicantStatus status, String mss) {
        if (supplicantStaIfaceHal.checkStatusAndLogFailure(status, "getMssState")) {
            gotMss.value = mss;
        }
    }

    public String getApVendorInfo(String ifaceName) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "getApVendorInfo");
            if (iface == null) {
                return null;
            }
            HidlSupport.Mutable<String> gotApVendorInfo = new HidlSupport.Mutable<>();
            try {
                iface.getApVendorInfo(new ISupplicantStaIface.getApVendorInfoCallback(gotApVendorInfo) {
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantStaIfaceHal.lambda$getApVendorInfo$15(SupplicantStaIfaceHal.this, this.f$1, supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                handleRemoteException(e, "getApVendorInfo");
            }
            String str = (String) gotApVendorInfo.value;
            return str;
        }
    }

    public static /* synthetic */ void lambda$getApVendorInfo$15(SupplicantStaIfaceHal supplicantStaIfaceHal, HidlSupport.Mutable gotApVendorInfo, SupplicantStatus status, String apvendorinfo) {
        if (supplicantStaIfaceHal.checkStatusAndLogFailure(status, "getApVendorInfo")) {
            gotApVendorInfo.value = apvendorinfo;
        }
    }

    public boolean setFilterEnable(String ifaceName, boolean enable) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantStaIface iface = checkVendorSupplicantStaIfaceAndLogFailure(ifaceName, "setFilterEnable");
            if (iface == null) {
                return false;
            }
            try {
                boolean checkStatusAndLogFailure = checkStatusAndLogFailure(iface.setFilterEnable(enable), "setFilterEnable");
                return checkStatusAndLogFailure;
            } catch (RemoteException e) {
                handleRemoteException(e, "setFilterEnable");
                return false;
            }
        }
    }
}
