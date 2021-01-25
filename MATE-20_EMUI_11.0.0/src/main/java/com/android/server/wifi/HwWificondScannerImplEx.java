package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.ArrayMap;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.ScanResultUtilEx;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HwWificondScannerImplEx implements IHwWificondScannerImplEx {
    private static String TAG = "WificondScannerImpl";
    private static HwWificondScannerImplEx mHwWificondScannerImplEx = null;
    private boolean bFirstScanResults = true;
    private ArrayMap<String, StringBuffer> filterResults = null;
    private StringBuffer filteredResultsString = new StringBuffer();
    private boolean isNeedClearResult = false;
    private Context mContext;

    private HwWificondScannerImplEx(Context context) {
        this.mContext = context;
    }

    public static HwWificondScannerImplEx createHwWificondScannerImplEx(Context context) {
        HwHiLog.d(TAG, false, "createHwWificondScannerImplEx is called!", new Object[0]);
        if (mHwWificondScannerImplEx == null) {
            mHwWificondScannerImplEx = new HwWificondScannerImplEx(context);
        }
        return mHwWificondScannerImplEx;
    }

    public void pollLatestScanData(ScanResult result, long nScanStartTime, boolean bContainsChannel) {
        boolean isFrequencyFiltered;
        boolean isFrequencyFiltered2;
        boolean z;
        List<ScanResult> singleScanResultsForApp = new ArrayList<>();
        if (this.filterResults == null) {
            this.filterResults = getSavedNetworkScanResults();
        }
        if (this.isNeedClearResult) {
            this.filteredResultsString.setLength(0);
            this.isNeedClearResult = false;
        }
        if (result.timestamp / 1000 <= nScanStartTime) {
            if (bContainsChannel) {
                singleScanResultsForApp.add(result);
            }
            isFrequencyFiltered = false;
            isFrequencyFiltered2 = true;
        } else if (bContainsChannel) {
            isFrequencyFiltered = false;
            isFrequencyFiltered2 = false;
        } else {
            isFrequencyFiltered = true;
            isFrequencyFiltered2 = false;
        }
        if (isFrequencyFiltered2 || isFrequencyFiltered) {
            if (this.bFirstScanResults) {
                StringBuffer stringBuffer = this.filteredResultsString;
                stringBuffer.append(nScanStartTime);
                stringBuffer.append("/");
            }
            if (isFrequencyFiltered) {
                StringBuffer stringBuffer2 = this.filteredResultsString;
                stringBuffer2.append(StringUtilEx.safeDisplaySsid(result.SSID));
                stringBuffer2.append("|");
                stringBuffer2.append(result.frequency);
                stringBuffer2.append("|");
                stringBuffer2.append(ScanResultUtilEx.getConfusedBssid(result.BSSID));
                stringBuffer2.append("|");
            }
            if (isFrequencyFiltered2) {
                StringBuffer stringBuffer3 = new StringBuffer("\"");
                stringBuffer3.append(result.SSID);
                stringBuffer3.append("\"");
                String filterSsid = stringBuffer3.toString();
                if (this.filterResults.containsKey(filterSsid)) {
                    StringBuffer filterSsidValue = this.filterResults.get(filterSsid);
                    if (filterSsidValue == null) {
                        filterSsidValue = new StringBuffer();
                    }
                    try {
                        ArrayMap<String, StringBuffer> arrayMap = this.filterResults;
                        filterSsidValue.append(result.BSSID.substring(result.BSSID.length() - 5));
                        filterSsidValue.append("|");
                        filterSsidValue.append(result.timestamp / 1000);
                        filterSsidValue.append("|");
                        arrayMap.put(filterSsid, filterSsidValue);
                        z = false;
                    } catch (StringIndexOutOfBoundsException e) {
                        z = false;
                        HwHiLog.d(TAG, false, "substring: StringIndexOutOfBoundsException", new Object[0]);
                    }
                } else {
                    z = false;
                }
            } else {
                z = false;
            }
            this.bFirstScanResults = z;
        }
    }

    public StringBuffer getResultsString() {
        ArrayMap<String, StringBuffer> arrayMap = this.filterResults;
        if (arrayMap == null) {
            return new StringBuffer();
        }
        for (Map.Entry<String, StringBuffer> entry : arrayMap.entrySet()) {
            StringBuffer arrayMap2 = entry.getValue();
            if (arrayMap2 != null) {
                StringBuffer stringBuffer = this.filteredResultsString;
                stringBuffer.append(StringUtilEx.safeDisplaySsid(entry.getKey()));
                stringBuffer.append("|");
                stringBuffer.append(arrayMap2);
            }
        }
        this.filterResults = null;
        this.bFirstScanResults = true;
        this.isNeedClearResult = true;
        return this.filteredResultsString;
    }

    private ArrayMap<String, StringBuffer> getSavedNetworkScanResults() {
        long nowMs = System.currentTimeMillis();
        ArrayMap<String, StringBuffer> filterResults2 = new ArrayMap<>();
        WifiManager wifiMgr = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiMgr == null) {
            HwHiLog.d(TAG, false, "WifiManager is null,error!", new Object[0]);
            return filterResults2;
        }
        List<WifiConfiguration> savedNetworks = wifiMgr.getConfiguredNetworks();
        int savedNetworksListSize = savedNetworks.size();
        for (int i = 0; i < savedNetworksListSize; i++) {
            WifiConfiguration network = savedNetworks.get(i);
            long diffMs = nowMs - (0 == network.lastHasInternetTimestamp ? network.lastConnected : network.lastHasInternetTimestamp);
            if (0 < diffMs && diffMs < 604800000) {
                filterResults2.put(network.SSID, null);
            }
        }
        return filterResults2;
    }
}
