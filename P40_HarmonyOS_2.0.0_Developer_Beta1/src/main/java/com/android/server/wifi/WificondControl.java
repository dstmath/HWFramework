package com.android.server.wifi;

import android.app.AlarmManager;
import android.net.wifi.IApInterface;
import android.net.wifi.IApInterfaceEventCallback;
import android.net.wifi.IClientInterface;
import android.net.wifi.IHwVendorEvent;
import android.net.wifi.IPnoScanEvent;
import android.net.wifi.IScanEvent;
import android.net.wifi.IWifiScannerImpl;
import android.net.wifi.IWificond;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiSsid;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hwUtil.HwInformationElementUtilEx;
import com.android.server.wifi.hwUtil.ScanResultRecords;
import com.android.server.wifi.hwUtil.ScanResultUtilEx;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.util.InformationElementUtil;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.wificond.ChannelSettings;
import com.android.server.wifi.wificond.HiddenNetwork;
import com.android.server.wifi.wificond.NativeMssResult;
import com.android.server.wifi.wificond.NativeScanResult;
import com.android.server.wifi.wificond.PnoNetwork;
import com.android.server.wifi.wificond.PnoSettings;
import com.android.server.wifi.wificond.RadioChainInfo;
import com.android.server.wifi.wificond.SingleScanSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class WificondControl implements IBinder.DeathRecipient {
    public static final int SCAN_TYPE_PNO_SCAN = 1;
    public static final int SCAN_TYPE_SINGLE_SCAN = 0;
    public static final int SEND_MGMT_FRAME_TIMEOUT_MS = 1000;
    private static final String TAG = "WificondControl";
    private static final String TIMEOUT_ALARM_TAG = "WificondControl Send Management Frame Timeout";
    public static final int WIFI_PARAMETER_LENGTH = 13;
    private AlarmManager mAlarmManager;
    private HashMap<String, IApInterfaceEventCallback> mApInterfaceListeners = new HashMap<>();
    private HashMap<String, IApInterface> mApInterfaces = new HashMap<>();
    private final CarrierNetworkConfig mCarrierNetworkConfig;
    private final HashMap<String, IClientInterface> mClientInterfaces = new HashMap<>();
    private Clock mClock;
    private WifiNative.WificondDeathEventHandler mDeathEventHandler;
    private Handler mEventHandler;
    private IHwVendorEvent mHwVendorEventHandler;
    private boolean mIsEnhancedOpenSupported;
    private boolean mIsEnhancedOpenSupportedInitialized = false;
    private final Set<String> mOldSsidList = new HashSet();
    private HashMap<String, IPnoScanEvent> mPnoScanEventHandlers = new HashMap<>();
    private HashMap<String, IScanEvent> mScanEventHandlers = new HashMap<>();
    private boolean mVerboseLoggingEnabled = false;
    private WifiInjector mWifiInjector;
    private WifiMonitor mWifiMonitor;
    private WifiNative mWifiNative = null;
    private IWificond mWificond;
    private HashMap<String, IWifiScannerImpl> mWificondScanners = new HashMap<>();

    /* access modifiers changed from: private */
    public class ScanEventHandler extends IScanEvent.Stub {
        private String mIfaceName;

        ScanEventHandler(String ifaceName) {
            this.mIfaceName = ifaceName;
        }

        @Override // android.net.wifi.IScanEvent
        public void OnScanResultReady() {
            Log.i(WificondControl.TAG, "Scan result ready event");
            WificondControl.this.mWifiMonitor.broadcastScanResultEvent(this.mIfaceName);
        }

        @Override // android.net.wifi.IScanEvent
        public void OnScanFailed() {
            Log.e(WificondControl.TAG, "Scan failed event");
            WificondControl.this.mWifiMonitor.broadcastScanFailedEvent(this.mIfaceName);
        }
    }

    WificondControl(WifiInjector wifiInjector, WifiMonitor wifiMonitor, CarrierNetworkConfig carrierNetworkConfig, AlarmManager alarmManager, Looper looper, Clock clock) {
        this.mWifiInjector = wifiInjector;
        this.mWifiMonitor = wifiMonitor;
        this.mCarrierNetworkConfig = carrierNetworkConfig;
        this.mAlarmManager = alarmManager;
        this.mEventHandler = new Handler(looper);
        this.mClock = clock;
    }

    /* access modifiers changed from: private */
    public class PnoScanEventHandler extends IPnoScanEvent.Stub {
        private String mIfaceName;

        PnoScanEventHandler(String ifaceName) {
            this.mIfaceName = ifaceName;
        }

        @Override // android.net.wifi.IPnoScanEvent
        public void OnPnoNetworkFound() {
            Log.i(WificondControl.TAG, "Pno scan result event");
            WificondControl.this.mWifiMonitor.broadcastPnoScanResultEvent(this.mIfaceName);
            WificondControl.this.mWifiInjector.getWifiMetrics().incrementPnoFoundNetworkEventCount();
        }

        @Override // android.net.wifi.IPnoScanEvent
        public void OnPnoScanFailed() {
            Log.e(WificondControl.TAG, "Pno Scan failed event");
            WificondControl.this.mWifiInjector.getWifiMetrics().incrementPnoScanFailedCount();
        }

        @Override // android.net.wifi.IPnoScanEvent
        public void OnPnoScanOverOffloadStarted() {
            Log.d(WificondControl.TAG, "Pno scan over offload started");
            WificondControl.this.mWifiInjector.getWifiMetrics().incrementPnoScanStartedOverOffloadCount();
        }

        @Override // android.net.wifi.IPnoScanEvent
        public void OnPnoScanOverOffloadFailed(int reason) {
            Log.d(WificondControl.TAG, "Pno scan over offload failed");
            WificondControl.this.mWifiInjector.getWifiMetrics().incrementPnoScanFailedOverOffloadCount();
        }
    }

    /* access modifiers changed from: private */
    public class ApInterfaceEventCallback extends IApInterfaceEventCallback.Stub {
        private WifiNative.SoftApListener mSoftApListener;

        ApInterfaceEventCallback(WifiNative.SoftApListener listener) {
            this.mSoftApListener = listener;
        }

        @Override // android.net.wifi.IApInterfaceEventCallback
        public void onNumAssociatedStationsChanged(int numStations) {
            this.mSoftApListener.onNumAssociatedStationsChanged(numStations);
        }

        @Override // android.net.wifi.IApInterfaceEventCallback
        public void onSoftApChannelSwitched(int frequency, int bandwidth) {
            this.mSoftApListener.onSoftApChannelSwitched(frequency, bandwidth);
        }

        @Override // android.net.wifi.IApInterfaceEventCallback
        public void OnApLinkedStaJoin(String macAddress) {
            this.mSoftApListener.OnApLinkedStaJoin(macAddress);
        }

        @Override // android.net.wifi.IApInterfaceEventCallback
        public void OnApLinkedStaLeave(String macAddress) {
            this.mSoftApListener.OnApLinkedStaLeave(macAddress);
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        this.mEventHandler.post(new Runnable() {
            /* class com.android.server.wifi.$$Lambda$WificondControl$JpHuQX0ohyDoa8WT39rjqI0Ce_Q */

            @Override // java.lang.Runnable
            public final void run() {
                WificondControl.this.lambda$binderDied$0$WificondControl();
            }
        });
    }

    public /* synthetic */ void lambda$binderDied$0$WificondControl() {
        Log.e(TAG, "Wificond died!");
        clearState();
        synchronized (this) {
            this.mWificond = null;
        }
        WifiNative.WificondDeathEventHandler wificondDeathEventHandler = this.mDeathEventHandler;
        if (wificondDeathEventHandler != null) {
            wificondDeathEventHandler.onDeath();
        }
    }

    public void enableVerboseLogging(boolean enable) {
        this.mVerboseLoggingEnabled = enable;
    }

    public boolean initialize(WifiNative.WificondDeathEventHandler handler) {
        if (this.mDeathEventHandler != null) {
            Log.e(TAG, "Death handler already present");
        }
        this.mDeathEventHandler = handler;
        tearDownInterfaces();
        return true;
    }

    private boolean retrieveWificondAndRegisterForDeath() {
        if (this.mWificond != null) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "Wificond handle already retrieved");
            }
            return true;
        }
        this.mWificond = this.mWifiInjector.makeWificond();
        try {
            synchronized (this) {
                if (this.mWificond == null) {
                    Log.e(TAG, "Failed to get reference to wificond");
                    return false;
                }
                this.mWificond.asBinder().linkToDeath(this, 0);
                return true;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register death notification for wificond");
            return false;
        }
    }

    public IClientInterface setupInterfaceForClientMode(String ifaceName) {
        Log.i(TAG, "Setting up interface for client mode");
        if (!retrieveWificondAndRegisterForDeath()) {
            return null;
        }
        try {
            IClientInterface clientInterface = this.mWificond.createClientInterface(ifaceName);
            if (clientInterface == null) {
                Log.e(TAG, "Could not get IClientInterface instance from wificond");
                return null;
            }
            Binder.allowBlocking(clientInterface.asBinder());
            this.mClientInterfaces.put(ifaceName, clientInterface);
            try {
                IWifiScannerImpl wificondScanner = clientInterface.getWifiScannerImpl();
                if (wificondScanner == null) {
                    Log.e(TAG, "Failed to get WificondScannerImpl");
                    return null;
                }
                this.mWificondScanners.put(ifaceName, wificondScanner);
                Binder.allowBlocking(wificondScanner.asBinder());
                ScanEventHandler scanEventHandler = new ScanEventHandler(ifaceName);
                this.mScanEventHandlers.put(ifaceName, scanEventHandler);
                wificondScanner.subscribeScanEvents(scanEventHandler);
                PnoScanEventHandler pnoScanEventHandler = new PnoScanEventHandler(ifaceName);
                this.mPnoScanEventHandlers.put(ifaceName, pnoScanEventHandler);
                wificondScanner.subscribePnoScanEvents(pnoScanEventHandler);
                this.mHwVendorEventHandler = new HwVendorEventHandler();
                clientInterface.SubscribeVendorEvents(this.mHwVendorEventHandler);
                return clientInterface;
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to refresh wificond scanner due to remote exception");
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to get IClientInterface due to remote exception");
            return null;
        }
    }

    public boolean tearDownClientInterface(String ifaceName) {
        boolean success;
        IClientInterface clientInterface = getClientInterface(ifaceName);
        if (clientInterface == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            return false;
        }
        try {
            IWifiScannerImpl scannerImpl = this.mWificondScanners.get(ifaceName);
            if (scannerImpl != null) {
                scannerImpl.unsubscribeScanEvents();
                scannerImpl.unsubscribePnoScanEvents();
            }
            clientInterface.UnsubscribeVendorEvents();
            try {
                synchronized (this) {
                    if (this.mWificond == null) {
                        Log.e(TAG, "mWificond is null, Wificond may be dead");
                        return false;
                    }
                    success = this.mWificond.tearDownClientInterface(ifaceName);
                }
                if (!success) {
                    Log.e(TAG, "Failed to teardown client interface");
                    return false;
                }
                this.mClientInterfaces.remove(ifaceName);
                this.mWificondScanners.remove(ifaceName);
                this.mScanEventHandlers.remove(ifaceName);
                this.mPnoScanEventHandlers.remove(ifaceName);
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to teardown client interface due to remote exception");
                return false;
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to unsubscribe wificond scanner due to remote exception");
            return false;
        }
    }

    public IApInterface setupInterfaceForSoftApMode(String ifaceName) {
        Log.i(TAG, "Setting up interface for soft ap mode");
        if (!retrieveWificondAndRegisterForDeath()) {
            return null;
        }
        try {
            IApInterface apInterface = this.mWificond.createApInterface(ifaceName);
            if (apInterface == null) {
                Log.e(TAG, "Could not get IApInterface instance from wificond");
                return null;
            }
            Binder.allowBlocking(apInterface.asBinder());
            this.mApInterfaces.put(ifaceName, apInterface);
            return apInterface;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get IApInterface due to remote exception");
            return null;
        }
    }

    public boolean tearDownSoftApInterface(String ifaceName) {
        boolean success;
        if (getApInterface(ifaceName) == null) {
            Log.e(TAG, "No valid wificond ap interface handler");
            return false;
        }
        try {
            synchronized (this) {
                if (this.mWificond == null) {
                    Log.e(TAG, "mWificond is null, Wificond may be dead in tearDownSoftApInterface");
                    return false;
                }
                success = this.mWificond.tearDownApInterface(ifaceName);
            }
            if (!success) {
                Log.e(TAG, "Failed to teardown AP interface");
                return false;
            }
            this.mApInterfaces.remove(ifaceName);
            this.mApInterfaceListeners.remove(ifaceName);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to teardown AP interface due to remote exception");
            return false;
        }
    }

    public boolean tearDownInterfaces() {
        Log.i(TAG, "tearing down interfaces in wificond");
        if (!retrieveWificondAndRegisterForDeath()) {
            return false;
        }
        try {
            synchronized (this.mClientInterfaces) {
                for (Map.Entry<String, IWifiScannerImpl> entry : this.mWificondScanners.entrySet()) {
                    entry.getValue().unsubscribeScanEvents();
                    entry.getValue().unsubscribePnoScanEvents();
                }
                for (Map.Entry<String, IClientInterface> entry2 : this.mClientInterfaces.entrySet()) {
                    entry2.getValue().UnsubscribeVendorEvents();
                }
            }
            synchronized (this) {
                if (this.mWificond == null) {
                    Log.e(TAG, "mWificond is null, Wificond may be dead");
                    return false;
                }
                this.mWificond.tearDownInterfaces();
                clearState();
                this.mHwVendorEventHandler = null;
                synchronized (this.mOldSsidList) {
                    this.mOldSsidList.clear();
                }
                return true;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to tear down interfaces due to remote exception");
            return false;
        }
    }

    private IClientInterface getClientInterface(String ifaceName) {
        return this.mClientInterfaces.get(ifaceName);
    }

    public WifiNative.SignalPollResult signalPoll(String ifaceName) {
        IClientInterface iface = getClientInterface(ifaceName);
        if (iface == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            return null;
        }
        try {
            int[] resultArray = iface.signalPoll();
            if (resultArray == null || !(resultArray.length == 13 || resultArray.length == 4)) {
                Log.e(TAG, "Invalid signal poll result from wificond");
                return null;
            }
            WifiNative.SignalPollResult pollResult = new WifiNative.SignalPollResult();
            pollResult.currentRssi = resultArray[0];
            pollResult.txBitrate = resultArray[1];
            pollResult.associationFrequency = resultArray[2];
            pollResult.rxBitrate = resultArray[3];
            if (resultArray.length == 13) {
                pollResult.currentNoise = resultArray[4];
                pollResult.currentSnr = resultArray[5];
                pollResult.currentChload = resultArray[6];
                pollResult.currentUlDelay = resultArray[7];
                pollResult.currentTxBytes = resultArray[8];
                pollResult.currentTxPackets = resultArray[9];
                pollResult.currentTxFailed = resultArray[10];
                pollResult.currentRxBytes = resultArray[11];
                pollResult.currentRxPackets = resultArray[12];
                Log.i(TAG, "Noise: " + pollResult.currentNoise + ", Snr: " + pollResult.currentSnr + ", Chload: " + pollResult.currentChload + ", rssi: " + pollResult.currentRssi + ", txBitrate: " + pollResult.txBitrate + ", rxBitrate: " + pollResult.rxBitrate + ", frequency: " + pollResult.associationFrequency + ", UlDelay: " + pollResult.currentUlDelay + ", currentTxBytes: " + pollResult.currentTxBytes + ", currentTxPackets: " + pollResult.currentTxPackets + ", currentTxFailed: " + pollResult.currentTxFailed + ", currentRxBytes: " + pollResult.currentRxBytes + ", currentRxPackets: " + pollResult.currentRxPackets);
            }
            return pollResult;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to do signal polling due to remote exception");
            return null;
        }
    }

    public WifiNative.TxPacketCounters getTxPacketCounters(String ifaceName) {
        IClientInterface iface = getClientInterface(ifaceName);
        if (iface == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            return null;
        }
        try {
            int[] resultArray = iface.getPacketCounters();
            if (resultArray == null || resultArray.length != 2) {
                Log.e(TAG, "Invalid signal poll result from wificond");
                return null;
            }
            WifiNative.TxPacketCounters counters = new WifiNative.TxPacketCounters();
            counters.txSucceeded = resultArray[0];
            counters.txFailed = resultArray[1];
            return counters;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to do signal polling due to remote exception");
            return null;
        }
    }

    private IWifiScannerImpl getScannerImpl(String ifaceName) {
        return this.mWificondScanners.get(ifaceName);
    }

    /* JADX WARNING: Removed duplicated region for block: B:99:0x02e3  */
    public ArrayList<ScanDetail> getScanResults(String ifaceName, int scanType) {
        NativeScanResult[] nativeResults;
        String result;
        int i;
        int i2;
        NativeScanResult[] nativeResults2;
        IWifiScannerImpl scannerImpl;
        String bssid;
        IllegalArgumentException e;
        boolean z;
        ArrayList<ScanDetail> results = new ArrayList<>();
        IWifiScannerImpl scannerImpl2 = getScannerImpl(ifaceName);
        if (scannerImpl2 == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return results;
        }
        if (scanType == 0) {
            try {
                nativeResults = scannerImpl2.getScanResults();
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to create ScanDetail ArrayList");
                if (this.mVerboseLoggingEnabled) {
                }
                return results;
            }
        } else {
            try {
                nativeResults = scannerImpl2.getPnoScanResults();
            } catch (RemoteException e3) {
                Log.e(TAG, "Failed to create ScanDetail ArrayList");
                if (this.mVerboseLoggingEnabled) {
                }
                return results;
            }
        }
        ScanResultRecords.getDefault().clearOrdSsidRecords();
        int length = nativeResults.length;
        int i3 = 0;
        while (i3 < length) {
            NativeScanResult result2 = nativeResults[i3];
            WifiSsid wifiSsid = WifiSsid.createFromByteArray(result2.ssid);
            try {
                bssid = NativeUtil.macAddressFromByteArray(result2.bssid);
                try {
                    ScanResultRecords.getDefault().recordOriSsid(bssid, wifiSsid.toString(), result2.ssid);
                    wifiSsid.oriSsid = NativeUtil.hexStringFromByteArray(result2.ssid);
                    if (bssid == null) {
                        try {
                            Log.e(TAG, "Illegal null bssid");
                            scannerImpl = scannerImpl2;
                            nativeResults2 = nativeResults;
                            i2 = length;
                            i = i3;
                        } catch (IllegalArgumentException e4) {
                            e = e4;
                            scannerImpl = scannerImpl2;
                            nativeResults2 = nativeResults;
                            i2 = length;
                            i = i3;
                            Log.e(TAG, "Illegal argument for scan result with bssid: " + StringUtilEx.safeDisplayBssid(bssid), e);
                            i3 = i + 1;
                            scannerImpl2 = scannerImpl;
                            nativeResults = nativeResults2;
                            length = i2;
                        }
                        i3 = i + 1;
                        scannerImpl2 = scannerImpl;
                        nativeResults = nativeResults2;
                        length = i2;
                    } else {
                        ScanResult.InformationElement[] ies = InformationElementUtil.parseInformationElements(result2.infoElement);
                        try {
                            InformationElementUtil.Capabilities capabilities = new InformationElementUtil.Capabilities();
                            capabilities.from(ies, result2.capability, isEnhancedOpenSupported());
                            String flags = capabilities.generateCapabilitiesString();
                            try {
                                ScanResultRecords.getDefault().recordPmf(bssid, capabilities.pmfCapabilities);
                                NetworkDetail networkDetail = new NetworkDetail(bssid, ies, null, result2.frequency);
                                if (!wifiSsid.toString().equals(networkDetail.getTrimmedSSID())) {
                                    scannerImpl = scannerImpl2;
                                    try {
                                        z = true;
                                        Log.w(TAG, String.format(Locale.ENGLISH, "Inconsistent SSID on BSSID '%s': '%s' vs '%s' ", StringUtilEx.safeDisplayBssid(bssid), wifiSsid.toString(), networkDetail.getTrimmedSSID()));
                                    } catch (RemoteException e5) {
                                        Log.e(TAG, "Failed to create ScanDetail ArrayList");
                                        if (this.mVerboseLoggingEnabled) {
                                        }
                                        return results;
                                    }
                                } else {
                                    scannerImpl = scannerImpl2;
                                    z = true;
                                }
                                nativeResults2 = nativeResults;
                                i2 = length;
                                i = i3;
                                ScanDetail scanDetail = new ScanDetail(networkDetail, wifiSsid, bssid, flags, result2.signalMbm / 100, result2.frequency, result2.tsf, ies, null);
                                ScanResult scanResult = scanDetail.getScanResult();
                                if (ScanResultUtil.isScanResultForEapNetwork(scanDetail.getScanResult()) && this.mCarrierNetworkConfig.isCarrierNetwork(wifiSsid.toString())) {
                                    scanResult.isCarrierAp = z;
                                    scanResult.carrierApEapType = this.mCarrierNetworkConfig.getNetworkEapType(wifiSsid.toString());
                                    scanResult.carrierName = this.mCarrierNetworkConfig.getCarrierName(wifiSsid.toString());
                                }
                                if (result2.radioChainInfos != null) {
                                    scanResult.radioChainInfos = new ScanResult.RadioChainInfo[result2.radioChainInfos.size()];
                                    int idx = 0;
                                    Iterator<RadioChainInfo> it = result2.radioChainInfos.iterator();
                                    while (it.hasNext()) {
                                        RadioChainInfo nativeRadioChainInfo = it.next();
                                        scanResult.radioChainInfos[idx] = new ScanResult.RadioChainInfo();
                                        scanResult.radioChainInfos[idx].id = nativeRadioChainInfo.chainId;
                                        scanResult.radioChainInfos[idx].level = nativeRadioChainInfo.level;
                                        idx++;
                                    }
                                }
                                HwInformationElementUtilEx.HiLinkNetwork hiLinkNetwork = new HwInformationElementUtilEx.HiLinkNetwork();
                                hiLinkNetwork.from(ies);
                                scanDetail.getScanResult().hilinkTag = hiLinkNetwork.parseHiLogoTag(ies);
                                int apType = ScanResultRecords.getDefault().getHiLinkAp(bssid);
                                if (hiLinkNetwork.isHiLinkNetwork || apType != 0) {
                                    scanDetail.getScanResult().isHiLinkNetwork = z;
                                    int hilinkApType = 1;
                                    if (scanDetail.getScanResult().hilinkTag != 3) {
                                        if (apType != 2) {
                                            ScanResultRecords.getDefault().recordHiLinkAp(bssid, hilinkApType);
                                            Log.i(TAG, "hilinkTag:" + scanDetail.getScanResult().hilinkTag + " ssid is: " + StringUtilEx.safeDisplaySsid(scanDetail.getScanResult().SSID));
                                        }
                                    }
                                    hilinkApType = 2;
                                    scanDetail.getScanResult().isHiLinkNetwork = false;
                                    ScanResultRecords.getDefault().recordHiLinkAp(bssid, hilinkApType);
                                    Log.i(TAG, "hilinkTag:" + scanDetail.getScanResult().hilinkTag + " ssid is: " + StringUtilEx.safeDisplaySsid(scanDetail.getScanResult().SSID));
                                } else {
                                    scanDetail.getScanResult().isHiLinkNetwork = false;
                                }
                                if (this.mWifiNative == null) {
                                    this.mWifiNative = this.mWifiInjector.getWifiNative();
                                }
                                if (this.mWifiNative != null) {
                                    if (this.mWifiNative.mHwWifiNativeEx.getChipsetWifiCategory() == 1) {
                                        scanResult.setSupportedWifiCategory(1);
                                    } else {
                                        int wifiCategory = new HwInformationElementUtilEx().getWifiCategoryFromIes(scanResult.informationElements, this.mWifiNative.mHwWifiNativeEx.getChipsetWifiFeatrureCapability());
                                        scanResult.setSupportedWifiCategory(wifiCategory);
                                        ScanResultRecords.getDefault().recordWifiCategory(bssid, wifiCategory);
                                    }
                                }
                                HwInformationElementUtilEx.Dot11vNetwork dot11vNetwork = new HwInformationElementUtilEx.Dot11vNetwork();
                                dot11vNetwork.from(ies);
                                scanDetail.getScanResult().dot11vNetwork = dot11vNetwork.dot11vNetwork;
                                results.add(scanDetail);
                            } catch (IllegalArgumentException e6) {
                                e = e6;
                                scannerImpl = scannerImpl2;
                                nativeResults2 = nativeResults;
                                i2 = length;
                                i = i3;
                                Log.e(TAG, "Illegal argument for scan result with bssid: " + StringUtilEx.safeDisplayBssid(bssid), e);
                                i3 = i + 1;
                                scannerImpl2 = scannerImpl;
                                nativeResults = nativeResults2;
                                length = i2;
                            }
                        } catch (IllegalArgumentException e7) {
                            e = e7;
                            scannerImpl = scannerImpl2;
                            nativeResults2 = nativeResults;
                            i2 = length;
                            i = i3;
                            Log.e(TAG, "Illegal argument for scan result with bssid: " + StringUtilEx.safeDisplayBssid(bssid), e);
                            i3 = i + 1;
                            scannerImpl2 = scannerImpl;
                            nativeResults = nativeResults2;
                            length = i2;
                        }
                        i3 = i + 1;
                        scannerImpl2 = scannerImpl;
                        nativeResults = nativeResults2;
                        length = i2;
                    }
                } catch (IllegalArgumentException e8) {
                    e = e8;
                    scannerImpl = scannerImpl2;
                    nativeResults2 = nativeResults;
                    i2 = length;
                    i = i3;
                    Log.e(TAG, "Illegal argument for scan result with bssid: " + StringUtilEx.safeDisplayBssid(bssid), e);
                    i3 = i + 1;
                    scannerImpl2 = scannerImpl;
                    nativeResults = nativeResults2;
                    length = i2;
                }
            } catch (IllegalArgumentException e9) {
                e = e9;
                scannerImpl = scannerImpl2;
                nativeResults2 = nativeResults;
                i2 = length;
                i = i3;
                bssid = null;
                Log.e(TAG, "Illegal argument for scan result with bssid: " + StringUtilEx.safeDisplayBssid(bssid), e);
                i3 = i + 1;
                scannerImpl2 = scannerImpl;
                nativeResults = nativeResults2;
                length = i2;
            }
        }
        synchronized (this.mOldSsidList) {
            result = ScanResultUtilEx.getScanResultLogs(this.mOldSsidList, results);
        }
        if (result.length() > 0) {
            Log.i(TAG, "get results:" + result);
        }
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "get " + results.size() + " scan results from wificond");
        }
        return results;
    }

    private static int getScanType(int scanType) {
        if (scanType == 0) {
            return 0;
        }
        if (scanType == 1) {
            return 1;
        }
        if (scanType == 2) {
            return 2;
        }
        throw new IllegalArgumentException("Invalid scan type " + scanType);
    }

    public boolean scan(String ifaceName, int scanType, Set<Integer> freqs, List<String> hiddenNetworkSSIDs) {
        IWifiScannerImpl scannerImpl = getScannerImpl(ifaceName);
        if (scannerImpl == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return false;
        }
        SingleScanSettings settings = new SingleScanSettings();
        try {
            settings.scanType = getScanType(scanType);
            settings.channelSettings = new ArrayList<>();
            settings.hiddenNetworks = new ArrayList<>();
            if (freqs != null) {
                for (Integer freq : freqs) {
                    ChannelSettings channel = new ChannelSettings();
                    channel.frequency = freq.intValue();
                    settings.channelSettings.add(channel);
                }
            }
            if (hiddenNetworkSSIDs != null) {
                for (String ssid : hiddenNetworkSSIDs) {
                    HiddenNetwork network = new HiddenNetwork();
                    try {
                        network.ssid = NativeUtil.byteArrayFromArrayList(NativeUtil.decodeSsid(ssid));
                        if (!settings.hiddenNetworks.contains(network)) {
                            settings.hiddenNetworks.add(network);
                        }
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Illegal argument " + StringUtilEx.safeDisplaySsid(ssid), e);
                    }
                }
            }
            try {
                return scannerImpl.scan(settings);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to request scan due to remote exception");
                return false;
            }
        } catch (IllegalArgumentException e3) {
            Log.e(TAG, "Invalid scan type ", e3);
            return false;
        }
    }

    public boolean startPnoScan(String ifaceName, WifiNative.PnoSettings pnoSettings) {
        IWifiScannerImpl scannerImpl = getScannerImpl(ifaceName);
        if (scannerImpl == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return false;
        }
        PnoSettings settings = new PnoSettings();
        settings.pnoNetworks = new ArrayList<>();
        settings.intervalMs = pnoSettings.periodInMs;
        settings.min2gRssi = pnoSettings.min24GHzRssi;
        settings.min5gRssi = pnoSettings.min5GHzRssi;
        if (pnoSettings.networkList != null) {
            WifiNative.PnoNetwork[] pnoNetworkArr = pnoSettings.networkList;
            for (WifiNative.PnoNetwork network : pnoNetworkArr) {
                PnoNetwork condNetwork = new PnoNetwork();
                boolean z = true;
                if ((network.flags & 1) == 0) {
                    z = false;
                }
                condNetwork.isHidden = z;
                try {
                    condNetwork.ssid = NativeUtil.byteArrayFromArrayList(NativeUtil.decodeSsid(network.ssid));
                    condNetwork.frequencies = network.frequencies;
                    settings.pnoNetworks.add(condNetwork);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Illegal argument " + StringUtilEx.safeDisplaySsid(network.ssid), e);
                }
            }
        }
        try {
            boolean success = scannerImpl.startPnoScan(settings);
            this.mWifiInjector.getWifiMetrics().incrementPnoScanStartAttempCount();
            if (!success) {
                this.mWifiInjector.getWifiMetrics().incrementPnoScanFailedCount();
            }
            return success;
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to start pno scan due to remote exception");
            return false;
        }
    }

    public boolean stopPnoScan(String ifaceName) {
        IWifiScannerImpl scannerImpl = getScannerImpl(ifaceName);
        if (scannerImpl == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return false;
        }
        try {
            return scannerImpl.stopPnoScan();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to stop pno scan due to remote exception");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class HwVendorEventHandler extends IHwVendorEvent.Stub {
        private HwVendorEventHandler() {
        }

        @Override // android.net.wifi.IHwVendorEvent
        public void OnMssSyncReport(NativeMssResult mssStru) {
            ClientModeImpl clientModeImpl = WificondControl.this.mWifiInjector.getClientModeImpl();
            if (clientModeImpl != null) {
                clientModeImpl.onMssSyncResultEvent(mssStru);
            }
        }

        @Override // android.net.wifi.IHwVendorEvent
        public void OnTasRssiReport(int index, int rssi, int[] rsv) {
            WificondControl.this.mWifiInjector.reportHwWiTasAntRssi(index, rssi);
        }
    }

    public void abortScan(String ifaceName) {
        IWifiScannerImpl scannerImpl = getScannerImpl(ifaceName);
        if (scannerImpl == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return;
        }
        try {
            scannerImpl.abortScan();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to request abortScan due to remote exception");
        }
    }

    public int[] getChannelsForBand(int band) {
        IWificond iWificond = this.mWificond;
        if (iWificond == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return null;
        } else if (band == 1) {
            return iWificond.getAvailable2gChannels();
        } else {
            if (band == 2) {
                return iWificond.getAvailable5gNonDFSChannels();
            }
            if (band == 4) {
                try {
                    return iWificond.getAvailableDFSChannels();
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to request getChannelsForBand due to remote exception");
                    return null;
                }
            } else {
                throw new IllegalArgumentException("unsupported band " + band);
            }
        }
    }

    private IApInterface getApInterface(String ifaceName) {
        return this.mApInterfaces.get(ifaceName);
    }

    public boolean registerApListener(String ifaceName, WifiNative.SoftApListener listener) {
        IApInterface iface = getApInterface(ifaceName);
        if (iface == null) {
            Log.e(TAG, "No valid ap interface handler");
            return false;
        }
        try {
            IApInterfaceEventCallback callback = new ApInterfaceEventCallback(listener);
            this.mApInterfaceListeners.put(ifaceName, callback);
            if (iface.registerCallback(callback)) {
                return true;
            }
            Log.e(TAG, "Failed to register ap callback.");
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Exception in registering AP callback: " + e);
            return false;
        }
    }

    public void sendMgmtFrame(String ifaceName, byte[] frame, WifiNative.SendMgmtFrameCallback callback, int mcs) {
        if (callback == null) {
            Log.e(TAG, "callback cannot be null!");
        } else if (frame == null) {
            Log.e(TAG, "frame cannot be null!");
            callback.onFailure(1);
        } else if (getClientInterface(ifaceName) == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            callback.onFailure(1);
        }
    }

    private void clearState() {
        synchronized (this.mClientInterfaces) {
            this.mClientInterfaces.clear();
            this.mWificondScanners.clear();
        }
        this.mPnoScanEventHandlers.clear();
        this.mScanEventHandlers.clear();
        this.mApInterfaces.clear();
        this.mApInterfaceListeners.clear();
    }

    public boolean isEnhancedOpenSupported() {
        if (this.mIsEnhancedOpenSupportedInitialized) {
            return this.mIsEnhancedOpenSupported;
        }
        boolean z = false;
        if (this.mWifiNative == null) {
            this.mWifiNative = this.mWifiInjector.getWifiNative();
            if (this.mWifiNative == null) {
                return false;
            }
        }
        String iface = this.mWifiNative.getClientInterfaceName();
        if (iface == null) {
            return false;
        }
        this.mIsEnhancedOpenSupportedInitialized = true;
        if ((this.mWifiNative.getSupportedFeatureSet(iface) & 536870912) != 0) {
            z = true;
        }
        this.mIsEnhancedOpenSupported = z;
        return this.mIsEnhancedOpenSupported;
    }
}
