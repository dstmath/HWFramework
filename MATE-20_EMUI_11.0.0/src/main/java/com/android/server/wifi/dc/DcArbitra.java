package com.android.server.wifi.dc;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.MSS.HwMSSUtils;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DcArbitra {
    private static final int SCANRESULS_NUM_MAX = 10;
    private static final String TAG = "DcArbitra";
    private static DcArbitra sDcArbitra = null;
    private Context mContext;
    private List<DcConfiguration> mDcConfigList = new ArrayList();
    private final Object mLock = new Object();
    private String mPreferIface = DcUtils.INTERFACE_5G_GAME;
    private List<List<ScanResult>> mScanResults = new ArrayList();
    private DcConfiguration mSelectedDcConfig = null;
    private WifiManager mWifiManager;

    private DcArbitra(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
    }

    public static DcArbitra createDcArbitra(Context context) {
        if (sDcArbitra == null) {
            sDcArbitra = new DcArbitra(context);
        }
        return sDcArbitra;
    }

    public static DcArbitra getInstance() {
        return sDcArbitra;
    }

    public void updateScanResults() {
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        if (scanResults == null || scanResults.size() == 0) {
            HwHiLog.d(TAG, false, "getSavedDcNetworks, WiFi scan results are invalid, getScanResults is null", new Object[0]);
            return;
        }
        synchronized (this.mLock) {
            if (this.mScanResults.size() >= 10) {
                this.mScanResults.remove(0);
            }
            this.mScanResults.add(scanResults);
        }
    }

    private static boolean isEncryptionWep(String encryption) {
        return encryption.contains("WEP");
    }

    private static boolean isEncryptionPsk(String encryption) {
        return encryption.contains("PSK");
    }

    private static boolean isEncryptionEap(String encryption) {
        return encryption.contains("EAP");
    }

    private static boolean isOpenNetwork(String encryption) {
        if (!TextUtils.isEmpty(encryption) && !isEncryptionWep(encryption) && !isEncryptionPsk(encryption) && !isEncryptionEap(encryption)) {
            return true;
        }
        return false;
    }

    private static boolean isSameNotOpenEncryptType(String encryption1, String encryption2) {
        if (TextUtils.isEmpty(encryption1) || TextUtils.isEmpty(encryption2)) {
            return false;
        }
        if (isEncryptionWep(encryption1) && isEncryptionWep(encryption2)) {
            return true;
        }
        if (isEncryptionPsk(encryption1) && isEncryptionPsk(encryption2)) {
            return true;
        }
        if (!isEncryptionEap(encryption1) || !isEncryptionEap(encryption2)) {
            return false;
        }
        return true;
    }

    private void updateDcConfig() {
        List<DcConfiguration> list = this.mDcConfigList;
        if (!(list == null || list.size() == 0)) {
            List<ScanResult> scanResults = this.mWifiManager.getScanResults();
            List<DcConfiguration> currentDcConfigList = this.mDcConfigList;
            synchronized (this.mLock) {
                HwHiLog.d(TAG, false, "scanResultsListSize = %{public}d", new Object[]{Integer.valueOf(this.mScanResults.size())});
                if (this.mScanResults.size() == 0) {
                    if (scanResults != null) {
                        if (scanResults.size() != 0) {
                            this.mScanResults.add(scanResults);
                        }
                    }
                    HwHiLog.d(TAG, false, "getSavedDcNetworks, WiFi scan results are invalid, getScanResults is null ", new Object[0]);
                    return;
                }
                for (DcConfiguration dcConfig : currentDcConfigList) {
                    if (dcConfig.getBssid() != null) {
                        int i = this.mScanResults.size() - 1;
                        while (true) {
                            if (i < 0) {
                                break;
                            } else if (isFrequencyAndRssiSet(dcConfig, this.mScanResults.get(i))) {
                                break;
                            } else {
                                i--;
                            }
                        }
                    }
                }
                updatePreSharedKeyforDcConfigs();
            }
        }
    }

    private void updatePreSharedKeyforDcConfigs() {
        List<WifiConfiguration> configs = this.mWifiManager.getPrivilegedConfiguredNetworks();
        if (configs == null || configs.size() == 0) {
            HwHiLog.d(TAG, false, "getSavedDcNetworks, WiFi configured networks are invalid", new Object[0]);
            return;
        }
        for (DcConfiguration dcConfig : this.mDcConfigList) {
            if (dcConfig.getSsid() != null && dcConfig.getConfigKey() != null) {
                String dcSsid = "\"" + dcConfig.getSsid() + "\"";
                String dcConfigKey = dcConfig.getConfigKey();
                Iterator<WifiConfiguration> it = configs.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    WifiConfiguration config = it.next();
                    if (config.SSID != null && config.SSID.equals(dcSsid) && isSameNotOpenEncryptType(dcConfigKey, config.configKey())) {
                        dcConfig.setPreSharedKey(config.preSharedKey.substring(1, config.preSharedKey.length() - 1));
                        dcConfig.setIsSavedNetworkFlag(true);
                        break;
                    }
                    if (config.SSID != null && config.SSID.equals(dcSsid) && isOpenNetwork(dcConfigKey) && isOpenNetwork(config.configKey())) {
                        dcConfig.setIsSavedNetworkFlag(true);
                        break;
                    }
                }
            }
        }
    }

    public boolean isHilinkGateway() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            HwHiLog.e(TAG, false, "mWifiManager is null", new Object[0]);
            return false;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getBSSID() == null) {
            HwHiLog.e(TAG, false, "wifiInfo or bssid is null", new Object[0]);
            return false;
        }
        String bssid = wifiInfo.getBSSID();
        synchronized (this.mLock) {
            for (int i = this.mScanResults.size() - 1; i >= 0; i--) {
                for (ScanResult scanResult : this.mScanResults.get(i)) {
                    if (scanResult.BSSID.equalsIgnoreCase(bssid)) {
                        if (scanResult.isHiLinkNetwork) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    public int getFrequencyForBssid(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            return 0;
        }
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        synchronized (this.mLock) {
            int scanResultsListSize = this.mScanResults.size();
            HwHiLog.i(TAG, false, "scanResultsListSize = %{public}d", new Object[]{Integer.valueOf(scanResultsListSize)});
            if (scanResultsListSize == 0) {
                if (scanResults != null) {
                    if (scanResults.size() != 0) {
                        this.mScanResults.add(scanResults);
                        scanResultsListSize = this.mScanResults.size();
                    }
                }
                HwHiLog.i(TAG, false, "getFrequencyForBssid, WiFi scan results are invalid, getScanResults is null", new Object[0]);
                return 0;
            }
            for (int i = scanResultsListSize - 1; i >= 0; i--) {
                for (ScanResult scanResult : this.mScanResults.get(i)) {
                    if (bssid.equalsIgnoreCase(scanResult.BSSID) && (ScanResult.is24GHz(scanResult.frequency) || ScanResult.is5GHz(scanResult.frequency))) {
                        return scanResult.frequency;
                    }
                }
            }
            return 0;
        }
    }

    private boolean isFrequencyAndRssiSet(DcConfiguration dcConfig, List<ScanResult> scanResults) {
        for (ScanResult scanResult : scanResults) {
            if (dcConfig.getBssid().equalsIgnoreCase(scanResult.BSSID) && (ScanResult.is24GHz(scanResult.frequency) || ScanResult.is5GHz(scanResult.frequency))) {
                if (dcConfig.getFrequency() <= 0) {
                    dcConfig.setFrequency(scanResult.frequency);
                }
                dcConfig.setRssi(scanResult.level);
                return true;
            }
        }
        return false;
    }

    public boolean isValidDcConfigSaved() {
        WifiManager wifiManager;
        List<DcConfiguration> list = this.mDcConfigList;
        if (list == null || list.size() == 0 || (wifiManager = this.mWifiManager) == null) {
            HwHiLog.e(TAG, false, "no valid DcNetwork is saved", new Object[0]);
            return false;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return false;
        }
        for (DcConfiguration dcConfig : this.mDcConfigList) {
            if (dcConfig.isSavedNetwork() && ScanResult.is24GHz(wifiInfo.getFrequency()) && dcConfig.getInterface() != null && (DcUtils.INTERFACE_5G.equals(dcConfig.getInterface()) || DcUtils.INTERFACE_5G_GAME.equals(dcConfig.getInterface()))) {
                return true;
            }
            if (dcConfig.isSavedNetwork() && ScanResult.is5GHz(wifiInfo.getFrequency()) && dcConfig.getInterface() != null && DcUtils.INTERFACE_2G.equals(dcConfig.getInterface())) {
                return true;
            }
            HwHiLog.d(TAG, false, "skip invalid DcNetwork", new Object[0]);
        }
        return false;
    }

    public DcConfiguration selectDcNetwork() {
        List<DcConfiguration> list = this.mDcConfigList;
        if (list == null || list.size() == 0) {
            HwHiLog.i(TAG, false, "no DcNetwork to select", new Object[0]);
            return null;
        }
        updateDcConfig();
        DcConfiguration preferDcConfig = null;
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        HwHiLog.d(TAG, false, "connected network: is24GHz:%{public}s", new Object[]{Boolean.valueOf(ScanResult.is24GHz(wifiInfo.getFrequency()))});
        if (!ScanResult.is24GHz(wifiInfo.getFrequency()) || !HwMSSUtils.is1105() || wifiInfo.getSupportedWifiCategory() < 2) {
            int nextPreferRssi = -127;
            for (DcConfiguration dcConfig : this.mDcConfigList) {
                HwHiLog.d(TAG, false, "IsSavedNetwork:%{public}s is24GHz:%{public}s", new Object[]{Boolean.valueOf(dcConfig.isSavedNetwork()), Boolean.valueOf(ScanResult.is24GHz(dcConfig.getFrequency()))});
                if (dcConfig.getBssid() != null && !dcConfig.getBssid().equalsIgnoreCase(wifiInfo.getBSSID())) {
                    if (!dcConfig.isSavedNetwork() || ((!ScanResult.is24GHz(dcConfig.getFrequency()) && !ScanResult.is5GHz(dcConfig.getFrequency())) || ((ScanResult.is24GHz(wifiInfo.getFrequency()) && ScanResult.is24GHz(dcConfig.getFrequency())) || (ScanResult.is5GHz(wifiInfo.getFrequency()) && ScanResult.is5GHz(dcConfig.getFrequency()))))) {
                        HwHiLog.i(TAG, false, "skip ssid that is not saved or in same band", new Object[0]);
                    } else if (dcConfig.getInterface() != null && dcConfig.getInterface().equals(this.mPreferIface)) {
                        return dcConfig;
                    } else {
                        if (dcConfig.getRssi() > nextPreferRssi) {
                            nextPreferRssi = dcConfig.getRssi();
                            preferDcConfig = dcConfig;
                        }
                    }
                }
            }
            return preferDcConfig;
        }
        HwHiLog.i(TAG, false, "skip ssid that is support wifi6 but hi110x chip", new Object[0]);
        return null;
    }

    public DcConfiguration selectDcNetworkFromPayload(String dcConfigPayload) {
        this.mSelectedDcConfig = null;
        if (TextUtils.isEmpty(dcConfigPayload)) {
            return null;
        }
        parseDcConfig(dcConfigPayload);
        this.mSelectedDcConfig = selectDcNetwork();
        if (this.mSelectedDcConfig == null) {
            HwHiLog.i(TAG, false, "no network to start DC", new Object[0]);
        }
        return this.mSelectedDcConfig;
    }

    public DcConfiguration getSelectedDcConfig() {
        return this.mSelectedDcConfig;
    }

    public List<DcConfiguration> getDcConfigList() {
        return this.mDcConfigList;
    }

    private void parseDcConfig(String dcConfigPayload) {
        List<DcConfiguration> list = this.mDcConfigList;
        if (list == null) {
            this.mDcConfigList = new ArrayList();
        } else {
            list.clear();
        }
        if (!TextUtils.isEmpty(dcConfigPayload)) {
            try {
                JSONArray dcConfigJsonArray = new JSONObject(dcConfigPayload).getJSONArray("deviceinfo");
                int jsonArrayLength = dcConfigJsonArray.length();
                for (int i = 0; i < jsonArrayLength; i++) {
                    DcConfiguration dcConfig = new DcConfiguration();
                    JSONObject jsonDcConfig = dcConfigJsonArray.getJSONObject(i);
                    dcConfig.setInterface(jsonDcConfig.getString("interface"));
                    dcConfig.setSsid(jsonDcConfig.getString("ssid"));
                    dcConfig.setAuthType(jsonDcConfig.getString("authtype"));
                    dcConfig.setBssid(jsonDcConfig.getString("mac"));
                    if (!dcConfig.isAuthTypeAllowed()) {
                        HwHiLog.i(TAG, false, "AuthType is not allowed, skip", new Object[0]);
                    } else {
                        if (jsonDcConfig.has("channel")) {
                            int channel = jsonDcConfig.getInt("channel");
                            dcConfig.setFrequency(WifiCommonUtils.convertChannelToFrequency(channel));
                            HwHiLog.i(TAG, false, "channel=%{public}d", new Object[]{Integer.valueOf(channel)});
                        }
                        this.mDcConfigList.add(dcConfig);
                    }
                }
            } catch (JSONException e) {
                this.mDcConfigList.clear();
                HwHiLog.e(TAG, false, "JSONException when parseDCConfig", new Object[0]);
            }
        }
    }
}
