package com.huawei.wifi2;

import android.hardware.wifi.V1_0.IWifiChip;
import android.hardware.wifi.V1_0.IWifiChipEventCallback;
import android.hardware.wifi.V1_0.IWifiIface;
import android.hardware.wifi.V1_0.IWifiStaIface;
import android.hardware.wifi.V1_0.IWifiStaIfaceEventCallback;
import android.hardware.wifi.V1_0.StaApfPacketFilterCapabilities;
import android.hardware.wifi.V1_0.StaLinkLayerIfaceStats;
import android.hardware.wifi.V1_0.StaLinkLayerRadioStats;
import android.hardware.wifi.V1_0.StaLinkLayerStats;
import android.hardware.wifi.V1_0.StaRoamingCapabilities;
import android.hardware.wifi.V1_0.StaRoamingConfig;
import android.hardware.wifi.V1_0.StaScanData;
import android.hardware.wifi.V1_0.StaScanResult;
import android.hardware.wifi.V1_0.WifiDebugRingBufferStatus;
import android.hardware.wifi.V1_0.WifiStatus;
import android.hardware.wifi.V1_2.IWifiChipEventCallback;
import android.hardware.wifi.V1_2.IWifiStaIface;
import android.hardware.wifi.V1_3.IWifiStaIface;
import android.hardware.wifi.V1_3.WifiChannelStats;
import android.net.MacAddress;
import android.net.apf.ApfCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.MutableBoolean;
import android.util.wifi.HwHiLog;
import com.huawei.wifi2.HwWifi2HalDeviceManager;
import com.huawei.wifi2.HwWifi2Native;
import com.huawei.wifi2.HwWifi2VendorHal;
import com.huawei.wifi2.WifiLinkLayerStats;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HwWifi2VendorHal {
    private static final ApfCapabilities APF_CAPA = new ApfCapabilities(0, 0, 0);
    private static final int INVALID_VALUE = -1;
    private static final Object LOCK_OBJ = new Object();
    private static final int PROTOCOL_POSITION = 1;
    private static final int RSSI_MONITOR_CMD_ID = 7551;
    private static final int SEND_PERIOD_POSITION = 2;
    private static final int SLOT_POSITION = 0;
    private static final String TAG = "HwWifi2VendorHal";
    private HwWifi2Native.VendorHalDeathEventHandler mDeathEventHandler;
    private final Handler mHalEventHandler;
    private final HwWifi2HalDeviceManager mHwWifi2HalDeviceManager;
    private final HwWifi2HalDeviceManagerStatusListener mHwWifi2HalDeviceManagerStatusCallbacks;
    private final ChipEventCallback mIWifiChipEventCallback;
    private final ChipEventCallbackV12 mIWifiChipEventCallbackV12;
    private final IWifiStaIfaceEventCallback mIWifiStaIfaceEventCallback;
    private final Looper mLooper;
    private IWifiChip mWifiChip;
    private HwWifi2Native.WifiRssiEventHandler mWifiRssiEventHandler;
    private HashMap<String, IWifiStaIface> mWifiStafaces = new HashMap<>();

    public HwWifi2VendorHal(HwWifi2HalDeviceManager wifi2HalDeviceManager, Looper looper) {
        this.mHwWifi2HalDeviceManager = wifi2HalDeviceManager;
        this.mLooper = looper;
        this.mHalEventHandler = new Handler(this.mLooper);
        this.mHwWifi2HalDeviceManagerStatusCallbacks = new HwWifi2HalDeviceManagerStatusListener();
        this.mIWifiStaIfaceEventCallback = new StaIfaceEventCallback();
        this.mIWifiChipEventCallback = new ChipEventCallback();
        this.mIWifiChipEventCallbackV12 = new ChipEventCallbackV12();
        HwHiLog.i(TAG, false, "HwWifi2VendorHal init success", new Object[0]);
    }

    private boolean isStatusOk(WifiStatus status) {
        if (status.code == 0) {
            return true;
        }
        return false;
    }

    private void handleRemoteException() {
        HwHiLog.e(TAG, false, "RemoteException in HIDL call", new Object[0]);
        clearState();
    }

    public boolean initialize(HwWifi2Native.VendorHalDeathEventHandler handler) {
        synchronized (LOCK_OBJ) {
            this.mHwWifi2HalDeviceManager.initialize();
            this.mHwWifi2HalDeviceManager.registerStatusListener(this.mHwWifi2HalDeviceManagerStatusCallbacks, this.mHalEventHandler);
            this.mDeathEventHandler = handler;
            HwHiLog.i(TAG, false, "initialize mHwWifi2HalDeviceManagerStatusCallbacks success", new Object[0]);
        }
        return true;
    }

    public class HwWifi2HalDeviceManagerStatusListener implements HwWifi2HalDeviceManager.ManagerStatusListener {
        public HwWifi2HalDeviceManagerStatusListener() {
        }

        @Override // com.huawei.wifi2.HwWifi2HalDeviceManager.ManagerStatusListener
        public void onStatusChanged() {
            HwWifi2Native.VendorHalDeathEventHandler handler;
            boolean isReady = HwWifi2VendorHal.this.mHwWifi2HalDeviceManager.isReady();
            HwHiLog.i(HwWifi2VendorHal.TAG, false, "Device Manager onStatusChanged. isReady(): %{public}b,isStarted(): %{public}b", new Object[]{Boolean.valueOf(isReady), Boolean.valueOf(HwWifi2VendorHal.this.mHwWifi2HalDeviceManager.isStarted())});
            if (!isReady) {
                synchronized (HwWifi2VendorHal.LOCK_OBJ) {
                    HwWifi2VendorHal.this.clearState();
                    handler = HwWifi2VendorHal.this.mDeathEventHandler;
                }
                if (handler != null) {
                    handler.onDeath();
                }
            }
        }
    }

    public void initialize() {
        HwHiLog.i(TAG, false, "Wifi2VendorHal initialize HAl DEVICE witouht input param", new Object[0]);
        synchronized (LOCK_OBJ) {
            this.mHwWifi2HalDeviceManager.initialize();
        }
    }

    public void registerHandler(HwWifi2Native.VendorHalDeathEventHandler handler) {
        HwHiLog.i(TAG, false, "Wifi2VendorHal registerHandler enter", new Object[0]);
        synchronized (LOCK_OBJ) {
            this.mHwWifi2HalDeviceManager.registerStatusListener(this.mHwWifi2HalDeviceManagerStatusCallbacks, this.mHalEventHandler);
            this.mDeathEventHandler = handler;
        }
    }

    public void reset() {
        synchronized (LOCK_OBJ) {
            this.mHwWifi2HalDeviceManager.reset();
        }
    }

    public boolean isVendorHalSupported() {
        boolean isSupported;
        synchronized (LOCK_OBJ) {
            isSupported = this.mHwWifi2HalDeviceManager.isSupported();
        }
        return isSupported;
    }

    public boolean startVendorHalSta() {
        HwHiLog.i(TAG, false, "startVendorHalSta enter", new Object[0]);
        synchronized (LOCK_OBJ) {
            if (!startVendorHal()) {
                return false;
            }
            if (!TextUtils.isEmpty(createStaIface(false, null))) {
                return true;
            }
            stopVendorHal();
            return false;
        }
    }

    public boolean startVendorHal() {
        synchronized (LOCK_OBJ) {
            if (!this.mHwWifi2HalDeviceManager.start()) {
                HwHiLog.e(TAG, false, "Failed to start vendor HAL", new Object[0]);
                return false;
            }
            HwHiLog.i(TAG, false, "Vendor Hal started successfully", new Object[0]);
            return true;
        }
    }

    public IWifiStaIface getStaIface(String ifaceName) {
        IWifiStaIface iWifiStaIface;
        synchronized (LOCK_OBJ) {
            iWifiStaIface = this.mWifiStafaces.get(ifaceName);
        }
        return iWifiStaIface;
    }

    public String createStaIface(boolean isLowPrioritySta, HwWifi2HalDeviceManager.InterfaceDestroyedListener destroyedListener) {
        HwHiLog.i(TAG, false, "createStaIface enter", new Object[0]);
        synchronized (LOCK_OBJ) {
            IWifiStaIface iface = this.mHwWifi2HalDeviceManager.createStaIface(isLowPrioritySta, new StaInterfaceDestroyedListenerInternal(destroyedListener), null);
            if (iface == null) {
                HwHiLog.e(TAG, false, "Failed to create STA iface", new Object[0]);
                return "";
            }
            HwWifi2HalDeviceManager hwWifi2HalDeviceManager = this.mHwWifi2HalDeviceManager;
            String ifaceName = HwWifi2HalDeviceManager.getName(iface);
            if (TextUtils.isEmpty(ifaceName)) {
                HwHiLog.e(TAG, false, "Failed to get iface name", new Object[0]);
                return "";
            } else if (!registerStaIfaceCallback(iface)) {
                HwHiLog.e(TAG, false, "Failed to register STA iface callback", new Object[0]);
                return "";
            } else if (!retrieveWifiChip(iface)) {
                HwHiLog.e(TAG, false, "Failed to get wifi chip", new Object[0]);
                return "";
            } else {
                this.mWifiStafaces.put(ifaceName, iface);
                HwHiLog.i(TAG, false, "createStaIface ifaceName = %{public}s", new Object[]{ifaceName});
                return ifaceName;
            }
        }
    }

    /* access modifiers changed from: private */
    public class StaInterfaceDestroyedListenerInternal implements HwWifi2HalDeviceManager.InterfaceDestroyedListener {
        private final HwWifi2HalDeviceManager.InterfaceDestroyedListener mExternalListener;

        StaInterfaceDestroyedListenerInternal(HwWifi2HalDeviceManager.InterfaceDestroyedListener externalListener) {
            this.mExternalListener = externalListener;
        }

        @Override // com.huawei.wifi2.HwWifi2HalDeviceManager.InterfaceDestroyedListener
        public void onDestroyed(String ifaceName) {
            HwHiLog.i(HwWifi2VendorHal.TAG, false, "StaInterface onDestroyed for ifaceName = %{public}s", new Object[]{ifaceName});
            synchronized (HwWifi2VendorHal.LOCK_OBJ) {
                HwWifi2VendorHal.this.mWifiStafaces.remove(ifaceName);
            }
            HwWifi2HalDeviceManager.InterfaceDestroyedListener interfaceDestroyedListener = this.mExternalListener;
            if (interfaceDestroyedListener != null) {
                interfaceDestroyedListener.onDestroyed(ifaceName);
            }
        }
    }

    public boolean removeStaIface(String ifaceName) {
        synchronized (LOCK_OBJ) {
            IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                HwHiLog.e(TAG, false, "getStaIface error", new Object[0]);
                return false;
            } else if (!(iface instanceof IWifiIface)) {
                return false;
            } else {
                if (!this.mHwWifi2HalDeviceManager.removeIface(iface)) {
                    HwHiLog.e(TAG, false, "Failed to remove STA iface", new Object[0]);
                    return false;
                }
                this.mWifiStafaces.remove(ifaceName);
                return true;
            }
        }
    }

    private boolean retrieveWifiChip(IWifiIface iface) {
        HwHiLog.i(TAG, false, "wifi2 vendor retrieveWifiChip enter", new Object[0]);
        synchronized (LOCK_OBJ) {
            boolean isRegistrationNeeded = this.mWifiChip == null;
            this.mWifiChip = this.mHwWifi2HalDeviceManager.getChip(iface);
            if (this.mWifiChip == null) {
                HwHiLog.e(TAG, false, "Failed to get the chip created for the Iface", new Object[0]);
                return false;
            } else if (!isRegistrationNeeded) {
                HwHiLog.e(TAG, false, "retrieveWifiChip isRegistrationNeeded is false", new Object[0]);
                return true;
            } else if (!registerChipCallback()) {
                HwHiLog.e(TAG, false, "Failed to register chip callback", new Object[0]);
                this.mWifiChip = null;
                return false;
            } else {
                HwHiLog.i(TAG, false, "register chip callback success", new Object[0]);
                return true;
            }
        }
    }

    private boolean registerStaIfaceCallback(IWifiStaIface iface) {
        HwHiLog.i(TAG, false, "registerStaIfaceCallback enter", new Object[0]);
        synchronized (LOCK_OBJ) {
            if (iface == null) {
                HwHiLog.e(TAG, false, "iface is null", new Object[0]);
                return false;
            } else if (this.mIWifiStaIfaceEventCallback == null) {
                HwHiLog.e(TAG, false, "mIWifiStaIfaceEventCallback is null", new Object[0]);
                return false;
            } else {
                try {
                    return isStatusOk(iface.registerEventCallback(this.mIWifiStaIfaceEventCallback));
                } catch (RemoteException e) {
                    handleRemoteException();
                    return false;
                }
            }
        }
    }

    private class StaIfaceEventCallback extends IWifiStaIfaceEventCallback.Stub {
        private StaIfaceEventCallback() {
        }

        public void onBackgroundScanFailure(int cmdId) {
            HwHiLog.i(HwWifi2VendorHal.TAG, false, "StaIfaceEventCallback:onBackgroundScanFailure %{public}d", new Object[]{Integer.valueOf(cmdId)});
        }

        public void onBackgroundFullScanResult(int cmdId, int bucketsScanned, StaScanResult result) {
            HwHiLog.i(HwWifi2VendorHal.TAG, false, "StaIfaceEventCallback: onBackgroundFullScanResult %{public}d", new Object[]{Integer.valueOf(cmdId)});
        }

        public void onBackgroundScanResults(int cmdId, ArrayList<StaScanData> arrayList) {
            HwHiLog.i(HwWifi2VendorHal.TAG, false, "StaIfaceEventCallback: onBackgroundScanResults %{public}d", new Object[]{Integer.valueOf(cmdId)});
        }

        public void onRssiThresholdBreached(int cmdId, byte[] currBssid, int currRssi) {
            HwHiLog.i(HwWifi2VendorHal.TAG, false, "StaIfaceEventCallback: onRssiThresholdBreached %{public}d, currRssi %{public}d", new Object[]{Integer.valueOf(cmdId), Integer.valueOf(currRssi)});
            synchronized (HwWifi2VendorHal.LOCK_OBJ) {
                if (HwWifi2VendorHal.this.mWifiRssiEventHandler != null) {
                    if (cmdId == HwWifi2VendorHal.RSSI_MONITOR_CMD_ID) {
                        HwWifi2VendorHal.this.mWifiRssiEventHandler.onRssiThresholdBreached((byte) currRssi);
                    }
                }
            }
        }
    }

    private boolean registerChipCallback() {
        WifiStatus status;
        synchronized (LOCK_OBJ) {
            if (this.mWifiChip == null) {
                return false;
            }
            try {
                android.hardware.wifi.V1_2.IWifiChip wifiChip = getWifiChipMockableForV1Point2();
                if (wifiChip != null) {
                    status = wifiChip.registerEventCallback_1_2(this.mIWifiChipEventCallbackV12);
                } else {
                    status = this.mWifiChip.registerEventCallback(this.mIWifiChipEventCallback);
                }
                return isStatusOk(status);
            } catch (RemoteException e) {
                handleRemoteException();
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public class ChipEventCallbackV12 extends IWifiChipEventCallback.Stub {
        private ChipEventCallbackV12() {
        }

        public void onChipReconfigured(int modeId) {
            HwWifi2VendorHal.this.mIWifiChipEventCallback.onChipReconfigured(modeId);
        }

        public void onChipReconfigureFailure(WifiStatus status) {
            HwWifi2VendorHal.this.mIWifiChipEventCallback.onChipReconfigureFailure(status);
        }

        public void onIfaceAdded(int type, String name) {
            HwWifi2VendorHal.this.mIWifiChipEventCallback.onIfaceAdded(type, name);
        }

        public void onIfaceRemoved(int type, String name) {
            HwWifi2VendorHal.this.mIWifiChipEventCallback.onIfaceRemoved(type, name);
        }

        public void onDebugRingBufferDataAvailable(WifiDebugRingBufferStatus status, ArrayList<Byte> data) {
            HwWifi2VendorHal.this.mIWifiChipEventCallback.onDebugRingBufferDataAvailable(status, data);
        }

        public void onDebugErrorAlert(int errorCode, ArrayList<Byte> debugData) {
            HwWifi2VendorHal.this.mIWifiChipEventCallback.onDebugErrorAlert(errorCode, debugData);
        }

        public void onRadioModeChange(ArrayList<IWifiChipEventCallback.RadioModeInfo> radioModeInfoList) {
            HwHiLog.i(HwWifi2VendorHal.TAG, false, "onRadioModeChange enter %{public}s", new Object[]{radioModeInfoList});
        }
    }

    /* access modifiers changed from: private */
    public class ChipEventCallback extends IWifiChipEventCallback.Stub {
        private ChipEventCallback() {
        }

        public void onChipReconfigured(int modeId) {
            HwHiLog.i(HwWifi2VendorHal.TAG, false, "onChipReconfigured %{public}d", new Object[]{Integer.valueOf(modeId)});
        }

        public void onChipReconfigureFailure(WifiStatus status) {
            HwHiLog.i(HwWifi2VendorHal.TAG, false, "onChipReconfigureFailure %{public}s", new Object[]{status});
        }

        public void onIfaceAdded(int type, String name) {
            HwHiLog.i(HwWifi2VendorHal.TAG, false, "onIfaceAdded %{public}d, name: %{public}s", new Object[]{Integer.valueOf(type), name});
        }

        public void onIfaceRemoved(int type, String name) {
            HwHiLog.i(HwWifi2VendorHal.TAG, false, "onIfaceRemoved %{public}d, name: %{public}s", new Object[]{Integer.valueOf(type), name});
        }

        public void onDebugRingBufferDataAvailable(WifiDebugRingBufferStatus status, ArrayList<Byte> arrayList) {
            HwHiLog.i(HwWifi2VendorHal.TAG, false, "onDebugRingBufferDataAvailable enter", new Object[0]);
        }

        public void onDebugErrorAlert(int errorCode, ArrayList<Byte> arrayList) {
            HwHiLog.i(HwWifi2VendorHal.TAG, false, "onDebugErrorAlert %{public}d", new Object[]{Integer.valueOf(errorCode)});
        }
    }

    public void stopVendorHal() {
        synchronized (LOCK_OBJ) {
            this.mHwWifi2HalDeviceManager.stop();
            clearState();
            HwHiLog.i(TAG, false, "clean Vendor Hal info", new Object[0]);
        }
    }

    public void clearState() {
        this.mWifiChip = null;
        this.mWifiStafaces.clear();
        this.mHwWifi2HalDeviceManager.dispatchAllDestroyedListeners();
    }

    public int startRssiMonitoring(String ifaceName, byte maxRssi, byte minRssi, HwWifi2Native.WifiRssiEventHandler rssiEventHandler) {
        HwHiLog.i(TAG, false, "startRssiMonitoring enter, maxRssi = %{public}d, minRssi = %{public}d", new Object[]{Byte.valueOf(maxRssi), Byte.valueOf(minRssi)});
        if (maxRssi <= minRssi || rssiEventHandler == null) {
            return -1;
        }
        synchronized (LOCK_OBJ) {
            IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return -1;
            }
            try {
                iface.stopRssiMonitoring((int) RSSI_MONITOR_CMD_ID);
                if (!isStatusOk(iface.startRssiMonitoring((int) RSSI_MONITOR_CMD_ID, maxRssi, minRssi))) {
                    return -1;
                }
                this.mWifiRssiEventHandler = rssiEventHandler;
                return 0;
            } catch (RemoteException e) {
                handleRemoteException();
                return -1;
            }
        }
    }

    public int stopRssiMonitoring(String ifaceName) {
        HwHiLog.i(TAG, false, "stopRssiMonitoring enter, ifaceName = %{public}s", new Object[]{ifaceName});
        synchronized (LOCK_OBJ) {
            this.mWifiRssiEventHandler = null;
            IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return -1;
            }
            try {
                if (!isStatusOk(iface.stopRssiMonitoring((int) RSSI_MONITOR_CMD_ID))) {
                    return -1;
                }
                return 0;
            } catch (RemoteException e) {
                handleRemoteException();
                return -1;
            }
        }
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_2.IWifiChip getWifiChipMockableForV1Point2() {
        IWifiChip iWifiChip = this.mWifiChip;
        if (iWifiChip == null) {
            return null;
        }
        return android.hardware.wifi.V1_2.IWifiChip.castFrom(iWifiChip);
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_3.IWifiChip getWifiChipMockableForV1Point3() {
        IWifiChip iWifiChip = this.mWifiChip;
        if (iWifiChip == null) {
            return null;
        }
        return android.hardware.wifi.V1_3.IWifiChip.castFrom(iWifiChip);
    }

    public boolean getRoamingCapabilities(String ifaceName, HwWifi2Native.RoamingCapabilities capabilities) {
        HwHiLog.i(TAG, false, "getRoamingCapabilities enter", new Object[0]);
        synchronized (LOCK_OBJ) {
            IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                HwHiLog.e(TAG, false, "getRoamingCapabilities: getStaIface error", new Object[0]);
                return false;
            }
            try {
                MutableBoolean ok = new MutableBoolean(false);
                HwHiLog.i(TAG, false, "getRoamingCapabilities start", new Object[0]);
                iface.getRoamingCapabilities(new IWifiStaIface.getRoamingCapabilitiesCallback(capabilities, ok) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2VendorHal$aygPteEvkD6K5sP9m2L8NLa0F0A */
                    private final /* synthetic */ HwWifi2Native.RoamingCapabilities f$1;
                    private final /* synthetic */ MutableBoolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void onValues(WifiStatus wifiStatus, StaRoamingCapabilities staRoamingCapabilities) {
                        HwWifi2VendorHal.this.lambda$getRoamingCapabilities$0$HwWifi2VendorHal(this.f$1, this.f$2, wifiStatus, staRoamingCapabilities);
                    }
                });
                return ok.value;
            } catch (RemoteException e) {
                handleRemoteException();
                HwHiLog.e(TAG, false, "getRoamingCapabilities: getStaIface error", new Object[0]);
                return false;
            }
        }
    }

    public /* synthetic */ void lambda$getRoamingCapabilities$0$HwWifi2VendorHal(HwWifi2Native.RoamingCapabilities out, MutableBoolean ok, WifiStatus status, StaRoamingCapabilities cap) {
        if (!isStatusOk(status)) {
            HwHiLog.e(TAG, false, "getRoamingCapabilities err", new Object[0]);
            return;
        }
        out.maxBlacklistSize = cap.maxBlacklistSize;
        out.maxWhitelistSize = cap.maxWhitelistSize;
        ok.value = true;
        HwHiLog.i(TAG, false, "getRoamingCapabilities success", new Object[0]);
    }

    public int enableFirmwareRoaming(String ifaceName, int state) {
        byte val;
        synchronized (LOCK_OBJ) {
            IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return 1;
            }
            if (state == 0) {
                val = 0;
            } else if (state != 1) {
                try {
                    HwHiLog.e(TAG, false, "enableFirmwareRoaming invalid state = %{public}d", new Object[]{Integer.valueOf(state)});
                    return 1;
                } catch (RemoteException e) {
                    handleRemoteException();
                    return 1;
                }
            } else {
                val = 1;
            }
            WifiStatus status = iface.setRoamingState(val);
            if (isStatusOk(status)) {
                HwHiLog.i(TAG, false, "iface.setRoamingState success", new Object[0]);
                return 0;
            } else if (status.code == 8) {
                return 2;
            } else {
                return 1;
            }
        }
    }

    public boolean configureRoaming(String ifaceName, HwWifi2Native.RoamingConfig config) {
        HwHiLog.i(TAG, false, "configureRoaming ENTER", new Object[0]);
        synchronized (LOCK_OBJ) {
            IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return false;
            }
            try {
                StaRoamingConfig roamingConfig = new StaRoamingConfig();
                if (config.blacklistBssids != null) {
                    Iterator<String> it = config.blacklistBssids.iterator();
                    while (it.hasNext()) {
                        roamingConfig.bssidBlacklist.add(NativeUtil.macAddressToByteArray(it.next()));
                    }
                }
                if (config.whitelistSsids != null) {
                    Iterator<String> it2 = config.whitelistSsids.iterator();
                    while (it2.hasNext()) {
                        roamingConfig.ssidWhitelist.add(NativeUtil.byteArrayFromArrayList(NativeUtil.decodeSsid(it2.next())));
                    }
                }
                if (!isStatusOk(iface.configureRoaming(roamingConfig))) {
                    return false;
                }
                HwHiLog.i(TAG, false, "iface.configureRoaming success", new Object[0]);
                return true;
            } catch (RemoteException e) {
                handleRemoteException();
                return false;
            } catch (IllegalArgumentException e2) {
                HwHiLog.e(TAG, false, "Illegal argument for roaming configuration", new Object[0]);
                return false;
            }
        }
    }

    public int startSendingOffloadedPacket(String ifaceName, byte[] srcMac, byte[] dstMac, byte[] packet, int[] param) {
        RemoteException e;
        HwHiLog.i(TAG, false, "startSendingOffloadedPacket enter", new Object[0]);
        ArrayList<Byte> data = NativeUtil.byteArrayToArrayList(packet);
        synchronized (LOCK_OBJ) {
            try {
                IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    return -1;
                }
                try {
                    try {
                        if (!isStatusOk(iface.startSendingKeepAlivePackets(param[0], data, (short) param[1], srcMac, dstMac, param[2]))) {
                            try {
                                return -1;
                            } catch (Throwable th) {
                                e = th;
                                throw e;
                            }
                        } else {
                            HwHiLog.i(TAG, false, "iface.startSendingKeepAlivePackets success", new Object[0]);
                            return 0;
                        }
                    } catch (RemoteException e2) {
                        handleRemoteException();
                        return -1;
                    }
                } catch (RemoteException e3) {
                    handleRemoteException();
                    return -1;
                }
            } catch (Throwable th2) {
                e = th2;
                throw e;
            }
        }
    }

    public int stopSendingOffloadedPacket(String ifaceName, int slot) {
        HwHiLog.i(TAG, false, "stopSendingOffloadedPacket enter", new Object[0]);
        synchronized (LOCK_OBJ) {
            IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return -1;
            }
            try {
                if (!isStatusOk(iface.stopSendingKeepAlivePackets(slot))) {
                    return -1;
                }
                HwHiLog.i(TAG, false, "iface.stopSendingOffloadedPacket success", new Object[0]);
                return 0;
            } catch (RemoteException e) {
                handleRemoteException();
                return -1;
            }
        }
    }

    public boolean setMacAddress(String ifaceName, MacAddress mac) {
        byte[] macByteArray = mac.toByteArray();
        synchronized (LOCK_OBJ) {
            try {
                android.hardware.wifi.V1_2.IWifiStaIface ifaceV12 = getWifiStaIfaceMockableForV1Point2(ifaceName);
                if (ifaceV12 == null) {
                    HwHiLog.e(TAG, false, "setMacAddress fail, ifaceV12 = null", new Object[0]);
                    return false;
                } else if (!isStatusOk(ifaceV12.setMacAddress(macByteArray))) {
                    HwHiLog.e(TAG, false, "setMacAddress ifaceV12 setMacAddress fail", new Object[0]);
                    return false;
                } else {
                    HwHiLog.i(TAG, false, "setMacAddress success", new Object[0]);
                    return true;
                }
            } catch (RemoteException e) {
                handleRemoteException();
                return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_2.IWifiStaIface getWifiStaIfaceMockableForV1Point2(String ifaceName) {
        IWifiStaIface iface = getStaIface(ifaceName);
        if (iface != null) {
            return android.hardware.wifi.V1_2.IWifiStaIface.castFrom(iface);
        }
        HwHiLog.e(TAG, false, "getWifiStaIfaceMockableForV1Point2 getStaIface fail", new Object[0]);
        return null;
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_3.IWifiStaIface getWifiStaIfaceMockableForV1Point3(String ifaceName) {
        IWifiStaIface iface = getStaIface(ifaceName);
        if (iface == null) {
            return null;
        }
        return android.hardware.wifi.V1_3.IWifiStaIface.castFrom(iface);
    }

    public MacAddress getFactoryMacAddress(String ifaceName) {
        synchronized (LOCK_OBJ) {
            try {
                android.hardware.wifi.V1_3.IWifiStaIface ifaceV13 = getWifiStaIfaceMockableForV1Point3(ifaceName);
                if (ifaceV13 == null) {
                    return null;
                }
                AnonymousClass1AnswerBox box = new Object() {
                    /* class com.huawei.wifi2.HwWifi2VendorHal.AnonymousClass1AnswerBox */
                    protected MacAddress mac = null;
                };
                ifaceV13.getFactoryMacAddress(new IWifiStaIface.getFactoryMacAddressCallback(box) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2VendorHal$KmlimVLCvMkATgi1g2xgQOmH_Lo */
                    private final /* synthetic */ HwWifi2VendorHal.AnonymousClass1AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, byte[] bArr) {
                        HwWifi2VendorHal.this.lambda$getFactoryMacAddress$1$HwWifi2VendorHal(this.f$1, wifiStatus, bArr);
                    }
                });
                return box.mac;
            } catch (RemoteException e) {
                handleRemoteException();
                return null;
            }
        }
    }

    public /* synthetic */ void lambda$getFactoryMacAddress$1$HwWifi2VendorHal(AnonymousClass1AnswerBox box, WifiStatus status, byte[] macBytes) {
        if (isStatusOk(status)) {
            HwHiLog.i(TAG, false, "getFactoryMacAddress success", new Object[0]);
            box.mac = MacAddress.fromBytes(macBytes);
        }
    }

    public WifiLinkLayerStats getWifiLinkLayerStats(String ifaceName) {
        if (getWifiStaIfaceForV13Mockable(ifaceName) != null) {
            return getWifiLinkLayerStatsV13Internal(ifaceName);
        }
        return getWifiLinkLayerStatsInternal(ifaceName);
    }

    public ApfCapabilities getApfCapabilities(String ifaceName) {
        synchronized (LOCK_OBJ) {
            try {
                android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    HwHiLog.e(TAG, false, "mIWifiStaIface is null, getApfCapabilities(0, 0, 0)", new Object[0]);
                    return APF_CAPA;
                }
                AnonymousClass2AnswerBox box = new Object() {
                    /* class com.huawei.wifi2.HwWifi2VendorHal.AnonymousClass2AnswerBox */
                    protected ApfCapabilities value = HwWifi2VendorHal.APF_CAPA;
                };
                iface.getApfPacketFilterCapabilities(new IWifiStaIface.getApfPacketFilterCapabilitiesCallback(box) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2VendorHal$C3xAjjLttONCWN_2191fCr6eho */
                    private final /* synthetic */ HwWifi2VendorHal.AnonymousClass2AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, StaApfPacketFilterCapabilities staApfPacketFilterCapabilities) {
                        HwWifi2VendorHal.this.lambda$getApfCapabilities$2$HwWifi2VendorHal(this.f$1, wifiStatus, staApfPacketFilterCapabilities);
                    }
                });
                return box.value;
            } catch (RemoteException e) {
                handleRemoteException();
                return APF_CAPA;
            }
        }
    }

    public /* synthetic */ void lambda$getApfCapabilities$2$HwWifi2VendorHal(AnonymousClass2AnswerBox box, WifiStatus status, StaApfPacketFilterCapabilities capabilities) {
        if (!isStatusOk(status)) {
            HwHiLog.e(TAG, false, "getApfCapabilities failed", new Object[0]);
            return;
        }
        box.value = new ApfCapabilities(capabilities.version, capabilities.maxLength, OsConstants.ARPHRD_ETHER);
        HwHiLog.i(TAG, false, "getApfCapabilities:(version:%{public}s, maxLength:%{public}s, %{public}d", new Object[]{String.valueOf(capabilities.version), String.valueOf(capabilities.maxLength), Integer.valueOf(OsConstants.ARPHRD_ETHER)});
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_2.IWifiStaIface getWifiStafaceForV12Mockable(String ifaceName) {
        android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
        if (iface == null) {
            return null;
        }
        return android.hardware.wifi.V1_2.IWifiStaIface.castFrom(iface);
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_3.IWifiStaIface getWifiStaIfaceForV13Mockable(String ifaceName) {
        android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
        if (iface == null) {
            return null;
        }
        return android.hardware.wifi.V1_3.IWifiStaIface.castFrom(iface);
    }

    private WifiLinkLayerStats getWifiLinkLayerStatsInternal(String ifaceName) {
        AnonymousClass3AnswerBox answer = new Object() {
            /* class com.huawei.wifi2.HwWifi2VendorHal.AnonymousClass3AnswerBox */
            protected StaLinkLayerStats value = null;
        };
        synchronized (LOCK_OBJ) {
            try {
                android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    return null;
                }
                iface.getLinkLayerStats(new IWifiStaIface.getLinkLayerStatsCallback(answer) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2VendorHal$LcXDf3LHgOL73UiiALVUBpUB8h4 */
                    private final /* synthetic */ HwWifi2VendorHal.AnonymousClass3AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, StaLinkLayerStats staLinkLayerStats) {
                        HwWifi2VendorHal.this.lambda$getWifiLinkLayerStatsInternal$3$HwWifi2VendorHal(this.f$1, wifiStatus, staLinkLayerStats);
                    }
                });
                return frameworkFromHalLinkLayerStats(answer.value);
            } catch (RemoteException e) {
                handleRemoteException();
                return null;
            }
        }
    }

    public /* synthetic */ void lambda$getWifiLinkLayerStatsInternal$3$HwWifi2VendorHal(AnonymousClass3AnswerBox answer, WifiStatus status, StaLinkLayerStats stats) {
        if (isStatusOk(status)) {
            answer.value = stats;
        }
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.V1_3.IWifiChip getWifiChipForV1Point3Mockable() {
        IWifiChip iWifiChip = this.mWifiChip;
        if (iWifiChip == null) {
            return null;
        }
        return android.hardware.wifi.V1_3.IWifiChip.castFrom(iWifiChip);
    }

    public boolean setLowLatencyMode(boolean isEnabled) {
        int mode;
        synchronized (LOCK_OBJ) {
            android.hardware.wifi.V1_3.IWifiChip wifiChipV13 = getWifiChipForV1Point3Mockable();
            if (wifiChipV13 == null) {
                return false;
            }
            if (isEnabled) {
                mode = 1;
            } else {
                mode = 0;
            }
            try {
                if (isStatusOk(wifiChipV13.setLatencyMode(mode))) {
                    HwHiLog.i(TAG, false, "Setting low-latency mode to %{public}b", new Object[]{Boolean.valueOf(isEnabled)});
                    return true;
                }
                HwHiLog.w(TAG, false, "Failed to set low-latency mode to %{public}b", new Object[]{Boolean.valueOf(isEnabled)});
                return false;
            } catch (RemoteException e) {
                handleRemoteException();
                return false;
            }
        }
    }

    private WifiLinkLayerStats getWifiLinkLayerStatsV13Internal(String ifaceName) {
        AnonymousClass4AnswerBox answer = new Object() {
            /* class com.huawei.wifi2.HwWifi2VendorHal.AnonymousClass4AnswerBox */
            protected android.hardware.wifi.V1_3.StaLinkLayerStats value = null;
        };
        synchronized (LOCK_OBJ) {
            try {
                android.hardware.wifi.V1_3.IWifiStaIface iface = getWifiStaIfaceForV13Mockable(ifaceName);
                if (iface == null) {
                    return null;
                }
                iface.getLinkLayerStats_1_3(new IWifiStaIface.getLinkLayerStats_1_3Callback(answer) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2VendorHal$LjhEPlL_Yw9oLvaQdbW1yEHDAyg */
                    private final /* synthetic */ HwWifi2VendorHal.AnonymousClass4AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, android.hardware.wifi.V1_3.StaLinkLayerStats staLinkLayerStats) {
                        HwWifi2VendorHal.this.lambda$getWifiLinkLayerStatsV13Internal$4$HwWifi2VendorHal(this.f$1, wifiStatus, staLinkLayerStats);
                    }
                });
                return frameworkFromHalLinkLayerStatsV13(answer.value);
            } catch (RemoteException e) {
                handleRemoteException();
                return null;
            }
        }
    }

    public /* synthetic */ void lambda$getWifiLinkLayerStatsV13Internal$4$HwWifi2VendorHal(AnonymousClass4AnswerBox answer, WifiStatus status, android.hardware.wifi.V1_3.StaLinkLayerStats stats) {
        if (isStatusOk(status)) {
            answer.value = stats;
        }
    }

    static WifiLinkLayerStats frameworkFromHalLinkLayerStats(StaLinkLayerStats stats) {
        if (stats == null) {
            return null;
        }
        WifiLinkLayerStats out = new WifiLinkLayerStats();
        setIfaceStats(out, stats.iface);
        setRadioStats(out, stats.radios);
        setTimeStamp(out, stats.timeStampInMs);
        out.version = WifiLinkLayerStats.V1_0;
        return out;
    }

    static WifiLinkLayerStats frameworkFromHalLinkLayerStatsV13(android.hardware.wifi.V1_3.StaLinkLayerStats stats) {
        if (stats == null) {
            return null;
        }
        WifiLinkLayerStats out = new WifiLinkLayerStats();
        setIfaceStats(out, stats.iface);
        setRadioStatsV13(out, stats.radios);
        setTimeStamp(out, stats.timeStampInMs);
        out.version = WifiLinkLayerStats.V1_3;
        return out;
    }

    private static void setIfaceStats(WifiLinkLayerStats stats, StaLinkLayerIfaceStats iface) {
        if (iface != null) {
            stats.beaconRx = iface.beaconRx;
            stats.rssiMgmt = iface.avgRssiMgmt;
            stats.rxmpduBe = iface.wmeBePktStats.rxMpdu;
            stats.txmpduBe = iface.wmeBePktStats.txMpdu;
            stats.lostmpduBe = iface.wmeBePktStats.lostMpdu;
            stats.retriesBe = iface.wmeBePktStats.retries;
            stats.rxmpduBk = iface.wmeBkPktStats.rxMpdu;
            stats.txmpduBk = iface.wmeBkPktStats.txMpdu;
            stats.lostmpduBk = iface.wmeBkPktStats.lostMpdu;
            stats.retriesBk = iface.wmeBkPktStats.retries;
            stats.rxmpduVi = iface.wmeViPktStats.rxMpdu;
            stats.txmpduVi = iface.wmeViPktStats.txMpdu;
            stats.lostmpduVi = iface.wmeViPktStats.lostMpdu;
            stats.retriesVi = iface.wmeViPktStats.retries;
            stats.rxmpduVo = iface.wmeVoPktStats.rxMpdu;
            stats.txmpduVo = iface.wmeVoPktStats.txMpdu;
            stats.lostmpduVo = iface.wmeVoPktStats.lostMpdu;
            stats.retriesVo = iface.wmeVoPktStats.retries;
        }
    }

    private static void setRadioStats(WifiLinkLayerStats stats, List<StaLinkLayerRadioStats> radios) {
        if (radios != null && radios.size() > 0) {
            StaLinkLayerRadioStats radioStats = radios.get(0);
            stats.onTime = radioStats.onTimeInMs;
            stats.txTime = radioStats.txTimeInMs;
            stats.txTimePerLevel = new int[radioStats.txTimeInMsPerLevel.size()];
            for (int i = 0; i < stats.txTimePerLevel.length; i++) {
                stats.txTimePerLevel[i] = ((Integer) radioStats.txTimeInMsPerLevel.get(i)).intValue();
            }
            stats.rxTime = radioStats.rxTimeInMs;
            stats.onTimeScan = radioStats.onTimeInMsForScan;
        }
    }

    private static void setRadioStatsV13(WifiLinkLayerStats stats, List<android.hardware.wifi.V1_3.StaLinkLayerRadioStats> radios) {
        if (radios != null && radios.size() > 0) {
            android.hardware.wifi.V1_3.StaLinkLayerRadioStats radioStats = radios.get(0);
            stats.onTime = radioStats.V1_0.onTimeInMs;
            stats.txTime = radioStats.V1_0.txTimeInMs;
            stats.txTimePerLevel = new int[radioStats.V1_0.txTimeInMsPerLevel.size()];
            for (int i = 0; i < stats.txTimePerLevel.length; i++) {
                stats.txTimePerLevel[i] = ((Integer) radioStats.V1_0.txTimeInMsPerLevel.get(i)).intValue();
            }
            stats.rxTime = radioStats.V1_0.rxTimeInMs;
            stats.onTimeScan = radioStats.V1_0.onTimeInMsForScan;
            stats.onTimeNanScan = radioStats.onTimeInMsForNanScan;
            stats.onTimeBackgroundScan = radioStats.onTimeInMsForBgScan;
            stats.onTimeRoamScan = radioStats.onTimeInMsForRoamScan;
            stats.onTimePnoScan = radioStats.onTimeInMsForPnoScan;
            stats.onTimeHs20Scan = radioStats.onTimeInMsForHs20Scan;
            for (int i2 = 0; i2 < radioStats.channelStats.size(); i2++) {
                WifiChannelStats channelStats = (WifiChannelStats) radioStats.channelStats.get(i2);
                WifiLinkLayerStats.ChannelStats channelStatsEntry = new WifiLinkLayerStats.ChannelStats();
                channelStatsEntry.frequency = channelStats.channel.centerFreq;
                channelStatsEntry.radioOnTimeMs = channelStats.onTimeInMs;
                channelStatsEntry.ccaBusyTimeMs = channelStats.ccaBusyTimeInMs;
                stats.channelStatsMap.put(channelStats.channel.centerFreq, channelStatsEntry);
            }
        }
    }

    private static void setTimeStamp(WifiLinkLayerStats stats, long timeStampInMs) {
        stats.timeStampInMs = timeStampInMs;
    }

    public boolean configureNeighborDiscoveryOffload(String ifaceName, boolean isEnabled) {
        synchronized (LOCK_OBJ) {
            android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
            if (iface == null) {
                return boolResult(false);
            }
            try {
                if (!isStatusOk(iface.enableNdOffload(isEnabled))) {
                    return false;
                }
                return true;
            } catch (RemoteException e) {
                handleRemoteException();
                return false;
            }
        }
    }

    public byte[] readPacketFilter(String ifaceName) {
        AnonymousClass5AnswerBox answer = new Object() {
            /* class com.huawei.wifi2.HwWifi2VendorHal.AnonymousClass5AnswerBox */
            protected byte[] data = null;
        };
        synchronized (LOCK_OBJ) {
            try {
                android.hardware.wifi.V1_2.IWifiStaIface ifaceV12 = getWifiStafaceForV12Mockable(ifaceName);
                if (ifaceV12 == null) {
                    return null;
                }
                ifaceV12.readApfPacketFilterData(new IWifiStaIface.readApfPacketFilterDataCallback(answer) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2VendorHal$YLgoRRdAkWCrZQflcmOwrQmOies */
                    private final /* synthetic */ HwWifi2VendorHal.AnonymousClass5AnswerBox f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        HwWifi2VendorHal.this.lambda$readPacketFilter$5$HwWifi2VendorHal(this.f$1, wifiStatus, arrayList);
                    }
                });
                return answer.data;
            } catch (RemoteException e) {
                handleRemoteException();
                return null;
            }
        }
    }

    public /* synthetic */ void lambda$readPacketFilter$5$HwWifi2VendorHal(AnonymousClass5AnswerBox answer, WifiStatus status, ArrayList dataByteArray) {
        if (isStatusOk(status)) {
            answer.data = NativeUtil.byteArrayFromArrayList(dataByteArray);
        }
    }

    public boolean installPacketFilter(String ifaceName, byte[] filter) {
        if (filter == null) {
            return boolResult(false);
        }
        ArrayList<Byte> program = NativeUtil.byteArrayToArrayList(filter);
        synchronized (LOCK_OBJ) {
            try {
                android.hardware.wifi.V1_0.IWifiStaIface iface = getStaIface(ifaceName);
                if (iface == null) {
                    return boolResult(false);
                } else if (!isStatusOk(iface.installApfPacketFilter(0, program))) {
                    return false;
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                handleRemoteException();
                return false;
            }
        }
    }

    private boolean boolResult(boolean isResultTrue) {
        return isResultTrue;
    }
}
