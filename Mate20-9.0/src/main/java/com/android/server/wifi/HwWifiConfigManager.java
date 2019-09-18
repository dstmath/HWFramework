package com.android.server.wifi;

import android.content.Context;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.StaticIpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class HwWifiConfigManager extends WifiConfigManager {
    private static final String DEFAULT_CERTIFICATE_PATH = (Environment.getDataDirectory().getPath() + "/wapi_certificate");
    private static final int HISI_WAPI = 0;
    public static final String TAG = "HwWifiConfigManager";
    private static WifiConfigManagerUtils wifiConfigManagerUtils = EasyInvokeFactory.getInvokeUtils(WifiConfigManagerUtils.class);
    private static HwWifiConfigStoreUtils wifiConfigStoreUtils = EasyInvokeFactory.getInvokeUtils(HwWifiConfigStoreUtils.class);
    private Context mContext;
    private WifiNative mWifiNative = WifiInjector.getInstance().getWifiNative();

    HwWifiConfigManager(Context context, Clock clock, UserManager userManager, TelephonyManager telephonyManager, WifiKeyStore wifiKeyStore, WifiConfigStore wifiConfigStore, WifiConfigStoreLegacy wifiConfigStoreLegacy, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper wifiPermissionsWrapper, NetworkListStoreData networkListStoreData, DeletedEphemeralSsidsStoreData deletedEphemeralSsidsStoreData) {
        super(context, clock, userManager, telephonyManager, wifiKeyStore, wifiConfigStore, wifiConfigStoreLegacy, wifiPermissionsUtil, wifiPermissionsWrapper, networkListStoreData, deletedEphemeralSsidsStoreData);
        this.mContext = context;
    }

    public void setSupportWapiType() {
        Settings.Global.putInt(wifiConfigStoreUtils.getContext(this).getContentResolver(), "wapi_type", 0);
    }

    public void updateInternetInfoByWifiPro(WifiConfiguration config) {
        if (config != null && config.networkId != -1) {
            WifiConfiguration savedConfig = wifiConfigManagerUtils.getInternalConfiguredNetwork(this, config.networkId);
            if (savedConfig != null) {
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
    }

    public void updateNetworkConnFailedInfo(int netId, int rssi, int reason) {
        WifiConfiguration config = wifiConfigManagerUtils.getInternalConfiguredNetwork(this, netId);
        if (config != null) {
            config.lastConnFailedType = reason;
            config.lastConnFailedTimestamp = System.currentTimeMillis();
            if (!config.getNetworkSelectionStatus().isNetworkEnabled() && rssi != -200) {
                config.rssiStatusDisabled = rssi;
            }
        }
    }

    public void resetNetworkConnFailedInfo(int netId) {
        WifiConfiguration config = wifiConfigManagerUtils.getInternalConfiguredNetwork(this, netId);
        if (config != null) {
            config.lastConnFailedType = 0;
            config.lastConnFailedTimestamp = 0;
            config.rssiStatusDisabled = WifiHandover.INVALID_RSSI;
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

    public void updateWifiConfigByWifiPro(WifiConfiguration config, boolean uiOnly) {
        if (config != null && config.networkId != -1) {
            WifiConfiguration savedConfig = wifiConfigManagerUtils.getInternalConfiguredNetwork(this, config.networkId);
            if (savedConfig != null) {
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
            }
        }
    }

    public boolean tryUseStaticIpForFastConnecting(int netId) {
        boolean usingStaticIp = false;
        WifiConfiguration currConfig = getConfiguredNetwork(netId);
        if (!(currConfig == null || currConfig.lastDhcpResults == null)) {
            String[] dhcpResults = currConfig.lastDhcpResults.split("\\|");
            StaticIpConfiguration staticIpConfig = new StaticIpConfiguration();
            InetAddress ipAddr = null;
            int prefLength = -1;
            int flag = -1;
            int scope = -1;
            int i = 0;
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
                    Log.e(TAG, "tryUseStaticIpForFastConnecting, UnknownHostException msg = " + e.getMessage());
                } catch (IllegalArgumentException e2) {
                    Log.e(TAG, "tryUseStaticIpForFastConnecting, IllegalArgumentException msg = " + e2.getMessage());
                }
            }
            if (!(ipAddr == null || prefLength == -1 || staticIpConfig.gateway == null || staticIpConfig.dnsServers.size() <= 0)) {
                staticIpConfig.ipAddress = new LinkAddress(ipAddr, prefLength, flag, scope);
                currConfig.setStaticIpConfiguration(staticIpConfig);
                currConfig.setIpAssignment(IpConfiguration.IpAssignment.STATIC);
                usingStaticIp = true;
            }
            Log.d(TAG, "tryUseStaticIpForFastConnecting, staticIpConfig = " + staticIpConfig);
        }
        return usingStaticIp;
    }
}
