package com.huawei.wifi2;

import android.net.InterfaceConfiguration;
import android.net.MacAddress;
import android.net.apf.ApfCapabilities;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.huawei.wifi2.HwWifi2HalDeviceManager;
import com.huawei.wifi2.HwWifi2Native;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class HwWifi2Native {
    public static final int BLUETOOTH_COEXISTENCE_MODE_DISABLED = 1;
    public static final int BLUETOOTH_COEXISTENCE_MODE_ENABLED = 0;
    public static final int BLUETOOTH_COEXISTENCE_MODE_SENSE = 2;
    private static final int CONNECT_TO_SUPPLICANT_RETRY_INTERVAL_MS = 50;
    private static final int CONNECT_TO_SUPPLICANT_RETRY_TIMES = 70;
    public static final int DISABLE_FIRMWARE_ROAMING = 0;
    public static final int ENABLE_FIRMWARE_ROAMING = 1;
    public static final int RX_FILTER_TYPE_V4_MULTICAST = 0;
    public static final int RX_FILTER_TYPE_V6_MULTICAST = 1;
    public static final int SET_FIRMWARE_ROAMING_BUSY = 2;
    public static final int SET_FIRMWARE_ROAMING_FAILURE = 1;
    public static final int SET_FIRMWARE_ROAMING_SUCCESS = 0;
    private static final String TAG = "HwWifi2Native";
    private Handler mHandler;
    private final HwWifi2SupplicantStaIfaceHal mHwWifi2SupplicantStaIfaceHal;
    private final HwWifi2VendorHal mHwWifi2VendorHal;
    private final HwWifi2CondControl mHwWifi2condControl;
    private final HwWifiIfaceManager mIfaceMgr = new HwWifiIfaceManager();
    private boolean mIsInitialized = false;
    private final Object mLock = new Object();
    private final INetworkManagementService mNwManagementService;
    private HashSet<StatusListener> mStatusListeners = new HashSet<>();
    private HwWifi2Monitor mWifi2Monitor = null;

    public interface InterfaceCallback {
        void onDestroyed(String str);

        void onDown(String str);

        void onUp(String str);
    }

    public static class RoamingCapabilities {
        protected int maxBlacklistSize;
        protected int maxWhitelistSize;
    }

    public static class RoamingConfig {
        protected ArrayList<String> blacklistBssids;
        protected ArrayList<String> whitelistSsids;
    }

    public static class SignalPollResult {
        protected int associationFrequency;
        protected int currentChload;
        protected int currentNoise;
        protected int currentRssi;
        protected int currentRxBytes;
        protected int currentRxPackets;
        protected int currentSnr;
        protected int currentTxBytes;
        protected int currentTxFailed;
        protected int currentTxPackets;
        protected int currentUlDelay;
        protected int rxBitrate;
        protected int txBitrate;
    }

    public interface StatusListener {
        void onStatusChanged(boolean z);
    }

    public interface SupplicantDeathEventHandler {
        void onDeath();
    }

    public static class TxPacketCounters {
        protected int txFailed;
        protected int txSucceeded;
    }

    public interface VendorHalDeathEventHandler {
        void onDeath();
    }

    public interface WifiRssiEventHandler {
        void onRssiThresholdBreached(byte b);
    }

    public interface WificondDeathEventHandler {
        void onDeath();
    }

    public HwWifi2Native(HwWifi2VendorHal vendorHal, HwWifi2SupplicantStaIfaceHal staIfaceHal, HwWifi2CondControl condControl, HwWifi2Monitor wifiMonitor, INetworkManagementService nwService, Handler handler) {
        this.mHwWifi2VendorHal = vendorHal;
        this.mNwManagementService = nwService;
        HwHiLog.i(TAG, false, "HwWifi2Native Started ", new Object[0]);
        this.mHandler = handler;
        this.mWifi2Monitor = wifiMonitor;
        this.mHwWifi2SupplicantStaIfaceHal = staIfaceHal;
        this.mHwWifi2condControl = condControl;
    }

    public boolean initialize() {
        synchronized (this.mLock) {
            if (this.mIsInitialized) {
                HwHiLog.i(TAG, false, "already initialized", new Object[0]);
                return true;
            } else if (!this.mHwWifi2VendorHal.initialize(new VendorHalDeathHandlerInternal())) {
                HwHiLog.e(TAG, false, "Failed to initialize vendor HAL", new Object[0]);
                return false;
            } else if (!this.mHwWifi2condControl.initialize(new WificondDeathHandlerInternal())) {
                HwHiLog.e(TAG, false, "Failed to initialize wificond", new Object[0]);
                return false;
            } else {
                HwHiLog.d(TAG, false, "HwWifi2CondControl initialize success", new Object[0]);
                this.mIsInitialized = true;
                return true;
            }
        }
    }

    private boolean connectToSupplicant() {
        synchronized (this.mLock) {
            if (!connectAndWaitSupplicantConnection()) {
                HwHiLog.e(TAG, false, "Failed to connect to supplicant", new Object[0]);
                return false;
            } else if (this.mHwWifi2SupplicantStaIfaceHal.registerDeathHandler(new SupplicantDeathHandlerInternal())) {
                return true;
            } else {
                HwHiLog.e(TAG, false, "Failed to register supplicant death handler", new Object[0]);
                return false;
            }
        }
    }

    private void disconnectSupplicant() {
        synchronized (this.mLock) {
            if (!this.mHwWifi2SupplicantStaIfaceHal.deregisterDeathHandler()) {
                HwHiLog.e(TAG, false, "Failed to deregister supplicant death handler", new Object[0]);
            }
        }
    }

    private boolean connectAndWaitSupplicantConnection() {
        if (!this.mHwWifi2SupplicantStaIfaceHal.isInitializationStarted() && !this.mHwWifi2SupplicantStaIfaceHal.initialize()) {
            return false;
        }
        boolean isConnected = false;
        int connectTries = 0;
        while (true) {
            if (isConnected) {
                break;
            }
            int connectTries2 = connectTries + 1;
            if (connectTries >= CONNECT_TO_SUPPLICANT_RETRY_TIMES) {
                break;
            }
            isConnected = this.mHwWifi2SupplicantStaIfaceHal.isInitializationComplete() && this.mHwWifi2SupplicantStaIfaceHal.isStaIfacesEmpty();
            if (isConnected) {
                break;
            }
            try {
                HwHiLog.w(TAG, false, "wait supplicant connectTries sleep: %{public}d", new Object[]{Integer.valueOf((int) CONNECT_TO_SUPPLICANT_RETRY_TIMES)});
                Thread.sleep(50);
            } catch (InterruptedException e) {
                HwHiLog.w(TAG, false, "InterruptedException", new Object[0]);
            }
            connectTries = connectTries2;
        }
        return isConnected;
    }

    /* access modifiers changed from: private */
    public class SupplicantDeathHandlerInternal implements SupplicantDeathEventHandler {
        private SupplicantDeathHandlerInternal() {
        }

        @Override // com.huawei.wifi2.HwWifi2Native.SupplicantDeathEventHandler
        public void onDeath() {
            HwHiLog.w(HwWifi2Native.TAG, false, "wpa_supplicant died.", new Object[0]);
        }
    }

    public boolean connectToNetwork(String ifaceName, WifiConfiguration configuration) {
        return this.mHwWifi2SupplicantStaIfaceHal.connectToNetwork(ifaceName, configuration);
    }

    public boolean reassociate(String ifaceName) {
        boolean isSuccess = this.mHwWifi2SupplicantStaIfaceHal.reassociate(ifaceName);
        if (isSuccess) {
            WifiCommonUtils.notifyDeviceState("WLAN", "CONNECT_START", "");
        }
        return isSuccess;
    }

    public boolean reconnect(String ifaceName) {
        boolean isSuccess = this.mHwWifi2SupplicantStaIfaceHal.reconnect(ifaceName);
        if (isSuccess) {
            WifiCommonUtils.notifyDeviceState("WLAN", "CONNECT_START", "");
        }
        return isSuccess;
    }

    public boolean disconnect(String ifaceName) {
        return this.mHwWifi2SupplicantStaIfaceHal.disconnect(ifaceName);
    }

    public void setPowerSave(String ifaceName, boolean isEnabled) {
        this.mHwWifi2SupplicantStaIfaceHal.setPowerSave(ifaceName, isEnabled);
    }

    public boolean setLowLatencyMode(boolean isModeEnabled) {
        return this.mHwWifi2VendorHal.setLowLatencyMode(isModeEnabled);
    }

    public boolean setBluetoothCoexistenceMode(String ifaceName, int mode) {
        return this.mHwWifi2SupplicantStaIfaceHal.setBtCoexistenceMode(ifaceName, mode);
    }

    public boolean setSuspendOptimizations(String ifaceName, boolean isEnabled) {
        HwHiLog.i(TAG, false, "setSuspendOptimizations:isEnabled =%{public}s", new Object[]{String.valueOf(isEnabled)});
        return this.mHwWifi2SupplicantStaIfaceHal.setSuspendModeEnabled(ifaceName, isEnabled);
    }

    public String getMacAddress(String ifaceName) {
        return this.mHwWifi2SupplicantStaIfaceHal.getMacAddress(ifaceName);
    }

    public boolean startFilteringMulticastV4Packets(String ifaceName) {
        if (!this.mHwWifi2SupplicantStaIfaceHal.stopRxFilter(ifaceName) || !this.mHwWifi2SupplicantStaIfaceHal.removeRxFilter(ifaceName, 0) || !this.mHwWifi2SupplicantStaIfaceHal.startRxFilter(ifaceName)) {
            return false;
        }
        return true;
    }

    public boolean stopFilteringMulticastV4Packets(String ifaceName) {
        if (!this.mHwWifi2SupplicantStaIfaceHal.stopRxFilter(ifaceName) || !this.mHwWifi2SupplicantStaIfaceHal.addRxFilter(ifaceName, 0) || !this.mHwWifi2SupplicantStaIfaceHal.startRxFilter(ifaceName)) {
            return false;
        }
        return true;
    }

    public boolean startFilteringMulticastV6Packets(String ifaceName) {
        if (!this.mHwWifi2SupplicantStaIfaceHal.stopRxFilter(ifaceName) || !this.mHwWifi2SupplicantStaIfaceHal.removeRxFilter(ifaceName, 1) || !this.mHwWifi2SupplicantStaIfaceHal.startRxFilter(ifaceName)) {
            return false;
        }
        return true;
    }

    public boolean stopFilteringMulticastV6Packets(String ifaceName) {
        if (!this.mHwWifi2SupplicantStaIfaceHal.stopRxFilter(ifaceName) || !this.mHwWifi2SupplicantStaIfaceHal.addRxFilter(ifaceName, 1) || !this.mHwWifi2SupplicantStaIfaceHal.startRxFilter(ifaceName)) {
            return false;
        }
        return true;
    }

    public SignalPollResult signalPoll(String ifaceName) {
        return this.mHwWifi2condControl.signalPoll();
    }

    public TxPacketCounters getTxPacketCounters() {
        return this.mHwWifi2condControl.getTxPacketCounters();
    }

    public boolean setBluetoothCoexistenceScanMode(String ifaceName, boolean isSetCoexScanMode) {
        return this.mHwWifi2SupplicantStaIfaceHal.setBtCoexistenceScanModeEnabled(ifaceName, isSetCoexScanMode);
    }

    /* access modifiers changed from: private */
    public class WificondDeathHandlerInternal implements WificondDeathEventHandler {
        private WificondDeathHandlerInternal() {
        }

        @Override // com.huawei.wifi2.HwWifi2Native.WificondDeathEventHandler
        public void onDeath() {
            synchronized (HwWifi2Native.this.mLock) {
                HwHiLog.i(HwWifi2Native.TAG, false, "wificond died. Cleaning up internal state.", new Object[0]);
            }
        }
    }

    public int startRssiMonitoring(String ifaceName, byte maxRssi, byte minRssi, WifiRssiEventHandler rssiEventHandler) {
        return this.mHwWifi2VendorHal.startRssiMonitoring(ifaceName, maxRssi, minRssi, rssiEventHandler);
    }

    public int stopRssiMonitoring(String ifaceName) {
        return this.mHwWifi2VendorHal.stopRssiMonitoring(ifaceName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onInterfaceStateChanged(HwWifiIface iface, boolean isUp) {
        synchronized (this.mLock) {
            if (isUp == iface.isUp) {
                HwHiLog.i(TAG, false, "Interface state not changed just return", new Object[0]);
                return;
            }
            HwHiLog.i(TAG, false, "Interface state changed on %{public}s, isUp =%{public}s", new Object[]{iface, String.valueOf(isUp)});
            if (isUp) {
                iface.externalListener.onUp(iface.name);
            } else {
                iface.externalListener.onDown(iface.name);
            }
            iface.isUp = isUp;
        }
    }

    public String setupInterfaceForClientInConnectivityMode(InterfaceCallback interfaceCallback) {
        HwHiLog.i(TAG, false, "start setupInterfaceForClientInConnectivityMode", new Object[0]);
        synchronized (this.mLock) {
            if (!initialize()) {
                HwHiLog.e(TAG, false, "Failed to initialize", new Object[0]);
                return null;
            } else if (!startHal()) {
                HwHiLog.e(TAG, false, "Failed to start Hal", new Object[0]);
                return null;
            } else if (!connectToSupplicant()) {
                HwHiLog.e(TAG, false, "Failed to connect supplicant", new Object[0]);
                return null;
            } else {
                HwWifiIface iface = this.mIfaceMgr.allocateIface(1);
                if (iface == null) {
                    HwHiLog.e(TAG, false, "Failed to allocate new STA iface", new Object[0]);
                    return null;
                }
                iface.externalListener = interfaceCallback;
                iface.name = createStaIface(iface, false);
                if (TextUtils.isEmpty(iface.name)) {
                    HwHiLog.e(TAG, false, "Failed to create STA iface in vendor HAL", new Object[0]);
                    this.mIfaceMgr.removeIface(iface.id);
                    return null;
                } else if (!this.mHwWifi2condControl.setup(iface.name)) {
                    HwHiLog.e(TAG, false, "Failed to setup iface in wificond on%{public}s", new Object[]{iface});
                    teardownInterface(iface.name);
                    return null;
                } else if (!this.mHwWifi2SupplicantStaIfaceHal.setupIface(iface.name)) {
                    HwHiLog.e(TAG, false, "Failed to setup iface in supplicant on %{public}s", new Object[]{iface});
                    teardownInterface(iface.name);
                    return null;
                } else {
                    iface.networkObserver = new NetworkObserverInternal(iface.id);
                    if (!registerNetworkObserver(iface.networkObserver)) {
                        HwHiLog.e(TAG, false, "Failed to register network observer on " + iface, new Object[0]);
                        teardownInterface(iface.name);
                        return null;
                    }
                    this.mWifi2Monitor.startMonitoring(iface.name);
                    onInterfaceStateChanged(iface, isInterfaceUp(iface.name));
                    initializeNwParamsForClientInterface(iface.name);
                    HwHiLog.i(TAG, false, "Successfully setup %{public}s", new Object[]{iface});
                    return iface.name;
                }
            }
        }
    }

    private boolean startHal() {
        synchronized (this.mLock) {
            HwHiLog.i(TAG, false, "startHal", new Object[0]);
            if (!this.mIfaceMgr.hasAnyIface()) {
                if (!this.mHwWifi2VendorHal.isVendorHalSupported()) {
                    HwHiLog.i(TAG, false, "Vendor Hal not supported, ignoring start.", new Object[0]);
                } else if (!this.mHwWifi2VendorHal.startVendorHal()) {
                    HwHiLog.e(TAG, false, "Failed to start vendor HAL", new Object[0]);
                    return false;
                }
            }
            return true;
        }
    }

    private String createStaIface(HwWifiIface iface, boolean isLowPrioritySta) {
        synchronized (this.mLock) {
            if (this.mHwWifi2VendorHal.isVendorHalSupported()) {
                return this.mHwWifi2VendorHal.createStaIface(isLowPrioritySta, new InterfaceDestoyedListenerInternal(iface.id));
            }
            HwHiLog.e(TAG, false, "Vendor Hal not supported, ignoring createStaIface.", new Object[0]);
            return handleIfaceCreationWhenVendorHalNotSupported(iface);
        }
    }

    /* access modifiers changed from: private */
    public class InterfaceDestoyedListenerInternal implements HwWifi2HalDeviceManager.InterfaceDestroyedListener {
        private final int mInterfaceId;

        InterfaceDestoyedListenerInternal(int ifaceId) {
            this.mInterfaceId = ifaceId;
        }

        @Override // com.huawei.wifi2.HwWifi2HalDeviceManager.InterfaceDestroyedListener
        public void onDestroyed(String ifaceName) {
            synchronized (HwWifi2Native.this.mLock) {
                HwWifiIface iface = HwWifi2Native.this.mIfaceMgr.removeIface(this.mInterfaceId);
                if (iface == null) {
                    HwHiLog.i(HwWifi2Native.TAG, false, "onDestroyed invalid iface = %{public}s", new Object[]{ifaceName});
                    return;
                }
                HwWifi2Native.this.onInterfaceDestroyed(iface);
                HwHiLog.i(HwWifi2Native.TAG, false, "Successfully tear down %{public}s", new Object[]{iface});
                if (!HwWifi2Native.this.mIfaceMgr.hasAnyIface()) {
                    HwWifi2Native.this.mHwWifi2VendorHal.reset();
                    HwHiLog.i(HwWifi2Native.TAG, false, "Successfully shutdown wificond service", new Object[0]);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onInterfaceDestroyed(HwWifiIface iface) {
        HwHiLog.i(TAG, false, "onInterfaceDestroyed: onDestroyed iface = %{public}s", new Object[]{iface});
        synchronized (this.mLock) {
            if (iface.type == 1) {
                onClientInterfaceForConnectivityDestroyed(iface);
                iface.externalListener.onDestroyed(iface.name);
                return;
            }
            HwHiLog.e(TAG, false, "wrong iface = %{public}s", new Object[]{iface});
        }
    }

    private void onClientInterfaceForConnectivityDestroyed(HwWifiIface iface) {
        synchronized (this.mLock) {
            this.mWifi2Monitor.stopMonitoring(iface.name);
            if (!unregisterNetworkObserver(iface.networkObserver)) {
                HwHiLog.e(TAG, false, "Failed to unregister network observer on %{public}s", new Object[]{iface});
            }
            if (!this.mHwWifi2SupplicantStaIfaceHal.teardownIface(iface.name)) {
                HwHiLog.e(TAG, false, "Failed to teardown iface in supplicant on %{public}s", new Object[]{iface});
            }
            if (!this.mHwWifi2condControl.tearDown()) {
                HwHiLog.e(TAG, false, "Failed to teardown iface in wificond on %{public}s", new Object[]{iface});
            }
            disconnectSupplicant();
            stopHalAndWificondIfNecessary();
        }
    }

    private void stopHalAndWificondIfNecessary() {
        synchronized (this.mLock) {
            if (!this.mIfaceMgr.hasAnyIface()) {
                if (this.mHwWifi2VendorHal.isVendorHalSupported()) {
                    this.mHwWifi2VendorHal.stopVendorHal();
                } else {
                    HwHiLog.i(TAG, false, "Vendor Hal not supported, ignoring stop.", new Object[0]);
                }
            }
        }
    }

    private String handleIfaceCreationWhenVendorHalNotSupported(HwWifiIface newIface) {
        synchronized (this.mLock) {
            HwWifiIface existingIface = this.mIfaceMgr.removeExistingIface(newIface.id);
            if (existingIface != null) {
                onInterfaceDestroyed(existingIface);
                HwHiLog.i(TAG, false, "Successfully torn down %{public}s", new Object[]{existingIface});
            }
        }
        return null;
    }

    public class NetworkObserverInternal extends BaseNetworkObserver {
        private final int mInterfaceId;

        NetworkObserverInternal(int id) {
            this.mInterfaceId = id;
        }

        public void interfaceLinkStateChanged(String ifaceName, boolean isLinkUp) {
            HwWifi2Native.this.mHandler.post(new Runnable(ifaceName, isLinkUp) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2Native$NetworkObserverInternal$bVMR2D_nx8v4kkczZgLCDMP8 */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwWifi2Native.NetworkObserverInternal.this.lambda$interfaceLinkStateChanged$0$HwWifi2Native$NetworkObserverInternal(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$interfaceLinkStateChanged$0$HwWifi2Native$NetworkObserverInternal(String ifaceName, boolean isLinkUp) {
            synchronized (HwWifi2Native.this.mLock) {
                HwWifiIface ifaceWithId = HwWifi2Native.this.mIfaceMgr.getIface(this.mInterfaceId);
                if (ifaceWithId == null) {
                    HwHiLog.i(HwWifi2Native.TAG, false, "interfaceLinkStateChanged mInterfaceId %{public}d is invalid", new Object[]{Integer.valueOf(this.mInterfaceId)});
                    return;
                }
                HwWifiIface ifaceWithName = HwWifi2Native.this.mIfaceMgr.getIface(ifaceName);
                if (ifaceWithName != null) {
                    if (ifaceWithName == ifaceWithId) {
                        HwWifi2Native.this.onInterfaceStateChanged(ifaceWithName, HwWifi2Native.this.isInterfaceUp(ifaceName));
                        return;
                    }
                }
                HwHiLog.i(HwWifi2Native.TAG, false, "iface= %{public}s, isLinkUp = %{public}s", new Object[]{ifaceName, String.valueOf(isLinkUp)});
            }
        }
    }

    public boolean isInterfaceUp(String ifaceName) {
        synchronized (this.mLock) {
            if (this.mIfaceMgr.getIface(ifaceName) == null) {
                HwHiLog.w(TAG, false, "Trying to get iface on invalid iface= %{public}s", new Object[]{ifaceName});
                return false;
            }
            InterfaceConfiguration config = null;
            try {
                config = this.mNwManagementService.getInterfaceConfig(ifaceName);
            } catch (RemoteException | IllegalStateException e) {
                HwHiLog.e(TAG, false, "Unable to get interface config", new Object[0]);
            }
            if (config == null) {
                return false;
            }
            return config.isUp();
        }
    }

    private void initializeNwParamsForClientInterface(String ifaceName) {
        try {
            this.mNwManagementService.clearInterfaceAddresses(ifaceName);
            this.mNwManagementService.setInterfaceIpv6PrivacyExtensions(ifaceName, true);
            this.mNwManagementService.disableIpv6(ifaceName);
        } catch (RemoteException | IllegalStateException e) {
            HwHiLog.e(TAG, false, "Unable to change interface settings", new Object[0]);
        }
    }

    public void teardownInterface(String ifaceName) {
        HwHiLog.i(TAG, false, "teardown specify Interface enter: %{public}s", new Object[]{ifaceName});
        synchronized (this.mLock) {
            HwWifiIface iface = this.mIfaceMgr.getIface(ifaceName);
            if (iface == null) {
                HwHiLog.e(TAG, false, "Trying to teardown an invalid iface= %{public}s", new Object[]{ifaceName});
            } else if (iface.type != 1) {
                HwHiLog.e(TAG, false, "Failed to remove NonStaiface in vendor HAL=%{public}s", new Object[]{ifaceName});
            } else if (!removeStaIface(iface)) {
                HwHiLog.i(TAG, false, "Failed to remove iface in vendor HAL= %{public}s", new Object[]{ifaceName});
                clearHalInfo();
            } else {
                HwHiLog.i(TAG, false, "Successfully initiated teardown for iface= %{public}s", new Object[]{ifaceName});
            }
        }
    }

    private void clearHalInfo() {
        this.mHwWifi2VendorHal.clearState();
    }

    public boolean removeAllNetworks(String ifaceName) {
        return this.mHwWifi2SupplicantStaIfaceHal.removeAllNetworks(ifaceName);
    }

    private boolean removeStaIface(HwWifiIface iface) {
        synchronized (this.mLock) {
            if (this.mHwWifi2VendorHal.isVendorHalSupported()) {
                return this.mHwWifi2VendorHal.removeStaIface(iface.name);
            }
            HwHiLog.i(TAG, false, "Wifi2 Vendor Hal not supported, ignoring removeStaIface.", new Object[0]);
            return handleIfaceRemovalWhenVendorHalNotSupported(iface);
        }
    }

    private boolean handleIfaceRemovalWhenVendorHalNotSupported(HwWifiIface iface) {
        synchronized (this.mLock) {
            this.mIfaceMgr.removeIface(iface.id);
            onInterfaceDestroyed(iface);
            HwHiLog.i(TAG, false, "Successfully torn down %{public}s", new Object[]{iface});
        }
        return true;
    }

    private boolean registerNetworkObserver(NetworkObserverInternal observer) {
        if (observer == null) {
            return false;
        }
        try {
            this.mNwManagementService.registerObserver(observer);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            HwHiLog.e(TAG, false, "Unable to register observer", new Object[0]);
            return false;
        }
    }

    private boolean unregisterNetworkObserver(NetworkObserverInternal observer) {
        if (observer == null) {
            return false;
        }
        try {
            this.mNwManagementService.unregisterObserver(observer);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            HwHiLog.e(TAG, false, "Unable to unregister observer", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class VendorHalDeathHandlerInternal implements VendorHalDeathEventHandler {
        private VendorHalDeathHandlerInternal() {
        }

        @Override // com.huawei.wifi2.HwWifi2Native.VendorHalDeathEventHandler
        public void onDeath() {
            synchronized (HwWifi2Native.this.mLock) {
                HwHiLog.i(HwWifi2Native.TAG, false, "Vendor HAL died, Cleaning up internal state.", new Object[0]);
                HwWifi2Native.this.onNativeDaemonDeath();
            }
        }
    }

    public boolean configureNeighborDiscoveryOffload(String ifaceName, boolean isOffloadEnabled) {
        return this.mHwWifi2VendorHal.configureNeighborDiscoveryOffload(ifaceName, isOffloadEnabled);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNativeDaemonDeath() {
        synchronized (this.mLock) {
            Iterator<StatusListener> it = this.mStatusListeners.iterator();
            while (it.hasNext()) {
                it.next().onStatusChanged(false);
            }
            Iterator<StatusListener> it2 = this.mStatusListeners.iterator();
            while (it2.hasNext()) {
                it2.next().onStatusChanged(true);
            }
        }
    }

    public void registerStatusListener(StatusListener listener) {
        this.mStatusListeners.add(listener);
    }

    public boolean getRoamingCapabilities(String ifaceName, RoamingCapabilities capabilities) {
        return this.mHwWifi2VendorHal.getRoamingCapabilities(ifaceName, capabilities);
    }

    public int enableFirmwareRoaming(String ifaceName, int state) {
        return this.mHwWifi2VendorHal.enableFirmwareRoaming(ifaceName, state);
    }

    public boolean configureRoaming(String ifaceName, RoamingConfig config) {
        return this.mHwWifi2VendorHal.configureRoaming(ifaceName, config);
    }

    public boolean resetRoamingConfiguration(String ifaceName) {
        return this.mHwWifi2VendorHal.configureRoaming(ifaceName, new RoamingConfig());
    }

    public int startSendingOffloadedPacket(String ifaceName, byte[] dstMac, byte[] packet, int[] params) {
        return this.mHwWifi2VendorHal.startSendingOffloadedPacket(ifaceName, NativeUtil.macAddressToByteArray(getMacAddress(ifaceName)), dstMac, packet, params);
    }

    public int stopSendingOffloadedPacket(String ifaceName, int slot) {
        return this.mHwWifi2VendorHal.stopSendingOffloadedPacket(ifaceName, slot);
    }

    public boolean setMacAddress(String interfaceName, MacAddress mac) {
        return this.mHwWifi2VendorHal.setMacAddress(interfaceName, mac);
    }

    public MacAddress getFactoryMacAddress(String interfaceName) {
        return this.mHwWifi2VendorHal.getFactoryMacAddress(interfaceName);
    }

    public boolean setConfiguredNetworkBssid(String ifaceName, String bssid) {
        return this.mHwWifi2SupplicantStaIfaceHal.setCurrentNetworkBssid(ifaceName, bssid);
    }

    public boolean roamToNetwork(String ifaceName, WifiConfiguration configuration) {
        boolean isResult = this.mHwWifi2SupplicantStaIfaceHal.roamToNetwork(ifaceName, configuration);
        if (isResult) {
            WifiCommonUtils.notifyDeviceState("WLAN", "CONNECT_START", "");
        }
        HwHiLog.i(TAG, false, "handle roamToNetwork over", new Object[0]);
        return isResult;
    }

    public String getEapAnonymousIdentity(String ifaceName) {
        return this.mHwWifi2SupplicantStaIfaceHal.getCurrentNetworkEapAnonymousIdentity(ifaceName);
    }

    public WifiLinkLayerStats getWifiLinkLayerStats(String ifaceName) {
        return this.mHwWifi2VendorHal.getWifiLinkLayerStats(ifaceName);
    }

    public ApfCapabilities getApfCapabilities(String ifaceName) {
        return this.mHwWifi2VendorHal.getApfCapabilities(ifaceName);
    }

    public void startTdls(String ifaceName, String macAddr, boolean isTdlsEnable) {
        if (isTdlsEnable) {
            this.mHwWifi2SupplicantStaIfaceHal.initiateTdlsDiscover(ifaceName, macAddr);
            this.mHwWifi2SupplicantStaIfaceHal.initiateTdlsSetup(ifaceName, macAddr);
            return;
        }
        this.mHwWifi2SupplicantStaIfaceHal.initiateTdlsTeardown(ifaceName, macAddr);
    }

    public boolean setConcurrencyPriority(boolean isStaHigherPriority) {
        return this.mHwWifi2SupplicantStaIfaceHal.setConcurrencyPriority(isStaHigherPriority);
    }

    public boolean setExternalSim(String ifaceName, boolean isExternal) {
        return this.mHwWifi2SupplicantStaIfaceHal.setExternalSim(ifaceName, isExternal);
    }

    public byte[] readPacketFilter(String ifaceName) {
        return this.mHwWifi2VendorHal.readPacketFilter(ifaceName);
    }

    public boolean installPacketFilter(String ifaceName, byte[] filter) {
        return this.mHwWifi2VendorHal.installPacketFilter(ifaceName, filter);
    }

    public String getClientInterfaceName() {
        String findAnyStaIfaceName;
        synchronized (this.mLock) {
            findAnyStaIfaceName = this.mIfaceMgr.findAnyStaIfaceName();
        }
        return findAnyStaIfaceName;
    }

    public boolean enableStaAutoReconnect(String ifaceName, boolean enable) {
        return this.mHwWifi2SupplicantStaIfaceHal.enableAutoReconnect(ifaceName, enable);
    }
}
