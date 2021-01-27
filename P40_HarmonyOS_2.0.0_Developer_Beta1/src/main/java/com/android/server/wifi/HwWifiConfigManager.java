package com.android.server.wifi;

import android.content.Context;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.StaticIpConfiguration;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.HwWifiConfigurationXmlUtil;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HwWifiConfigManager extends WifiConfigManager {
    private static final String DEFAULT_CERTIFICATE_PATH = (Environment.getDataDirectory().getPath() + "/wapi_certificate");
    private static final String ENCRYPTION_EAP = "EAP";
    private static final String ENCRYPTION_FT_PSK = "FT/PSK";
    private static final String ENCRYPTION_PSK = "PSK";
    private static final String ENCRYPTION_PSK_SHA256 = "PSK-SHA256";
    private static final String ENCRYPTION_SAE = "SAE";
    private static final String ENCRYPTION_WEP = "WEP";
    private static final int FIND_CANDIDATES_FROM_HISTORY_SCAN_RESULT = 1;
    private static final int FIND_CANDIDATES_FROM_LAST_SCAN_RESULT = 0;
    private static final int FUZZY_BSSID_INDEX_BEGIN = 2;
    private static final int FUZZY_BSSID_INDEX_END = 15;
    private static final int HISI_WAPI = 0;
    private static final String NETWORK_DEFAULT_HISTORY = "-1/-1/-1/-1/-1/-1/-1/-1/-1/-1";
    private static final int RANDOM_MAC_BSSID_FLAG = 1;
    private static final int RANDOM_MAC_MATCH_ENTRY_CNT_THR = 30;
    private static final int RANDOM_MAC_RETREAT_FLAG = 2;
    private static final int RANDOM_MAC_SWTICH_DEFAULT_VAL = 3;
    private static final int RUN_WITH_SCISSORS_TIMEOUT_MILLIS = 4000;
    public static final String TAG = "HwWifiConfigManager";
    private static final int VALID_BSSID_LENGTH = 17;
    private static WifiConfigManagerUtils wifiConfigManagerUtils = EasyInvokeFactory.getInvokeUtils(WifiConfigManagerUtils.class);
    private static HwWifiConfigStoreUtils wifiConfigStoreUtils = EasyInvokeFactory.getInvokeUtils(HwWifiConfigStoreUtils.class);
    private Context mContext = null;
    private int mRandomizedMacAddressMatchEntryCnt = SystemProperties.getInt("ro.config.hw_wifi_rmac_number", 30);
    private int mRandomizedMacAddressSwitch = SystemProperties.getInt("ro.config.hw_wifi_rmac_switch", 3);
    private ScanRequestProxy mScanRequestProxy = null;
    private WifiInjector mWifiInjector = null;
    private WifiNative mWifiNative = null;
    private Wpa3SelfCureImpl mWpa3SelfCureImpl = null;

    HwWifiConfigManager(Context context, Clock clock, UserManager userManager, TelephonyManager telephonyManager, WifiKeyStore wifiKeyStore, WifiConfigStore wifiConfigStore, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper wifiPermissionsWrapper, WifiInjector wifiInjector, NetworkListSharedStoreData networkListSharedStoreData, NetworkListUserStoreData networkListUserStoreData, DeletedEphemeralSsidsStoreData deletedEphemeralSsidsStoreData, RandomizedMacStoreData randomizedMacStoreData, FrameworkFacade frameworkFacade, Looper looper) {
        super(context, clock, userManager, telephonyManager, wifiKeyStore, wifiConfigStore, wifiPermissionsUtil, wifiPermissionsWrapper, wifiInjector, networkListSharedStoreData, networkListUserStoreData, deletedEphemeralSsidsStoreData, randomizedMacStoreData, frameworkFacade, looper);
        this.mContext = context;
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
        this.mWifiInjector = wifiInjector;
        this.mWpa3SelfCureImpl = Wpa3SelfCureImpl.createSelfCureImpl(context);
    }

    public void setSupportWapiType() {
        Settings.Global.putInt(wifiConfigStoreUtils.getContext(this).getContentResolver(), "wapi_type", 0);
    }

    public void updateInternetInfoByWifiPro(WifiConfiguration config) {
        WifiConfiguration savedConfig;
        if (config != null && config.networkId != -1 && (savedConfig = wifiConfigManagerUtils.getInternalConfiguredNetwork(this, config.networkId)) != null) {
            savedConfig.noInternetAccess = config.noInternetAccess;
            savedConfig.internetHistory = config.internetHistory;
            savedConfig.portalNetwork = config.portalNetwork;
            savedConfig.validatedInternetAccess = config.validatedInternetAccess;
            savedConfig.portalCheckStatus = config.portalCheckStatus;
            savedConfig.internetRecoveryStatus = config.internetRecoveryStatus;
            savedConfig.lastDhcpResults = config.lastDhcpResults;
            savedConfig.lastHasInternetTimestamp = config.lastHasInternetTimestamp;
        }
    }

    public void updateNetworkConnFailedInfo(int netId, int rssi, int reason) {
        WifiConfiguration config = wifiConfigManagerUtils.getInternalConfiguredNetwork(this, netId);
        if (config != null) {
            config.lastConnFailedType = reason;
            config.lastConnFailedTimestamp = System.currentTimeMillis();
            if (!config.getNetworkSelectionStatus().isNetworkEnabled() && rssi != -200) {
                HwHiLog.d(TAG, false, "updateNetworkConnFailedInfo, rssi = %{public}d", new Object[]{Integer.valueOf(rssi)});
                config.rssiStatusDisabled = rssi;
            }
        }
    }

    public void resetNetworkConnFailedInfo(int netId) {
        WifiConfiguration config = wifiConfigManagerUtils.getInternalConfiguredNetwork(this, netId);
        if (config != null) {
            config.lastConnFailedType = 0;
            config.lastConnFailedTimestamp = 0;
            config.rssiStatusDisabled = -200;
        }
    }

    public void updateRssiDiscNonLocally(int netid, boolean disc, int rssi, long ts) {
        WifiConfiguration config = wifiConfigManagerUtils.getInternalConfiguredNetwork(this, netid);
        if (config != null) {
            config.rssiDiscNonLocally = rssi;
            config.timestampDiscNonLocally = ts;
            config.consecutiveGoodRssiCounter = 0;
        }
    }

    public void mergeHwParamsWithInternalWifiConfiguration(WifiConfiguration internalConfig, WifiConfiguration externalConfig) {
        boolean z = false;
        if (internalConfig == null || externalConfig == null) {
            HwHiLog.e(TAG, false, "invalid parameter", new Object[0]);
            return;
        }
        internalConfig.priority = externalConfig.priority;
        internalConfig.getNetworkSelectionStatus().setNetworkSelectionStatus(externalConfig.getNetworkSelectionStatus().getNetworkSelectionStatus());
        internalConfig.oriSsid = externalConfig.oriSsid;
        internalConfig.isTempCreated = externalConfig.isTempCreated;
        internalConfig.cloudSecurityCheck = externalConfig.cloudSecurityCheck;
        internalConfig.connectToCellularAndWLAN = externalConfig.connectToCellularAndWLAN;
        internalConfig.wifiApType = externalConfig.wifiApType;
        internalConfig.mRandomizedMacSuccessEver = externalConfig.mRandomizedMacSuccessEver;
        internalConfig.mIsCreatedByClone = externalConfig.mIsCreatedByClone;
        if (NETWORK_DEFAULT_HISTORY.equals(externalConfig.internetHistory)) {
            HwHiLog.i(TAG, false, "external config internet record is initial, not allowed to replace", new Object[0]);
            return;
        }
        internalConfig.internetHistory = externalConfig.internetHistory;
        internalConfig.portalNetwork = externalConfig.portalNetwork;
        internalConfig.numNoInternetAccessReports = externalConfig.numNoInternetAccessReports;
        internalConfig.validatedInternetAccess = externalConfig.validatedInternetAccess;
        internalConfig.noInternetAccess = !externalConfig.portalNetwork && externalConfig.hasNoInternetAccess();
        if (externalConfig.noInternetAccess || externalConfig.portalNetwork) {
            z = true;
        }
        internalConfig.wifiProNoInternetAccess = z;
        HwWifiConfigurationXmlUtil.updateAccessType(internalConfig);
    }

    public void updateWifiConfigByWifiPro(WifiConfiguration config, boolean uiOnly) {
        WifiConfiguration savedConfig;
        if (config != null && config.networkId != -1 && (savedConfig = wifiConfigManagerUtils.getInternalConfiguredNetwork(this, config.networkId)) != null) {
            if (!uiOnly) {
                savedConfig.noInternetAccess = config.noInternetAccess;
                savedConfig.validatedInternetAccess = config.validatedInternetAccess;
                savedConfig.internetHistory = config.internetHistory;
                savedConfig.internetSelfCureHistory = config.internetSelfCureHistory;
                savedConfig.portalNetwork = config.portalNetwork;
                savedConfig.portalCheckStatus = config.portalCheckStatus;
                savedConfig.internetRecoveryStatus = config.internetRecoveryStatus;
                savedConfig.lastHasInternetTimestamp = config.lastHasInternetTimestamp;
                savedConfig.internetRecoveryCheckTimestamp = config.internetRecoveryCheckTimestamp;
                savedConfig.poorRssiDectected = config.poorRssiDectected;
                savedConfig.consecutiveGoodRssiCounter = config.consecutiveGoodRssiCounter;
                savedConfig.portalValidityDuration = config.portalValidityDuration;
                savedConfig.portalAuthTimestamp = config.portalAuthTimestamp;
            }
            savedConfig.wifiProNoInternetAccess = config.wifiProNoInternetAccess;
            savedConfig.wifiProNoInternetReason = config.wifiProNoInternetReason;
            savedConfig.wifiProNoHandoverNetwork = config.wifiProNoHandoverNetwork;
            savedConfig.internetAccessType = config.internetAccessType;
            savedConfig.networkQosLevel = config.networkQosLevel;
            savedConfig.networkQosScore = config.networkQosScore;
            savedConfig.isTempCreated = config.isTempCreated;
            savedConfig.lastTrySwitchWifiTimestamp = config.lastTrySwitchWifiTimestamp;
            savedConfig.isReassocSelfcureWithFactoryMacAddress = config.isReassocSelfcureWithFactoryMacAddress;
        }
    }

    public boolean tryUseStaticIpForFastConnecting(int netId) {
        boolean usingStaticIp = false;
        WifiConfiguration currConfig = getConfiguredNetwork(netId);
        if (!(currConfig == null || currConfig.lastDhcpResults == null)) {
            String[] dhcpResults = currConfig.lastDhcpResults.split("\\|");
            StaticIpConfiguration staticIpConfig = new StaticIpConfiguration();
            int i = 0;
            int scope = -1;
            int flag = -1;
            int prefLength = -1;
            InetAddress ipAddr = null;
            while (true) {
                try {
                    if (i >= dhcpResults.length) {
                        break;
                    }
                    if (i == 0) {
                        int lastCellid = Integer.parseInt(dhcpResults[i]);
                        int currCellid = WifiProCommonUtils.getCurrentCellId();
                        if (currCellid == -1) {
                            break;
                        } else if (currCellid != lastCellid) {
                            break;
                        }
                    } else if (i == 1) {
                        staticIpConfig.domains = dhcpResults[i];
                    } else if (i == 2) {
                        ipAddr = InetAddress.getByName(dhcpResults[i]);
                    } else if (i == 3) {
                        prefLength = Integer.valueOf(dhcpResults[i]).intValue();
                    } else if (i == 4) {
                        flag = Integer.valueOf(dhcpResults[i]).intValue();
                    } else if (i == 5) {
                        scope = Integer.valueOf(dhcpResults[i]).intValue();
                    } else if (i == 6) {
                        staticIpConfig.gateway = InetAddress.getByName(dhcpResults[i]);
                    } else {
                        staticIpConfig.dnsServers.add(InetAddress.getByName(dhcpResults[i]));
                    }
                    i++;
                } catch (UnknownHostException e) {
                    HwHiLog.e(TAG, false, "tryUseStaticIpForFastConnecting, UnknownHostException msg = %{public}s", new Object[]{e.getMessage()});
                } catch (IllegalArgumentException e2) {
                    HwHiLog.e(TAG, false, "tryUseStaticIpForFastConnecting, IllegalArgumentException msg = %{public}s", new Object[]{e2.getMessage()});
                }
            }
            if (!(ipAddr == null || prefLength == -1 || staticIpConfig.gateway == null || staticIpConfig.dnsServers.size() <= 0)) {
                staticIpConfig.ipAddress = new LinkAddress(ipAddr, prefLength, flag, scope);
                currConfig.setStaticIpConfiguration(staticIpConfig);
                currConfig.setIpAssignment(IpConfiguration.IpAssignment.STATIC);
                usingStaticIp = true;
            }
            HwHiLog.d(TAG, false, "tryUseStaticIpForFastConnecting, staticIpConfig = %{private}s", new Object[]{staticIpConfig.toString()});
        }
        return usingStaticIp;
    }

    public boolean isBssidAlgorithmOn() {
        if ((this.mRandomizedMacAddressSwitch & 1) != 0) {
            return true;
        }
        return false;
    }

    public boolean isRetreatAlgorithmOn() {
        if ((this.mRandomizedMacAddressSwitch & 2) != 0) {
            return true;
        }
        return false;
    }

    public void generateRandomizedMacAddressMatchInfoMap() {
        for (Map.Entry<String, String> MappingPlusEntry : this.mRandomizedMacAddressMappingPlus.entrySet()) {
            String MappingPlusValue = MappingPlusEntry.getValue();
            if (this.mRandomizedMacAddressMatchInfoMap.containsKey(MappingPlusValue)) {
                this.mRandomizedMacAddressMatchInfoMap.put(MappingPlusValue, Integer.valueOf(((Integer) this.mRandomizedMacAddressMatchInfoMap.get(MappingPlusValue)).intValue() + 1));
            } else {
                this.mRandomizedMacAddressMatchInfoMap.put(MappingPlusValue, 1);
            }
        }
    }

    public void updateRandomizedMacAddressPlus(WifiConfiguration config, String bssid) {
        if (config == null || bssid == null) {
            Log.e(TAG, "randommac: config or bssid is null");
            return;
        }
        String fuzzyBssid = fuzzyBssid(bssid);
        if (!isPskEncryption(config)) {
            Log.i(TAG, "randommac, current ap is not PSK, do not add entry");
        } else if (!this.mRandomizedMacAddressMappingPlus.containsKey(fuzzyBssid)) {
            String randomizedMacAddress = config.getRandomizedMacAddress().toString();
            if (this.mRandomizedMacAddressMatchInfoMap.containsKey(randomizedMacAddress)) {
                Integer MatchInfoValue = (Integer) this.mRandomizedMacAddressMatchInfoMap.get(randomizedMacAddress);
                if (MatchInfoValue.intValue() >= this.mRandomizedMacAddressMatchEntryCnt) {
                    Log.i(TAG, "randommac, return directly, entry with same mac reach the limit: " + this.mRandomizedMacAddressMatchEntryCnt);
                    return;
                }
                this.mRandomizedMacAddressMatchInfoMap.put(randomizedMacAddress, Integer.valueOf(MatchInfoValue.intValue() + 1));
            } else {
                this.mRandomizedMacAddressMatchInfoMap.put(randomizedMacAddress, 1);
            }
            Log.i(TAG, "randommac, update plus entry,fuzzyBssid: " + StringUtilEx.safeDisplayBssid(fuzzyBssid) + ", randomizedMac:" + StringUtilEx.safeDisplayBssid(randomizedMacAddress));
            this.mRandomizedMacAddressMappingPlus.put(fuzzyBssid, randomizedMacAddress);
        }
    }

    public String searchRandomizedMacAddressPlusEntry(WifiConfiguration config) {
        boolean isFoundCandidates;
        if (!isBssidAlgorithmOn()) {
            return null;
        }
        List<String> candidates = new ArrayList<>();
        updateRandomizedMacAddressPlusCandidates(config, candidates, 0);
        if (candidates.isEmpty()) {
            Log.w(TAG, "randommac: search from last scan result, candidates is empty");
            isFoundCandidates = false;
        } else {
            isFoundCandidates = true;
        }
        if (!isFoundCandidates) {
            updateRandomizedMacAddressPlusCandidates(config, candidates, 1);
            if (candidates.isEmpty()) {
                Log.w(TAG, "randommac: search from history scan result, candidates is empty");
                return null;
            }
        }
        for (String candidate : candidates) {
            String fuzzyBssid = fuzzyBssid(candidate);
            if (this.mRandomizedMacAddressMappingPlus.containsKey(fuzzyBssid)) {
                String randomizedMacAddress = (String) this.mRandomizedMacAddressMappingPlus.get(fuzzyBssid);
                Log.i(TAG, "randommac, match plus entry,fuzzyBssid: " + StringUtilEx.safeDisplayBssid(fuzzyBssid) + ", randomizedMac:" + StringUtilEx.safeDisplayBssid(randomizedMacAddress));
                return randomizedMacAddress;
            }
        }
        return null;
    }

    private boolean isPskEncryption(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(1) || config.allowedKeyManagement.get(6) || config.allowedKeyManagement.get(11) || config.allowedKeyManagement.get(8)) {
            return true;
        }
        return false;
    }

    private String fuzzyBssid(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            Log.e(TAG, "randommac: bssid is empty");
            return null;
        } else if (bssid.length() != 17) {
            Log.e(TAG, "randommac: bssid length is invalid");
            return null;
        } else {
            return "xx" + bssid.substring(2, 15) + "xx";
        }
    }

    private boolean isEncryptionMatched(WifiConfiguration config, ScanResult result) {
        if (config.allowedKeyManagement.get(1) && result.capabilities.contains(ENCRYPTION_PSK)) {
            return true;
        }
        if (config.allowedKeyManagement.get(6) && result.capabilities.contains(ENCRYPTION_FT_PSK)) {
            return true;
        }
        if (config.allowedKeyManagement.get(11) && result.capabilities.contains(ENCRYPTION_PSK_SHA256)) {
            return true;
        }
        if (!config.allowedKeyManagement.get(8) || !result.capabilities.contains(ENCRYPTION_SAE)) {
            return false;
        }
        return true;
    }

    private boolean isScanResultMatched(WifiConfiguration config, ScanResult result) {
        if (config == null || result == null) {
            Log.e(TAG, "randommac: config or result is null");
            return false;
        }
        if (!result.capabilities.contains(ENCRYPTION_WEP) && !result.capabilities.contains(ENCRYPTION_EAP) && config.SSID != null) {
            String str = config.SSID;
            if (str.equals("\"" + result.SSID + "\"") && isEncryptionMatched(config, result)) {
                return true;
            }
        }
        return false;
    }

    private void updateRandomizedMacAddressPlusCandidates(WifiConfiguration config, List<String> candidates, int type) {
        if (config == null || candidates == null) {
            Log.e(TAG, "randommac: config or candidates is null");
            return;
        }
        if (this.mScanRequestProxy == null) {
            this.mScanRequestProxy = this.mWifiInjector.getScanRequestProxy();
        }
        boolean isSuccess = false;
        if (config.BSSID != null) {
            candidates.add(config.BSSID);
            return;
        }
        List<ScanResult> scanResults = new ArrayList<>();
        if (type == 0) {
            isSuccess = this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(scanResults) {
                /* class com.android.server.wifi.$$Lambda$HwWifiConfigManager$WkVhkJ_UO4KWj1osPuY6Q1a79fI */
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwWifiConfigManager.this.lambda$updateRandomizedMacAddressPlusCandidates$0$HwWifiConfigManager(this.f$1);
                }
            }, 4000);
        } else if (type != 1) {
            Log.e(TAG, "randommac: invalid type: " + type);
        } else {
            isSuccess = this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(scanResults) {
                /* class com.android.server.wifi.$$Lambda$HwWifiConfigManager$YUrhLE606AlrNNPCVvRR9IMRgKI */
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwWifiConfigManager.this.lambda$updateRandomizedMacAddressPlusCandidates$1$HwWifiConfigManager(this.f$1);
                }
            }, 4000);
        }
        if (!isSuccess) {
            Log.e(TAG, "randommac: get scan result failed");
        } else if (scanResults.isEmpty()) {
            Log.e(TAG, "randommac: scanResults is empty");
        } else {
            for (ScanResult result : scanResults) {
                if (isScanResultMatched(config, result)) {
                    candidates.add(result.BSSID);
                }
            }
        }
    }

    public /* synthetic */ void lambda$updateRandomizedMacAddressPlusCandidates$0$HwWifiConfigManager(List scanResults) {
        scanResults.addAll(this.mScanRequestProxy.getScanResults());
    }

    public /* synthetic */ void lambda$updateRandomizedMacAddressPlusCandidates$1$HwWifiConfigManager(List scanResults) {
        scanResults.addAll(this.mScanRequestProxy.getHistoryScanResults());
    }

    public void removeWpa3BlackList(String configKey) {
        this.mWpa3SelfCureImpl.removeBlackList(configKey);
    }
}
