package com.android.server.wifi;

import android.net.wifi.IApInterface;
import android.net.wifi.IApLinkedEvent;
import android.net.wifi.IApLinkedEvent.Stub;
import android.net.wifi.IClientInterface;
import android.net.wifi.IPnoScanEvent;
import android.net.wifi.IScanEvent;
import android.net.wifi.IWifiScannerImpl;
import android.net.wifi.IWificond;
import android.net.wifi.ScanResult.InformationElement;
import android.net.wifi.WifiSsid;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.wifi.WifiNative.PnoNetwork;
import com.android.server.wifi.WifiNative.PnoSettings;
import com.android.server.wifi.WifiNative.SignalPollResult;
import com.android.server.wifi.WifiNative.TxPacketCounters;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.scanner.ScanResultRecords;
import com.android.server.wifi.util.InformationElementUtil;
import com.android.server.wifi.util.InformationElementUtil.Capabilities;
import com.android.server.wifi.util.InformationElementUtil.Dot11vNetwork;
import com.android.server.wifi.util.InformationElementUtil.HiLinkNetwork;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.wificond.ChannelSettings;
import com.android.server.wifi.wificond.HiddenNetwork;
import com.android.server.wifi.wificond.NativeScanResult;
import com.android.server.wifi.wificond.SingleScanSettings;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WificondControl {
    private static final String TAG = "WificondControl";
    private IApInterface mApInterface;
    private String mApInterfaceName;
    private IApLinkedEvent mApLinkedEventHandler;
    private IClientInterface mClientInterface;
    private String mClientInterfaceName;
    private Set<String> mOldSsidList = new HashSet();
    private IPnoScanEvent mPnoScanEventHandler;
    private IScanEvent mScanEventHandler;
    private boolean mVerboseLoggingEnabled = false;
    private WifiInjector mWifiInjector;
    private WifiMonitor mWifiMonitor;
    private IWificond mWificond;
    private IWifiScannerImpl mWificondScanner;

    private class ApLinkedEventHandler extends Stub {
        /* synthetic */ ApLinkedEventHandler(WificondControl this$0, ApLinkedEventHandler -this1) {
            this();
        }

        private ApLinkedEventHandler() {
        }

        public void OnApLinkedStaJoin(String macAddress) {
            Log.d(WificondControl.TAG, "Ap linked STA_JOIN event, mApInterfaceName=" + WificondControl.this.mApInterfaceName);
            WificondControl.this.mWifiMonitor.broadcastApLinkedStaChangedEvent(WificondControl.this.mApInterfaceName, WifiMonitor.STA_JOIN_EVENT, macAddress);
        }

        public void OnApLinkedStaLeave(String macAddress) {
            Log.d(WificondControl.TAG, "Ap linked STA_LEAVE event, mApInterfaceName=" + WificondControl.this.mApInterfaceName);
            WificondControl.this.mWifiMonitor.broadcastApLinkedStaChangedEvent(WificondControl.this.mApInterfaceName, WifiMonitor.STA_LEAVE_EVENT, macAddress);
        }
    }

    private class PnoScanEventHandler extends IPnoScanEvent.Stub {
        /* synthetic */ PnoScanEventHandler(WificondControl this$0, PnoScanEventHandler -this1) {
            this();
        }

        private PnoScanEventHandler() {
        }

        public void OnPnoNetworkFound() {
            Log.d(WificondControl.TAG, "Pno scan result event");
            WificondControl.this.mWifiMonitor.broadcastPnoScanResultEvent(WificondControl.this.mClientInterfaceName);
        }

        public void OnPnoScanFailed() {
            Log.d(WificondControl.TAG, "Pno Scan failed event");
        }
    }

    private class ScanEventHandler extends IScanEvent.Stub {
        /* synthetic */ ScanEventHandler(WificondControl this$0, ScanEventHandler -this1) {
            this();
        }

        private ScanEventHandler() {
        }

        public void OnScanResultReady() {
            Log.d(WificondControl.TAG, "Scan result ready event");
            WificondControl.this.mWifiMonitor.broadcastScanResultEvent(WificondControl.this.mClientInterfaceName);
        }

        public void OnScanFailed() {
            Log.d(WificondControl.TAG, "Scan failed event");
            WificondControl.this.mWifiMonitor.broadcastScanFailedEvent(WificondControl.this.mClientInterfaceName);
        }
    }

    WificondControl(WifiInjector wifiInjector, WifiMonitor wifiMonitor) {
        this.mWifiInjector = wifiInjector;
        this.mWifiMonitor = wifiMonitor;
    }

    public void enableVerboseLogging(boolean enable) {
        this.mVerboseLoggingEnabled = enable;
    }

    public IClientInterface setupDriverForClientMode() {
        Log.d(TAG, "Setting up driver for client mode");
        this.mWificond = this.mWifiInjector.makeWificond();
        if (this.mWificond == null) {
            Log.e(TAG, "Failed to get reference to wificond");
            return null;
        }
        try {
            IClientInterface clientInterface = this.mWificond.createClientInterface();
            if (clientInterface == null) {
                Log.e(TAG, "Could not get IClientInterface instance from wificond");
                return null;
            }
            Binder.allowBlocking(clientInterface.asBinder());
            this.mClientInterface = clientInterface;
            try {
                this.mClientInterfaceName = clientInterface.getInterfaceName();
                this.mWificondScanner = this.mClientInterface.getWifiScannerImpl();
                if (this.mWificondScanner == null) {
                    Log.e(TAG, "Failed to get WificondScannerImpl");
                    return null;
                }
                Binder.allowBlocking(this.mWificondScanner.asBinder());
                this.mScanEventHandler = new ScanEventHandler(this, null);
                this.mWificondScanner.subscribeScanEvents(this.mScanEventHandler);
                this.mPnoScanEventHandler = new PnoScanEventHandler(this, null);
                this.mWificondScanner.subscribePnoScanEvents(this.mPnoScanEventHandler);
                return clientInterface;
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to refresh wificond scanner due to remote exception");
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to get IClientInterface due to remote exception");
            return null;
        }
    }

    public IApInterface setupDriverForSoftApMode() {
        Log.d(TAG, "Setting up driver for soft ap mode");
        this.mWificond = this.mWifiInjector.makeWificond();
        if (this.mWificond == null) {
            Log.e(TAG, "Failed to get reference to wificond");
            return null;
        }
        try {
            IApInterface apInterface = this.mWificond.createApInterface();
            if (apInterface == null) {
                Log.e(TAG, "Could not get IApInterface instance from wificond");
                return null;
            }
            Binder.allowBlocking(apInterface.asBinder());
            this.mApInterface = apInterface;
            try {
                this.mApInterfaceName = apInterface.getInterfaceName();
                this.mApLinkedEventHandler = new ApLinkedEventHandler(this, null);
                this.mApInterface.subscribeStationChangeEvents(this.mApLinkedEventHandler);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to refresh wificond ap handler due to remote exception");
            }
            return apInterface;
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to get IApInterface due to remote exception");
            return null;
        }
    }

    public boolean tearDownInterfaces() {
        Log.d(TAG, "tearing down interfaces in wificond");
        this.mWificond = this.mWifiInjector.makeWificond();
        if (this.mWificond == null) {
            Log.e(TAG, "Failed to get reference to wificond");
            return false;
        }
        try {
            if (this.mWificondScanner != null) {
                this.mWificondScanner.unsubscribeScanEvents();
                this.mWificondScanner.unsubscribePnoScanEvents();
            }
            if (this.mApInterface != null) {
                this.mApInterface.unsubscribeStationChangeEvents();
            }
            this.mWificond.tearDownInterfaces();
            this.mClientInterface = null;
            this.mWificondScanner = null;
            this.mPnoScanEventHandler = null;
            this.mScanEventHandler = null;
            this.mApInterface = null;
            this.mApLinkedEventHandler = null;
            synchronized (this.mOldSsidList) {
                this.mOldSsidList.clear();
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to tear down interfaces due to remote exception");
            return false;
        }
    }

    public boolean disableSupplicant() {
        if (this.mClientInterface == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            return false;
        }
        try {
            return this.mClientInterface.disableSupplicant();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to disable supplicant due to remote exception");
            return false;
        }
    }

    public boolean enableSupplicant() {
        if (this.mClientInterface == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            return false;
        }
        try {
            return this.mClientInterface.enableSupplicant();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to enable supplicant due to remote exception");
            return false;
        }
    }

    public SignalPollResult signalPoll() {
        if (this.mClientInterface == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            return null;
        }
        try {
            int[] resultArray = this.mClientInterface.signalPoll();
            if (resultArray == null || !(resultArray.length == 6 || resultArray.length == 3)) {
                Log.e(TAG, "Invalid signal poll result from wificond");
                return null;
            }
            SignalPollResult pollResult = new SignalPollResult();
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

    public TxPacketCounters getTxPacketCounters() {
        if (this.mClientInterface == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            return null;
        }
        try {
            int[] resultArray = this.mClientInterface.getPacketCounters();
            if (resultArray == null || resultArray.length != 2) {
                Log.e(TAG, "Invalid signal poll result from wificond");
                return null;
            }
            TxPacketCounters counters = new TxPacketCounters();
            counters.txSucceeded = resultArray[0];
            counters.txFailed = resultArray[1];
            return counters;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to do signal polling due to remote exception");
            return null;
        }
    }

    public ArrayList<ScanDetail> getScanResults() {
        ArrayList<ScanDetail> results = new ArrayList();
        if (this.mWificondScanner == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return results;
        }
        try {
            NativeScanResult[] nativeResults = this.mWificondScanner.getScanResults();
            ScanResultRecords.getDefault().clearOrdSsidRecords();
            int i = 0;
            int length = nativeResults.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    break;
                }
                NativeScanResult result = nativeResults[i2];
                WifiSsid wifiSsid = WifiSsid.createFromByteArray(result.ssid);
                NetworkDetail networkDetail;
                try {
                    String bssid = NativeUtil.macAddressFromByteArray(result.bssid);
                    ScanResultRecords.getDefault().recordOriSsid(bssid, wifiSsid.toString(), result.ssid);
                    wifiSsid.oriSsid = NativeUtil.hexStringFromByteArray(result.ssid);
                    if (bssid == null) {
                        Log.e(TAG, "Illegal null bssid");
                        networkDetail = null;
                    } else {
                        InformationElement[] ies = InformationElementUtil.parseInformationElements(result.infoElement);
                        Capabilities capabilities = new Capabilities();
                        capabilities.from(ies, result.capability);
                        String flags = capabilities.generateCapabilitiesString();
                        networkDetail = new NetworkDetail(bssid, ies, null, result.frequency);
                        if (!wifiSsid.toString().equals(networkDetail.getTrimmedSSID())) {
                            Log.d(TAG, String.format("Inconsistent SSID on BSSID '%s': '%s' vs '%s' ", new Object[]{bssid, wifiSsid.toString(), networkDetail.getTrimmedSSID()}));
                        }
                        ScanDetail scanDetail = new ScanDetail(networkDetail, wifiSsid, bssid, flags, result.signalMbm / 100, result.frequency, result.tsf, ies, null);
                        HiLinkNetwork hiLinkNetwork = new HiLinkNetwork();
                        hiLinkNetwork.from(ies);
                        if (hiLinkNetwork.isHiLinkNetwork || (ScanResultRecords.getDefault().isHiLink(bssid) ^ 1) == 0) {
                            scanDetail.getScanResult().isHiLinkNetwork = true;
                            ScanResultRecords.getDefault().recordHiLink(bssid);
                        } else {
                            scanDetail.getScanResult().isHiLinkNetwork = false;
                        }
                        Dot11vNetwork dot11vNetwork = new Dot11vNetwork();
                        dot11vNetwork.from(ies);
                        scanDetail.getScanResult().dot11vNetwork = dot11vNetwork.dot11vNetwork;
                        results.add(scanDetail);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Illegal argument " + result.bssid, e);
                    networkDetail = null;
                }
                i = i2 + 1;
            }
            String result2 = "";
            synchronized (this.mOldSsidList) {
                result2 = ScanResultUtil.getScanResultLogs(this.mOldSsidList, results);
            }
            if (result2.length() > 0) {
                Log.d(TAG, "get results:" + result2);
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to create ScanDetail ArrayList");
        }
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "get " + results.size() + " scan results from wificond");
        }
        return results;
    }

    public boolean scan(Set<Integer> freqs, List<String> hiddenNetworkSSIDs) {
        if (this.mWificondScanner == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return false;
        }
        SingleScanSettings settings = new SingleScanSettings();
        settings.channelSettings = new ArrayList();
        settings.hiddenNetworks = new ArrayList();
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
            return this.mWificondScanner.scan(settings);
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to request scan due to remote exception");
            return false;
        }
    }

    public boolean startPnoScan(PnoSettings pnoSettings) {
        if (this.mWificondScanner == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return false;
        }
        com.android.server.wifi.wificond.PnoSettings settings = new com.android.server.wifi.wificond.PnoSettings();
        settings.pnoNetworks = new ArrayList();
        settings.intervalMs = pnoSettings.periodInMs;
        settings.min2gRssi = pnoSettings.min24GHzRssi;
        settings.min5gRssi = pnoSettings.min5GHzRssi;
        if (pnoSettings.networkList != null) {
            for (PnoNetwork network : pnoSettings.networkList) {
                boolean z;
                com.android.server.wifi.wificond.PnoNetwork condNetwork = new com.android.server.wifi.wificond.PnoNetwork();
                if ((network.flags & 1) != 0) {
                    z = true;
                } else {
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
            return this.mWificondScanner.startPnoScan(settings);
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to stop pno scan due to remote exception");
            return false;
        }
    }

    public boolean stopPnoScan() {
        if (this.mWificondScanner == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return false;
        }
        try {
            return this.mWificondScanner.stopPnoScan();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to stop pno scan due to remote exception");
            return false;
        }
    }

    public void abortScan() {
        if (this.mWificondScanner == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return;
        }
        try {
            this.mWificondScanner.abortScan();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to request abortScan due to remote exception");
        }
    }
}
