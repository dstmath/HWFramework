package com.android.server.wifi;

import android.net.wifi.AnqpInformationElement;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiSsid;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.ANQPParser;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.hotspot2.anqp.HSFriendlyNameElement;
import com.android.server.wifi.hotspot2.anqp.RawByteElement;
import com.android.server.wifi.hotspot2.anqp.VenueNameElement;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.util.List;
import java.util.Map;

public class ScanDetail {
    private volatile NetworkDetail mNetworkDetail;
    private final ScanResult mScanResult;
    private long mSeen;

    public ScanDetail(NetworkDetail networkDetail, WifiSsid wifiSsid, String bssid, String caps, int level, int frequency, long tsf, ScanResult.InformationElement[] informationElements, List<String> anqpLines) {
        this.mSeen = 0;
        this.mNetworkDetail = networkDetail;
        this.mScanResult = new ScanResult(wifiSsid, bssid, networkDetail.getHESSID(), networkDetail.getAnqpDomainID(), networkDetail.getOsuProviders(), caps, level, frequency, tsf);
        this.mSeen = System.currentTimeMillis();
        ScanResult scanResult = this.mScanResult;
        scanResult.seen = this.mSeen;
        scanResult.channelWidth = networkDetail.getChannelWidth();
        this.mScanResult.centerFreq0 = networkDetail.getCenterfreq0();
        this.mScanResult.centerFreq1 = networkDetail.getCenterfreq1();
        ScanResult scanResult2 = this.mScanResult;
        scanResult2.informationElements = informationElements;
        scanResult2.anqpLines = anqpLines;
        if (networkDetail.is80211McResponderSupport()) {
            this.mScanResult.setFlag(2);
        }
        if (networkDetail.isInterworking()) {
            this.mScanResult.setFlag(1);
        }
    }

    public ScanDetail(WifiSsid wifiSsid, String bssid, String caps, int level, int frequency, long tsf, long seen) {
        this.mSeen = 0;
        this.mNetworkDetail = null;
        this.mScanResult = new ScanResult(wifiSsid, bssid, 0, -1, null, caps, level, frequency, tsf);
        this.mSeen = seen;
        ScanResult scanResult = this.mScanResult;
        scanResult.seen = this.mSeen;
        scanResult.channelWidth = 0;
        scanResult.centerFreq0 = 0;
        scanResult.centerFreq1 = 0;
        scanResult.flags = 0;
    }

    public ScanDetail(ScanResult scanResult, NetworkDetail networkDetail) {
        this.mSeen = 0;
        this.mScanResult = scanResult;
        this.mNetworkDetail = networkDetail;
        this.mSeen = this.mScanResult.seen == 0 ? System.currentTimeMillis() : this.mScanResult.seen;
    }

    public void propagateANQPInfo(Map<Constants.ANQPElementType, ANQPElement> anqpElements) {
        if (!anqpElements.isEmpty()) {
            if (this.mNetworkDetail != null) {
                this.mNetworkDetail = this.mNetworkDetail.complete(anqpElements);
            }
            HSFriendlyNameElement fne = (HSFriendlyNameElement) anqpElements.get(Constants.ANQPElementType.HSFriendlyName);
            if (fne == null || fne.getNames().isEmpty()) {
                VenueNameElement vne = (VenueNameElement) anqpElements.get(Constants.ANQPElementType.ANQPVenueName);
                if (vne != null && !vne.getNames().isEmpty()) {
                    this.mScanResult.venueName = vne.getNames().get(0).getText();
                }
            } else {
                this.mScanResult.venueName = fne.getNames().get(0).getText();
            }
            RawByteElement osuProviders = (RawByteElement) anqpElements.get(Constants.ANQPElementType.HSOSUProviders);
            if (osuProviders != null) {
                ScanResult scanResult = this.mScanResult;
                scanResult.anqpElements = new AnqpInformationElement[1];
                scanResult.anqpElements[0] = new AnqpInformationElement((int) ANQPParser.VENDOR_SPECIFIC_HS20_OI, 8, osuProviders.getPayload());
            }
        }
    }

    public ScanResult getScanResult() {
        return this.mScanResult;
    }

    public NetworkDetail getNetworkDetail() {
        return this.mNetworkDetail;
    }

    public String getSSID() {
        return this.mNetworkDetail == null ? this.mScanResult.SSID : this.mNetworkDetail.getSSID();
    }

    public String getBSSIDString() {
        return this.mNetworkDetail == null ? this.mScanResult.BSSID : this.mNetworkDetail.getBSSIDString();
    }

    public String toKeyString() {
        NetworkDetail networkDetail = this.mNetworkDetail;
        if (networkDetail != null) {
            return networkDetail.toKeyString();
        }
        return String.format("'%s':%012x", this.mScanResult.BSSID, Long.valueOf(Utils.parseMac(this.mScanResult.BSSID)));
    }

    public long getSeen() {
        return this.mSeen;
    }

    public long setSeen() {
        this.mSeen = System.currentTimeMillis();
        ScanResult scanResult = this.mScanResult;
        long j = this.mSeen;
        scanResult.seen = j;
        return j;
    }

    public String toString() {
        try {
            return String.format("'%s'/%012x", StringUtilEx.safeDisplaySsid(this.mScanResult.SSID), Long.valueOf(Utils.parseMac(this.mScanResult.BSSID)));
        } catch (IllegalArgumentException e) {
            return String.format("'%s'/----", StringUtilEx.safeDisplayBssid(this.mScanResult.BSSID));
        }
    }
}
