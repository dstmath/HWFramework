package com.android.server.wifi;

import android.content.Context;
import android.net.IpConfiguration.IpAssignment;
import android.net.LinkAddress;
import android.net.StaticIpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.security.KeyStore;
import android.util.Log;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class HwWifiConfigManager extends WifiConfigManager {
    private static final String DEFAULT_CERTIFICATE_PATH;
    private static final int HISI_WAPI = 0;
    private static final int INVALID_WAPI = -1;
    private static final int QUALCOMM_WAPI = 1;
    private static final String SUPPLICANT_CONFIG_FILE = "/data/misc/wifi/wpa_supplicant.conf";
    public static final String TAG = "HwWifiConfigManager";
    private static HwWifiConfigStoreUtils wifiConfigStoreUtils;
    private Context mContext;
    private int mWapiType;
    private WifiNative mWifiNative;

    static {
        wifiConfigStoreUtils = (HwWifiConfigStoreUtils) EasyInvokeFactory.getInvokeUtils(HwWifiConfigStoreUtils.class);
        DEFAULT_CERTIFICATE_PATH = Environment.getDataDirectory().getPath() + "/wapi_certificate";
    }

    HwWifiConfigManager(Context context, WifiNative wifiNative, FrameworkFacade frameworkFacade, Clock clock, UserManager userManager, KeyStore keyStore) {
        super(context, wifiNative, frameworkFacade, clock, userManager, keyStore);
        this.mContext = context;
        this.mWifiNative = wifiNative;
    }

    public void setSupportWapiType() {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        BufferedReader bufferedReader = null;
        int wapiType = INVALID_WAPI;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(SUPPLICANT_CONFIG_FILE));
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    String trimmedLine = line.trim();
                    if (trimmedLine.startsWith("wapi_type=")) {
                        String sWapiType = trimmedLine.substring(10);
                        log("getSupportWapiType wapiType = " + sWapiType);
                        if ("hisi".equals(sWapiType)) {
                            wapiType = HISI_WAPI;
                        } else if ("qualcomm".equals(sWapiType)) {
                            wapiType = QUALCOMM_WAPI;
                        }
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e3) {
                    }
                }
                bufferedReader = reader;
            } catch (FileNotFoundException e4) {
                e = e4;
                bufferedReader = reader;
                loge("Could not open /data/misc/wifi/wpa_supplicant.conf, " + e);
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e5) {
                    }
                }
                Global.putInt(wifiConfigStoreUtils.getContext(this).getContentResolver(), "wapi_type", wapiType);
            } catch (IOException e6) {
                e2 = e6;
                bufferedReader = reader;
                try {
                    loge("Could not read /data/misc/wifi/wpa_supplicant.conf, " + e2);
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e7) {
                        }
                    }
                    Global.putInt(wifiConfigStoreUtils.getContext(this).getContentResolver(), "wapi_type", wapiType);
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e8) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedReader = reader;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            loge("Could not open /data/misc/wifi/wpa_supplicant.conf, " + e);
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            Global.putInt(wifiConfigStoreUtils.getContext(this).getContentResolver(), "wapi_type", wapiType);
        } catch (IOException e10) {
            e2 = e10;
            loge("Could not read /data/misc/wifi/wpa_supplicant.conf, " + e2);
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            Global.putInt(wifiConfigStoreUtils.getContext(this).getContentResolver(), "wapi_type", wapiType);
        }
        Global.putInt(wifiConfigStoreUtils.getContext(this).getContentResolver(), "wapi_type", wapiType);
    }

    public void updateWifiConfigByWifiPro(WifiConfiguration config, boolean uiOnly) {
        if (config != null && config.networkId != INVALID_WAPI) {
            WifiConfiguration savedConfig = getWifiConfiguration(config.networkId);
            if (savedConfig != null) {
                if (!uiOnly) {
                    savedConfig.noInternetAccess = config.noInternetAccess;
                    savedConfig.validatedInternetAccess = config.validatedInternetAccess;
                    savedConfig.internetHistory = config.internetHistory;
                    savedConfig.internetSelfCureHistory = config.internetSelfCureHistory;
                    savedConfig.portalCheckStatus = config.portalCheckStatus;
                    savedConfig.internetRecoveryStatus = config.internetRecoveryStatus;
                    savedConfig.internetRecoveryCheckTimestamp = config.internetRecoveryCheckTimestamp;
                    savedConfig.poorRssiDectected = config.poorRssiDectected;
                    savedConfig.consecutiveGoodRssiCounter = config.consecutiveGoodRssiCounter;
                }
                savedConfig.wifiProNoInternetAccess = config.wifiProNoInternetAccess;
                savedConfig.wifiProNoInternetReason = config.wifiProNoInternetReason;
                savedConfig.wifiProNoHandoverNetwork = config.wifiProNoHandoverNetwork;
                savedConfig.internetAccessType = config.internetAccessType;
                savedConfig.networkQosLevel = config.networkQosLevel;
                savedConfig.networkQosScore = config.networkQosScore;
                savedConfig.isTempCreated = config.isTempCreated;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean tryUseStaticIpForFastConnecting(int netId) {
        boolean usingStaticIp = false;
        WifiConfiguration currConfig = getWifiConfiguration(netId);
        if (!(currConfig == null || currConfig.lastDhcpResults == null)) {
            String[] dhcpResults = currConfig.lastDhcpResults.split("\\|");
            StaticIpConfiguration staticIpConfig = new StaticIpConfiguration();
            InetAddress ipAddr = null;
            int prefLength = INVALID_WAPI;
            int flag = INVALID_WAPI;
            int scope = INVALID_WAPI;
            int i = HISI_WAPI;
            while (i < dhcpResults.length) {
                try {
                    if (i == 0) {
                        int lastCellid = Integer.parseInt(dhcpResults[i]);
                        int currCellid = WifiStateMachineUtils.getCurrentCellId(wifiConfigStoreUtils.getContext(this));
                        if (currCellid != INVALID_WAPI && currCellid == lastCellid) {
                        }
                    } else if (i == QUALCOMM_WAPI) {
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
                    i += QUALCOMM_WAPI;
                } catch (UnknownHostException e) {
                    Log.e(TAG, "tryUseStaticIpForFastConnecting, UnknownHostException msg = " + e.getMessage());
                } catch (IllegalArgumentException e2) {
                    Log.e(TAG, "tryUseStaticIpForFastConnecting, IllegalArgumentException msg = " + e2.getMessage());
                }
            }
            if (!(ipAddr == null || prefLength == INVALID_WAPI || staticIpConfig.gateway == null || staticIpConfig.dnsServers.size() <= 0)) {
                staticIpConfig.ipAddress = new LinkAddress(ipAddr, prefLength, flag, scope);
                currConfig.setStaticIpConfiguration(staticIpConfig);
                currConfig.setIpAssignment(IpAssignment.STATIC);
                usingStaticIp = true;
            }
            Log.d(TAG, "tryUseStaticIpForFastConnecting, staticIpConfig = " + staticIpConfig);
        }
        return usingStaticIp;
    }

    public void resetStaticIpConfig(int netId) {
        WifiConfiguration currConfig = getWifiConfiguration(netId);
        if (currConfig != null) {
            currConfig.setIpAssignment(IpAssignment.DHCP);
            currConfig.setStaticIpConfiguration(null);
            Log.d(TAG, "resetStaticIpConfig, netId = " + netId);
        }
    }

    public boolean skipEnableWithoutInternet(WifiConfiguration config) {
        if (config == null || this.mContext == null || this.mWifiNative == null) {
            return false;
        }
        return WifiConfigStoreUtils.ignoreEnableNetwork(this.mContext, config, this.mWifiNative);
    }
}
