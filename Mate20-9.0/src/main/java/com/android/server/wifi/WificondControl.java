package com.android.server.wifi;

import android.net.MacAddress;
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
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.scanner.ScanResultRecords;
import com.android.server.wifi.util.InformationElementUtil;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.StringUtil;
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
import java.util.Map;
import java.util.Set;

public class WificondControl implements IBinder.DeathRecipient {
    public static final int SCAN_TYPE_PNO_SCAN = 1;
    public static final int SCAN_TYPE_SINGLE_SCAN = 0;
    private static final String TAG = "WificondControl";
    private HashMap<String, IApInterfaceEventCallback> mApInterfaceListeners = new HashMap<>();
    private HashMap<String, IApInterface> mApInterfaces = new HashMap<>();
    private final CarrierNetworkConfig mCarrierNetworkConfig;
    private HashMap<String, IClientInterface> mClientInterfaces = new HashMap<>();
    private WifiNative.WificondDeathEventHandler mDeathEventHandler;
    private IHwVendorEvent mHwVendorEventHandler;
    private Set<String> mOldSsidList = new HashSet();
    private HashMap<String, IPnoScanEvent> mPnoScanEventHandlers = new HashMap<>();
    private HashMap<String, IScanEvent> mScanEventHandlers = new HashMap<>();
    private boolean mVerboseLoggingEnabled = false;
    /* access modifiers changed from: private */
    public WifiInjector mWifiInjector;
    /* access modifiers changed from: private */
    public WifiMonitor mWifiMonitor;
    private IWificond mWificond;
    private HashMap<String, IWifiScannerImpl> mWificondScanners = new HashMap<>();

    private class ApInterfaceEventCallback extends IApInterfaceEventCallback.Stub {
        private WifiNative.SoftApListener mSoftApListener;

        ApInterfaceEventCallback(WifiNative.SoftApListener listener) {
            this.mSoftApListener = listener;
        }

        public void onNumAssociatedStationsChanged(int numStations) {
            this.mSoftApListener.onNumAssociatedStationsChanged(numStations);
        }

        public void onSoftApChannelSwitched(int frequency, int bandwidth) {
            this.mSoftApListener.onSoftApChannelSwitched(frequency, bandwidth);
        }

        public void OnApLinkedStaJoin(String macAddress) {
            this.mSoftApListener.OnApLinkedStaJoin(macAddress);
        }

        public void OnApLinkedStaLeave(String macAddress) {
            this.mSoftApListener.OnApLinkedStaLeave(macAddress);
        }
    }

    private class HwVendorEventHandler extends IHwVendorEvent.Stub {
        private HwVendorEventHandler() {
        }

        public void OnMssSyncReport(NativeMssResult mssStru) {
            WifiStateMachine machine = WificondControl.this.mWifiInjector.getWifiStateMachine();
            if (machine != null) {
                machine.onMssSyncResultEvent(mssStru);
            }
        }

        public void OnTasRssiReport(int index, int rssi, int[] rsv) {
            WificondControl.this.mWifiInjector.reportHwWiTasAntRssi(index, rssi);
        }
    }

    private class PnoScanEventHandler extends IPnoScanEvent.Stub {
        private String mIfaceName;

        PnoScanEventHandler(String ifaceName) {
            this.mIfaceName = ifaceName;
        }

        public void OnPnoNetworkFound() {
            Log.d(WificondControl.TAG, "Pno scan result event");
            WificondControl.this.mWifiMonitor.broadcastPnoScanResultEvent(this.mIfaceName);
            WificondControl.this.mWifiInjector.getWifiMetrics().incrementPnoFoundNetworkEventCount();
        }

        public void OnPnoScanFailed() {
            Log.d(WificondControl.TAG, "Pno Scan failed event");
            WificondControl.this.mWifiInjector.getWifiMetrics().incrementPnoScanFailedCount();
        }

        public void OnPnoScanOverOffloadStarted() {
            Log.d(WificondControl.TAG, "Pno scan over offload started");
            WificondControl.this.mWifiInjector.getWifiMetrics().incrementPnoScanStartedOverOffloadCount();
        }

        public void OnPnoScanOverOffloadFailed(int reason) {
            Log.d(WificondControl.TAG, "Pno scan over offload failed");
            WificondControl.this.mWifiInjector.getWifiMetrics().incrementPnoScanFailedOverOffloadCount();
        }
    }

    private class ScanEventHandler extends IScanEvent.Stub {
        private String mIfaceName;

        ScanEventHandler(String ifaceName) {
            this.mIfaceName = ifaceName;
        }

        public void OnScanResultReady() {
            Log.d(WificondControl.TAG, "Scan result ready event");
            WificondControl.this.mWifiMonitor.broadcastScanResultEvent(this.mIfaceName);
        }

        public void OnScanFailed() {
            Log.d(WificondControl.TAG, "Scan failed event");
            WificondControl.this.mWifiMonitor.broadcastScanFailedEvent(this.mIfaceName);
        }
    }

    WificondControl(WifiInjector wifiInjector, WifiMonitor wifiMonitor, CarrierNetworkConfig carrierNetworkConfig) {
        this.mWifiInjector = wifiInjector;
        this.mWifiMonitor = wifiMonitor;
        this.mCarrierNetworkConfig = carrierNetworkConfig;
    }

    public void binderDied() {
        Log.e(TAG, "Wificond died!");
        clearState();
        synchronized (this) {
            this.mWificond = null;
        }
        if (this.mDeathEventHandler != null) {
            this.mDeathEventHandler.onDeath();
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
        Log.d(TAG, "Setting up interface for client mode");
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
                clientInterface.subscribeVendorEvents(this.mHwVendorEventHandler);
                return clientInterface;
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to refresh wificond scanner due to remote exception");
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to get IClientInterface due to remote exception");
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003a, code lost:
        if (r2 != false) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003c, code lost:
        android.util.Log.e(TAG, "Failed to teardown client interface");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0043, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0044, code lost:
        r5.mClientInterfaces.remove(r6);
        r5.mWificondScanners.remove(r6);
        r5.mScanEventHandlers.remove(r6);
        r5.mPnoScanEventHandlers.remove(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0059, code lost:
        return true;
     */
    public boolean tearDownClientInterface(String ifaceName) {
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
            clientInterface.unsubscribeVendorEvents();
            try {
                synchronized (this) {
                    if (this.mWificond == null) {
                        Log.e(TAG, "mWificond is null, Wificond may be dead");
                        return false;
                    }
                    boolean success = this.mWificond.tearDownClientInterface(ifaceName);
                }
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
        Log.d(TAG, "Setting up interface for soft ap mode");
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

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0026, code lost:
        if (r0 != false) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        android.util.Log.e(TAG, "Failed to teardown AP interface");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002f, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0030, code lost:
        r4.mApInterfaces.remove(r5);
        r4.mApInterfaceListeners.remove(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003b, code lost:
        return true;
     */
    public boolean tearDownSoftApInterface(String ifaceName) {
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
                boolean success = this.mWificond.tearDownApInterface(ifaceName);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to teardown AP interface due to remote exception");
            return false;
        }
    }

    public boolean tearDownInterfaces() {
        Log.d(TAG, "tearing down interfaces in wificond");
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
                    entry2.getValue().unsubscribeVendorEvents();
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

    public boolean disableSupplicant() {
        if (!retrieveWificondAndRegisterForDeath()) {
            return false;
        }
        try {
            synchronized (this) {
                if (this.mWificond == null) {
                    Log.e(TAG, "mWificond is null, Wificond may be dead");
                    return false;
                }
                boolean disableSupplicant = this.mWificond.disableSupplicant();
                return disableSupplicant;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to disable supplicant due to remote exception");
            return false;
        }
    }

    public boolean enableSupplicant() {
        if (!retrieveWificondAndRegisterForDeath()) {
            return false;
        }
        try {
            return this.mWificond.enableSupplicant();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to enable supplicant due to remote exception");
            return false;
        }
    }

    public WifiNative.SignalPollResult signalPoll(String ifaceName) {
        IClientInterface iface = getClientInterface(ifaceName);
        if (iface == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            return null;
        }
        try {
            int[] resultArray = iface.signalPoll();
            if (resultArray == null || !(resultArray.length == 6 || resultArray.length == 3)) {
                Log.e(TAG, "Invalid signal poll result from wificond");
                return null;
            }
            WifiNative.SignalPollResult pollResult = new WifiNative.SignalPollResult();
            pollResult.currentRssi = resultArray[0];
            pollResult.txBitrate = resultArray[1];
            pollResult.associationFrequency = resultArray[2];
            if (resultArray.length == 6) {
                pollResult.currentNoise = resultArray[3];
                pollResult.currentSnr = resultArray[4];
                pollResult.currentChload = resultArray[5];
                Log.e(TAG, "Noise: " + pollResult.currentNoise + ", Snr: " + pollResult.currentSnr + ", Chload: " + pollResult.currentChload);
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

    /* JADX WARNING: Removed duplicated region for block: B:98:0x026c  */
    public ArrayList<ScanDetail> getScanResults(String ifaceName, int scanType) {
        NativeScanResult[] nativeResults;
        String result;
        IWifiScannerImpl scannerImpl;
        WifiSsid wifiSsid;
        IWifiScannerImpl scannerImpl2;
        InformationElementUtil.Capabilities capabilities;
        String flags;
        boolean z;
        WifiSsid bssid;
        ArrayList<ScanDetail> results = new ArrayList<>();
        IWifiScannerImpl scannerImpl3 = getScannerImpl(ifaceName);
        if (scannerImpl3 == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return results;
        }
        if (scanType == 0) {
            try {
                nativeResults = scannerImpl3.getScanResults();
            } catch (RemoteException e) {
                IWifiScannerImpl iWifiScannerImpl = scannerImpl3;
            }
        } else {
            try {
                nativeResults = scannerImpl3.getPnoScanResults();
            } catch (RemoteException e2) {
                IWifiScannerImpl iWifiScannerImpl2 = scannerImpl3;
                Log.e(TAG, "Failed to create ScanDetail ArrayList");
                if (this.mVerboseLoggingEnabled) {
                }
                return results;
            }
        }
        NativeScanResult[] nativeResults2 = nativeResults;
        ScanResultRecords.getDefault().clearOrdSsidRecords();
        int length = nativeResults2.length;
        int i = 0;
        while (i < length) {
            NativeScanResult result2 = nativeResults2[i];
            WifiSsid wifiSsid2 = WifiSsid.createFromByteArray(result2.ssid);
            try {
                WifiSsid bssid2 = NativeUtil.macAddressFromByteArray(result2.bssid);
                try {
                    ScanResultRecords.getDefault().recordOriSsid(bssid2, wifiSsid2.toString(), result2.ssid);
                    wifiSsid2.oriSsid = NativeUtil.hexStringFromByteArray(result2.ssid);
                    if (bssid2 == null) {
                        try {
                            Log.e(TAG, "Illegal null bssid");
                            scannerImpl = scannerImpl3;
                        } catch (IllegalArgumentException e3) {
                            e = e3;
                            scannerImpl2 = scannerImpl3;
                            WifiSsid wifiSsid3 = wifiSsid2;
                            wifiSsid = bssid2;
                            Log.e(TAG, "Illegal argument for scan result with bssid: " + StringUtil.safeDisplayBssid(wifiSsid), e);
                            i++;
                            scannerImpl3 = scannerImpl;
                        }
                    } else {
                        ScanResult.InformationElement[] ies = InformationElementUtil.parseInformationElements(result2.infoElement);
                        try {
                            capabilities = new InformationElementUtil.Capabilities();
                            capabilities.from(ies, result2.capability);
                            flags = capabilities.generateCapabilitiesString();
                        } catch (IllegalArgumentException e4) {
                            e = e4;
                            scannerImpl2 = scannerImpl3;
                            WifiSsid wifiSsid4 = wifiSsid2;
                            wifiSsid = bssid2;
                            ScanResult.InformationElement[] informationElementArr = ies;
                            Log.e(TAG, "Illegal argument for scan result with bssid: " + StringUtil.safeDisplayBssid(wifiSsid), e);
                            i++;
                            scannerImpl3 = scannerImpl;
                        }
                        try {
                            ScanResultRecords.getDefault().recordPmf(bssid2, capabilities.pmfCapabilities);
                            InformationElementUtil.Capabilities capabilities2 = capabilities;
                            NetworkDetail networkDetail = new NetworkDetail(bssid2, ies, null, result2.frequency);
                            if (!wifiSsid2.toString().equals(networkDetail.getTrimmedSSID())) {
                                z = true;
                                Log.d(TAG, String.format("Inconsistent SSID on BSSID '%s': '%s' vs '%s' ", new Object[]{StringUtil.safeDisplayBssid(bssid2), wifiSsid2.toString(), networkDetail.getTrimmedSSID()}));
                            } else {
                                z = true;
                            }
                            WifiSsid bssid3 = bssid2;
                            r10 = r10;
                            NetworkDetail networkDetail2 = networkDetail;
                            NetworkDetail networkDetail3 = networkDetail;
                            boolean z2 = z;
                            scannerImpl = scannerImpl3;
                            WifiSsid wifiSsid5 = wifiSsid2;
                            try {
                                ScanDetail scanDetail = new ScanDetail(networkDetail2, wifiSsid2, bssid3, flags, result2.signalMbm / 100, result2.frequency, result2.tsf, ies, null);
                                ScanDetail scanDetail2 = scanDetail;
                                ScanResult scanResult = scanDetail2.getScanResult();
                                if (ScanResultUtil.isScanResultForEapNetwork(scanDetail2.getScanResult()) && this.mCarrierNetworkConfig.isCarrierNetwork(wifiSsid5.toString())) {
                                    scanResult.isCarrierAp = z2;
                                    scanResult.carrierApEapType = this.mCarrierNetworkConfig.getNetworkEapType(wifiSsid5.toString());
                                    scanResult.carrierName = this.mCarrierNetworkConfig.getCarrierName(wifiSsid5.toString());
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
                                        wifiSsid5 = wifiSsid5;
                                    }
                                }
                                InformationElementUtil.HiLinkNetwork hiLinkNetwork = new InformationElementUtil.HiLinkNetwork();
                                hiLinkNetwork.from(ies);
                                scanDetail2.getScanResult().hilinkTag = hiLinkNetwork.parseHiLogoTag(ies);
                                Log.d(TAG, "hilinkTag:" + scanDetail2.getScanResult().hilinkTag);
                                if (!hiLinkNetwork.isHiLinkNetwork) {
                                    bssid = bssid3;
                                    if (!ScanResultRecords.getDefault().isHiLink(bssid)) {
                                        scanDetail2.getScanResult().isHiLinkNetwork = false;
                                        InformationElementUtil.Dot11vNetwork dot11vNetwork = new InformationElementUtil.Dot11vNetwork();
                                        dot11vNetwork.from(ies);
                                        scanDetail2.getScanResult().dot11vNetwork = dot11vNetwork.dot11vNetwork;
                                        results.add(scanDetail2);
                                    }
                                } else {
                                    bssid = bssid3;
                                }
                                scanDetail2.getScanResult().isHiLinkNetwork = true;
                                ScanResultRecords.getDefault().recordHiLink(bssid);
                                InformationElementUtil.Dot11vNetwork dot11vNetwork2 = new InformationElementUtil.Dot11vNetwork();
                                dot11vNetwork2.from(ies);
                                scanDetail2.getScanResult().dot11vNetwork = dot11vNetwork2.dot11vNetwork;
                                results.add(scanDetail2);
                            } catch (RemoteException e5) {
                            }
                        } catch (IllegalArgumentException e6) {
                            e = e6;
                            scannerImpl2 = scannerImpl3;
                            WifiSsid wifiSsid6 = wifiSsid2;
                            wifiSsid = bssid2;
                            ScanResult.InformationElement[] informationElementArr2 = ies;
                            String str = flags;
                            Log.e(TAG, "Illegal argument for scan result with bssid: " + StringUtil.safeDisplayBssid(wifiSsid), e);
                            i++;
                            scannerImpl3 = scannerImpl;
                        }
                    }
                } catch (IllegalArgumentException e7) {
                    e = e7;
                    scannerImpl2 = scannerImpl3;
                    WifiSsid wifiSsid7 = wifiSsid2;
                    wifiSsid = bssid2;
                    Log.e(TAG, "Illegal argument for scan result with bssid: " + StringUtil.safeDisplayBssid(wifiSsid), e);
                    i++;
                    scannerImpl3 = scannerImpl;
                }
            } catch (IllegalArgumentException e8) {
                e = e8;
                scannerImpl2 = scannerImpl3;
                WifiSsid wifiSsid8 = wifiSsid2;
                wifiSsid = null;
                Log.e(TAG, "Illegal argument for scan result with bssid: " + StringUtil.safeDisplayBssid(wifiSsid), e);
                i++;
                scannerImpl3 = scannerImpl;
            }
            i++;
            scannerImpl3 = scannerImpl;
        }
        Object obj = "";
        synchronized (this.mOldSsidList) {
            result = ScanResultUtil.getScanResultLogs(this.mOldSsidList, results);
        }
        if (result.length() > 0) {
            Log.d(TAG, "get results:" + result);
        }
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "get " + results.size() + " scan results from wificond");
        }
        return results;
    }

    private static int getScanType(int scanType) {
        switch (scanType) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                throw new IllegalArgumentException("Invalid scan type " + scanType);
        }
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
                        settings.hiddenNetworks.add(network);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Illegal argument " + ssid, e);
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
            for (WifiNative.PnoNetwork network : pnoSettings.networkList) {
                PnoNetwork condNetwork = new PnoNetwork();
                boolean z = true;
                if ((network.flags & 1) == 0) {
                    z = false;
                }
                condNetwork.isHidden = z;
                try {
                    condNetwork.ssid = NativeUtil.byteArrayFromArrayList(NativeUtil.decodeSsid(network.ssid));
                    settings.pnoNetworks.add(condNetwork);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Illegal argument " + network.ssid, e);
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
        if (this.mWificond == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return null;
        } else if (band == 4) {
            return this.mWificond.getAvailableDFSChannels();
        } else {
            switch (band) {
                case 1:
                    return this.mWificond.getAvailable2gChannels();
                case 2:
                    return this.mWificond.getAvailable5gNonDFSChannels();
                default:
                    try {
                        throw new IllegalArgumentException("unsupported band " + band);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Failed to request getChannelsForBand due to remote exception");
                        return null;
                    }
            }
        }
    }

    private IApInterface getApInterface(String ifaceName) {
        return this.mApInterfaces.get(ifaceName);
    }

    public boolean startHostapd(String ifaceName, WifiNative.SoftApListener listener) {
        IApInterface iface = getApInterface(ifaceName);
        if (iface == null) {
            Log.e(TAG, "No valid ap interface handler");
            return false;
        }
        try {
            IApInterfaceEventCallback callback = new ApInterfaceEventCallback(listener);
            this.mApInterfaceListeners.put(ifaceName, callback);
            if (iface.startHostapd(callback)) {
                return true;
            }
            Log.e(TAG, "Failed to start hostapd.");
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Exception in starting soft AP: " + e);
            return false;
        }
    }

    public boolean stopHostapd(String ifaceName) {
        IApInterface iface = getApInterface(ifaceName);
        if (iface == null) {
            Log.e(TAG, "No valid ap interface handler");
            return false;
        }
        try {
            if (!iface.stopHostapd()) {
                Log.e(TAG, "Failed to stop hostapd.");
                return false;
            }
            this.mApInterfaceListeners.remove(ifaceName);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Exception in stopping soft AP: " + e);
            return false;
        }
    }

    public boolean setMacAddress(String interfaceName, MacAddress mac) {
        IClientInterface mClientInterface = getClientInterface(interfaceName);
        if (mClientInterface == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            return false;
        }
        try {
            mClientInterface.setMacAddress(mac.toByteArray());
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to setMacAddress due to remote exception");
            return false;
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
}
