package com.android.server.wifi;

import android.net.wifi.AnqpInformationElement;
import android.net.wifi.ScanResult;
import android.net.wifi.ScanResult.InformationElement;
import android.net.wifi.WifiSsid;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.ANQPParser;
import com.android.server.wifi.hotspot2.anqp.Constants.ANQPElementType;
import com.android.server.wifi.hotspot2.anqp.HSFriendlyNameElement;
import com.android.server.wifi.hotspot2.anqp.I18Name;
import com.android.server.wifi.hotspot2.anqp.RawByteElement;
import com.android.server.wifi.hotspot2.anqp.VenueNameElement;
import java.util.List;
import java.util.Map;

public class ScanDetail {
    private volatile NetworkDetail mNetworkDetail;
    private final ScanResult mScanResult;
    private long mSeen;

    public ScanDetail(NetworkDetail networkDetail, WifiSsid wifiSsid, String bssid, String caps, int level, int frequency, long tsf, InformationElement[] informationElements, List<String> anqpLines) {
        this.mSeen = 0;
        this.mNetworkDetail = networkDetail;
        this.mScanResult = new ScanResult(wifiSsid, bssid, networkDetail.getHESSID(), networkDetail.getAnqpDomainID(), networkDetail.getOsuProviders(), caps, level, frequency, tsf);
        this.mSeen = System.currentTimeMillis();
        this.mScanResult.seen = this.mSeen;
        this.mScanResult.channelWidth = networkDetail.getChannelWidth();
        this.mScanResult.centerFreq0 = networkDetail.getCenterfreq0();
        this.mScanResult.centerFreq1 = networkDetail.getCenterfreq1();
        this.mScanResult.informationElements = informationElements;
        this.mScanResult.anqpLines = anqpLines;
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
        this.mScanResult.seen = this.mSeen;
        this.mScanResult.channelWidth = 0;
        this.mScanResult.centerFreq0 = 0;
        this.mScanResult.centerFreq1 = 0;
        this.mScanResult.flags = 0;
    }

    public ScanDetail(ScanResult scanResult, NetworkDetail networkDetail) {
        this.mSeen = 0;
        this.mScanResult = scanResult;
        this.mNetworkDetail = networkDetail;
        this.mSeen = this.mScanResult.seen == 0 ? System.currentTimeMillis() : this.mScanResult.seen;
    }

    public void propagateANQPInfo(Map<ANQPElementType, ANQPElement> anqpElements) {
        if (!anqpElements.isEmpty()) {
            if (this.mNetworkDetail != null) {
                this.mNetworkDetail = this.mNetworkDetail.complete(anqpElements);
            }
            HSFriendlyNameElement fne = (HSFriendlyNameElement) anqpElements.get(ANQPElementType.HSFriendlyName);
            if (fne == null || (fne.getNames().isEmpty() ^ 1) == 0) {
                VenueNameElement vne = (VenueNameElement) anqpElements.get(ANQPElementType.ANQPVenueName);
                if (!(vne == null || (vne.getNames().isEmpty() ^ 1) == 0)) {
                    this.mScanResult.venueName = ((I18Name) vne.getNames().get(0)).getText();
                }
            } else {
                this.mScanResult.venueName = ((I18Name) fne.getNames().get(0)).getText();
            }
            RawByteElement osuProviders = (RawByteElement) anqpElements.get(ANQPElementType.HSOSUProviders);
            if (osuProviders != null) {
                this.mScanResult.anqpElements = new AnqpInformationElement[1];
                this.mScanResult.anqpElements[0] = new AnqpInformationElement(ANQPParser.VENDOR_SPECIFIC_HS20_OI, 8, osuProviders.getPayload());
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
        return String.format("'%s':%012x", new Object[]{this.mScanResult.BSSID, Long.valueOf(Utils.parseMac(this.mScanResult.BSSID))});
    }

    public long getSeen() {
        return this.mSeen;
    }

    public long setSeen() {
        this.mSeen = System.currentTimeMillis();
        this.mScanResult.seen = this.mSeen;
        return this.mSeen;
    }

    public String toString() {
        try {
            return String.format("'%s'/%012x", new Object[]{this.mScanResult.SSID, Long.valueOf(Utils.parseMac(this.mScanResult.BSSID))});
        } catch (IllegalArgumentException e) {
            return String.format("'%s'/----", new Object[]{this.mScanResult.BSSID});
        }
    }
}
